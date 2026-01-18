package clubmate.backend.tournament.repository;

import clubmate.backend.tournament.domain.Tournament;
import clubmate.backend.tournament.domain.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {

    /**
     * Find a tournament by its name
     */
    Optional<Tournament> findByName(String name);

    /**
     * Find all tournaments with a specific status
     */
    List<Tournament> findByStatus(TournamentStatus status);

    /**
     * Find all active tournaments (IN_PROGRESS or CREATED status)
     */
    List<Tournament> findByStatusIn(List<TournamentStatus> statuses);

    /**
     * Check if a tournament with the given name already exists
     */
    boolean existsByName(String name);
}
