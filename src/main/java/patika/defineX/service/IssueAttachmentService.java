package patika.defineX.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.IssueAttachmentRequest;
import patika.defineX.dto.response.IssueAttachmentResponse;
import patika.defineX.event.IssueDeletedEvent;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.Issue;
import patika.defineX.model.IssueAttachment;
import patika.defineX.repository.IssueAttachmentRepository;

import java.util.List;
import java.util.UUID;

@Service
public class IssueAttachmentService {

    private final IssueAttachmentRepository issueAttachmentRepository;
    private final IssueService issueService;

    public IssueAttachmentService(IssueAttachmentRepository issueAttachmentRepository, IssueService issueService) {
        this.issueAttachmentRepository = issueAttachmentRepository;
        this.issueService = issueService;
    }

    public List<IssueAttachmentResponse> findByIssueId(UUID issueId) {
        return findByIssueIdAndIsDeletedFalse(issueId).stream()
                .map(IssueAttachmentResponse::from)
                .toList();
    }

    public IssueAttachmentResponse save (IssueAttachmentRequest issueAttachmentRequest) {
        Issue issue = issueService.findById(issueAttachmentRequest.issueId());
        IssueAttachment issueAttachment = IssueAttachmentRequest.from(issueAttachmentRequest);
        issueAttachment.setIssue(issue);
        return IssueAttachmentResponse.from(issueAttachmentRepository.save(issueAttachment));
    }

    public void delete (UUID id) {
        IssueAttachment issueAttachment = findById(id);
        issueAttachment.setDeleted(true);
        issueAttachmentRepository.save(issueAttachment);
    }

    @EventListener
    @Transactional
    public void deleteIssueAttachments(IssueDeletedEvent event) {
        List<IssueAttachment> issueAttachments = findByIssueIdAndIsDeletedFalse(event.issueId());
        issueAttachments.forEach(issueAttachment -> issueAttachment.setDeleted(true));
        issueAttachmentRepository.saveAll(issueAttachments);
    }

    private List<IssueAttachment> findByIssueIdAndIsDeletedFalse(UUID issueId) {
        return issueAttachmentRepository.findByIssueIdAndIsDeletedFalse(issueId);
    }

    private IssueAttachment findById(UUID id) {
        return issueAttachmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomNotFoundException("Issue attachment not found with id: " + id));
    }
}
