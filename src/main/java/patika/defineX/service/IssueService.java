package patika.defineX.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.IssueRequest;
import patika.defineX.dto.request.IssueStatusChangeRequest;
import patika.defineX.dto.request.IssueUpdateRequest;
import patika.defineX.dto.response.IssueResponse;
import patika.defineX.event.HistoryCreatedEvent;
import patika.defineX.event.IssueDeletedEvent;
import patika.defineX.event.ProjectDeletedEvent;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.exception.custom.StatusChangeException;
import patika.defineX.model.Issue;
import patika.defineX.model.enums.IssueStatus;
import patika.defineX.repository.IssueRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IssueService {
    private static final Logger log = LoggerFactory.getLogger(IssueService.class);

    private final IssueRepository issueRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Cacheable(value = "issues")
    public List<IssueResponse> listAllByProjectId(UUID projectId) {
        log.info("Fetching all issues for project ID: {}", projectId);
        projectService.findById(projectId);
        List<IssueResponse> issueResponses = issueRepository.findAllByProjectIdAndDeletedAtNull(projectId).stream()
                .map(IssueResponse::from)
                .toList();
        log.info("Found {} issues for project ID: {}", issueResponses.size(), projectId);
        return issueResponses;
    }

    @Cacheable(value = "issues", key = "#id")
    public IssueResponse getById(UUID id) {
        log.info("Fetching issue with ID: {}", id);
        IssueResponse issueResponse = IssueResponse.from(findById(id));
        log.info("Fetched issue: {}", issueResponse);
        return issueResponse;
    }

    @CacheEvict(value = "issues", allEntries = true)
    public IssueResponse save(IssueRequest issueRequest) {
        log.info("Saving new issue with project ID: {} and reporter ID: {}", issueRequest.projectId(), issueRequest.reporterId());
        Issue issue = IssueRequest.from(issueRequest);
        issue.setProject(projectService.findById(issueRequest.projectId()));
        issue.setReporter(userService.findById(issueRequest.reporterId()));
        if (issueRequest.assigneeId() != null) {
            issue.setAssignee(userService.findById(issueRequest.assigneeId()));
        }
        IssueResponse savedIssue = IssueResponse.from(issueRepository.save(issue));
        log.info("Saved new issue with ID: {}", savedIssue.id());
        return savedIssue;
    }

    @CacheEvict(value = "issues", allEntries = true)
    public IssueResponse update(UUID id, IssueUpdateRequest issueUpdateRequest) {
        log.info("Updating issue with ID: {}", id);
        Issue issue = findById(id);

        if (issueUpdateRequest.assigneeId() == null) {
            issue.setAssignee(null);
            log.info("Removed assignee from issue with ID: {}", id);
        } else {
            issue.setAssignee(userService.findById(issueUpdateRequest.assigneeId()));
            log.info("Assigned user ID {} to issue with ID: {}", issueUpdateRequest.assigneeId(), id);
        }

        issue.setType(issueUpdateRequest.type());
        issue.setTitle(issueUpdateRequest.title());
        issue.setDescription(issueUpdateRequest.description());
        issue.setUserStory(issueUpdateRequest.userStory());
        issue.setAcceptanceCriteria(issueUpdateRequest.acceptanceCriteria());
        issue.setPriority(issueUpdateRequest.priority());
        issue.setDueDate(issueUpdateRequest.dueDate());

        IssueResponse updatedIssue = IssueResponse.from(issueRepository.save(issue));
        log.info("Updated issue with ID: {}", id);
        return updatedIssue;
    }

    @CacheEvict(value = "issues", allEntries = true)
    @Transactional
    public IssueResponse updateStatus(UUID id, IssueStatusChangeRequest request) {
        log.info("Changing status of issue with ID: {} to {}", id, request.status());
        Issue issue = findById(id);

        if (issue.getStatus() == request.status()) {
            log.info("Status of issue with ID: {} is already {}", id, request.status());
            return IssueResponse.from(issue);
        }

        if (!isStatusChangeValid(issue.getStatus(), request.status(), request.reason())) {
            throw new StatusChangeException("Invalid status change from " + issue.getStatus() + " to " + request.status());
        }

        applicationEventPublisher.publishEvent(new HistoryCreatedEvent(issue, request));
        issue.setStatus(request.status());
        IssueResponse updatedIssue = IssueResponse.from(issueRepository.save(issue));
        log.info("Successfully updated status of issue with ID: {} to {}", id, request.status());
        return updatedIssue;
    }

    @CacheEvict(value = "issues", allEntries = true)
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting issue with ID: {}", id);
        Issue issue = findById(id);
        issue.softDelete();
        issue.setStatus(IssueStatus.CANCELLED);
        issueRepository.save(issue);
        applicationEventPublisher.publishEvent(new IssueDeletedEvent(id));
        log.info("Successfully deleted issue with ID: {}", id);
    }

    @CacheEvict(value = "issues", allEntries = true)
    @EventListener
    @Transactional
    public void deleteAllByProjectId(ProjectDeletedEvent event) {
        log.info("Deleting all issues for project ID: {}", event.projectId());
        List<Issue> issues = issueRepository.findAllByProjectIdAndDeletedAtNull(event.projectId());
        issues.forEach(issue -> {
            issue.softDelete();
            issue.setStatus(IssueStatus.CANCELLED);
            applicationEventPublisher.publishEvent(new IssueDeletedEvent(issue.getId()));
        });
        issueRepository.saveAll(issues);
        log.info("Successfully deleted {} issues for project ID: {}", issues.size(), event.projectId());
    }

    protected Issue findById(UUID id) {
        log.debug("Finding issue with ID: {}", id);
        return issueRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> {
                    log.error("Issue not found with ID: {}", id);
                    return new CustomNotFoundException("Issue not found with id: " + id);
                });
    }

    private boolean isStatusChangeValid(IssueStatus currentStatus, IssueStatus newStatus, String reason) {
        log.debug("Validating status change from {} to {}", currentStatus, newStatus);

        if (currentStatus == IssueStatus.COMPLETED) {
            throw new StatusChangeException("Issue status cannot be changed because it is completed.");
        }

        if ((newStatus == IssueStatus.BLOCKED || newStatus == IssueStatus.CANCELLED)
                && reason == null) {
            throw new StatusChangeException("A reason must be provided when the status is " + newStatus);
        }

        boolean isValid = switch (currentStatus) {
            case BACKLOG -> newStatus == IssueStatus.IN_ANALYSIS;
            case IN_ANALYSIS -> newStatus == IssueStatus.BACKLOG ||
                    newStatus == IssueStatus.IN_PROGRESS ||
                    newStatus == IssueStatus.BLOCKED;
            case IN_PROGRESS -> newStatus == IssueStatus.IN_ANALYSIS ||
                    newStatus == IssueStatus.COMPLETED ||
                    newStatus == IssueStatus.BLOCKED;
            case BLOCKED -> newStatus == IssueStatus.IN_PROGRESS ||
                    newStatus == IssueStatus.CANCELLED;
            default -> false;
        };
        log.debug("Status change validity: {}", isValid);
        return isValid;
    }

}
