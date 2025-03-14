package patika.defineX.event;

import java.util.UUID;

public record ProjectDeletedEvent(
        UUID projectId
){
}
