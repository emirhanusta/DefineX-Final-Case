package patika.defineX.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.TeamRequest;
import patika.defineX.dto.response.TeamMemberListResponse;
import patika.defineX.dto.response.TeamMemberResponse;
import patika.defineX.dto.response.TeamResponse;
import patika.defineX.event.ProjectDeletedEvent;
import patika.defineX.exception.custom.CustomAlreadyExistException;
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

    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);

    private final TeamRepository teamRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final TeamMemberService teamMemberService;

    public TeamService(TeamRepository teamRepository, ProjectService projectService,
                       UserService userService, TeamMemberService teamMemberService) {
        this.teamRepository = teamRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.teamMemberService = teamMemberService;
    }

    public List<TeamResponse> getAllTeamsByProjectId(UUID projectId) {
        logger.info("Fetching all teams for project with id: {}", projectId);
        projectService.findById(projectId); // Ensure project exists
        List<TeamResponse> teams = teamRepository.findAllByProjectIdAndDeletedAtNull(projectId)
                .stream()
                .map(TeamResponse::from)
                .toList();
        logger.info("Found {} teams for project with id: {}", teams.size(), projectId);
        return teams;
    }

    public TeamMemberListResponse getTeamById(UUID teamId) {
        logger.info("Fetching team with id: {}", teamId);
        Team team = findById(teamId);
        List<TeamMemberResponse> teamMembers = teamMemberService.getAllByTeamId(teamId);
        logger.info("Found team with id: {} and {} members", teamId, teamMembers.size());
        return new TeamMemberListResponse(
                teamId,
                team.getName(),
                teamMembers
        );
    }

    public TeamResponse save(TeamRequest teamRequest) {
        logger.info("Creating team with name: {}", teamRequest.name());
        existsByName(teamRequest.name().toUpperCase());
        Project project = projectService.findById(teamRequest.projectId());
        Team team = Team.builder()
                .name(teamRequest.name().toUpperCase())
                .project(project)
                .build();
        Team savedTeam = teamRepository.save(team);
        logger.info("Team created with id: {}", savedTeam.getId());
        return TeamResponse.from(savedTeam);
    }

    public TeamResponse update(UUID id, TeamRequest teamRequest) {
        logger.info("Updating team with id: {}", id);
        Team team = findById(id);
        String name = teamRequest.name().toUpperCase();
        if (!team.getName().equals(name)) {
            existsByName(name);
        }

        if (!team.getProject().getId().equals(teamRequest.projectId())) {
            Project project = projectService.findById(teamRequest.projectId());
            team.setProject(project);
        }
        team.setName(teamRequest.name());
        Team updatedTeam = teamRepository.save(team);
        logger.info("Team updated with id: {}", updatedTeam.getId());
        return TeamResponse.from(updatedTeam);
    }

    public void delete(UUID id) {
        logger.info("Deleting team with id: {}", id);
        Team team = findById(id);
        team.softDelete();
        teamRepository.save(team);
        teamMemberService.deleteAllByTeamId(id);
        logger.info("Team deleted with id: {}", id);
    }

    @Transactional
    public TeamMemberListResponse addTeamMember(UUID teamId, UUID userId) {
        logger.info("Adding user with id: {} to team with id: {}", userId, teamId);
        Team team = findById(teamId);
        User user = userService.findById(userId);
        teamMemberService.createTeamMember(team, user);
        logger.info("User with id: {} added to team with id: {}", userId, teamId);
        return new TeamMemberListResponse(
                teamId,
                team.getName(),
                teamMemberService.getAllByTeamId(teamId)
        );
    }

    @Transactional
    public void removeTeamMember(UUID teamMemberId) {
        logger.info("Removing team member with id: {}", teamMemberId);
        TeamMember teamMember = teamMemberService.findById(teamMemberId);
        Team team = teamMember.getTeam();
        teamRepository.save(team);
        teamMemberService.removeTeamMember(teamMemberId);
        logger.info("Team member with id: {} removed from team with id: {}", teamMemberId, team.getId());
    }

    @EventListener
    @Transactional
    public void deleteTeamsByProjectId(ProjectDeletedEvent event) {
        logger.info("Deleting teams for project with id: {}", event.projectId());
        List<Team> teams = teamRepository.findAllByProjectIdAndDeletedAtNull(event.projectId());
        teams.forEach(team -> {
            team.softDelete();
            teamMemberService.deleteAllByTeamId(team.getId());
        });
        teamRepository.saveAll(teams);
        logger.info("Deleted {} teams for project with id: {}", teams.size(), event.projectId());
    }

    private Team findById(UUID id) {
        logger.debug("Finding team by id: {}", id);
        return teamRepository.findByIdAndDeletedAtNull(id)
                .orElseThrow(() -> {
                    logger.error("Team not found with id: {}", id);
                    return new CustomNotFoundException("Team not found with id: " + id);
                });
    }

    private void existsByName(String name) {
        logger.debug("Checking if team exists with name: {}", name);
        if (teamRepository.existsByNameAndDeletedAtNull(name)) {
            logger.error("Team already exists with name: {}", name);
            throw new CustomAlreadyExistException("Team already exist with name: " + name);
        }
    }
}