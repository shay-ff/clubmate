package model;
import java.util.List;
import java.util.UUID;


public class Round {
    private UUID roundsId;
    private UUID tournamentId;
    int roundNumber;

    public UUID getTournamentId() {
        return tournamentId;
    }
    public void setTournamentId(UUID tournamentId) {
        this.tournamentId = tournamentId;
    }
    public UUID getRoundsId() {
        return roundsId;
    }
    public void setRoundsId(UUID roundsId) {
        this.roundsId = roundsId;
    }
}
