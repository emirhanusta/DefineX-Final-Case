package patika.defineX.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.TeamRequest;
import patika.defineX.dto.response.TeamMemberListResponse;
import patika.defineX.dto.response.TeamMemberResponse;
import patika.defineX.dto.response.TeamResponse;
import patika.defineX.event.ProjectDeletedEvent;
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

    public TeamService(TeamRepository teamRepository, ProjectService projectService,
                       UserService userService,TeamMemberService teamMemberService) {
        this.teamRepository = teamRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.teamMemberService = teamMemberService;
    }

    public List<TeamResponse> getAllTeamsByProjectId(UUID projectId) {
        projectService.findById(projectId);
        return teamRepository.findAllByProjectIdAndIsDeletedFalse(projectId)
                .stream()
                .map(TeamResponse::from)
                .toList();
    }

    public TeamMemberListResponse getTeamById(UUID teamId) {
        Team team = findById(teamId);
        List<TeamMemberResponse> teamMembers = teamMemberService.getAllByTeamId(teamId);
        return new TeamMemberListResponse(
                teamId,
                team.getName(),
                teamMembers
        );
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
        teamRepository.save(team);
        teamMemberService.deleteAllByTeamId(id);
    }

    @Transactional
    public TeamMemberListResponse addTeamMember(UUID teamId, UUID userId) {
        Team team = findById(teamId);
        User user = userService.findById(userId);
        teamMemberService.createTeamMember(team, user);
        return new TeamMemberListResponse(
                teamId,
                team.getName(),
                teamMemberService.getAllByTeamId(teamId)
        );
    }

    @Transactional
    public void removeTeamMember(UUID teamMemberId) {
        TeamMember teamMember = teamMemberService.findById(teamMemberId);
        Team team = teamMember.getTeam();
        teamRepository.save(team);
        teamMemberService.removeTeamMember(teamMemberId);
    }

    @EventListener
    @Transactional
    public void deleteTeamsByProjectId(ProjectDeletedEvent event) {
        List<Team> teams = teamRepository.findAllByProjectIdAndIsDeletedFalse(event.id());
        teams.forEach(team -> {
            team.setDeleted(true);
            teamMemberService.deleteAllByTeamId(team.getId());
        });
        teamRepository.saveAll(teams);
    }


    private Team findById(UUID id) {
        return teamRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomNotFoundException("Team not found with id: " + id));
    }
}
