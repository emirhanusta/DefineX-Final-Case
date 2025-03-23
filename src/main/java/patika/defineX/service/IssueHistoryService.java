package patika.defineX.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.response.IssueHistoryResponse;
import patika.defineX.event.HistoryCreatedEvent;
import patika.defineX.event.IssueDeletedEvent;
import patika.defineX.model.BaseEntity;
import patika.defineX.model.IssueHistory;
import patika.defineX.model.User;
import patika.defineX.repository.IssueHistoryRepository;

import java.util.List;
import java.util.UUID;

@Service
public class IssueHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(IssueHistoryService.class);

    private final IssueHistoryRepository issueHistoryRepository;
    private final UserService userService;
    private final IssueService issueService;

    public IssueHistoryService(IssueHistoryRepository issueHistoryRepository, UserService userService,
                               IssueService issueService) {
        this.issueHistoryRepository = issueHistoryRepository;
        this.userService = userService;
        this.issueService = issueService;
    }

    public List<IssueHistoryResponse> listAllByIssueId(UUID issueId) {
        logger.info("Fetching history records for issue with id: {}", issueId);
        issueService.findById(issueId); // Ensure issue exists
        List<IssueHistoryResponse> histories = findAllByIssueIdAndIsDeletedFalse(issueId).stream()
                .map(IssueHistoryResponse::from)
                .toList();
        logger.info("Found {} history records for issue with id: {}", histories.size(), issueId);
        return histories;
    }

    @EventListener
    public void createHistory(HistoryCreatedEvent historyCreatedEvent) {
        logger.info("Creating history record for issue with id: {}", historyCreatedEvent.issue().getId());
        User user = userService.getAuthenticatedUser();
        IssueHistory issueHistory = IssueHistory.builder()
                .issue(historyCreatedEvent.issue())
                .previousStatus(historyCreatedEvent.issue().getStatus())
                .newStatus(historyCreatedEvent.request().status())
                .changedBy(user)
                .reason(historyCreatedEvent.request().reason())
                .build();
        IssueHistory savedHistory = issueHistoryRepository.save(issueHistory);
        logger.info("History record created with id: {}", savedHistory.getId());
    }

    @EventListener
    @Transactional
    public void deleteIssueHistories(IssueDeletedEvent event) {
        logger.info("Deleting history records for issue with id: {}", event.issueId());
        List<IssueHistory> issueHistories = findAllByIssueIdAndIsDeletedFalse(event.issueId());
        issueHistories.forEach(BaseEntity::softDelete);
        issueHistoryRepository.saveAll(issueHistories);
        logger.info("Deleted {} history records for issue with id: {}", issueHistories.size(), event.issueId());
    }

    private List<IssueHistory> findAllByIssueIdAndIsDeletedFalse(UUID issueId) {
        logger.debug("Finding history records for issue with id: {} (not deleted)", issueId);
        return issueHistoryRepository.findAllByIssueIdAndDeletedAtNull(issueId);
    }
}