package patika.defineX.event;

import java.util.UUID;

public record IssueDeletedEvent (
        UUID issueId
){}
