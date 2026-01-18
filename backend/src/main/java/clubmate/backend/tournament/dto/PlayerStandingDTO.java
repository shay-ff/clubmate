package clubmate.backend.tournament.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO representing a player's standing in the tournament.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStandingDTO {

    private UUID playerId;
    private String playerName;
    private Double score;
    private Integer matchesPlayed;
    private Integer rank;

    public PlayerStandingDTO(UUID playerId, String playerName, Double score, Integer matchesPlayed) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.score = score;
        this.matchesPlayed = matchesPlayed;
    }
}
