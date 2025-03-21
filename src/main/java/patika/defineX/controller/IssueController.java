package patika.defineX.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import patika.defineX.dto.request.IssueRequest;
import patika.defineX.dto.request.IssueStatusChangeRequest;
import patika.defineX.dto.request.IssueUpdateRequest;
import patika.defineX.dto.response.IssueResponse;
import patika.defineX.service.IssueService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issues")
@Tag(name = "Issue", description = "Issue API")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @Operation(
            method = "GET",
            summary = "List all issues",
            description = "List all issues in the system"
    )
    @GetMapping("/v1/list/{projectId}")
    public ResponseEntity<List<IssueResponse>> listAllByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(issueService.listAllByProjectId(projectId));
    }

    @Operation(
            method = "GET",
            summary = "Get issue",
            description = "Get issue by id"
    )
    @GetMapping("/v1/{id}")
    public ResponseEntity<IssueResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(issueService.getById(id));
    }

    @Operation(
            method = "POST",
            summary = "Create an issue",
            description = "Create an issue. Only project managers and team leaders can create an issue"
    )
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGER', 'TEAM_LEADER')")
    @PostMapping("/v1")
    public ResponseEntity<IssueResponse> save(@Valid @RequestBody IssueRequest issueRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.save(issueRequest));
    }

    @Operation(
            method = "PUT",
            summary = "Update an issue",
            description = "Update an issue by id with new values. Only project managers and team leaders can update an issue"
    )
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGER', 'TEAM_LEADER')")
    @PutMapping("/v1/{id}")
    public ResponseEntity<IssueResponse> update(@PathVariable UUID id,@Valid @RequestBody IssueUpdateRequest issueRequest) {
        return ResponseEntity.ok(issueService.update(id, issueRequest));
    }

    @Operation(
            method = "PATCH",
            summary = "Update issue status",
            description = "Update issue status by id."
    )
    @PatchMapping("/v1/status/{id}")
    public ResponseEntity<IssueResponse> updateStatus(@PathVariable UUID id,@Valid @RequestBody IssueStatusChangeRequest request) {
        return ResponseEntity.ok(issueService.updateStatus(id, request));
    }

    @Operation(
            method = "DELETE",
            summary = "Delete an issue",
            description = "Delete an issue by id. Only project managers can delete an issue"
    )
    @PreAuthorize("hasAuthority('PROJECT_MANAGER')")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        issueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
