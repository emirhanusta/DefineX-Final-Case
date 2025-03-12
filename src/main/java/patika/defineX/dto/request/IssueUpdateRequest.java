package patika.defineX.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import patika.defineX.model.enums.IssueType;
import patika.defineX.model.enums.PriorityLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public record IssueUpdateRequest(
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
        LocalDateTime dueDate
) {
}
