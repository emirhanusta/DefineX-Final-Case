package patika.defineX.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import patika.defineX.model.Project;
import patika.defineX.model.enums.ProjectStatus;

import java.util.UUID;

public record ProjectRequest(
        @NotNull
        UUID departmentId,
        @NotBlank
        String title,
        String description,
        @NotNull
        ProjectStatus status
) {
    public static Project from (ProjectRequest projectRequest) {
        return Project.builder()
                .title(projectRequest.title().toUpperCase())
                .description(projectRequest.description())
                .status(projectRequest.status())
                .build();
    }
}
