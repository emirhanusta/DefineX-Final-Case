package patika.defineX.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import patika.defineX.dto.request.IssueAttachmentRequest;
import patika.defineX.dto.response.IssueAttachmentResponse;
import patika.defineX.service.IssueAttachmentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issue-attachment")
@Tag(name = "Issue Attachment", description = "Issue Attachment API")
public class IssueAttachmentController {

    private final IssueAttachmentService issueAttachmentService;

    public IssueAttachmentController(IssueAttachmentService issueAttachmentService) {
        this.issueAttachmentService = issueAttachmentService;
    }

    @Operation(
            method = "GET",
            summary = "Get issue attachment",
            description = "Get issue attachment by issue id"
    )
    @GetMapping("/v1/{issueId}")
    public ResponseEntity<List<IssueAttachmentResponse>> getIssueAttachment(@PathVariable UUID issueId) {
        return ResponseEntity.ok(issueAttachmentService.findByIssueId(issueId));
    }

    @Operation(
            method = "POST",
            summary = "Create issue attachment",
            description = "Create issue attachment"
    )
    @PostMapping("/v1")
    public ResponseEntity<IssueAttachmentResponse> createIssueAttachment(@Valid @RequestBody IssueAttachmentRequest issueAttachmentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueAttachmentService.save(issueAttachmentRequest));
    }

    @Operation(
            method = "DELETE",
            summary = "Delete issue attachment",
            description = "Delete issue attachment by id"
    )
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> deleteIssueAttachment(@PathVariable UUID id) {
        issueAttachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
