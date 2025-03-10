package patika.defineX.dto.response;

import patika.defineX.model.Project;
import patika.defineX.model.enums.ProjectStatus;

import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String title,
        String description,
        String departmentName,
        ProjectStatus status
) {
    public static ProjectResponse from (Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getDepartment().getName(),
                project.getStatus()
        );
    }
}
