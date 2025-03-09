package patika.defineX.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberId {
    private UUID teamId;
    private UUID userId;
}

