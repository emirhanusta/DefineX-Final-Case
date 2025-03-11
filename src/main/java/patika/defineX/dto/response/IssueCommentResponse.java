package patika.defineX.dto.response;

import patika.defineX.model.IssueComment;

import java.util.UUID;

public record IssueCommentResponse(
        UUID id,
        UUID issueId,
        UUID userId,
        String comment
) {
    public static IssueCommentResponse from(IssueComment issueComment) {
        return new IssueCommentResponse(
                issueComment.getId(),
                issueComment.getIssue().getId(),
                issueComment.getUser().getId(),
                issueComment.getComment()
        );
    }
}
