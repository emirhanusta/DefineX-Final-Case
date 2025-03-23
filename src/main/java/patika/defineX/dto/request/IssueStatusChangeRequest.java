package patika.defineX.dto.request;

import jakarta.validation.constraints.NotNull;
import patika.defineX.model.enums.IssueStatus;


public record IssueStatusChangeRequest(
        @NotNull
        IssueStatus status,
        String reason
) {
}
