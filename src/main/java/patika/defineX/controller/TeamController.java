package patika.defineX.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Team", description = "Team API")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @Operation(
            method = "GET",
            summary = "List all teams",
            description = "List all teams in the system"
    )
    @GetMapping("/v1/list/{projectId}")
    public ResponseEntity<List<TeamResponse>> listAllByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(teamService.getAllTeamsByProjectId(projectId));
    }

    @Operation(
            method = "GET",
            summary = "Get team",
            description = "Get team by id"
    )
    @GetMapping("/v1/{teamId}")
    public ResponseEntity<TeamMemberListResponse> getById(@PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.getTeamById(teamId));
    }

    @Operation(
            method = "POST",
            summary = "Create a team",
            description = "Create a team. Only project managers can create a team"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @PostMapping("/v1")
    public ResponseEntity<TeamResponse> save (@RequestBody TeamRequest teamRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.save(teamRequest));
    }

    @Operation(
            method = "POST",
            summary = "Add member to team",
            description = "Add member to team. Only project managers and team leaders can add a member to a team"
    )
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGER', 'TEAM_LEADER')")
    @PostMapping("/v1/members/{teamId}")
    public ResponseEntity<TeamMemberListResponse> addMember(
            @PathVariable UUID teamId,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(teamService.addTeamMember(teamId, userId));
    }

    @Operation(
            method = "DELETE",
            summary = "Remove member from team",
            description = "Remove member from team. Only project managers and team leaders can remove a member from a team"
    )
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGER', 'TEAM_LEADER')")
    @DeleteMapping("/v1/members/{teamMemberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID teamMemberId) {
        teamService.removeTeamMember(teamMemberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            method = "DELETE",
            summary = "Delete a team",
            description = "Delete a team by id. Only project managers can delete a team"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
