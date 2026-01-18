package clubmate.backend.player.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for adding a player to a tournament.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddPlayerDTO {

    private String name;

    public boolean isValid() {
        return name != null && !name.trim().isEmpty();
    }
}
