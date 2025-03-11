package patika.defineX.dto.request;

import patika.defineX.model.enums.IssueType;
import patika.defineX.model.enums.PriorityLevel;

import java.time.LocalDateTime;
import java.util.UUID;

public record IssueUpdateRequest(
        UUID assigneeId,
        String title,
        String description,
        IssueType type,
        PriorityLevel priority,
        String userStory,
        String acceptanceCriteria,
        LocalDateTime dueDate
) {
}
