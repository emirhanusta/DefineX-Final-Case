package patika.defineX.dto.request;

import patika.defineX.model.Department;

public record DepartmentRequest(
        String name
) {
     public static Department from (DepartmentRequest departmentRequest) {
        return Department.builder()
                .name(departmentRequest.name())
                .build();
     }
}
