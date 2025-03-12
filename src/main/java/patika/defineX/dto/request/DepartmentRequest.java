package patika.defineX.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import patika.defineX.model.Department;

public record DepartmentRequest(
        @NotBlank(message = "Department name cannot be blank")
        @Size(min = 2, max = 255, message = "Department name length must be between {min} and {max}")
        String name
) {
     public static Department from (DepartmentRequest departmentRequest) {
        return Department.builder()
                .name(departmentRequest.name().toUpperCase())
                .build();
     }
}
