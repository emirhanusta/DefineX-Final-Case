package patika.defineX.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import patika.defineX.dto.request.TeamRequest;
import patika.defineX.dto.response.TeamMemberListResponse;
import patika.defineX.dto.response.TeamResponse;
import patika.defineX.service.TeamService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/v1/list/{projectId}")
    public ResponseEntity<List<TeamResponse>> listAllByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(teamService.getAllTeamsByProjectId(projectId));
    }

    @GetMapping("/v1/{teamId}")
    public ResponseEntity<TeamMemberListResponse> getById(@PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.getTeamById(teamId));
    }

    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @PostMapping("/v1")
    public ResponseEntity<TeamResponse> save (@RequestBody TeamRequest teamRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.save(teamRequest));
    }

    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'TEAM_LEADER')")
    @PostMapping("/v1/members/{teamId}")
    public ResponseEntity<TeamMemberListResponse> addMember(
            @PathVariable UUID teamId,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(teamService.addTeamMember(teamId, userId));
    }

    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'TEAM_LEADER')")
    @DeleteMapping("/v1/members/{teamMemberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID teamMemberId) {
        teamService.removeTeamMember(teamMemberId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
