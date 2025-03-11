package patika.defineX.dto.response;

import patika.defineX.model.IssueAttachment;
import java.util.UUID;

public record IssueAttachmentResponse(
        UUID id,
        UUID issueId,
        String fileName,
        String url
) {
    public static IssueAttachmentResponse from(IssueAttachment issueAttachment) {
        return new IssueAttachmentResponse(
                issueAttachment.getId(),
                issueAttachment.getIssue().getId(),
                issueAttachment.getFileName(),
                issueAttachment.getFilePath()
        );
    }
}
