package model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name="tournaments")
public class Tournament {
    @Id
    @GeneratedValue
    private UUID tournamentId;

    @Column(nullable = false, unique = true)
    private String tournamentName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus tournamentStatus;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "tournaments", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Player>  players;
    @OneToMany(mappedBy = "tournaments", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Round> rounds;


}


