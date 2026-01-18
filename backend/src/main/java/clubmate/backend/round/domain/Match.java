package clubmate.backend.round.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID roundId;

    @Column(nullable = false)
    private UUID player1Id;

    @Column(nullable = false)
    private UUID player2Id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchResult result = MatchResult.PENDING;

    @Column(nullable = true)
    private UUID winnerId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = true)
    private Instant updatedAt;

    public Match(UUID roundId, UUID player1Id, UUID player2Id) {
        this.roundId = roundId;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.result = MatchResult.PENDING;
    }

    public void setWinner(UUID winnerId) {
        this.winnerId = winnerId;
        if (winnerId.equals(this.player1Id)) {
            this.result = MatchResult.PLAYER1_WIN;
        } else if (winnerId.equals(this.player2Id)) {
            this.result = MatchResult.PLAYER2_WIN;
        }
        this.updatedAt = Instant.now();
    }

    public void setDraw() {
        this.result = MatchResult.DRAW;
        this.winnerId = null;
        this.updatedAt = Instant.now();
    }

    public boolean isPending() {
        return this.result == MatchResult.PENDING;
    }

    public boolean isCompleted() {
        return this.result != MatchResult.PENDING;
    }
}
