package patika.defineX.dto.response;

import java.util.List;
import java.util.UUID;

public record TeamMemberListResponse(
        UUID id,
        String name,
        List<TeamMemberResponse> members
) {
}