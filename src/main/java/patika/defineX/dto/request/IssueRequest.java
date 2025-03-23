package patika.defineX.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import patika.defineX.model.Issue;
import patika.defineX.model.enums.IssueStatus;
import patika.defineX.model.enums.IssueType;
import patika.defineX.model.enums.PriorityLevel;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record IssueRequest(
        @NotNull
        UUID projectId,
        UUID assigneeId,
        @NotBlank
        @Size(min = 2, max = 50)
        String title,
        @NotBlank
        @Size(min = 2, max = 50)
        String description,
        @NotNull
        IssueType type,
        @NotNull
        PriorityLevel priority,
        String userStory,
        String acceptanceCriteria,
        IssueStatus status,
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
