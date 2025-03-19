package patika.defineX.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.IssueCommentRequest;
import patika.defineX.dto.response.IssueCommentResponse;
import patika.defineX.event.IssueDeletedEvent;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.BaseEntity;
import patika.defineX.model.Issue;
import patika.defineX.model.IssueComment;
import patika.defineX.model.User;
import patika.defineX.repository.IssueCommentRepository;

import java.util.List;
import java.util.UUID;

@Service
public class IssueCommentService {

    private static final Logger logger = LoggerFactory.getLogger(IssueCommentService.class);

    private final IssueCommentRepository issueCommentRepository;
    private final UserService userService;
    private final IssueService issueService;

    public IssueCommentService(IssueCommentRepository issueCommentRepository, UserService userService, IssueService issueService) {
        this.issueCommentRepository = issueCommentRepository;
        this.userService = userService;
        this.issueService = issueService;
    }

    public List<IssueCommentResponse> getIssueComments(UUID issueId) {
        logger.info("Fetching comments for issue with id: {}", issueId);
        issueService.findById(issueId); // Ensure issue exists
        List<IssueCommentResponse> comments = issueCommentRepository.findAllByIssueIdAndDeletedAtNull(issueId).stream()
                .map(IssueCommentResponse::from)
                .toList();
        logger.info("Found {} comments for issue with id: {}", comments.size(), issueId);
        return comments;
    }

    public IssueCommentResponse create(IssueCommentRequest issueCommentRequest) {
        logger.info("Creating comment for issue with id: {}", issueCommentRequest.issueId());
        Issue issue = issueService.findById(issueCommentRequest.issueId());
        User user = userService.findById(issueCommentRequest.userId());
        IssueComment issueComment = IssueComment.builder()
                .issue(issue)
                .user(user)
                .comment(issueCommentRequest.comment())
                .build();
        IssueComment savedComment = issueCommentRepository.save(issueComment);
        logger.info("Comment created with id: {}", savedComment.getId());
        return IssueCommentResponse.from(savedComment);
    }

    public IssueCommentResponse update(UUID issueCommentId, IssueCommentRequest issueCommentRequest) {
        logger.info("Updating comment with id: {}", issueCommentId);
        IssueComment issueComment = findById(issueCommentId);
        issueComment.setComment(issueCommentRequest.comment());
        IssueComment updatedComment = issueCommentRepository.save(issueComment);
        logger.info("Comment updated with id: {}", updatedComment.getId());
        return IssueCommentResponse.from(updatedComment);
    }

    public void delete(UUID id) {
        logger.info("Deleting comment with id: {}", id);
        IssueComment issueComment = findById(id);
        issueComment.softDelete();
        issueCommentRepository.save(issueComment);
        logger.info("Comment deleted with id: {}", id);
    }

    @EventListener
    @Transactional
    public void deleteIssueComments(IssueDeletedEvent event) {
        logger.info("Deleting comments for issue with id: {}", event.issueId());
        List<IssueComment> issueComments = issueCommentRepository.findAllByIssueIdAndDeletedAtNull(event.issueId());
        issueComments.forEach(BaseEntity::softDelete);
        issueCommentRepository.saveAll(issueComments);
        logger.info("Deleted {} comments for issue with id: {}", issueComments.size(), event.issueId());
    }

    private IssueComment findById(UUID id) {
        logger.debug("Finding comment by id: {}", id);
        return issueCommentRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> {
                    logger.error("Comment not found with id: {}", id);
                    return new CustomNotFoundException("Issue comment not found with id: " + id);
                });
    }
}