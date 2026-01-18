package clubmate.backend.player.service;

import clubmate.backend.common.exception.ValidationException;
import clubmate.backend.player.domain.Player;
import clubmate.backend.player.dto.AddPlayerDTO;
import clubmate.backend.player.repository.PlayerRepository;
import clubmate.backend.tournament.repository.TournamentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing players in tournaments.
 * Handles player creation, activation/deactivation, and listing.
 */
@Slf4j
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TournamentRepository tournamentRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository,
                        TournamentRepository tournamentRepository) {
        this.playerRepository = playerRepository;
        this.tournamentRepository = tournamentRepository;
    }

    /**
     * Add a new player to a tournament.
     *
     * @param tournamentId Tournament ID
     * @param addPlayerDTO DTO with player details
     * @return Created Player
     * @throws ValidationException if validation fails
     * @throws IllegalArgumentException if tournament not found
     */
    public Player addPlayer(UUID tournamentId, AddPlayerDTO addPlayerDTO) {
        log.info("Adding player '{}' to tournament {}", addPlayerDTO.getName(), tournamentId);

        if (!addPlayerDTO.isValid()) {
            throw new ValidationException("Player name is required");
        }

        // Verify tournament exists
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new IllegalArgumentException("Tournament not found: " + tournamentId);
        }

        Player player = new Player(addPlayerDTO.getName(), tournamentId);
        Player savedPlayer = playerRepository.save(player);

        log.info("Player added successfully: {} (ID: {}) to tournament {}", 
                addPlayerDTO.getName(), savedPlayer.getId(), tournamentId);

        return savedPlayer;
    }

    /**
     * Get all players in a tournament.
     *
     * @param tournamentId Tournament ID
     * @return List of players
     */
    public List<Player> getPlayersInTournament(UUID tournamentId) {
        return playerRepository.findByTournamentId(tournamentId);
    }

    /**
     * Get all active players in a tournament.
     *
     * @param tournamentId Tournament ID
     * @return List of active players
     */
    public List<Player> getActivePlayers(UUID tournamentId) {
        return playerRepository.findByTournamentIdAndActiveTrue(tournamentId);
    }

    /**
     * Deactivate a player (remove from tournament).
     *
     * @param playerId Player ID
     * @return Updated Player
     * @throws IllegalArgumentException if player not found
     */
    public Player deactivatePlayer(UUID playerId) {
        log.info("Deactivating player: {}", playerId);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        player.deactivate();
        Player updated = playerRepository.save(player);

        log.info("Player deactivated: {}", playerId);
        return updated;
    }

    /**
     * Reactivate a player.
     *
     * @param playerId Player ID
     * @return Updated Player
     * @throws IllegalArgumentException if player not found
     */
    public Player reactivatePlayer(UUID playerId) {
        log.info("Reactivating player: {}", playerId);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        player.activate();
        Player updated = playerRepository.save(player);

        log.info("Player reactivated: {}", playerId);
        return updated;
    }
}
