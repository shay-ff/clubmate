package clubmate.backend.player.controller;

import clubmate.backend.common.response.ApiResponse;
import clubmate.backend.player.domain.Player;
import clubmate.backend.player.dto.AddPlayerDTO;
import clubmate.backend.player.dto.PlayerDTO;
import clubmate.backend.player.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for player management.
 * Admin endpoints for adding/managing players; public endpoint for listing.
 */
@Slf4j
@RestController
@RequestMapping("/api/tournaments/{tournamentId}/players")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Get all players in a tournament (including inactive).
     * PUBLIC endpoint - no authentication required.
     *
     * @param tournamentId Path variable: Tournament ID
     * @return List of all players
     *
     * Example request:
     * GET /api/tournaments/{tournamentId}/players
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlayerDTO>>> getAllPlayers(
            @PathVariable UUID tournamentId) {
        
        log.info("Public request: Fetching all players for tournament {}", tournamentId);

        List<Player> players = playerService.getPlayersInTournament(tournamentId);
        List<PlayerDTO> playerDTOs = players.stream()
                .map(this::buildPlayerDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(playerDTOs, "Players retrieved"));
    }

    /**
     * Get all active players in a tournament.
     * PUBLIC endpoint - no authentication required.
     *
     * @param tournamentId Path variable: Tournament ID
     * @return List of active players
     *
     * Example request:
     * GET /api/tournaments/{tournamentId}/players?active=true
     */
    @GetMapping(params = "active")
    public ResponseEntity<ApiResponse<List<PlayerDTO>>> getActivePlayers(
            @PathVariable UUID tournamentId,
            @RequestParam(value = "active", defaultValue = "true") Boolean activeOnly) {
        
        log.info("Public request: Fetching active players for tournament {}", tournamentId);

        List<Player> players = activeOnly 
                ? playerService.getActivePlayers(tournamentId)
                : playerService.getPlayersInTournament(tournamentId);

        List<PlayerDTO> playerDTOs = players.stream()
                .map(this::buildPlayerDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(playerDTOs, "Players retrieved"));
    }

    /**
     * Add a new player to a tournament.
     * ADMIN-ONLY endpoint.
     *
     * @param tournamentId Path variable: Tournament ID
     * @param addPlayerDTO Request body: Player details
     * @return Created player
     *
     * Example request:
     * POST /api/tournaments/{tournamentId}/players
     * {
     *   "name": "John Doe"
     * }
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlayerDTO>> addPlayer(
            @PathVariable UUID tournamentId,
            @RequestBody AddPlayerDTO addPlayerDTO) {
        
        log.info("Admin adding player '{}' to tournament {}", addPlayerDTO.getName(), tournamentId);

        try {
            Player player = playerService.addPlayer(tournamentId, addPlayerDTO);
            PlayerDTO playerDTO = buildPlayerDTO(player);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(playerDTO, "Player added successfully"));

        } catch (Exception e) {
            log.warn("Failed to add player to tournament {}: {}", tournamentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to add player", e.getMessage()));
        }
    }

    /**
     * Deactivate a player (remove from tournament).
     * ADMIN-ONLY endpoint.
     *
     * @param tournamentId Path variable: Tournament ID
     * @param playerId     Path variable: Player ID
     * @return Updated player
     *
     * Example request:
     * DELETE /api/tournaments/{tournamentId}/players/{playerId}
     */
    @DeleteMapping("/{playerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlayerDTO>> deactivatePlayer(
            @PathVariable UUID tournamentId,
            @PathVariable UUID playerId) {
        
        log.info("Admin deactivating player {} in tournament {}", playerId, tournamentId);

        try {
            Player player = playerService.deactivatePlayer(playerId);
            PlayerDTO playerDTO = buildPlayerDTO(player);

            return ResponseEntity.ok(ApiResponse.success(playerDTO, "Player deactivated"));

        } catch (Exception e) {
            log.warn("Failed to deactivate player {}: {}", playerId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to deactivate player", e.getMessage()));
        }
    }

    /**
     * Reactivate a player.
     * ADMIN-ONLY endpoint.
     *
     * @param tournamentId Path variable: Tournament ID
     * @param playerId     Path variable: Player ID
     * @return Updated player
     *
     * Example request:
     * POST /api/tournaments/{tournamentId}/players/{playerId}/reactivate
     */
    @PostMapping("/{playerId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlayerDTO>> reactivatePlayer(
            @PathVariable UUID tournamentId,
            @PathVariable UUID playerId) {
        
        log.info("Admin reactivating player {} in tournament {}", playerId, tournamentId);

        try {
            Player player = playerService.reactivatePlayer(playerId);
            PlayerDTO playerDTO = buildPlayerDTO(player);

            return ResponseEntity.ok(ApiResponse.success(playerDTO, "Player reactivated"));

        } catch (Exception e) {
            log.warn("Failed to reactivate player {}: {}", playerId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to reactivate player", e.getMessage()));
        }
    }

    /**
     * Health check endpoint.
     * PUBLIC endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Player Controller is healthy");
    }

    /**
     * Build player DTO from player entity.
     */
    private PlayerDTO buildPlayerDTO(Player player) {
        return new PlayerDTO(
                player.getId(),
                player.getName(),
                player.getTournamentId(),
                player.getScore(),
                player.getActive()
        );
    }
}
