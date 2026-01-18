package clubmate.backend.round.dto;

import clubmate.backend.round.domain.MatchResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for returning match result information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultResponseDTO {

    private UUID matchId;
    private UUID roundId;
    private UUID player1Id;
    private String player1Name;
    private UUID player2Id;
    private String player2Name;
    private MatchResult result;
    private UUID winnerId;
    private String status;

    public MatchResultResponseDTO(UUID matchId, UUID roundId, UUID player1Id, String player1Name,
                                  UUID player2Id, String player2Name, MatchResult result, UUID winnerId) {
        this.matchId = matchId;
        this.roundId = roundId;
        this.player1Id = player1Id;
        this.player1Name = player1Name;
        this.player2Id = player2Id;
        this.player2Name = player2Name;
        this.result = result;
        this.winnerId = winnerId;
        this.status = result.toString();
    }
}
