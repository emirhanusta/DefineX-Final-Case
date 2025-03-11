package patika.defineX.controller;

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
public class IssueHistoryController {

    private final IssueHistoryService issueHistoryService;

    public IssueHistoryController(IssueHistoryService issueHistoryService) {
        this.issueHistoryService = issueHistoryService;
    }

    @GetMapping("/v1/{issueId}")
    public ResponseEntity<List<IssueHistoryResponse>> list(@PathVariable UUID issueId) {
        return ResponseEntity.ok(issueHistoryService.listAllByIssueId(issueId));
    }
}
