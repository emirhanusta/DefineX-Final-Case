package patika.defineX.controller;

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
public class IssueAttachmentController {

    private final IssueAttachmentService issueAttachmentService;

    public IssueAttachmentController(IssueAttachmentService issueAttachmentService) {
        this.issueAttachmentService = issueAttachmentService;
    }

    @GetMapping("/v1/{issueId}")
    public ResponseEntity<List<IssueAttachmentResponse>> getIssueAttachment(@PathVariable UUID issueId) {
        return ResponseEntity.ok(issueAttachmentService.findByIssueId(issueId));
    }

    @PostMapping("/v1")
    public ResponseEntity<IssueAttachmentResponse> createIssueAttachment(@RequestBody IssueAttachmentRequest issueAttachmentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueAttachmentService.save(issueAttachmentRequest));
    }

    @DeleteMapping("/v1/{id}")
    public ResponseEntity<Void> deleteIssueAttachment(@PathVariable UUID id) {
        issueAttachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
