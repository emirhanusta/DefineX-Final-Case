package patika.defineX.dto.request;

import jakarta.validation.constraints.NotNull;
import patika.defineX.model.enums.IssueStatus;

import java.util.UUID;

public record IssueStatusChangeRequest(
        @NotNull
        UUID changedBy,
        @NotNull
        IssueStatus status,
        String reason
) {
}
