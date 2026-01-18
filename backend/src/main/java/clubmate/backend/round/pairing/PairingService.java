package clubmate.backend.round.pairing;

import clubmate.backend.player.domain.Player;
import clubmate.backend.round.domain.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing tournament pairings.
 * Uses the strategy pattern to support multiple pairing algorithms.
 * Can be easily extended with new strategies like Round-Robin, Knockout, etc.
 */
@Slf4j
@Service
public class PairingService {

    private final PairingStrategy pairingStrategy;

    @Autowired
    public PairingService(SwissPairingStrategy swissPairingStrategy) {
        // Default to Swiss pairing. Can be made configurable per tournament in the future.
        this.pairingStrategy = swissPairingStrategy;
    }

    /**
     * Generate pairings for a round using the current strategy.
     *
     * @param roundId The ID of the round
     * @param players List of active players in the tournament
     * @return        List of matches for the round
     * @throws IllegalArgumentException if pairing cannot be performed
     */
    public List<Match> generateRoundPairings(UUID roundId, List<Player> players) {
        log.info("Generating pairings for round {} with {} players using {}",
                roundId, players.size(), pairingStrategy.getStrategyName());

        if (!pairingStrategy.canPair(players.size())) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot generate pairings with %d players using %s strategy",
                    players.size(),
                    pairingStrategy.getStrategyName()
                )
            );
        }

        return pairingStrategy.generatePairings(roundId, players);
    }

    /**
     * Get the current pairing strategy name.
     *
     * @return Strategy name
     */
    public String getCurrentStrategyName() {
        return pairingStrategy.getStrategyName();
    }

    /**
     * Check if pairing is possible with the given number of players.
     *
     * @param playerCount Number of active players
     * @return true if pairing can be performed
     */
    public boolean canGeneratePairings(int playerCount) {
        return pairingStrategy.canPair(playerCount);
    }
}
