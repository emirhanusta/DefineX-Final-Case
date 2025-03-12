package patika.defineX.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import patika.defineX.dto.request.IssueCommentRequest;
import patika.defineX.dto.response.IssueCommentResponse;
import patika.defineX.service.IssueCommentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issue-comments")
public class IssueCommentController {

    private final IssueCommentService issueCommentService;

    public IssueCommentController(IssueCommentService issueCommentService) {
        this.issueCommentService = issueCommentService;
    }

    @GetMapping("/v1/{issueId}")
    public ResponseEntity<List<IssueCommentResponse>> list(@PathVariable UUID issueId) {
        return ResponseEntity.ok(issueCommentService.getIssueComments(issueId));
    }

    @PostMapping("/v1")
    public ResponseEntity<IssueCommentResponse> save(@Valid @RequestBody IssueCommentRequest issueCommentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueCommentService.create(issueCommentRequest));
    }

    @PutMapping("/v1/{id}")
    public ResponseEntity<IssueCommentResponse> update(@PathVariable UUID id,@Valid @RequestBody IssueCommentRequest issueCommentRequest) {
        return ResponseEntity.ok(issueCommentService.update(id, issueCommentRequest));
    }

    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        issueCommentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
