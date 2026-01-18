package clubmate.backend.round.service;

import clubmate.backend.player.domain.Player;
import clubmate.backend.player.repository.PlayerRepository;
import clubmate.backend.round.domain.Match;
import clubmate.backend.round.domain.Round;
import clubmate.backend.round.domain.RoundStatus;
import clubmate.backend.round.repository.MatchRepository;
import clubmate.backend.round.repository.RoundRepository;
import clubmate.backend.tournament.domain.Tournament;
import clubmate.backend.tournament.repository.TournamentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing tournament rounds with advanced features:
 * - Creates new rounds with validation of previous round completion
 * - Locks rounds once all match results are submitted
 * - Prevents operations on invalid round states
 * - Tournament-scoped operations
 */
@Slf4j
@Service
public class RoundManagementService {

    private final RoundRepository roundRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final TournamentRepository tournamentRepository;
    private final RoundService roundService;

    @Autowired
    public RoundManagementService(RoundRepository roundRepository,
                                 MatchRepository matchRepository,
                                 PlayerRepository playerRepository,
                                 TournamentRepository tournamentRepository,
                                 RoundService roundService) {
        this.roundRepository = roundRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.tournamentRepository = tournamentRepository;
        this.roundService = roundService;
    }

    /**
     * Create a new round for a tournament with validation that previous round is complete.
     * Automatically generates pairings for the new round.
     *
     * @param tournamentId Tournament ID
     * @return             Created Round with generated pairings
     * @throws IllegalStateException if tournament not found, or previous round is incomplete
     * @throws IllegalArgumentException if tournament has insufficient players
     */
    public Round createNewRound(UUID tournamentId) {
        log.info("Creating new round for tournament: {}", tournamentId);

        // Verify tournament exists
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalStateException("Tournament not found: " + tournamentId));

        // Check if we've exceeded total rounds
        if (tournament.getCurrentRound() >= tournament.getTotalRounds()) {
            throw new IllegalStateException(
                String.format(
                    "Cannot create new round. Tournament %s has completed all %d rounds",
                    tournamentId, tournament.getTotalRounds()
                )
            );
        }

        // Validate previous round is complete (if not first round)
        if (tournament.getCurrentRound() > 0) {
            validatePreviousRoundComplete(tournamentId, tournament.getCurrentRound());
        }

        // Get active players
        List<Player> activePlayers = playerRepository.findByTournamentIdAndActiveTrue(tournamentId);
        
        if (activePlayers.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Cannot create round. Tournament %s has no active players", tournamentId)
            );
        }

        if (activePlayers.size() < 2) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot create round. Tournament requires minimum 2 active players, found: %d",
                    activePlayers.size()
                )
            );
        }

        // Create the round
        Integer newRoundNumber = tournament.getCurrentRound() + 1;
        Round newRound = roundService.createRoundWithPairings(tournamentId, newRoundNumber, activePlayers);

        // Save round and generated matches
        Round savedRound = roundRepository.save(newRound);
        
        // Generate and save matches
        List<Match> matches = roundService.generateRoundPairings(newRoundNumber, activePlayers);
        matchRepository.saveAll(matches);

        // Update tournament current round
        tournament.advanceRound();
        tournamentRepository.save(tournament);

        log.info("Successfully created round {} for tournament {}. Generated {} matches",
                newRoundNumber, tournamentId, matches.size());

        return savedRound;
    }

    /**
     * Lock a round once all match results have been submitted.
     * This prevents any further modifications to the round and marks it as COMPLETED.
     *
     * @param roundId Round ID
     * @return        Updated locked Round
     * @throws IllegalStateException if not all matches are completed, or round is already locked
     */
    public Round lockRound(UUID roundId) {
        log.info("Attempting to lock round: {}", roundId);

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new IllegalStateException("Round not found: " + roundId));

        // Check if round is already locked
        if (round.isCompleted()) {
            throw new IllegalStateException(
                String.format("Round %d is already locked", round.getRoundNumber())
            );
        }

        // Check if all matches in the round are completed
        List<Match> roundMatches = matchRepository.findByRoundId(roundId);
        
        if (roundMatches.isEmpty()) {
            throw new IllegalStateException(
                String.format("Round %d has no matches to lock", round.getRoundNumber())
            );
        }

        long incompleteMatches = roundMatches.stream()
                .filter(Match::isPending)
                .count();

        if (incompleteMatches > 0) {
            throw new IllegalStateException(
                String.format(
                    "Cannot lock round %d. Found %d incomplete matches (pending results)",
                    round.getRoundNumber(), incompleteMatches
                )
            );
        }

        // Lock the round
        round.finishRound();
        Round lockedRound = roundRepository.save(round);

        log.info("Successfully locked round {}. All {} matches have results",
                round.getRoundNumber(), roundMatches.size());

        return lockedRound;
    }

    /**
     * Check if a round is fully locked (all matches completed and round marked as COMPLETED).
     *
     * @param roundId Round ID
     * @return true if round is locked, false otherwise
     */
    public boolean isRoundLocked(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new IllegalStateException("Round not found: " + roundId));

        if (!round.isCompleted()) {
            return false;
        }

        // Double-check all matches are actually completed
        List<Match> roundMatches = matchRepository.findByRoundId(roundId);
        return roundMatches.stream().allMatch(Match::isCompleted);
    }

    /**
     * Check if all matches in a round have been submitted (no pending results).
     *
     * @param roundId Round ID
     * @return true if all matches have results, false otherwise
     */
    public boolean areAllMatchesCompleted(UUID roundId) {
        List<Match> roundMatches = matchRepository.findByRoundId(roundId);
        
        if (roundMatches.isEmpty()) {
            return false;
        }

        return roundMatches.stream().allMatch(Match::isCompleted);
    }

    /**
     * Get the status of a round (how many matches are pending/completed).
     *
     * @param roundId Round ID
     * @return RoundStatusInfo with completion details
     */
    public RoundStatusInfo getRoundStatus(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new IllegalStateException("Round not found: " + roundId));

        List<Match> roundMatches = matchRepository.findByRoundId(roundId);

        long completedMatches = roundMatches.stream().filter(Match::isCompleted).count();
        long pendingMatches = roundMatches.stream().filter(Match::isPending).count();

        return new RoundStatusInfo(
                roundId,
                round.getRoundNumber(),
                round.getStatus(),
                (int) completedMatches,
                (int) pendingMatches,
                roundMatches.size(),
                (int) completedMatches == roundMatches.size()
        );
    }

    /**
     * Start a round (transition from PENDING to IN_PROGRESS).
     *
     * @param roundId Round ID
     * @return Updated Round
     */
    public Round startRound(UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new IllegalStateException("Round not found: " + roundId));

        roundService.startRound(round);
        return roundRepository.save(round);
    }

    /**
     * Get all rounds for a tournament.
     *
     * @param tournamentId Tournament ID
     * @return List of rounds sorted by round number
     */
    public List<Round> getTournamentRounds(UUID tournamentId) {
        return roundRepository.findByTournamentId(tournamentId).stream()
                .sorted((r1, r2) -> Integer.compare(r1.getRoundNumber(), r2.getRoundNumber()))
                .collect(Collectors.toList());
    }

    /**
     * Get the latest/current round for a tournament.
     *
     * @param tournamentId Tournament ID
     * @return Optional containing the current round, or empty if no rounds exist
     */
    public Optional<Round> getCurrentRound(UUID tournamentId) {
        List<Round> rounds = roundRepository.findByTournamentId(tournamentId);
        return rounds.stream()
                .max((r1, r2) -> Integer.compare(r1.getRoundNumber(), r2.getRoundNumber()));
    }

    /**
     * Validate that the previous round is complete before creating a new one.
     * Throws exception if previous round has incomplete matches.
     *
     * @param tournamentId Tournament ID
     * @param currentRound Current round number
     * @throws IllegalStateException if previous round is not complete
     */
    private void validatePreviousRoundComplete(UUID tournamentId, Integer currentRound) {
        Integer previousRoundNumber = currentRound;
        
        Optional<Round> previousRound = roundRepository.findByTournamentId(tournamentId)
                .stream()
                .filter(r -> r.getRoundNumber() == previousRoundNumber)
                .findFirst();

        if (previousRound.isEmpty()) {
            throw new IllegalStateException(
                String.format(
                    "Cannot find previous round %d for tournament %s",
                    previousRoundNumber, tournamentId
                )
            );
        }

        Round round = previousRound.get();
        
        if (!round.isCompleted()) {
            long incompleteMatches = matchRepository.findByRoundId(round.getId())
                    .stream()
                    .filter(Match::isPending)
                    .count();

            throw new IllegalStateException(
                String.format(
                    "Cannot create new round. Round %d is incomplete with %d pending matches",
                    previousRoundNumber, incompleteMatches
                )
            );
        }
    }

    /**
     * Inner class for providing round status information.
     */
    public static class RoundStatusInfo {
        public final UUID roundId;
        public final Integer roundNumber;
        public final RoundStatus status;
        public final Integer completedMatches;
        public final Integer pendingMatches;
        public final Integer totalMatches;
        public final Boolean isComplete;

        public RoundStatusInfo(UUID roundId, Integer roundNumber, RoundStatus status,
                              Integer completedMatches, Integer pendingMatches,
                              Integer totalMatches, Boolean isComplete) {
            this.roundId = roundId;
            this.roundNumber = roundNumber;
            this.status = status;
            this.completedMatches = completedMatches;
            this.pendingMatches = pendingMatches;
            this.totalMatches = totalMatches;
            this.isComplete = isComplete;
        }
    }
}
