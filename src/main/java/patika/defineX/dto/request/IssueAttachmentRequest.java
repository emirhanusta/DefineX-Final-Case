package patika.defineX.dto.request;

import patika.defineX.model.IssueAttachment;

import java.util.UUID;

public record IssueAttachmentRequest(
        UUID issueId,
        String fileName,
        String url
) {
    public static IssueAttachment from(IssueAttachmentRequest issueAttachmentRequest) {
        return IssueAttachment.builder()
                .fileName(issueAttachmentRequest.fileName())
                .filePath(issueAttachmentRequest.url())
                .build();
    }
}
