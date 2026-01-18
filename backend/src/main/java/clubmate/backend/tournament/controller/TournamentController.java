package clubmate.backend.tournament.controller;

import clubmate.backend.tournament.domain.Tournament;
import clubmate.backend.tournament.dto.TournamentDetailDTO;
import clubmate.backend.tournament.repository.TournamentRepository;
import clubmate.backend.player.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PUBLIC REST Controller for viewing tournament details.
 * All endpoints are read-only and require NO authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public TournamentController(TournamentRepository tournamentRepository,
                               PlayerRepository playerRepository) {
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
    }

    /**
     * Get a list of all tournaments.
     * PUBLIC endpoint - no authentication required.
     *
     * @return List of tournament details
     *
     * Example request:
     * GET /api/tournaments
     */
    @GetMapping
    public ResponseEntity<List<TournamentDetailDTO>> getAllTournaments() {
        log.info("Public request: Fetching all tournaments");

        List<Tournament> tournaments = tournamentRepository.findAll();
        
        List<TournamentDetailDTO> tournamentDTOs = tournaments.stream()
                .map(this::buildTournamentDetailDTO)
                .collect(Collectors.toList());

        log.info("Returning {} tournaments", tournamentDTOs.size());
        return ResponseEntity.ok(tournamentDTOs);
    }

    /**
     * Get details for a specific tournament.
     * PUBLIC endpoint - no authentication required.
     *
     * @param tournamentId Path variable: Tournament ID
     * @return Tournament details including player counts and status
     *
     * Example request:
     * GET /api/tournaments/{tournamentId}
     */
    @GetMapping("/{tournamentId}")
    public ResponseEntity<TournamentDetailDTO> getTournamentDetails(
            @PathVariable UUID tournamentId) {
        
        log.info("Public request: Fetching tournament details - {}", tournamentId);

        return tournamentRepository.findById(tournamentId)
                .map(tournament -> {
                    TournamentDetailDTO dto = buildTournamentDetailDTO(tournament);
                    log.info("Tournament found: {}", tournament.getName());
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> {
                    log.warn("Tournament not found: {}", tournamentId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Health check endpoint to verify tournament controller is accessible.
     * PUBLIC endpoint.
     *
     * @return OK status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Tournament Controller is healthy");
    }

    /**
     * Build a tournament detail DTO from a tournament entity.
     *
     * @param tournament The tournament entity
     * @return TournamentDetailDTO with enriched information
     */
    private TournamentDetailDTO buildTournamentDetailDTO(Tournament tournament) {
        int totalPlayers = (int) playerRepository.findByTournamentId(tournament.getId()).size();
        int activePlayers = (int) playerRepository.findByTournamentIdAndActiveTrue(tournament.getId()).size();

        TournamentDetailDTO dto = new TournamentDetailDTO(
                tournament.getId(),
                tournament.getName(),
                tournament.getTotalRounds(),
                tournament.getCurrentRound(),
                tournament.getStatus(),
                tournament.getCreatedAt()
        );
        
        dto.setTotalPlayers(totalPlayers);
        dto.setActivePlayers(activePlayers);
        
        return dto;
    }
}
