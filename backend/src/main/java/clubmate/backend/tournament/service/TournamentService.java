package clubmate.backend.tournament.service;

import clubmate.backend.common.exception.ValidationException;
import clubmate.backend.tournament.domain.Tournament;
import clubmate.backend.tournament.domain.TournamentStatus;
import clubmate.backend.tournament.dto.CreateTournamentDTO;
import clubmate.backend.tournament.repository.TournamentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for managing tournament operations.
 * Handles tournament creation, updates, and admin operations.
 */
@Slf4j
@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    @Autowired
    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    /**
     * Create a new tournament.
     *
     * @param createDTO DTO with tournament details
     * @return Created Tournament
     * @throws ValidationException if validation fails
     */
    public Tournament createTournament(CreateTournamentDTO createDTO) {
        log.info("Creating new tournament: {}", createDTO.getName());

        if (!createDTO.isValid()) {
            throw new ValidationException("Invalid tournament data. Name and totalRounds are required.");
        }

        if (tournamentRepository.existsByName(createDTO.getName())) {
            throw new ValidationException("Tournament with name already exists: " + createDTO.getName());
        }

        if (createDTO.getTotalRounds() > 100) {
            throw new ValidationException("Total rounds cannot exceed 100");
        }

        Tournament tournament = new Tournament(
                createDTO.getName(),
                createDTO.getTotalRounds()
        );

        Tournament savedTournament = tournamentRepository.save(tournament);
        log.info("Tournament created successfully: {} (ID: {})", savedTournament.getName(), savedTournament.getId());

        return savedTournament;
    }

    /**
     * Update tournament status.
     *
     * @param tournamentId Tournament ID
     * @param newStatus    New status
     * @return Updated Tournament
     * @throws IllegalArgumentException if tournament not found
     */
    public Tournament updateTournamentStatus(UUID tournamentId, TournamentStatus newStatus) {
        log.info("Updating tournament {} status to {}", tournamentId, newStatus);

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentId));

        tournament.updateStatus(newStatus);
        Tournament updated = tournamentRepository.save(tournament);

        log.info("Tournament status updated: {}", tournamentId);
        return updated;
    }

    /**
     * Start a tournament (transition from CREATED to IN_PROGRESS).
     *
     * @param tournamentId Tournament ID
     * @return Updated Tournament
     */
    public Tournament startTournament(UUID tournamentId) {
        log.info("Starting tournament: {}", tournamentId);

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentId));

        if (!tournament.getStatus().equals(TournamentStatus.CREATED)) {
            throw new IllegalStateException(
                String.format("Cannot start tournament. Current status: %s", tournament.getStatus())
            );
        }

        return updateTournamentStatus(tournamentId, TournamentStatus.IN_PROGRESS);
    }

    /**
     * Pause a tournament.
     *
     * @param tournamentId Tournament ID
     * @return Updated Tournament
     */
    public Tournament pauseTournament(UUID tournamentId) {
        log.info("Pausing tournament: {}", tournamentId);

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentId));

        if (!tournament.getStatus().equals(TournamentStatus.IN_PROGRESS)) {
            throw new IllegalStateException(
                String.format("Cannot pause tournament. Current status: %s", tournament.getStatus())
            );
        }

        return updateTournamentStatus(tournamentId, TournamentStatus.PAUSED);
    }

    /**
     * Resume a paused tournament.
     *
     * @param tournamentId Tournament ID
     * @return Updated Tournament
     */
    public Tournament resumeTournament(UUID tournamentId) {
        log.info("Resuming tournament: {}", tournamentId);

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentId));

        if (!tournament.getStatus().equals(TournamentStatus.PAUSED)) {
            throw new IllegalStateException(
                String.format("Cannot resume tournament. Current status: %s", tournament.getStatus())
            );
        }

        return updateTournamentStatus(tournamentId, TournamentStatus.IN_PROGRESS);
    }

    /**
     * Finish a tournament.
     *
     * @param tournamentId Tournament ID
     * @return Updated Tournament
     */
    public Tournament finishTournament(UUID tournamentId) {
        log.info("Finishing tournament: {}", tournamentId);

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentId));

        return updateTournamentStatus(tournamentId, TournamentStatus.FINISHED);
    }
}
