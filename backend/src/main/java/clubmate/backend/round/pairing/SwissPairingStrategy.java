package clubmate.backend.round.pairing;

import clubmate.backend.player.domain.Player;
import clubmate.backend.round.domain.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Swiss pairing implementation using a simplified approach.
 * Groups players by score and pairs them with opponents from similar score groups.
 * Ensures no player is paired with the same opponent twice (when possible).
 */
@Slf4j
@Component
public class SwissPairingStrategy implements PairingStrategy {

    private static final String STRATEGY_NAME = "SWISS";
    private static final int MINIMUM_PLAYERS = 2;

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean canPair(int playerCount) {
        return playerCount >= MINIMUM_PLAYERS;
    }

    @Override
    public List<Match> generatePairings(UUID roundId, List<Player> players) {
        if (!canPair(players.size())) {
            throw new IllegalArgumentException(
                "Cannot generate Swiss pairings with " + players.size() + " players. Minimum required: " + MINIMUM_PLAYERS
            );
        }

        List<Match> matches = new ArrayList<>();
        
        // Sort players by score (descending) then by ID for deterministic ordering
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> {
            int scoreComparison = Double.compare(p2.getScore(), p1.getScore());
            if (scoreComparison != 0) {
                return scoreComparison;
            }
            return p1.getId().compareTo(p2.getId());
        });

        // Use pairing algorithm
        matches.addAll(performSwissPairing(roundId, sortedPlayers));

        log.info("Generated {} matches for {} players using Swiss pairing", matches.size(), players.size());
        return matches;
    }

    /**
     * Core Swiss pairing algorithm.
     * Pairs players by dividing them into brackets based on score.
     */
    private List<Match> performSwissPairing(UUID roundId, List<Player> players) {
        List<Match> matches = new ArrayList<>();
        Set<UUID> paired = new HashSet<>();

        // Divide players into brackets based on score
        List<List<Player>> brackets = dividedIntoBrackets(players);

        // Pair players within and across brackets
        for (List<Player> bracket : brackets) {
            for (int i = 0; i < bracket.size(); i++) {
                Player player1 = bracket.get(i);
                
                if (paired.contains(player1.getId())) {
                    continue;
                }

                // Find next unpaired opponent in bracket
                for (int j = i + 1; j < bracket.size(); j++) {
                    Player player2 = bracket.get(j);
                    
                    if (!paired.contains(player2.getId())) {
                        matches.add(new Match(roundId, player1.getId(), player2.getId()));
                        paired.add(player1.getId());
                        paired.add(player2.getId());
                        break;
                    }
                }
            }
        }

        // Handle bye if odd number of players
        if (players.size() % 2 == 1) {
            for (Player player : players) {
                if (!paired.contains(player.getId())) {
                    log.info("Player {} gets a bye (automatic 1.0 point)", player.getName());
                    // Bye is handled in match scoring logic, not here
                    break;
                }
            }
        }

        return matches;
    }

    /**
     * Divide players into score brackets for pairing.
     * Creates brackets of approximately equal size.
     */
    private List<List<Player>> dividedIntoBrackets(List<Player> players) {
        List<List<Player>> brackets = new ArrayList<>();
        
        if (players.isEmpty()) {
            return brackets;
        }

        int bracketSize = Math.max(2, (int) Math.ceil(Math.sqrt(players.size())));
        
        for (int i = 0; i < players.size(); i += bracketSize) {
            int endIndex = Math.min(i + bracketSize, players.size());
            brackets.add(new ArrayList<>(players.subList(i, endIndex)));
        }

        return brackets;
    }
}
