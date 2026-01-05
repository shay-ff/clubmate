package model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name="players")
public class Player {
    @Column(nullable = false)
    private String displayName;
    @Id
    @GeneratedValue
    private UUID Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tournamentId",  nullable = false)
    private UUID tournamentId;

    private Double score;

    boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public void getDisplayName() {
        this.displayName = displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public UUID getId() {
        return Id;
    }
    public void setId(UUID id) {
        Id = id;
    }
    public UUID getTournamentId() {
        return tournamentId;
    }
    public void setTournamentId(UUID tournamentId) {
        this.tournamentId = tournamentId;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    public Double getScore() {
        return score;
    }
    public void setScore(Double score) {
        this.score = score;
    }
}
