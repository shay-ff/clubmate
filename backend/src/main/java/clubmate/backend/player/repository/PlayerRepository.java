package clubmate.backend.player.repository;

import clubmate.backend.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    /**
     * Find all players in a specific tournament
     */
    List<Player> findByTournamentId(UUID tournamentId);

    /**
     * Find all active players in a specific tournament
     */
    List<Player> findByTournamentIdAndActiveTrue(UUID tournamentId);

    /**
     * Check if a player exists in a tournament
     */
    boolean existsByIdAndTournamentId(UUID playerId, UUID tournamentId);
}
