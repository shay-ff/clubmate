package clubmate.backend.round.dto;

import clubmate.backend.round.domain.MatchResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for displaying match/pairing information to public users.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PairingDTO {

    private UUID matchId;
    private Integer roundNumber;
    private UUID player1Id;
    private String player1Name;
    private UUID player2Id;
    private String player2Name;
    private MatchResult result;
    private UUID winnerId;
    private String winnerName;
    private Boolean isCompleted;

    public PairingDTO(UUID matchId, Integer roundNumber, UUID player1Id, String player1Name,
                     UUID player2Id, String player2Name, MatchResult result, UUID winnerId) {
        this.matchId = matchId;
        this.roundNumber = roundNumber;
        this.player1Id = player1Id;
        this.player1Name = player1Name;
        this.player2Id = player2Id;
        this.player2Name = player2Name;
        this.result = result;
        this.winnerId = winnerId;
        this.isCompleted = result != null && !result.equals(MatchResult.PENDING);
    }
}
