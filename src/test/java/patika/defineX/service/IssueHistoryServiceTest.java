package patika.defineX.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import patika.defineX.dto.request.IssueStatusChangeRequest;
import patika.defineX.dto.response.IssueHistoryResponse;
import patika.defineX.event.HistoryCreatedEvent;
import patika.defineX.event.IssueDeletedEvent;
import patika.defineX.model.Issue;
import patika.defineX.model.IssueHistory;
import patika.defineX.model.User;
import patika.defineX.model.enums.IssueStatus;
import patika.defineX.repository.IssueHistoryRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueHistoryServiceTest {

    @Mock
    private IssueHistoryRepository issueHistoryRepository;

    @Mock
    private UserService userService;

    @Mock
    private IssueService issueService;

    @InjectMocks
    private IssueHistoryService issueHistoryService;

    private UUID issueId;
    private UUID userId;
    private Issue issue;
    private User user;
    private IssueHistory issueHistory;

    @BeforeEach
    void setUp() {
        issueId = UUID.randomUUID();
        userId = UUID.randomUUID();
        issue = new Issue();
        issue.setId(issueId);

        user = new User();
        user.setId(userId);

        issueHistory = IssueHistory.builder()
                .issue(issue)
                .previousStatus(IssueStatus.IN_PROGRESS)
                .newStatus(IssueStatus.CANCELLED)
                .changedBy(user)
                .reason("Issue resolved")
                .build();
    }

    @Test
    void listAllByIssueId_ShouldReturnIssueHistories() {
        when(issueService.findById(issueId)).thenReturn(issue);
        when(issueHistoryRepository.findAllByIssueIdAndDeletedAtNull(issueId))
                .thenReturn(List.of(issueHistory));

        List<IssueHistoryResponse> result = issueHistoryService.listAllByIssueId(issueId);

        assertEquals(1, result.size());
        assertEquals(IssueStatus.CANCELLED, result.getFirst().newStatus());
        verify(issueHistoryRepository, times(1)).findAllByIssueIdAndDeletedAtNull(issueId);
    }

    @Test
    void createHistory_ShouldSaveHistory() {
        IssueStatusChangeRequest request = new IssueStatusChangeRequest(userId, IssueStatus.COMPLETED, "Issue resolved");
        HistoryCreatedEvent event = new HistoryCreatedEvent(issue, request);

        when(userService.findById(userId)).thenReturn(user);
        when(issueHistoryRepository.save(any(IssueHistory.class))).thenReturn(issueHistory);

        issueHistoryService.createHistory(event);

        verify(issueHistoryRepository, times(1)).save(any(IssueHistory.class));
    }

    @Test
    void deleteIssueHistories_ShouldSoftDeleteHistories() {
        IssueDeletedEvent event = new IssueDeletedEvent(issueId);
        when(issueHistoryRepository.findAllByIssueIdAndDeletedAtNull(issueId))
                .thenReturn(List.of(issueHistory));

        issueHistoryService.deleteIssueHistories(event);

        assertNotNull(issueHistory.getDeletedAt());
        verify(issueHistoryRepository, times(1)).saveAll(anyList());
    }
}
