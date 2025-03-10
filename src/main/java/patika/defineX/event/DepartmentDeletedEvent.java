package patika.defineX.event;

import java.util.UUID;

public record DepartmentDeletedEvent(
        UUID departmentId
) {}
