package patika.defineX.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import patika.defineX.dto.request.IssueRequest;
import patika.defineX.dto.request.IssueStatusChangeRequest;
import patika.defineX.dto.request.IssueUpdateRequest;
import patika.defineX.dto.response.IssueResponse;
import patika.defineX.event.IssueDeletedEvent;
import patika.defineX.event.ProjectDeletedEvent;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.exception.custom.StatusChangeException;
import patika.defineX.model.Issue;
import patika.defineX.model.Project;
import patika.defineX.model.User;
import patika.defineX.model.enums.IssueStatus;
import patika.defineX.model.enums.IssueType;
import patika.defineX.model.enums.PriorityLevel;
import patika.defineX.repository.IssueRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private IssueService issueService;

    private UUID projectId;
    private UUID issueId;
    private UUID assigneeId;
    private Issue issue;
    private IssueRequest issueRequest;
    private Project project;
    private User user;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        issueId = UUID.randomUUID();
        UUID reporterId = UUID.randomUUID();
        assigneeId = UUID.randomUUID();

        user = User.builder()
                .name("Test User")
                .email("test@mail.com")
                .password("password")
                .build();
        user.setId(reporterId);

        project = Project.builder()
                .title("Test Project")
                .description("Test Description")
                .build();
        project.setId(projectId);

        issue = Issue.builder()
                .title("Test Issue")
                .description("Test Description")
                .project(project)
                .reporter(user)
                .status(IssueStatus.BACKLOG)
                .type(IssueType.BUG)
                .priority(PriorityLevel.HIGH)
                .dueDate(LocalDateTime.now().plusDays(5))
                .build();
        issue.setId(issueId);

        issueRequest = IssueRequest.builder()
                .projectId(projectId)
                .reporterId(reporterId)
                .assigneeId(assigneeId)
                .title("New Issue")
                .description("New Description")
                .type(IssueType.TASK)
                .priority(PriorityLevel.MEDIUM)
                .dueDate(LocalDateTime.now().plusDays(10))
                .build();
    }

    @Test
    void listAllByProjectId_ShouldReturnIssueList() {
        when(issueRepository.findAllByProjectIdAndDeletedAtNull(projectId))
                .thenReturn(List.of(issue));

        List<IssueResponse> response = issueService.listAllByProjectId(projectId);

        assertEquals(1, response.size());
        assertEquals(issue.getTitle(), response.getFirst().title());
        verify(issueRepository, times(1)).findAllByProjectIdAndDeletedAtNull(projectId);
    }

    @Test
    void getById_WhenExists_ShouldReturnIssue() {
        when(issueRepository.findByIdAndDeletedAtNull(issueId))
                .thenReturn(Optional.of(issue));

        IssueResponse response = issueService.getById(issueId);

        assertEquals(issue.getTitle(), response.title());
        verify(issueRepository, times(1)).findByIdAndDeletedAtNull(issueId);
    }

    @Test
    void getById_WhenIssueNotFound_ShouldThrowException() {
        when(issueRepository.findByIdAndDeletedAtNull(issueId)).thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> issueService.getById(issueId));
    }

    @Test
    void save_ShouldCreateNewIssue() {
        Issue newIssue = IssueRequest.from(issueRequest);
        newIssue.setReporter(user);
        newIssue.setProject(project);
        when(issueRepository.save(any(Issue.class))).thenReturn(newIssue);

        IssueResponse response = issueService.save(issueRequest);

        assertEquals(issueRequest.title(), response.title());
        verify(issueRepository, times(1)).save(any(Issue.class));
    }

    @Test
    void update_ShouldModifyExistingIssue() {
        IssueUpdateRequest updateRequest = new IssueUpdateRequest(
                assigneeId, "Updated Title", "Updated Description", IssueType.TASK,
                PriorityLevel.LOW, "Updated Story", "Updated Criteria", LocalDateTime.now().plusDays(7)
        );

        when(issueRepository.findByIdAndDeletedAtNull(issueId)).thenReturn(Optional.of(issue));
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);

        IssueResponse response = issueService.update(issueId, updateRequest);

        assertEquals(updateRequest.title(), response.title());
        verify(issueRepository, times(1)).save(any(Issue.class));
    }

    @Test
    void update_WhenNull_ShouldRemoveAssignee() {
        IssueUpdateRequest updateRequest = new IssueUpdateRequest(
                null, "Updated Title", "Updated Description", IssueType.TASK,
                PriorityLevel.LOW, "Updated Story", "Updated Criteria", LocalDateTime.now().plusDays(7)
        );

        when(issueRepository.findByIdAndDeletedAtNull(issueId)).thenReturn(Optional.of(issue));
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);

        IssueResponse response = issueService.update(issueId, updateRequest);

        assertNull(response.assigneeId());
        verify(issueRepository, times(1)).save(any(Issue.class));
    }

    @Test
    void updateStatus_WhenValid_ShouldChangeStatus() {
        IssueStatusChangeRequest statusChangeRequest = new IssueStatusChangeRequest(
                IssueStatus.IN_ANALYSIS, null
        );

        when(issueRepository.findByIdAndDeletedAtNull(issueId)).thenReturn(Optional.of(issue));
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);

        IssueResponse response = issueService.updateStatus(issueId, statusChangeRequest);

        assertEquals(IssueStatus.IN_ANALYSIS, response.status());
        verify(issueRepository, times(1)).save(any(Issue.class));
    }

    @Test
    void updateStatus_WhenSameStatus_ShouldNotChangeStatus() {
        IssueStatusChangeRequest statusChangeRequest = new IssueStatusChangeRequest(
                IssueStatus.BACKLOG, null
        );

        when(issueRepository.findByIdAndDeletedAtNull(issueId)).thenReturn(Optional.of(issue));

        IssueResponse response = issueService.updateStatus(issueId, statusChangeRequest);

        assertEquals(IssueStatus.BACKLOG, response.status());
        verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    void updateStatus_WhenInvalidChange_ShouldThrowException() {
        IssueStatusChangeRequest statusChangeRequest = new IssueStatusChangeRequest(
                IssueStatus.BLOCKED, "reason"
        );

        when(issueRepository.findByIdAndDeletedAtNull(issueId)).thenReturn(Optional.of(issue));

        assertThrows(StatusChangeException.class, () -> issueService.updateStatus(issueId, statusChangeRequest));
    }

    @Test
    void updateStatus_IsIssueCompleted_ShouldThrowException() {
        IssueStatusChangeRequest statusChangeRequest = new IssueStatusChangeRequest(
                IssueStatus.IN_PROGRESS, null
        );
        issue.setStatus(IssueStatus.COMPLETED);

        when(issueRepository.findByIdAndDeletedAtNull(issueId)).thenReturn(Optional.of(issue));

        assertThrows(StatusChangeException.class, () -> issueService.updateStatus(issueId, statusChangeRequest));
    }

    @Test
    void updateStatus_WhenNoReasonProvided_ShouldThrowException() {
        IssueStatusChangeRequest statusChangeRequest = new IssueStatusChangeRequest(
                IssueStatus.CANCELLED, null
        );

        when(issueRepository.findByIdAndDeletedAtNull(issueId)).thenReturn(Optional.of(issue));

        assertThrows(StatusChangeException.class, () -> issueService.updateStatus(issueId, statusChangeRequest));
    }

    @Test
    void updateStatus_WhenIssueChangeIsInvalid_ShouldThrowException() {
        IssueStatusChangeRequest statusChangeRequest = new IssueStatusChangeRequest(
                IssueStatus.COMPLETED, null
        );

        when(issueRepository.findByIdAndDeletedAtNull(issueId)).thenReturn(Optional.of(issue));

        assertThrows(StatusChangeException.class, () -> issueService.updateStatus(issueId, statusChangeRequest));
    }

    @Test
    void delete_ShouldSoftDeleteIssue() {
        when(issueRepository.findByIdAndDeletedAtNull(issueId)).thenReturn(Optional.of(issue));

        issueService.delete(issueId);

        assertEquals(IssueStatus.CANCELLED, issue.getStatus());
        verify(issueRepository, times(1)).save(any(Issue.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(IssueDeletedEvent.class));
    }

    @Test
    void delete_WhenIssueNotFound_ShouldThrowException() {
        when(issueRepository.findByIdAndDeletedAtNull(issueId)).thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> issueService.delete(issueId));
    }

    @Test
    void deleteAllByProjectId_ShouldSoftDeleteAllIssues() {
        ProjectDeletedEvent event = new ProjectDeletedEvent(projectId);
        when(issueRepository.findAllByProjectIdAndDeletedAtNull(projectId)).thenReturn(List.of(issue));

        issueService.deleteAllByProjectId(event);

        assertEquals(IssueStatus.CANCELLED, issue.getStatus());
        verify(issueRepository, times(1)).saveAll(anyList());
        verify(applicationEventPublisher, times(1)).publishEvent(any(IssueDeletedEvent.class));
    }
}
