package patika.defineX.dto.request;

import java.util.UUID;

public record IssueCommentRequest(
        UUID issueId,
        UUID userId,
        String comment
) {
}
