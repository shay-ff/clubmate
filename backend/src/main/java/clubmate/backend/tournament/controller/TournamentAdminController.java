package clubmate.backend.tournament.controller;

import clubmate.backend.common.response.ApiResponse;
import clubmate.backend.tournament.domain.Tournament;
import clubmate.backend.tournament.dto.CreateTournamentDTO;
import clubmate.backend.tournament.dto.TournamentDetailDTO;
import clubmate.backend.tournament.service.TournamentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for admin tournament management.
 * All endpoints are admin-only and protected with JWT authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/tournaments")
public class TournamentAdminController {

    private final TournamentService tournamentService;

    @Autowired
    public TournamentAdminController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    /**
     * Create a new tournament.
     * ADMIN-ONLY endpoint.
     *
     * @param createDTO DTO with tournament details
     * @return Created tournament
     *
     * Example request:
     * POST /api/admin/tournaments
     * {
     *   "name": "Regional Chess Championship 2026",
     *   "totalRounds": 5
     * }
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TournamentDetailDTO>> createTournament(
            @RequestBody CreateTournamentDTO createDTO) {
        
        log.info("Admin creating tournament: {}", createDTO.getName());

        try {
            Tournament tournament = tournamentService.createTournament(createDTO);
            TournamentDetailDTO dto = buildTournamentDetailDTO(tournament);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(dto, "Tournament created successfully"));

        } catch (Exception e) {
            log.warn("Failed to create tournament: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Tournament Creation Failed", e.getMessage()));
        }
    }

    /**
     * Start a tournament (transition from CREATED to IN_PROGRESS).
     * ADMIN-ONLY endpoint.
     *
     * @param tournamentId Tournament ID
     * @return Updated tournament
     *
     * Example request:
     * POST /api/admin/tournaments/{tournamentId}/start
     */
    @PostMapping("/{tournamentId}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TournamentDetailDTO>> startTournament(
            @PathVariable UUID tournamentId) {
        
        log.info("Admin starting tournament: {}", tournamentId);

        try {
            Tournament tournament = tournamentService.startTournament(tournamentId);
            TournamentDetailDTO dto = buildTournamentDetailDTO(tournament);
            
            return ResponseEntity.ok(ApiResponse.success(dto, "Tournament started"));

        } catch (Exception e) {
            log.warn("Failed to start tournament: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Tournament Start Failed", e.getMessage()));
        }
    }

    /**
     * Pause a tournament.
     * ADMIN-ONLY endpoint.
     *
     * @param tournamentId Tournament ID
     * @return Updated tournament
     *
     * Example request:
     * POST /api/admin/tournaments/{tournamentId}/pause
     */
    @PostMapping("/{tournamentId}/pause")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TournamentDetailDTO>> pauseTournament(
            @PathVariable UUID tournamentId) {
        
        log.info("Admin pausing tournament: {}", tournamentId);

        try {
            Tournament tournament = tournamentService.pauseTournament(tournamentId);
            TournamentDetailDTO dto = buildTournamentDetailDTO(tournament);
            
            return ResponseEntity.ok(ApiResponse.success(dto, "Tournament paused"));

        } catch (Exception e) {
            log.warn("Failed to pause tournament: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Tournament Pause Failed", e.getMessage()));
        }
    }

    /**
     * Resume a paused tournament.
     * ADMIN-ONLY endpoint.
     *
     * @param tournamentId Tournament ID
     * @return Updated tournament
     *
     * Example request:
     * POST /api/admin/tournaments/{tournamentId}/resume
     */
    @PostMapping("/{tournamentId}/resume")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TournamentDetailDTO>> resumeTournament(
            @PathVariable UUID tournamentId) {
        
        log.info("Admin resuming tournament: {}", tournamentId);

        try {
            Tournament tournament = tournamentService.resumeTournament(tournamentId);
            TournamentDetailDTO dto = buildTournamentDetailDTO(tournament);
            
            return ResponseEntity.ok(ApiResponse.success(dto, "Tournament resumed"));

        } catch (Exception e) {
            log.warn("Failed to resume tournament: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Tournament Resume Failed", e.getMessage()));
        }
    }

    /**
     * Finish a tournament.
     * ADMIN-ONLY endpoint.
     *
     * @param tournamentId Tournament ID
     * @return Updated tournament
     *
     * Example request:
     * POST /api/admin/tournaments/{tournamentId}/finish
     */
    @PostMapping("/{tournamentId}/finish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TournamentDetailDTO>> finishTournament(
            @PathVariable UUID tournamentId) {
        
        log.info("Admin finishing tournament: {}", tournamentId);

        try {
            Tournament tournament = tournamentService.finishTournament(tournamentId);
            TournamentDetailDTO dto = buildTournamentDetailDTO(tournament);
            
            return ResponseEntity.ok(ApiResponse.success(dto, "Tournament finished"));

        } catch (Exception e) {
            log.warn("Failed to finish tournament: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Tournament Finish Failed", e.getMessage()));
        }
    }

    /**
     * Build tournament detail DTO.
     */
    private TournamentDetailDTO buildTournamentDetailDTO(Tournament tournament) {
        return new TournamentDetailDTO(
                tournament.getId(),
                tournament.getName(),
                tournament.getTotalRounds(),
                tournament.getCurrentRound(),
                tournament.getStatus(),
                tournament.getCreatedAt()
        );
    }
}
