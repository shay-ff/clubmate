package clubmate.backend.round.service;

import clubmate.backend.player.domain.Player;
import clubmate.backend.player.repository.PlayerRepository;
import clubmate.backend.round.domain.Match;
import clubmate.backend.round.domain.MatchResult;
import clubmate.backend.round.domain.Round;
import clubmate.backend.round.dto.MatchResultResponseDTO;
import clubmate.backend.round.dto.SubmitMatchResultDTO;
import clubmate.backend.round.repository.MatchRepository;
import clubmate.backend.round.repository.RoundRepository;
import clubmate.backend.tournament.domain.Tournament;
import clubmate.backend.tournament.repository.TournamentRepository;
import clubmate.backend.tournament.service.StandingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for handling match result submissions.
 * Validates match ownership to tournament, processes results, and triggers score recalculation.
 */
@Slf4j
@Service
public class MatchResultService {

    private final MatchRepository matchRepository;
    private final RoundRepository roundRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final StandingsService standingsService;

    @Autowired
    public MatchResultService(MatchRepository matchRepository,
                             RoundRepository roundRepository,
                             TournamentRepository tournamentRepository,
                             PlayerRepository playerRepository,
                             StandingsService standingsService) {
        this.matchRepository = matchRepository;
        this.roundRepository = roundRepository;
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
        this.standingsService = standingsService;
    }

    /**
     * Submit result for a match. Validates ownership to tournament and recalculates standings.
     *
     * @param tournamentId       The tournament ID (for ownership validation)
     * @param matchResultDTO     The match result submission
     * @return                   Match result response with updated information
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException    if match state is invalid
     */
    public MatchResultResponseDTO submitMatchResult(UUID tournamentId, SubmitMatchResultDTO matchResultDTO) {
        log.info("Submitting result for match {} in tournament {}", matchResultDTO.getMatchId(), tournamentId);

        // Validate the DTO
        if (!matchResultDTO.isValid()) {
            throw new IllegalArgumentException(
                "Invalid match result: must be DRAW or specify a winner"
            );
        }

        // Get the match
        Match match = matchRepository.findById(matchResultDTO.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Match not found: " + matchResultDTO.getMatchId()
                ));

        // Validate match ownership to tournament
        validateMatchOwnershipToTournament(match, tournamentId);

        // Validate match state
        if (match.isCompleted()) {
            throw new IllegalStateException(
                String.format("Match %s already has a result: %s",
                    match.getId(), match.getResult())
            );
        }

        // Process the result
        processMatchResult(match, matchResultDTO);

        // Save the updated match
        Match updatedMatch = matchRepository.save(match);

        log.info("Match {} result submitted successfully. Recalculating standings...",
                match.getId());

        // Trigger score recalculation
        recalculatePlayerScores(tournamentId, match);

        // Return response DTO with updated information
        return buildMatchResultResponse(updatedMatch);
    }

    /**
     * Validate that a match belongs to the specified tournament.
     *
     * @param match        The match to validate
     * @param tournamentId The tournament ID
     * @throws IllegalArgumentException if match doesn't belong to tournament
     */
    private void validateMatchOwnershipToTournament(Match match, UUID tournamentId) {
        log.debug("Validating match {} ownership to tournament {}", match.getId(), tournamentId);

        // Get the round to check tournament ownership
        Round round = roundRepository.findById(match.getRoundId())
                .orElseThrow(() -> new IllegalStateException(
                    "Round not found for match: " + match.getId()
                ));

        // Verify tournament owns this round
        if (!round.getTournamentId().equals(tournamentId)) {
            throw new IllegalArgumentException(
                String.format(
                    "Match %s does not belong to tournament %s. It belongs to tournament %s",
                    match.getId(), tournamentId, round.getTournamentId()
                )
            );
        }

        log.debug("Match {} ownership validated for tournament {}", match.getId(), tournamentId);
    }

    /**
     * Validate that the winner (if specified) is a player in the match.
     *
     * @param match  The match
     * @param winnerId The winner ID
     * @throws IllegalArgumentException if winner is not a participant
     */
    private void validateWinner(Match match, UUID winnerId) {
        if (!match.getPlayer1Id().equals(winnerId) && !match.getPlayer2Id().equals(winnerId)) {
            throw new IllegalArgumentException(
                String.format(
                    "Winner %s is not a participant in match %s",
                    winnerId, match.getId()
                )
            );
        }
    }

    /**
     * Process the match result and update the match entity.
     *
     * @param match          The match to update
     * @param resultDTO      The result submission
     */
    private void processMatchResult(Match match, SubmitMatchResultDTO resultDTO) {
        log.debug("Processing result for match {}: {}", match.getId(), resultDTO.getResult());

        if (resultDTO.isDraw()) {
            match.setDraw();
            log.info("Match {} marked as DRAW", match.getId());
        } else if (resultDTO.isPlayer1Win()) {
            validateWinner(match, resultDTO.getWinnerId());
            if (!match.getPlayer1Id().equals(resultDTO.getWinnerId())) {
                throw new IllegalArgumentException("Winner must be Player1 for PLAYER1_WIN result");
            }
            match.setWinner(resultDTO.getWinnerId());
            log.info("Match {} marked as PLAYER1_WIN (Player: {})", match.getId(), resultDTO.getWinnerId());
        } else if (resultDTO.isPlayer2Win()) {
            validateWinner(match, resultDTO.getWinnerId());
            if (!match.getPlayer2Id().equals(resultDTO.getWinnerId())) {
                throw new IllegalArgumentException("Winner must be Player2 for PLAYER2_WIN result");
            }
            match.setWinner(resultDTO.getWinnerId());
            log.info("Match {} marked as PLAYER2_WIN (Player: {})", match.getId(), resultDTO.getWinnerId());
        }
    }

    /**
     * Recalculate player scores after a match result is submitted.
     * This triggers the standings service to recalculate tournament standings.
     *
     * @param tournamentId The tournament ID
     * @param match        The match that was just updated
     */
    private void recalculatePlayerScores(UUID tournamentId, Match match) {
        log.info("Triggering score recalculation for tournament {} after match {}", tournamentId, match.getId());

        try {
            // Recalculate standings - this will update all player scores based on completed matches
            standingsService.getTournamentStandings(tournamentId);
            
            // Update player scores from the standings calculation
            updatePlayerScoresFromStandings(tournamentId, match);
            
            log.info("Score recalculation completed for tournament {}", tournamentId);
        } catch (Exception e) {
            log.error("Error recalculating scores for tournament {}: {}", tournamentId, e.getMessage());
            throw new IllegalStateException("Failed to recalculate standings", e);
        }
    }

    /**
     * Update player score entities based on standings calculation.
     * This ensures the Player entity's score field is synchronized with calculated standings.
     *
     * @param tournamentId The tournament ID
     * @param match        The match that was updated
     */
    private void updatePlayerScoresFromStandings(UUID tournamentId, Match match) {
        log.debug("Updating player scores for tournament {}", tournamentId);

        // Get updated scores from standings service
        Double player1Score = standingsService.getPlayerScore(tournamentId, match.getPlayer1Id());
        Double player2Score = standingsService.getPlayerScore(tournamentId, match.getPlayer2Id());

        // Update player entities with recalculated scores
        Player player1 = playerRepository.findById(match.getPlayer1Id())
                .orElseThrow(() -> new IllegalStateException("Player not found: " + match.getPlayer1Id()));
        Player player2 = playerRepository.findById(match.getPlayer2Id())
                .orElseThrow(() -> new IllegalStateException("Player not found: " + match.getPlayer2Id()));

        player1.setScore(player1Score);
        player2.setScore(player2Score);

        playerRepository.saveAll(java.util.List.of(player1, player2));
        
        log.debug("Updated scores - Player1: {}, Player2: {}", player1Score, player2Score);
    }

    /**
     * Build response DTO from a match entity.
     *
     * @param match The match entity
     * @return MatchResultResponseDTO
     */
    private MatchResultResponseDTO buildMatchResultResponse(Match match) {
        Player player1 = playerRepository.findById(match.getPlayer1Id())
                .orElseThrow(() -> new IllegalStateException("Player not found: " + match.getPlayer1Id()));
        Player player2 = playerRepository.findById(match.getPlayer2Id())
                .orElseThrow(() -> new IllegalStateException("Player not found: " + match.getPlayer2Id()));

        return new MatchResultResponseDTO(
                match.getId(),
                match.getRoundId(),
                player1.getId(),
                player1.getName(),
                player2.getId(),
                player2.getName(),
                match.getResult(),
                match.getWinnerId()
        );
    }

    /**
     * Get match details for a specific match (without submitting result).
     *
     * @param matchId Match ID
     * @return MatchResultResponseDTO
     */
    public MatchResultResponseDTO getMatchDetails(UUID matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        return buildMatchResultResponse(match);
    }
}
