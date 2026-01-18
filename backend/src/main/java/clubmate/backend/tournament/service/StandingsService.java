package clubmate.backend.tournament.service;

import clubmate.backend.player.domain.Player;
import clubmate.backend.player.repository.PlayerRepository;
import clubmate.backend.round.domain.Match;
import clubmate.backend.round.domain.MatchResult;
import clubmate.backend.round.domain.Round;
import clubmate.backend.round.repository.MatchRepository;
import clubmate.backend.round.repository.RoundRepository;
import clubmate.backend.tournament.domain.Tournament;
import clubmate.backend.tournament.dto.PlayerStandingDTO;
import clubmate.backend.tournament.dto.TournamentStandingsDTO;
import clubmate.backend.tournament.repository.TournamentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating and retrieving tournament standings.
 * Calculates player scores based on match results and provides sorted standings.
 * Tournament-scoped: all standings calculations are tied to a specific tournament.
 */
@Slf4j
@Service
public class StandingsService {

    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final RoundRepository roundRepository;
    private final MatchRepository matchRepository;

    private static final double POINTS_FOR_WIN = 1.0;
    private static final double POINTS_FOR_DRAW = 0.5;
    private static final double POINTS_FOR_LOSS = 0.0;

    @Autowired
    public StandingsService(TournamentRepository tournamentRepository,
                           PlayerRepository playerRepository,
                           RoundRepository roundRepository,
                           MatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
        this.roundRepository = roundRepository;
        this.matchRepository = matchRepository;
    }

    /**
     * Get complete tournament standings including all players and their scores.
     *
     * @param tournamentId The ID of the tournament
     * @return TournamentStandingsDTO with sorted standings
     * @throws IllegalArgumentException if tournament not found
     */
    public TournamentStandingsDTO getTournamentStandings(UUID tournamentId) {
        log.info("Fetching standings for tournament: {}", tournamentId);

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Tournament not found with ID: " + tournamentId
                ));

        // Get all active players in the tournament
        List<Player> players = playerRepository.findByTournamentIdAndActiveTrue(tournamentId);
        
        // Calculate standings for each player
        List<PlayerStandingDTO> standings = players.stream()
                .map(player -> calculatePlayerStanding(tournamentId, player))
                .sorted(Comparator.comparingDouble(PlayerStandingDTO::getScore).reversed())
                .peek((standing, rank) -> standing.setRank(rank + 1), 0)
                .collect(Collectors.toList());

        // Assign ranks
        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setRank(i + 1);
        }

        log.info("Generated standings for tournament {} with {} players", tournamentId, standings.size());

        return new TournamentStandingsDTO(
                tournament.getId(),
                tournament.getName(),
                tournament.getTotalRounds(),
                tournament.getCurrentRound(),
                standings
        );
    }

    /**
     * Get standings for a specific round (cumulative score up to that round).
     *
     * @param tournamentId The ID of the tournament
     * @param roundNumber  The round number (cumulative)
     * @return TournamentStandingsDTO with standings up to the specified round
     */
    public TournamentStandingsDTO getTournamentStandingsByRound(UUID tournamentId, Integer roundNumber) {
        log.info("Fetching standings for tournament {} up to round {}", tournamentId, roundNumber);

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Tournament not found with ID: " + tournamentId
                ));

        List<Player> players = playerRepository.findByTournamentIdAndActiveTrue(tournamentId);
        List<Round> rounds = roundRepository.findByTournamentId(tournamentId);
        
        // Filter rounds up to the specified round number
        Set<UUID> roundsUpToSpecified = rounds.stream()
                .filter(r -> r.getRoundNumber() <= roundNumber)
                .map(Round::getId)
                .collect(Collectors.toSet());

        List<PlayerStandingDTO> standings = players.stream()
                .map(player -> calculatePlayerStandingUpToRound(player, roundsUpToSpecified))
                .sorted(Comparator.comparingDouble(PlayerStandingDTO::getScore).reversed())
                .collect(Collectors.toList());

        // Assign ranks
        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setRank(i + 1);
        }

        log.info("Generated standings for tournament {} up to round {} with {} players",
                tournamentId, roundNumber, standings.size());

        return new TournamentStandingsDTO(
                tournament.getId(),
                tournament.getName(),
                tournament.getTotalRounds(),
                roundNumber,
                standings
        );
    }

    /**
     * Calculate standings for a single player across all their completed matches.
     *
     * @param tournamentId The tournament ID
     * @param player       The player to calculate standings for
     * @return PlayerStandingDTO with calculated score and match count
     */
    private PlayerStandingDTO calculatePlayerStanding(UUID tournamentId, Player player) {
        // Get all matches involving this player
        List<Match> playerMatches = matchRepository.findByPlayer1IdOrPlayer2Id(player.getId(), player.getId());

        // Filter to only completed matches
        List<Match> completedMatches = playerMatches.stream()
                .filter(Match::isCompleted)
                .collect(Collectors.toList());

        // Calculate score
        double score = calculateScore(player.getId(), completedMatches);

        return new PlayerStandingDTO(
                player.getId(),
                player.getName(),
                score,
                completedMatches.size()
        );
    }

    /**
     * Calculate standings for a player up to a specific set of rounds.
     *
     * @param player             The player
     * @param roundIdsToInclude  Set of round IDs to include
     * @return PlayerStandingDTO with calculated score
     */
    private PlayerStandingDTO calculatePlayerStandingUpToRound(Player player, Set<UUID> roundIdsToInclude) {
        List<Match> playerMatches = matchRepository.findByPlayer1IdOrPlayer2Id(player.getId(), player.getId());

        List<Match> relevantMatches = playerMatches.stream()
                .filter(match -> roundIdsToInclude.contains(match.getRoundId()))
                .filter(Match::isCompleted)
                .collect(Collectors.toList());

        double score = calculateScore(player.getId(), relevantMatches);

        return new PlayerStandingDTO(
                player.getId(),
                player.getName(),
                score,
                relevantMatches.size()
        );
    }

    /**
     * Calculate total score for a player from their matches.
     *
     * @param playerId The player ID
     * @param matches  List of completed matches
     * @return Total score
     */
    private double calculateScore(UUID playerId, List<Match> matches) {
        return matches.stream()
                .mapToDouble(match -> {
                    if (match.getResult() == MatchResult.DRAW) {
                        return POINTS_FOR_DRAW;
                    } else if (match.getResult() == MatchResult.PLAYER1_WIN && playerId.equals(match.getPlayer1Id())) {
                        return POINTS_FOR_WIN;
                    } else if (match.getResult() == MatchResult.PLAYER2_WIN && playerId.equals(match.getPlayer2Id())) {
                        return POINTS_FOR_WIN;
                    } else {
                        return POINTS_FOR_LOSS;
                    }
                })
                .sum();
    }

    /**
     * Get the score for a specific player in a tournament.
     *
     * @param tournamentId The tournament ID
     * @param playerId     The player ID
     * @return The player's current score
     */
    public Double getPlayerScore(UUID tournamentId, UUID playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        if (!player.getTournamentId().equals(tournamentId)) {
            throw new IllegalArgumentException(
                String.format("Player %s is not in tournament %s", playerId, tournamentId)
            );
        }

        List<Match> playerMatches = matchRepository.findByPlayer1IdOrPlayer2Id(playerId, playerId);
        List<Match> completedMatches = playerMatches.stream()
                .filter(Match::isCompleted)
                .collect(Collectors.toList());

        return calculateScore(playerId, completedMatches);
    }

    /**
     * Get the ranking for a specific player in the current tournament standings.
     *
     * @param tournamentId The tournament ID
     * @param playerId     The player ID
     * @return The player's rank (1-based), or null if not in top rankings
     */
    public Integer getPlayerRank(UUID tournamentId, UUID playerId) {
        TournamentStandingsDTO standings = getTournamentStandings(tournamentId);

        return standings.getStandings().stream()
                .filter(standing -> standing.getPlayerId().equals(playerId))
                .map(PlayerStandingDTO::getRank)
                .findFirst()
                .orElse(null);
    }
}
