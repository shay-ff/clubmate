package clubmate.backend.tournament.dto;

import clubmate.backend.tournament.domain.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for displaying tournament details to public users.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentDetailDTO {

    private UUID id;
    private String name;
    private Integer totalRounds;
    private Integer currentRound;
    private TournamentStatus status;
    private Instant createdAt;
    private Integer totalPlayers;
    private Integer activePlayers;

    public TournamentDetailDTO(UUID id, String name, Integer totalRounds, Integer currentRound,
                              TournamentStatus status, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.totalRounds = totalRounds;
        this.currentRound = currentRound;
        this.status = status;
        this.createdAt = createdAt;
    }
}
