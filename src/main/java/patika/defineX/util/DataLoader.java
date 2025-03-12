package patika.defineX.util;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import patika.defineX.model.*;
import patika.defineX.model.enums.*;
import patika.defineX.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader {

    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final IssueHistoryRepository issueHistoryRepository;
    private final IssueCommentRepository issueCommentRepository;
    private final IssueAttachmentRepository issueAttachmentRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        //user
        User teamLead = User.builder().name("teamLead").email("teamLead@mail.com").password("pass").role(Role.TEAM_LEADER).build();
        User teamMember = User.builder().name("teamMember").email("teamMember@mail.com").password("pass").role(Role.TEAM_MEMBER).build();
        userRepository.saveAll(List.of(teamLead,teamMember));

        //department
        Department itDepartment = Department.builder().name("IT").build();
        Department hrDepartment = Department.builder().name("HR").build();
        departmentRepository.saveAll(List.of(itDepartment,hrDepartment));

        //project
        Project projectA = Project.builder().department(itDepartment).title("A").description("Description A").status(ProjectStatus.IN_PROGRESS).build();
        Project projectB = Project.builder().department(hrDepartment).title("B").description("Description B").status(ProjectStatus.IN_PROGRESS).build();
        projectRepository.saveAll(List.of(projectA,projectB));

        //issue
        Issue task = Issue.builder().project(projectA).assignee(teamMember).reporter(teamLead).type(IssueType.TASK)
                .title("Task").description("Description").status(IssueStatus.IN_PROGRESS)
                .userStory("User Story").acceptanceCriteria("Acceptance Criteria")
                .priority(PriorityLevel.HIGH).dueDate(LocalDateTime.now().plusDays(20)).build();
        Issue bug = Issue.builder().project(projectB).assignee(teamMember).reporter(teamLead).type(IssueType.BUG)
                .title("Bug").description("Description").status(IssueStatus.IN_PROGRESS)
                .userStory("User Story").acceptanceCriteria("Acceptance Criteria")
                .priority(PriorityLevel.HIGH).dueDate(LocalDateTime.now().plusDays(20)).build();
        issueRepository.saveAll(List.of(task,bug));

        //history
        IssueHistory issueHistory = IssueHistory.builder().issue(task).previousStatus(IssueStatus.IN_PROGRESS).newStatus(IssueStatus.COMPLETED)
                .changedBy(teamLead).reason("Reason").build();
        IssueHistory issueHistory2 = IssueHistory.builder().issue(bug).previousStatus(IssueStatus.IN_ANALYSIS).newStatus(IssueStatus.IN_PROGRESS)
                .changedBy(teamMember).reason("Reason").build();
        issueHistoryRepository.saveAll(List.of(issueHistory, issueHistory2));

        //comment
        IssueComment issueComment = IssueComment.builder().issue(task).user(teamLead).comment("Comment").build();
        IssueComment issueComment2 = IssueComment.builder().issue(bug).user(teamMember).comment("Comment2").build();
        issueCommentRepository.saveAll(List.of(issueComment, issueComment2));

        //attachment
        IssueAttachment issueAttachment = IssueAttachment.builder().issue(task).fileName("Attachment").filePath("url").build();
        IssueAttachment issueAttachment2 = IssueAttachment.builder().issue(bug).fileName("Attachment2").filePath("url2").build();
        issueAttachmentRepository.saveAll(List.of(issueAttachment, issueAttachment2));

        //team
        Team team = Team.builder().name("Team").project(projectA).build();
        teamRepository.save(team);


        //teamMember
        TeamMember teamMember1 = TeamMember.builder().team(team).user(teamLead).build();
        TeamMember teamMember2 = TeamMember.builder().team(team).user(teamMember).build();
        teamMemberRepository.saveAll(List.of(teamMember1,teamMember2));

    }
}