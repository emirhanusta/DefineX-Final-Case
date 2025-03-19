package patika.defineX.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.IssueAttachmentRequest;
import patika.defineX.dto.response.IssueAttachmentResponse;
import patika.defineX.event.IssueDeletedEvent;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.BaseEntity;
import patika.defineX.model.Issue;
import patika.defineX.model.IssueAttachment;
import patika.defineX.repository.IssueAttachmentRepository;

import java.util.List;
import java.util.UUID;

@Service
public class IssueAttachmentService {

    private static final Logger logger = LoggerFactory.getLogger(IssueAttachmentService.class);

    private final IssueAttachmentRepository issueAttachmentRepository;
    private final IssueService issueService;

    public IssueAttachmentService(IssueAttachmentRepository issueAttachmentRepository, IssueService issueService) {
        this.issueAttachmentRepository = issueAttachmentRepository;
        this.issueService = issueService;
    }

    public List<IssueAttachmentResponse> findByIssueId(UUID issueId) {
        logger.info("Fetching attachments for issue with id: {}", issueId);
        List<IssueAttachmentResponse> attachments = findByIssueIdAndIsDeletedFalse(issueId).stream()
                .map(IssueAttachmentResponse::from)
                .toList();
        logger.info("Found {} attachments for issue with id: {}", attachments.size(), issueId);
        return attachments;
    }

    public IssueAttachmentResponse save(IssueAttachmentRequest issueAttachmentRequest) {
        logger.info("Saving attachment for issue with id: {}", issueAttachmentRequest.issueId());
        Issue issue = issueService.findById(issueAttachmentRequest.issueId());
        IssueAttachment issueAttachment = IssueAttachmentRequest.from(issueAttachmentRequest);
        issueAttachment.setIssue(issue);
        IssueAttachment savedAttachment = issueAttachmentRepository.save(issueAttachment);
        logger.info("Attachment saved with id: {}", savedAttachment.getId());
        return IssueAttachmentResponse.from(savedAttachment);
    }

    public void delete(UUID id) {
        logger.info("Deleting attachment with id: {}", id);
        IssueAttachment issueAttachment = findById(id);
        issueAttachment.softDelete();
        issueAttachmentRepository.save(issueAttachment);
        logger.info("Attachment deleted with id: {}", id);
    }

    @EventListener
    @Transactional
    public void deleteIssueAttachments(IssueDeletedEvent event) {
        logger.info("Deleting attachments for issue with id: {}", event.issueId());
        List<IssueAttachment> issueAttachments = findByIssueIdAndIsDeletedFalse(event.issueId());
        issueAttachments.forEach(BaseEntity::softDelete);
        issueAttachmentRepository.saveAll(issueAttachments);
        logger.info("Deleted {} attachments for issue with id: {}", issueAttachments.size(), event.issueId());
    }

    private List<IssueAttachment> findByIssueIdAndIsDeletedFalse(UUID issueId) {
        logger.debug("Finding attachments for issue with id: {} (not deleted)", issueId);
        return issueAttachmentRepository.findByIssueIdAndDeletedAtNull(issueId);
    }

    private IssueAttachment findById(UUID id) {
        logger.debug("Finding attachment by id: {}", id);
        return issueAttachmentRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> {
                    logger.error("Attachment not found with id: {}", id);
                    return new CustomNotFoundException("Issue attachment not found with id: " + id);
                });
    }
}