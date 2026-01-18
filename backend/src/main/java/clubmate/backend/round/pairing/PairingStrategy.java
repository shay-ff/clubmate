package clubmate.backend.round.pairing;

import clubmate.backend.player.domain.Player;
import clubmate.backend.round.domain.Match;

import java.util.List;
import java.util.UUID;

/**
 * Abstract strategy for tournament pairing algorithms.
 * Implement this interface to add new pairing strategies (Round-Robin, Elimination, etc.)
 */
public interface PairingStrategy {

    /**
     * Generate pairings for a round given a list of players.
     *
     * @param roundId       The ID of the current round
     * @param players       List of players to pair
     * @return              List of matches representing the pairings
     */
    List<Match> generatePairings(UUID roundId, List<Player> players);

    /**
     * Get the name of this pairing strategy
     *
     * @return Strategy name (e.g., "SWISS", "ROUND_ROBIN", "KNOCKOUT")
     */
    String getStrategyName();

    /**
     * Validate if the pairing strategy can be applied to the given number of players
     *
     * @param playerCount Number of active players
     * @return true if pairing can be generated, false otherwise
     */
    boolean canPair(int playerCount);
}
