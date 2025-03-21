package patika.defineX.dto.response;

import patika.defineX.model.Department;

import java.io.Serializable;
import java.util.UUID;

public record DepartmentResponse(
        UUID id,
        String name
) implements Serializable {
    public static DepartmentResponse from (Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName()
        );
    }
}
