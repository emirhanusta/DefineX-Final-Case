package patika.defineX.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record IssueCommentRequest(
        @NotNull
        UUID issueId,
        @NotNull
        UUID userId,
        @NotBlank
        @Size(min = 1, max = 255)
        String comment
) {
}
