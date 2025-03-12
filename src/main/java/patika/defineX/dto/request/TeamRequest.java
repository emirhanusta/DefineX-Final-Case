package patika.defineX.dto.request;

import java.util.UUID;

public record TeamRequest(
        String name,
        UUID projectId
) {
}
