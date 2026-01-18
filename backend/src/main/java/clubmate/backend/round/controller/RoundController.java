package clubmate.backend.round.controller;

import clubmate.backend.player.repository.PlayerRepository;
import clubmate.backend.round.domain.Match;
import clubmate.backend.round.domain.Round;
import clubmate.backend.round.dto.PairingDTO;
import clubmate.backend.round.dto.RoundDetailDTO;
import clubmate.backend.round.repository.MatchRepository;
import clubmate.backend.round.repository.RoundRepository;
import clubmate.backend.tournament.repository.TournamentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PUBLIC REST Controller for viewing tournament rounds and pairings.
 * All endpoints are read-only and require NO authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/tournaments/{tournamentId}/rounds")
public class RoundController {

    private final RoundRepository roundRepository;
    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public RoundController(RoundRepository roundRepository,
                         MatchRepository matchRepository,
                         TournamentRepository tournamentRepository,
                         PlayerRepository playerRepository) {
        this.roundRepository = roundRepository;
        this.matchRepository = matchRepository;
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
    }

    /**
     * Get all rounds for a tournament.
     * Displays round status and progress.
     * PUBLIC endpoint - no authentication required.
     *
     * @param tournamentId Path variable: Tournament ID
     * @return List of rounds with status information
     *
     * Example request:
     * GET /api/tournaments/{tournamentId}/rounds
     */
    @GetMapping
    public ResponseEntity<List<RoundDetailDTO>> getTournamentRounds(
            @PathVariable UUID tournamentId) {
        
        log.info("Public request: Fetching rounds for tournament {}", tournamentId);

        // Verify tournament exists
        if (!tournamentRepository.existsById(tournamentId)) {
            log.warn("Tournament not found: {}", tournamentId);
            return ResponseEntity.notFound().build();
        }

        List<Round> rounds = roundRepository.findByTournamentId(tournamentId).stream()
                .sorted((r1, r2) -> Integer.compare(r1.getRoundNumber(), r2.getRoundNumber()))
                .collect(Collectors.toList());

        List<RoundDetailDTO> roundDTOs = rounds.stream()
                .map(this::buildRoundDetailDTO)
                .collect(Collectors.toList());

        log.info("Returning {} rounds for tournament {}", roundDTOs.size(), tournamentId);
        return ResponseEntity.ok(roundDTOs);
    }

    /**
     * Get details for a specific round including all pairings/matches.
     * Displays match information and results (if available).
     * PUBLIC endpoint - no authentication required.
     *
     * @param tournamentId Path variable: Tournament ID
     * @param roundId      Path variable: Round ID
     * @return Round details with match list
     *
     * Example request:
     * GET /api/tournaments/{tournamentId}/rounds/{roundId}
     */
    @GetMapping("/{roundId}")
    public ResponseEntity<Object> getRoundDetails(
            @PathVariable UUID tournamentId,
            @PathVariable UUID roundId) {
        
        log.info("Public request: Fetching round details - Tournament: {}, Round: {}", 
                tournamentId, roundId);

        // Verify tournament exists
        if (!tournamentRepository.existsById(tournamentId)) {
            log.warn("Tournament not found: {}", tournamentId);
            return ResponseEntity.notFound().build();
        }

        // Get the round and verify it belongs to the tournament
        return roundRepository.findById(roundId)
                .filter(round -> round.getTournamentId().equals(tournamentId))
                .map(round -> {
                    RoundDetailDTO roundDetail = buildRoundDetailDTO(round);
                    log.info("Round found: Round {}", round.getRoundNumber());
                    return ResponseEntity.ok((Object) roundDetail);
                })
                .orElseGet(() -> {
                    log.warn("Round not found: {} in tournament {}", roundId, tournamentId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Get all pairings/matches for a specific round.
     * Shows matchups and results (if completed).
     * PUBLIC endpoint - no authentication required.
     *
     * @param tournamentId Path variable: Tournament ID
     * @param roundId      Path variable: Round ID
     * @return List of pairings with match information
     *
     * Example request:
     * GET /api/tournaments/{tournamentId}/rounds/{roundId}/matches
     */
    @GetMapping("/{roundId}/matches")
    public ResponseEntity<List<PairingDTO>> getRoundPairings(
            @PathVariable UUID tournamentId,
            @PathVariable UUID roundId) {
        
        log.info("Public request: Fetching pairings - Tournament: {}, Round: {}", 
                tournamentId, roundId);

        // Verify tournament exists
        if (!tournamentRepository.existsById(tournamentId)) {
            log.warn("Tournament not found: {}", tournamentId);
            return ResponseEntity.notFound().build();
        }

        // Verify round exists and belongs to tournament
        Round round = roundRepository.findById(roundId)
                .filter(r -> r.getTournamentId().equals(tournamentId))
                .orElse(null);

        if (round == null) {
            log.warn("Round not found: {} in tournament {}", roundId, tournamentId);
            return ResponseEntity.notFound().build();
        }

        // Get matches for this round
        List<Match> matches = matchRepository.findByRoundId(roundId);

        List<PairingDTO> pairings = matches.stream()
                .map(match -> buildPairingDTO(match, round.getRoundNumber()))
                .collect(Collectors.toList());

        log.info("Returning {} pairings for round {} in tournament {}", 
                pairings.size(), round.getRoundNumber(), tournamentId);
        return ResponseEntity.ok(pairings);
    }

    /**
     * Get a specific match/pairing details.
     * PUBLIC endpoint - no authentication required.
     *
     * @param tournamentId Path variable: Tournament ID
     * @param roundId      Path variable: Round ID
     * @param matchId      Path variable: Match ID
     * @return Pairing details
     *
     * Example request:
     * GET /api/tournaments/{tournamentId}/rounds/{roundId}/matches/{matchId}
     */
    @GetMapping("/{roundId}/matches/{matchId}")
    public ResponseEntity<PairingDTO> getMatchDetails(
            @PathVariable UUID tournamentId,
            @PathVariable UUID roundId,
            @PathVariable UUID matchId) {
        
        log.info("Public request: Fetching match details - Tournament: {}, Round: {}, Match: {}",
                tournamentId, roundId, matchId);

        // Verify tournament exists
        if (!tournamentRepository.existsById(tournamentId)) {
            log.warn("Tournament not found: {}", tournamentId);
            return ResponseEntity.notFound().build();
        }

        // Verify round exists and belongs to tournament
        Round round = roundRepository.findById(roundId)
                .filter(r -> r.getTournamentId().equals(tournamentId))
                .orElse(null);

        if (round == null) {
            log.warn("Round not found: {} in tournament {}", roundId, tournamentId);
            return ResponseEntity.notFound().build();
        }

        // Get the match and verify it belongs to the round
        return matchRepository.findById(matchId)
                .filter(match -> match.getRoundId().equals(roundId))
                .map(match -> {
                    PairingDTO pairing = buildPairingDTO(match, round.getRoundNumber());
                    log.info("Match found in round {}", round.getRoundNumber());
                    return ResponseEntity.ok(pairing);
                })
                .orElseGet(() -> {
                    log.warn("Match not found: {} in round {}", matchId, roundId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Health check endpoint to verify round controller is accessible.
     * PUBLIC endpoint.
     *
     * @return OK status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Round Controller is healthy");
    }

    /**
     * Build a round detail DTO from a round entity.
     *
     * @param round The round entity
     * @return RoundDetailDTO with match information
     */
    private RoundDetailDTO buildRoundDetailDTO(Round round) {
        List<Match> matches = matchRepository.findByRoundId(round.getId());
        
        int completedMatches = (int) matches.stream()
                .filter(Match::isCompleted)
                .count();
        int pendingMatches = matches.size() - completedMatches;

        return new RoundDetailDTO(
                round.getId(),
                round.getTournamentId(),
                round.getRoundNumber(),
                round.getStatus(),
                matches.size(),
                completedMatches,
                pendingMatches
        );
    }

    /**
     * Build a pairing DTO from a match entity.
     *
     * @param match       The match entity
     * @param roundNumber The round number for reference
     * @return PairingDTO with player information and result
     */
    private PairingDTO buildPairingDTO(Match match, Integer roundNumber) {
        String player1Name = playerRepository.findById(match.getPlayer1Id())
                .map(p -> p.getName())
                .orElse("Unknown");

        String player2Name = playerRepository.findById(match.getPlayer2Id())
                .map(p -> p.getName())
                .orElse("Unknown");

        PairingDTO pairing = new PairingDTO(
                match.getId(),
                roundNumber,
                match.getPlayer1Id(),
                player1Name,
                match.getPlayer2Id(),
                player2Name,
                match.getResult(),
                match.getWinnerId()
        );

        // Set winner name if match is completed
        if (match.getWinnerId() != null) {
            String winnerName = playerRepository.findById(match.getWinnerId())
                    .map(p -> p.getName())
                    .orElse("Unknown");
            pairing.setWinnerName(winnerName);
        }

        return pairing;
    }
}
