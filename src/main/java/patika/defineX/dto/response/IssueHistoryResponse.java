package patika.defineX.dto.response;

import patika.defineX.model.IssueHistory;
import patika.defineX.model.enums.IssueStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record IssueHistoryResponse(
        UUID id,
        UUID issueId,
        IssueStatus previousStatus,
        IssueStatus newStatus,
        UUID changedBy,
        String reason,
        LocalDateTime createdAt
) {
    public static IssueHistoryResponse from (IssueHistory issueHistories) {
        return new IssueHistoryResponse(
                issueHistories.getId(),
                issueHistories.getIssue().getId(),
                issueHistories.getPreviousStatus(),
                issueHistories.getNewStatus(),
                issueHistories.getChangedBy().getId(),
                issueHistories.getReason(),
                issueHistories.getCreatedAt()
        );
    }
}
