package patika.defineX.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Issue Comment", description = "Issue Comment API")
public class IssueCommentController {

    private final IssueCommentService issueCommentService;

    public IssueCommentController(IssueCommentService issueCommentService) {
        this.issueCommentService = issueCommentService;
    }

    @Operation(
            method = "GET",
            summary = "List issue comments",
            description = "List issue comments by issue id"
    )
    @GetMapping("/v1/{issueId}")
    public ResponseEntity<List<IssueCommentResponse>> list(@PathVariable UUID issueId) {
        return ResponseEntity.ok(issueCommentService.getIssueComments(issueId));
    }

    @Operation(
            method = "POST",
            summary = "Create issue comment",
            description = "Create issue comment"
    )
    @PostMapping("/v1")
    public ResponseEntity<IssueCommentResponse> save(@Valid @RequestBody IssueCommentRequest issueCommentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueCommentService.create(issueCommentRequest));
    }

    @Operation(
            method = "PUT",
            summary = "Update issue comment",
            description = "Update issue comment by id"
    )
    @PutMapping("/v1/{id}")
    public ResponseEntity<IssueCommentResponse> update(@PathVariable UUID id,@Valid @RequestBody IssueCommentRequest issueCommentRequest) {
        return ResponseEntity.ok(issueCommentService.update(id, issueCommentRequest));
    }

    @Operation(
            method = "DELETE",
            summary = "Delete issue comment",
            description = "Delete issue comment by id"
    )
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        issueCommentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
