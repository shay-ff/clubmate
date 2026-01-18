package clubmate.backend.round.repository;

import clubmate.backend.round.domain.Round;
import clubmate.backend.round.domain.RoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {

    /**
     * Find all rounds for a specific tournament
     */
    List<Round> findByTournamentId(UUID tournamentId);

    /**
     * Find rounds with a specific status for a tournament
     */
    List<Round> findByTournamentIdAndStatus(UUID tournamentId, RoundStatus status);

    /**
     * Find completed rounds for a tournament
     */
    List<Round> findByTournamentIdAndStatus(UUID tournamentId, RoundStatus status);
}
