package clubmate.backend.round.repository;

import clubmate.backend.round.domain.Match;
import clubmate.backend.round.domain.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {

    /**
     * Find all matches in a specific round
     */
    List<Match> findByRoundId(UUID roundId);

    /**
     * Find all completed matches in a specific round
     */
    List<Match> findByRoundIdAndResultNot(UUID roundId, MatchResult result);

    /**
     * Find all matches for a specific player in a tournament (either as player1 or player2)
     */
    List<Match> findByPlayer1IdOrPlayer2Id(UUID player1Id, UUID player2Id);

    /**
     * Find matches in a specific tournament (by roundId association)
     * This requires joining with Round table
     */
    List<Match> findAll();
}
