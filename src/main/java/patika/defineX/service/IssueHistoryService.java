package patika.defineX.service;

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
        issueService.findById(issueId);
        return findAllByIssueIdAndIsDeletedFalse(issueId).stream()
                .map(IssueHistoryResponse::from)
                .toList();
    }

    @EventListener
    public void createHistory(HistoryCreatedEvent historyCreatedEvent) {
        User user = historyCreatedEvent.request().changedBy() != null ? userService.findById(historyCreatedEvent.request().changedBy()) : null;
        IssueHistory issueHistory = IssueHistory.builder()
                .issue(historyCreatedEvent.issue())
                .previousStatus(historyCreatedEvent.issue().getStatus())
                .newStatus(historyCreatedEvent.request().status())
                .changedBy(user)
                .reason(historyCreatedEvent.request().reason())
                .build();
        issueHistoryRepository.save(issueHistory);
    }

    @EventListener
    @Transactional
    public void deleteIssueHistories(IssueDeletedEvent event) {
        List<IssueHistory> issueHistories = findAllByIssueIdAndIsDeletedFalse(event.issueId());
        issueHistories.forEach(BaseEntity::softDelete);
        issueHistoryRepository.saveAll(issueHistories);
    }

    private List<IssueHistory> findAllByIssueIdAndIsDeletedFalse(UUID issueId) {
        return issueHistoryRepository.findAllByIssueIdAndDeletedAtNull(issueId);
    }

}
