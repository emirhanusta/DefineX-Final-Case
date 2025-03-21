package patika.defineX.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import patika.defineX.dto.request.ProjectRequest;
import patika.defineX.dto.response.ProjectResponse;
import patika.defineX.dto.response.TeamMemberResponse;
import patika.defineX.service.ProjectService;
import patika.defineX.service.TeamMemberService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Project", description = "Project API")
public class ProjectController {

    private final ProjectService projectService;
    private final TeamMemberService teamMemberService;

    public ProjectController(ProjectService projectService, TeamMemberService teamMemberService) {
        this.projectService = projectService;
        this.teamMemberService = teamMemberService;
    }

    @Operation(
            method = "GET",
            summary = "List all team members",
            description = "List all team members that are working on the project"
    )
    @GetMapping("/all-members/{id}")
    public ResponseEntity<List<TeamMemberResponse>> getMembersByProject(@PathVariable UUID id) {
        return ResponseEntity.ok(teamMemberService.getAllMembersByProjectId(id));
    }

    @Operation(
            method = "GET",
            summary = "List all projects",
            description = """
                    List all projects in the system with pagination
                    You can customize the results using query parameters:
                    - `page`: The page number (default: 0).
                    - `size`: The number of records per page (default: 20).
                    - `sort`: Sorting criteria in the format  Examples: `createdAt,desc` (default)
                    """
    )
    @GetMapping("/v1/list/{departmentId}")
    public ResponseEntity<Page<ProjectResponse>> listAllByDepartmentId(@PathVariable UUID departmentId, Pageable pageable) {
        return ResponseEntity.ok(projectService.listAllByDepartmentId(departmentId, pageable));
    }

    @Operation(
            method = "GET",
            summary = "Get project",
            description = "Get project by id"
    )
    @GetMapping("/v1/{id}")
    public ResponseEntity<ProjectResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @Operation(
            method = "POST",
            summary = "Create a project",
            description = "Create a project. Only project managers can create a project"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PostMapping("/v1")
    public ResponseEntity<ProjectResponse> save(@Valid @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.save(projectRequest));
    }

    @Operation(
            method = "PUT",
            summary = "Update a project",
            description = "Update a project by id with new values. Only project managers can update a project"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable UUID id,@Valid @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.ok(projectService.update(id, projectRequest));
    }

    @Operation(
            method = "PATCH",
            summary = "Update project status",
            description = "Update project status by id. Only project managers can update project status"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PatchMapping("/v1/{id}")
    public ResponseEntity<ProjectResponse> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        return ResponseEntity.ok(projectService.updateStatus(id, status));
    }

    @Operation(
            method = "DELETE",
            summary = "Delete a project",
            description = "Delete a project by id. Only project managers can delete a project"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
