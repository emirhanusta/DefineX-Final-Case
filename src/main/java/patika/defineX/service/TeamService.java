package patika.defineX.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.TeamRequest;
import patika.defineX.dto.response.TeamResponse;
import patika.defineX.event.ProjectDeletedEvent;
import patika.defineX.event.TeamDeletedEvent;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.Project;
import patika.defineX.model.Team;
import patika.defineX.model.TeamMember;
import patika.defineX.model.User;
import patika.defineX.repository.TeamRepository;

import java.util.List;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final TeamMemberService teamMemberService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TeamService(TeamRepository teamRepository, ProjectService projectService, UserService userService,
                       TeamMemberService teamMemberService, ApplicationEventPublisher applicationEventPublisher) {
        this.teamRepository = teamRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.teamMemberService = teamMemberService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public List<TeamResponse> getAllTeamsByProjectId(UUID projectId) {
        projectService.findById(projectId);
        return teamRepository.findAllByProjectIdAndIsDeletedFalse(projectId)
                .stream()
                .map(TeamResponse::from)
                .toList();
    }

    public TeamResponse getTeamById(UUID teamId) {
        return TeamResponse.from(findById(teamId));
    }

    public TeamResponse save (TeamRequest teamRequest) {
        Project project = projectService.findById(teamRequest.projectId());
        Team team = Team.builder()
                .name(teamRequest.name())
                .project(project)
                .build();
        return TeamResponse.from(teamRepository.save(team));
    }

    public TeamResponse update(UUID id, TeamRequest teamRequest) {
        Team team = findById(id);
        if (!team.getProject().getId().equals(teamRequest.projectId())) {
            Project project = projectService.findById(teamRequest.projectId());
            team.setProject(project);
        }
        team.setName(teamRequest.name());
        return TeamResponse.from(teamRepository.save(team));
    }

    public void delete(UUID id) {
        Team team = findById(id);
        team.setDeleted(true);
        //team.getMembers().clear();
        teamRepository.save(team);
        //teamMemberService.deleteAllByTeamId(id);
        applicationEventPublisher.publishEvent(new TeamDeletedEvent(id));
    }

    @Transactional
    public TeamResponse addTeamMember(UUID teamId, UUID userId) {
        Team team = findById(teamId);
        User user = userService.findById(userId);
        team.getMembers().add(teamMemberService.createTeamMember(team, user));
        return TeamResponse.from(teamRepository.save(team));
    }

    @Transactional
    public void removeTeamMember(UUID teamMemberId) {
        TeamMember teamMember = teamMemberService.findById(teamMemberId);
        Team team = teamMember.getTeam();
        team.getMembers().remove(teamMember);
        teamRepository.save(team);
        teamMemberService.removeTeamMember(teamMemberId);
    }

    @EventListener
    @Transactional
    public void deleteTeamsByProjectId(ProjectDeletedEvent event) {
        List<Team> teams = teamRepository.findAllByProjectIdAndIsDeletedFalse(event.id());
        teams.forEach(team -> {
            team.setDeleted(true);
            //team.getMembers().clear();

            //teamMemberService.deleteAllByTeamId(team.getId());
            applicationEventPublisher.publishEvent(new TeamDeletedEvent(team.getId()));
        });
        teamRepository.saveAll(teams);
    }


    private Team findById(UUID id) {
        return teamRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomNotFoundException("Team not found with id: " + id));
    }
}
