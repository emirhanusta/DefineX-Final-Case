package patika.defineX.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/v1")
    public ResponseEntity<IssueResponse> save(@RequestBody IssueRequest issueRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.save(issueRequest));
    }

    @PutMapping("/v1/{id}")
    public ResponseEntity<IssueResponse> update(@PathVariable UUID id, @RequestBody IssueUpdateRequest issueRequest) {
        return ResponseEntity.ok(issueService.update(id, issueRequest));
    }

    @PatchMapping("/v1/status/{id}")
    public ResponseEntity<IssueResponse> updateStatus(@PathVariable UUID id, @RequestBody IssueStatusChangeRequest request) {
        return ResponseEntity.ok(issueService.updateStatus(id, request));
    }

    @PatchMapping("/v1/assignee/{id}")
    public ResponseEntity<IssueResponse> unAssign(@PathVariable UUID id) {
        return ResponseEntity.ok(issueService.unAssign(id));
    }

    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        issueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
