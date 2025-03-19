package patika.defineX.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import patika.defineX.dto.request.IssueAttachmentRequest;
import patika.defineX.dto.response.IssueAttachmentResponse;
import patika.defineX.event.IssueDeletedEvent;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.Issue;
import patika.defineX.model.IssueAttachment;
import patika.defineX.repository.IssueAttachmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueAttachmentServiceTest {

    @Mock
    private IssueAttachmentRepository issueAttachmentRepository;

    @Mock
    private IssueService issueService;

    @InjectMocks
    private IssueAttachmentService issueAttachmentService;

    private UUID issueId;
    private UUID attachmentId;
    private Issue issue;
    private IssueAttachment issueAttachment;
    private IssueAttachmentRequest issueAttachmentRequest;

    @BeforeEach
    void setUp() {
        issueId = UUID.randomUUID();
        attachmentId = UUID.randomUUID();

        issue = new Issue();
        issue.setId(issueId);

        issueAttachment = new IssueAttachment();
        issueAttachment = IssueAttachment.builder()
                .issue(issue)
                .fileName("Attachment1")
                .filePath("FilePath1")
                .build();
        issueAttachment.setId(attachmentId);

        issueAttachmentRequest = new IssueAttachmentRequest(issueId, "Attachment1", "FilePath1");
    }

    @Test
    void findByIssueId_ShouldReturnAttachments() {
        when(issueAttachmentRepository.findByIssueIdAndDeletedAtNull(issueId))
                .thenReturn(List.of(issueAttachment));

        List<IssueAttachmentResponse> result = issueAttachmentService.findByIssueId(issueId);

        assertEquals(1, result.size());
        assertEquals("Attachment1", result.getFirst().fileName());
        verify(issueAttachmentRepository, times(1)).findByIssueIdAndDeletedAtNull(issueId);
    }

    @Test
    void save_ShouldSaveAttachment() {
        when(issueService.findById(issueId)).thenReturn(issue);
        when(issueAttachmentRepository.save(any(IssueAttachment.class))).thenReturn(issueAttachment);

        IssueAttachmentResponse result = issueAttachmentService.save(issueAttachmentRequest);

        assertNotNull(result);
        assertEquals("Attachment1", result.fileName());
        verify(issueService, times(1)).findById(issueId);
        verify(issueAttachmentRepository, times(1)).save(any(IssueAttachment.class));
    }

    @Test
    void delete_ShouldSoftDeleteAttachment() {
        when(issueAttachmentRepository.findByIdAndDeletedAtNull(attachmentId))
                .thenReturn(Optional.of(issueAttachment));

        issueAttachmentService.delete(attachmentId);

        assertNotNull(issueAttachment.getDeletedAt());
        verify(issueAttachmentRepository, times(1)).findByIdAndDeletedAtNull(attachmentId);
        verify(issueAttachmentRepository, times(1)).save(issueAttachment);
    }

    @Test
    void delete_WhenAttachmentNotFound_ShouldThrowException() {
        when(issueAttachmentRepository.findByIdAndDeletedAtNull(attachmentId))
                .thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> issueAttachmentService.delete(attachmentId));
        verify(issueAttachmentRepository, times(1)).findByIdAndDeletedAtNull(attachmentId);
    }

    @Test
    void deleteIssueAttachments_ShouldSoftDeleteAllAttachments() {
        IssueDeletedEvent event = new IssueDeletedEvent(issueId);
        when(issueAttachmentRepository.findByIssueIdAndDeletedAtNull(issueId))
                .thenReturn(List.of(issueAttachment));

        issueAttachmentService.deleteIssueAttachments(event);

        assertNotNull(issueAttachment.getDeletedAt());
        verify(issueAttachmentRepository, times(1)).findByIssueIdAndDeletedAtNull(issueId);
        verify(issueAttachmentRepository, times(1)).saveAll(anyList());
    }
}
