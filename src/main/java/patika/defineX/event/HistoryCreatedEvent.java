package patika.defineX.event;

import patika.defineX.dto.request.IssueStatusChangeRequest;
import patika.defineX.model.Issue;

public record HistoryCreatedEvent(
        Issue issue,
        IssueStatusChangeRequest request
) {
}
