package patika.defineX.dto.request;

import patika.defineX.model.enums.IssueStatus;

import java.util.UUID;

public record IssueStatusChangeRequest(
        UUID changedBy,
        IssueStatus status,
        String reason
) {
}
