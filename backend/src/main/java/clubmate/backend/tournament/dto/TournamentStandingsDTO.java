package clubmate.backend.tournament.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * DTO representing complete tournament standings.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentStandingsDTO {

    private UUID tournamentId;
    private String tournamentName;
    private Integer totalRounds;
    private Integer currentRound;
    private List<PlayerStandingDTO> standings;

    public TournamentStandingsDTO(UUID tournamentId, String tournamentName, Integer totalRounds, 
                                  Integer currentRound, List<PlayerStandingDTO> standings) {
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;
        this.totalRounds = totalRounds;
        this.currentRound = currentRound;
        this.standings = standings;
    }
}
