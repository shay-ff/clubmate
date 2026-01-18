package clubmate.backend.round.dto;

import clubmate.backend.round.domain.RoundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for displaying round information to public users.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoundDetailDTO {

    private UUID id;
    private UUID tournamentId;
    private Integer roundNumber;
    private RoundStatus status;
    private Integer totalMatches;
    private Integer completedMatches;
    private Integer pendingMatches;

    public RoundDetailDTO(UUID id, UUID tournamentId, Integer roundNumber, RoundStatus status,
                         Integer totalMatches, Integer completedMatches, Integer pendingMatches) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.roundNumber = roundNumber;
        this.status = status;
        this.totalMatches = totalMatches;
        this.completedMatches = completedMatches;
        this.pendingMatches = pendingMatches;
    }
}
