package patika.defineX.dto.request;

import patika.defineX.model.Issue;
import patika.defineX.model.enums.IssueStatus;
import patika.defineX.model.enums.IssueType;
import patika.defineX.model.enums.PriorityLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public record IssueRequest(
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
    public static Issue from (IssueRequest issueRequest) {
        return Issue.builder()
                .type(issueRequest.type())
                .title(issueRequest.title())
                .description(issueRequest.description())
                .userStory(issueRequest.userStory())
                .acceptanceCriteria(issueRequest.acceptanceCriteria())
                .status(issueRequest.status())
                .priority(issueRequest.priority())
                .dueDate(issueRequest.dueDate())
                .build();
    }
}
