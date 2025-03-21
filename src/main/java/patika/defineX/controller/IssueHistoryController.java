package patika.defineX.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import patika.defineX.dto.response.IssueHistoryResponse;
import patika.defineX.service.IssueHistoryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issue-histories")
@Tag(name = "Issue History", description = "Issue History API")
public class IssueHistoryController {

    private final IssueHistoryService issueHistoryService;

    public IssueHistoryController(IssueHistoryService issueHistoryService) {
        this.issueHistoryService = issueHistoryService;
    }

    @Operation(
            method = "GET",
            summary = "List issue histories",
            description = "List issue histories by issue id"
    )
    @GetMapping("/v1/{issueId}")
    public ResponseEntity<List<IssueHistoryResponse>> list(@PathVariable UUID issueId) {
        return ResponseEntity.ok(issueHistoryService.listAllByIssueId(issueId));
    }
}
