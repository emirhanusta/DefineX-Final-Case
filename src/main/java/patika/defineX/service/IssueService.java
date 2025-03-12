package patika.defineX.service;

import lombok.RequiredArgsConstructor;
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

    private final IssueRepository issueRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public List<IssueResponse> listAllByProjectId(UUID projectId) {
        projectService.findById(projectId);
        return issueRepository.findAllByProjectIdAndIsDeletedFalse(projectId).stream()
                .map(IssueResponse::from)
                .toList();
    }

    public IssueResponse getById(UUID id) {
        return IssueResponse.from(findById(id));
    }

    public IssueResponse save(IssueRequest issueRequest) {
        Issue issue = IssueRequest.from(issueRequest);
        issue.setProject(projectService.findById(issueRequest.projectId()));
        issue.setReporter(userService.findById(issueRequest.reporterId()));
        if (issueRequest.assigneeId() != null) {
            issue.setAssignee(userService.findById(issueRequest.assigneeId()));
        }
        return IssueResponse.from(issueRepository.save(issue));
    }

    public IssueResponse update(UUID id, IssueUpdateRequest issueUpdateRequest) {
        Issue issue = findById(id);

        if (issueUpdateRequest.assigneeId() != null) {
            issue.setAssignee(userService.findById(issueUpdateRequest.assigneeId()));
        }

        issue.setType(issueUpdateRequest.type());
        issue.setTitle(issueUpdateRequest.title());
        issue.setDescription(issueUpdateRequest.description());
        issue.setUserStory(issueUpdateRequest.userStory());
        issue.setAcceptanceCriteria(issueUpdateRequest.acceptanceCriteria());
        issue.setPriority(issueUpdateRequest.priority());
        issue.setDueDate(issueUpdateRequest.dueDate());

        return IssueResponse.from(issueRepository.save(issue));
    }

    @Transactional
    public IssueResponse updateStatus(UUID id, IssueStatusChangeRequest request) {
        Issue issue = findById(id);

        if (issue.getStatus() == request.status()) {
            return IssueResponse.from(issue);
        }

        if (issue.getStatus() == IssueStatus.COMPLETED) {
            throw new StatusChangeException("Issue status cannot be changed because it is completed.");
        }

        if ((request.status() == IssueStatus.BLOCKED || request.status() == IssueStatus.CANCELLED) && request.reason().isEmpty()) {
            throw new StatusChangeException("If the issue is blocked or cancelled, a reason must be provided.");
        }
        applicationEventPublisher.publishEvent(new HistoryCreatedEvent(issue, request));
        issue.setStatus(request.status());
        return IssueResponse.from(issueRepository.save(issue));
    }

    public IssueResponse unAssign(UUID id) {
        Issue issue = findById(id);
        issue.setAssignee(null);
        return IssueResponse.from(issueRepository.save(issue));
    }

    @Transactional
    public void delete(UUID id) {
        Issue issue = findById(id);
        issue.setDeleted(true);
        issue.setStatus(IssueStatus.CANCELLED);
        issueRepository.save(issue);
        applicationEventPublisher.publishEvent(new IssueDeletedEvent(id));
    }

    @EventListener
    @Transactional
    protected void deleteAllByProjectId(ProjectDeletedEvent event) {
        List<Issue> issues = issueRepository.findAllByProjectIdAndIsDeletedFalse(event.id());
        issues.forEach(issue -> {
            issue.setDeleted(true);
            applicationEventPublisher.publishEvent(new IssueDeletedEvent(issue.getId()));
        });
        issueRepository.saveAll(issues);
    }

    protected Issue findById(UUID id) {
        return issueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomNotFoundException("Issue not found with id: " + id));
    }

}
