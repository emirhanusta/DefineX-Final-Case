package patika.defineX.dto.response;

import patika.defineX.model.Project;

import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String title,
        String description,
        String departmentName,
        String status
) {
    public static ProjectResponse from (Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getDepartment().getName(),
                project.getStatus().getDisplayName()
        );
    }
}
