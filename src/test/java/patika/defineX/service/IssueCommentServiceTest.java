package patika.defineX.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import patika.defineX.dto.request.IssueCommentRequest;
import patika.defineX.dto.response.IssueCommentResponse;
import patika.defineX.event.IssueDeletedEvent;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.Issue;
import patika.defineX.model.IssueComment;
import patika.defineX.model.User;
import patika.defineX.repository.IssueCommentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueCommentServiceTest {

    @Mock
    private IssueCommentRepository issueCommentRepository;

    @Mock
    private IssueService issueService;

    @Mock
    private UserService userService;

    @InjectMocks
    private IssueCommentService issueCommentService;

    private UUID issueId;
    private UUID commentId;
    private UUID userId;
    private Issue issue;
    private User user;
    private IssueComment issueComment;
    private IssueCommentRequest issueCommentRequest;

    @BeforeEach
    void setUp() {
        issueId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();

        issue = new Issue();
        issue.setId(issueId);

        user = new User();
        user.setId(userId);

        issueComment = IssueComment.builder()
                .issue(issue)
                .user(user)
                .comment("Initial comment")
                .build();
        issueComment.setId(commentId);

        issueCommentRequest = new IssueCommentRequest(issueId, userId, "Updated comment");
    }

    @Test
    void getIssueComments_ShouldReturnList() {
        when(issueService.findById(issueId)).thenReturn(issue);
        when(issueCommentRepository.findAllByIssueIdAndDeletedAtNull(issueId))
                .thenReturn(List.of(issueComment));

        List<IssueCommentResponse> result = issueCommentService.getIssueComments(issueId);

        assertEquals(1, result.size());
        assertEquals("Initial comment", result.getFirst().comment());
        verify(issueCommentRepository, times(1)).findAllByIssueIdAndDeletedAtNull(issueId);
    }

    @Test
    void create_ShouldSaveNewComment() {
        when(issueService.findById(issueId)).thenReturn(issue);
        when(userService.findById(userId)).thenReturn(user);
        when(issueCommentRepository.save(any(IssueComment.class))).thenReturn(issueComment);

        IssueCommentResponse result = issueCommentService.create(issueCommentRequest);

        assertNotNull(result);
        assertEquals("Initial comment", result.comment());
        verify(issueService, times(1)).findById(issueId);
        verify(userService, times(1)).findById(userId);
        verify(issueCommentRepository, times(1)).save(any(IssueComment.class));
    }

    @Test
    void update_ShouldUpdateComment() {
        when(issueCommentRepository.findByIdAndDeletedAtNull(commentId))
                .thenReturn(Optional.of(issueComment));
        when(issueCommentRepository.save(any(IssueComment.class))).thenReturn(issueComment);

        IssueCommentResponse result = issueCommentService.update(commentId, issueCommentRequest);

        assertNotNull(result);
        assertEquals("Updated comment", result.comment());
        verify(issueCommentRepository, times(1)).findByIdAndDeletedAtNull(commentId);
        verify(issueCommentRepository, times(1)).save(any(IssueComment.class));
    }

    @Test
    void delete_ShouldSoftDeleteComment() {
        when(issueCommentRepository.findByIdAndDeletedAtNull(commentId))
                .thenReturn(Optional.of(issueComment));

        issueCommentService.delete(commentId);

        assertNotNull(issueComment.getDeletedAt());
        verify(issueCommentRepository, times(1)).save(issueComment);
    }

    @Test
    void delete_WhenCommentNotFound_ShouldThrowException() {
        when(issueCommentRepository.findByIdAndDeletedAtNull(commentId))
                .thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> issueCommentService.delete(commentId));
        verify(issueCommentRepository, times(1)).findByIdAndDeletedAtNull(commentId);
    }

    @Test
    void deleteIssueComments_ShouldSoftDeleteAllComments() {
        IssueDeletedEvent event = new IssueDeletedEvent(issueId);
        when(issueCommentRepository.findAllByIssueIdAndDeletedAtNull(issueId))
                .thenReturn(List.of(issueComment));

        issueCommentService.deleteIssueComments(event);

        assertNotNull(issueComment.getDeletedAt());
        verify(issueCommentRepository, times(1)).saveAll(anyList());
    }
}
