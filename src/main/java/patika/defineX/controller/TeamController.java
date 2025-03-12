package patika.defineX.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import patika.defineX.dto.request.TeamRequest;
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
    public ResponseEntity<TeamResponse> getById(@PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.getTeamById(teamId));
    }

    @PostMapping("/v1")
    public ResponseEntity<TeamResponse> save (@RequestBody TeamRequest teamRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.save(teamRequest));
    }

    @PostMapping("/v1/members/{teamId}")
    public ResponseEntity<TeamResponse> addMember(
            @PathVariable UUID teamId,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(teamService.addTeamMember(teamId, userId));
    }

    @DeleteMapping("/v1/members/{teamMemberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID teamMemberId) {
        teamService.removeTeamMember(teamMemberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
