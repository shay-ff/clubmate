package clubmate.backend.round.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rounds")
@Getter
@Setter
@NoArgsConstructor
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tournamentId;

    @Column(nullable = false)
    private Integer roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundStatus status = RoundStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = true)
    private Instant updatedAt;

    public Round(UUID tournamentId, Integer roundNumber) {
        this.tournamentId = tournamentId;
        this.roundNumber = roundNumber;
        this.status = RoundStatus.PENDING;
    }

    public void startRound() {
        this.status = RoundStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public void finishRound() {
        this.status = RoundStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public boolean isPending() {
        return this.status == RoundStatus.PENDING;
    }

    public boolean isInProgress() {
        return this.status == RoundStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return this.status == RoundStatus.COMPLETED;
    }
}
