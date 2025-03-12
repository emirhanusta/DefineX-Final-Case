package patika.defineX.event;

import java.util.UUID;

public record TeamDeletedEvent(
        UUID teamId
) {
}
