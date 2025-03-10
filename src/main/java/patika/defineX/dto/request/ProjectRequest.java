package patika.defineX.dto.request;

import patika.defineX.model.Project;
import patika.defineX.model.enums.ProjectStatus;

import java.util.UUID;

public record ProjectRequest(
        UUID departmentId,
        String title,
        String description,
        String status
) {
    public static Project from (ProjectRequest projectRequest) {
        return Project.builder()
                .title(projectRequest.title())
                .description(projectRequest.description())
                .status(ProjectStatus.valueOf(projectRequest.status()))
                .build();
    }
}
