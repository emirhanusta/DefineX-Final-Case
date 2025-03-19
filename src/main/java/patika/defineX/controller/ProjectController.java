package patika.defineX.controller;

import jakarta.validation.Valid;
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
public class ProjectController {

    private final ProjectService projectService;
    private final TeamMemberService teamMemberService;

    public ProjectController(ProjectService projectService, TeamMemberService teamMemberService) {
        this.projectService = projectService;
        this.teamMemberService = teamMemberService;
    }

    @GetMapping("/all-members/{id}")
    public ResponseEntity<List<TeamMemberResponse>> getMembersByProject(@PathVariable UUID id) {
        return ResponseEntity.ok(teamMemberService.getAllMembersByProjectId(id));
    }

    @GetMapping("/v1/list/{departmentId}")
    public ResponseEntity<List<ProjectResponse>> listAllByDepartmentId(@PathVariable UUID departmentId) {
        return ResponseEntity.ok(projectService.listAllByDepartmentId(departmentId));
    }

    @GetMapping("/v1/{id}")
    public ResponseEntity<ProjectResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PostMapping("/v1")
    public ResponseEntity<ProjectResponse> save(@Valid @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.save(projectRequest));
    }

    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable UUID id,@Valid @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.ok(projectService.update(id, projectRequest));
    }

    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PatchMapping("/v1/{id}")
    public ResponseEntity<ProjectResponse> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        return ResponseEntity.ok(projectService.updateStatus(id, status));
    }

    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
