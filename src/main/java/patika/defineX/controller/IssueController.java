package patika.defineX.controller;

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
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping("/v1/list/{projectId}")
    public ResponseEntity<List<IssueResponse>> listAllByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(issueService.listAllByProjectId(projectId));
    }

    @GetMapping("/v1/{id}")
    public ResponseEntity<IssueResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(issueService.getById(id));
    }

    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'TEAM_LEADER')")
    @PostMapping("/v1")
    public ResponseEntity<IssueResponse> save(@Valid @RequestBody IssueRequest issueRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.save(issueRequest));
    }

    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'TEAM_LEADER')")
    @PutMapping("/v1/{id}")
    public ResponseEntity<IssueResponse> update(@PathVariable UUID id,@Valid @RequestBody IssueUpdateRequest issueRequest) {
        return ResponseEntity.ok(issueService.update(id, issueRequest));
    }

    @PatchMapping("/v1/status/{id}")
    public ResponseEntity<IssueResponse> updateStatus(@PathVariable UUID id,@Valid @RequestBody IssueStatusChangeRequest request) {
        return ResponseEntity.ok(issueService.updateStatus(id, request));
    }

    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        issueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
