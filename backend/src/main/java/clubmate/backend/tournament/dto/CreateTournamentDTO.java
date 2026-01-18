package clubmate.backend.tournament.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating a new tournament.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTournamentDTO {

    private String name;
    private Integer totalRounds;

    public boolean isValid() {
        return name != null && !name.trim().isEmpty()
                && totalRounds != null && totalRounds > 0;
    }
}
