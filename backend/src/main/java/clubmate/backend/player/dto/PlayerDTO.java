package clubmate.backend.player.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO for displaying player information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {

    private UUID id;
    private String name;
    private UUID tournamentId;
    private Double score;
    private Boolean active;
}
