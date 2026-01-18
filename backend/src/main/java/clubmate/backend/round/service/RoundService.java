package clubmate.backend.round.service;

import clubmate.backend.player.domain.Player;
import clubmate.backend.round.domain.Match;
import clubmate.backend.round.domain.Round;
import clubmate.backend.round.pairing.PairingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing tournament rounds.
 * Coordinates with PairingService to generate matches and manage round progression.
 */
@Slf4j
@Service
public class RoundService {

    private final PairingService pairingService;

    @Autowired
    public RoundService(PairingService pairingService) {
        this.pairingService = pairingService;
    }

    /**
     * Create and generate pairings for a new round.
     *
     * @param tournamentId Tournament ID
     * @param roundNumber  Round number
     * @param players      List of active players
     * @return             Generated Round with associated matches
     */
    public Round createRoundWithPairings(UUID tournamentId, Integer roundNumber, List<Player> players) {
        log.info("Creating round {} for tournament {} with {} players",
                roundNumber, tournamentId, players.size());

        Round round = new Round(tournamentId, roundNumber);

        // Validate pairing can be performed
        if (!pairingService.canGeneratePairings(players.size())) {
            throw new IllegalStateException(
                String.format(
                    "Cannot create round with %d players. Minimum required: 2",
                    players.size()
                )
            );
        }

        // Generate pairings
        List<Match> matches = pairingService.generateRoundPairings(round.getId(), players);
        
        log.info("Round {} created with {} matches", roundNumber, matches.size());
        return round;
    }

    /**
     * Start a round (mark as IN_PROGRESS).
     *
     * @param round The round to start
     */
    public void startRound(Round round) {
        if (!round.isPending()) {
            throw new IllegalStateException(
                String.format("Cannot start round %d. Current status: %s",
                    round.getRoundNumber(), round.getStatus())
            );
        }

        round.startRound();
        log.info("Round {} started", round.getRoundNumber());
    }

    /**
     * Finish a round (mark as COMPLETED).
     *
     * @param round The round to finish
     */
    public void finishRound(Round round) {
        if (!round.isInProgress()) {
            throw new IllegalStateException(
                String.format("Cannot finish round %d. Current status: %s",
                    round.getRoundNumber(), round.getStatus())
            );
        }

        round.finishRound();
        log.info("Round {} finished", round.getRoundNumber());
    }

    /**
     * Get the current pairing strategy being used.
     *
     * @return Strategy name
     */
    public String getCurrentPairingStrategy() {
        return pairingService.getCurrentStrategyName();
    }
}
