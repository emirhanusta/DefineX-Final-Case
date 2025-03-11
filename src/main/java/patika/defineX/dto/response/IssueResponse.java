package patika.defineX.dto.response;

import patika.defineX.model.Issue;
import patika.defineX.model.enums.IssueStatus;
import patika.defineX.model.enums.IssueType;
import patika.defineX.model.enums.PriorityLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public record IssueResponse(
        UUID id,
        UUID projectId,
        UUID assigneeId,
        UUID reporterId,
        IssueType type,
        String title,
        String description,
        String userStory,
        String acceptanceCriteria,
        IssueStatus status,
        PriorityLevel priority,
        LocalDateTime dueDate
) {
    public static IssueResponse from(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getProject().getId(),
                issue.getAssignee() != null ? issue.getAssignee().getId() : null,
                issue.getReporter().getId(),
                issue.getType(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getUserStory(),
                issue.getAcceptanceCriteria(),
                issue.getStatus(),
                issue.getPriority(),
                issue.getDueDate()
        );
    }
}
