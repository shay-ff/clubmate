package clubmate.backend.round.controller;

import clubmate.backend.round.dto.MatchResultResponseDTO;
import clubmate.backend.round.dto.SubmitMatchResultDTO;
import clubmate.backend.round.service.MatchResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for match result submission.
 * All endpoints are admin-only and protected with JWT authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/tournaments/{tournamentId}/matches")
public class MatchResultController {

    private final MatchResultService matchResultService;

    @Autowired
    public MatchResultController(MatchResultService matchResultService) {
        this.matchResultService = matchResultService;
    }

    /**
     * Submit a match result for a specific match in a tournament.
     * ADMIN-ONLY endpoint.
     *
     * @param tournamentId     Path variable: Tournament ID
     * @param matchResultDTO   Request body: Match result submission
     * @return                 MatchResultResponseDTO with updated match information
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException    if match state is invalid
     *
     * Example request:
     * POST /api/tournaments/{tournamentId}/matches/result
     * {
     *   "matchId": "uuid",
     *   "result": "PLAYER1_WIN",
     *   "winnerId": "uuid-of-player1"
     * }
     *
     * Or for a draw:
     * {
     *   "matchId": "uuid",
     *   "result": "DRAW",
     *   "winnerId": null
     * }
     */
    @PostMapping("/result")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MatchResultResponseDTO> submitMatchResult(
            @PathVariable UUID tournamentId,
            @RequestBody SubmitMatchResultDTO matchResultDTO) {
        
        log.info("Admin submitting match result for tournament {} - Match: {}",
                tournamentId, matchResultDTO.getMatchId());

        try {
            MatchResultResponseDTO result = matchResultService.submitMatchResult(
                    tournamentId, matchResultDTO
            );

            log.info("Match result accepted. Score recalculation triggered.");
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid match result submission for tournament {}: {}",
                    tournamentId, e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (IllegalStateException e) {
            log.warn("Cannot submit match result for tournament {}: {}",
                    tournamentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Get details for a specific match without submitting a result.
     * ADMIN-ONLY endpoint.
     *
     * @param tournamentId Path variable: Tournament ID
     * @param matchId      Path variable: Match ID
     * @return             MatchResultResponseDTO with match details
     *
     * Example request:
     * GET /api/tournaments/{tournamentId}/matches/{matchId}/details
     */
    @GetMapping("/{matchId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MatchResultResponseDTO> getMatchDetails(
            @PathVariable UUID tournamentId,
            @PathVariable UUID matchId) {
        
        log.info("Admin fetching match details - Tournament: {}, Match: {}", tournamentId, matchId);

        try {
            MatchResultResponseDTO matchDetails = matchResultService.getMatchDetails(matchId);
            return ResponseEntity.ok(matchDetails);

        } catch (IllegalArgumentException e) {
            log.warn("Match not found: {}", matchId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Health check endpoint to verify controller is accessible.
     * Public endpoint for testing connectivity.
     *
     * @return OK status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Match Result Controller is healthy");
    }
}
