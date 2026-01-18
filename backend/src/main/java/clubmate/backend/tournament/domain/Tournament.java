package clubmate.backend.tournament.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@NoArgsConstructor
public class Tournament {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer totalRounds;

    @Column(nullable = false)
    private Integer currentRound = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status = TournamentStatus.CREATED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = true)
    private Instant updatedAt;

    // Relationships will be added in future extensions
    // @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Player> players;
    //
    // @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Round> rounds;

    public Tournament(String name, Integer totalRounds) {
        this.name = name;
        this.totalRounds = totalRounds;
        this.status = TournamentStatus.CREATED;
        this.currentRound = 0;
    }

    public void updateStatus(TournamentStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void advanceRound() {
        if (this.currentRound < this.totalRounds) {
            this.currentRound++;
            this.updatedAt = Instant.now();
        }
    }

    public boolean isActive() {
        return this.status == TournamentStatus.IN_PROGRESS;
    }

    public boolean isFinished() {
        return this.status == TournamentStatus.FINISHED;
    }
}
