package patika.defineX.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.IssueCommentRequest;
import patika.defineX.dto.response.IssueCommentResponse;
import patika.defineX.event.IssueDeletedEvent;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.Issue;
import patika.defineX.model.IssueComment;
import patika.defineX.model.User;
import patika.defineX.repository.IssueCommentRepository;

import java.util.List;
import java.util.UUID;

@Service
public class IssueCommentService {

    private final IssueCommentRepository issueCommentRepository;
    private final UserService userService;
    private final IssueService issueService;

    public IssueCommentService(IssueCommentRepository issueCommentRepository, UserService userService, IssueService issueService) {
        this.issueCommentRepository = issueCommentRepository;
        this.userService = userService;
        this.issueService = issueService;
    }

    public List<IssueCommentResponse> getIssueComments(UUID issueId) {
        issueService.findById(issueId);
        return issueCommentRepository.findAllByIssueIdAndIsDeletedFalse(issueId).stream()
                .map(IssueCommentResponse::from)
                .toList();
    }

    public IssueCommentResponse create (IssueCommentRequest issueCommentRequest) {
        Issue issue = issueService.findById(issueCommentRequest.issueId());
        User user = userService.findById(issueCommentRequest.userId());
        IssueComment issueComment = IssueComment.builder()
                .issue(issue)
                .user(user)
                .comment(issueCommentRequest.comment())
                .build();
        return IssueCommentResponse.from(issueCommentRepository.save(issueComment));
    }

    public IssueCommentResponse update (UUID issueCommentId, IssueCommentRequest issueCommentRequest) {
        IssueComment issueComment = findById(issueCommentId);
        issueComment.setComment(issueCommentRequest.comment());
        return IssueCommentResponse.from(issueCommentRepository.save(issueComment));
    }

    public void delete (UUID id) {
        IssueComment issueComment = findById(id);
        issueComment.setDeleted(true);
        issueCommentRepository.save(issueComment);
    }

    @EventListener
    @Transactional
    public void deleteIssueComments(IssueDeletedEvent event) {
        List<IssueComment> issueComments = issueCommentRepository.findAllByIssueIdAndIsDeletedFalse(event.issueId());
        issueComments.forEach(issueComment -> issueComment.setDeleted(true));
        issueCommentRepository.saveAll(issueComments);
    }

    private IssueComment findById(UUID id) {
        return issueCommentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomNotFoundException("Issue comment not found with id: " + id));
    }
}