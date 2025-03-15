package patika.defineX.dto.response;

import patika.defineX.model.Issue;
import patika.defineX.model.enums.IssueStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record IssueResponse(
        UUID id,
        UUID projectId,
        UUID assigneeId,
        UUID reporterId,
        String type,
        String title,
        String description,
        String userStory,
        String acceptanceCriteria,
        IssueStatus status,
        String priority,
        LocalDateTime dueDate
) {
    public static IssueResponse from(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getProject().getId(),
                issue.getAssignee() != null ? issue.getAssignee().getId() : null,
                issue.getReporter().getId(),
                issue.getType().getDisplayName(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getUserStory(),
                issue.getAcceptanceCriteria(),
                issue.getStatus(),
                issue.getPriority().getDisplayName(),
                issue.getDueDate()
        );
    }
}
