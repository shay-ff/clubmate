package model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Entity
@Getter
@Setter
@Table(name="Match")
public class Match {
    @Id
    @GeneratedValue
    private UUID Id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Player whitePlayer;
    @ManyToOne(fetch = FetchType.LAZY)
    private Player blackPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    private MatchStatus status;
    int version;

    public void  setId(UUID id) {
        Id = id;
    }
    public UUID getId() {
        return Id;
    }
    public void setWhitePlayer(Player whitePlayer) {
        this.whitePlayer = whitePlayer;
    }
    public Player getWhitePlayer() {
        return whitePlayer;
    }
    public void setBlackPlayer(Player blackPlayer) {
        this.blackPlayer = blackPlayer;
    }
    public Player getBlackPlayer() {
        return blackPlayer;
    }
    public void setRound(Round round) {
        this.round = round;
    }
    public Round getRound() {
        return round;
    }
    public void setStatus(MatchStatus status) {
        this.status = status;
    }
    public MatchStatus getStatus() {
        return status;
    }
}
