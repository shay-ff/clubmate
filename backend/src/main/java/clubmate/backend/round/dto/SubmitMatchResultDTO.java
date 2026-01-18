package clubmate.backend.round.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for submitting match results.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitMatchResultDTO {

    private UUID matchId;
    private String result; // "PLAYER1_WIN", "PLAYER2_WIN", or "DRAW"
    private UUID winnerId; // Required for WIN results, null for DRAW

    public boolean isDraw() {
        return "DRAW".equalsIgnoreCase(result);
    }

    public boolean isPlayer1Win() {
        return "PLAYER1_WIN".equalsIgnoreCase(result);
    }

    public boolean isPlayer2Win() {
        return "PLAYER2_WIN".equalsIgnoreCase(result);
    }

    public boolean isValid() {
        if (isDraw()) {
            return true;
        }
        return (isPlayer1Win() || isPlayer2Win()) && winnerId != null;
    }
}
