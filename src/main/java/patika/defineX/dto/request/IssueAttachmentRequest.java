package patika.defineX.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import patika.defineX.model.IssueAttachment;

import java.util.UUID;

public record IssueAttachmentRequest(
        @NotNull
        UUID issueId,
        String fileName,
        @NotBlank
        String url
) {
    public static IssueAttachment from(IssueAttachmentRequest issueAttachmentRequest) {
        return IssueAttachment.builder()
                .fileName(issueAttachmentRequest.fileName())
                .filePath(issueAttachmentRequest.url())
                .build();
    }
}
