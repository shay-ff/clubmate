package clubmate.backend.tournament.controller;

import clubmate.backend.tournament.domain.Tournament;
import clubmate.backend.tournament.dto.TournamentStandingsDTO;
import clubmate.backend.tournament.repository.TournamentRepository;
import clubmate.backend.tournament.service.StandingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * PUBLIC REST Controller for viewing tournament standings.
 * All endpoints are read-only and require NO authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/tournaments/{tournamentId}/standings")
public class StandingsController {

    private final StandingsService standingsService;
    private final TournamentRepository tournamentRepository;

    @Autowired
    public StandingsController(StandingsService standingsService,
                              TournamentRepository tournamentRepository) {
        this.standingsService = standingsService;
        this.tournamentRepository = tournamentRepository;
    }

    /**
     * Get current standings for a tournament.
     * Displays all players sorted by score (highest first).
     * PUBLIC endpoint - no authentication required.
     *
     * @param tournamentId Path variable: Tournament ID
     * @return Tournament standings with ranked players
     *
     * Example request:
     * GET /api/tournaments/{tournamentId}/standings
     */
    @GetMapping
    public ResponseEntity<TournamentStandingsDTO> getCurrentStandings(
            @PathVariable UUID tournamentId) {
        
        log.info("Public request: Fetching current standings for tournament {}", tournamentId);

        // Verify tournament exists
        if (!tournamentRepository.existsById(tournamentId)) {
            log.warn("Tournament not found: {}", tournamentId);
            return ResponseEntity.notFound().build();
        }

        try {
            TournamentStandingsDTO standings = standingsService.getTournamentStandings(tournamentId);
            log.info("Returning standings for {} players in tournament {}", 
                    standings.getStandings().size(), tournamentId);
            return ResponseEntity.ok(standings);

        } catch (IllegalArgumentException e) {
            log.warn("Error fetching standings for tournament {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get standings for a specific round (cumulative score up to that round).
     * Useful for viewing historical standings progression.
     * PUBLIC endpoint - no authentication required.
     *
     * @param tournamentId Path variable: Tournament ID
     * @param round        Query parameter: Round number (1-based)
     * @return Tournament standings as of the specified round
     *
     * Example request:
     * GET /api/tournaments/{tournamentId}/standings?round=3
     */
    @GetMapping(params = "round")
    public ResponseEntity<TournamentStandingsDTO> getStandingsByRound(
            @PathVariable UUID tournamentId,
            @RequestParam Integer round) {
        
        log.info("Public request: Fetching standings for tournament {} at round {}", tournamentId, round);

        // Verify tournament exists
        if (!tournamentRepository.existsById(tournamentId)) {
            log.warn("Tournament not found: {}", tournamentId);
            return ResponseEntity.notFound().build();
        }

        // Validate round number
        if (round == null || round < 1) {
            log.warn("Invalid round number: {}", round);
            return ResponseEntity.badRequest().build();
        }

        try {
            TournamentStandingsDTO standings = standingsService.getTournamentStandingsByRound(tournamentId, round);
            log.info("Returning standings for {} players in tournament {} at round {}", 
                    standings.getStandings().size(), tournamentId, round);
            return ResponseEntity.ok(standings);

        } catch (IllegalArgumentException e) {
            log.warn("Error fetching standings for tournament {} at round {}: {}", 
                    tournamentId, round, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error fetching standings: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint to verify standings controller is accessible.
     * PUBLIC endpoint.
     *
     * @return OK status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Standings Controller is healthy");
    }
}
