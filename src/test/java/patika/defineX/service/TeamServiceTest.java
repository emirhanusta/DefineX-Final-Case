package patika.defineX.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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
import patika.defineX.model.enums.Role;
import patika.defineX.repository.TeamRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private TeamMemberService teamMemberService;

    @InjectMocks
    private TeamService teamService;

    private UUID teamId;
    private UUID userId;
    private UUID projectId;
    private Team team;
    private User user;
    private Project project;

    @BeforeEach
    public void setUp() {
        teamId = UUID.randomUUID();
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        project = Project.builder()
                .title("Test Project")
                .description("Test Description")
                .build();
        project.setId(projectId);
        user = User.builder()
                .name("Test User")
                .email("user@example.com")
                .password("password")
                .authorities(Set.of(Role.TEAM_LEADER))
                .build();
        team = new Team("Test Team", project);
    }

    @Test
    public void getAllTeamsByProjectId_ShouldReturnTeams() {
        when(projectService.findById(projectId)).thenReturn(project);
        Team team1 = new Team( "Team 1", project);
        Team team2 = new Team( "Team 2", project);
        when(teamRepository.findAllByProjectIdAndDeletedAtNull(projectId)).thenReturn(List.of(team1, team2));

        List<TeamResponse> result = teamService.getAllTeamsByProjectId(projectId);

        assertEquals(2, result.size());
    }

    @Test
    public void getTeamById_ShouldReturnTeam() {
        TeamMemberResponse teamMemberResponse = new TeamMemberResponse(UUID.randomUUID(), teamId, userId);
        when(teamRepository.findByIdAndDeletedAtNull(teamId)).thenReturn(Optional.of(team));
        when(teamMemberService.getAllByTeamId(teamId)).thenReturn(List.of(teamMemberResponse));

        TeamMemberListResponse result = teamService.getTeamById(teamId);

        assertEquals(teamId, result.id());
        assertEquals("Test Team", result.name());
        assertEquals(1, result.members().size());
    }

    @Test
    public void save_ShouldSaveTeam() {
        TeamRequest teamRequest = new TeamRequest("New Team", projectId);
        when(projectService.findById(projectId)).thenReturn(project);

        Team teamToSave = Team.builder()
                .name("NEW TEAM")
                .project(project)
                .build();
        when(teamRepository.save(any(Team.class))).thenReturn(teamToSave);

        TeamResponse result = teamService.save(teamRequest);

        assertEquals("NEW TEAM", result.name());
    }


    @Test
    public void update_ShouldUpdateTeam() {
        TeamRequest teamRequest = new TeamRequest("Updated Team", projectId);
        when(teamRepository.findByIdAndDeletedAtNull(teamId)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        TeamResponse result = teamService.update(teamId, teamRequest);

        assertEquals("Updated Team", result.name());
    }

    @Test
    public void update_ShouldUpdateTeamWithNewProject() {
        TeamRequest teamRequest = new TeamRequest("Updated Team", UUID.randomUUID());
        when(teamRepository.findByIdAndDeletedAtNull(teamId)).thenReturn(Optional.of(team));
        when(projectService.findById(teamRequest.projectId())).thenReturn(project);
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        TeamResponse result = teamService.update(teamId, teamRequest);

        assertEquals("Updated Team", result.name());
    }

    @Test
    public void delete_ShouldDeleteTeam() {
        when(teamRepository.findByIdAndDeletedAtNull(teamId)).thenReturn(Optional.of(team));
        doNothing().when(teamMemberService).deleteAllByTeamId(teamId);

        teamService.delete(teamId);

        verify(teamRepository).save(team);
        verify(teamMemberService).deleteAllByTeamId(teamId);
    }

    @Test
    public void addTeamMember_ShouldAddTeamMember() {
        when(teamRepository.findByIdAndDeletedAtNull(teamId)).thenReturn(Optional.of(team));
        when(userService.findById(userId)).thenReturn(user);
        doNothing().when(teamMemberService).createTeamMember(any(Team.class), any(User.class));
        when(teamMemberService.getAllByTeamId(teamId)).thenReturn(List.of(new TeamMemberResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        )));

        TeamMemberListResponse result = teamService.addTeamMember(teamId, userId);

        assertEquals(teamId, result.id());
        assertEquals(1, result.members().size());
    }

    @Test
    public void removeTeamMember_ShouldRemoveTeamMember() {
        TeamMember teamMember = new TeamMember(team, user);
        when(teamMemberService.findById(any(UUID.class))).thenReturn(teamMember);

        teamService.removeTeamMember(UUID.randomUUID());

        verify(teamMemberService).removeTeamMember(any(UUID.class));
    }

    @Test
    public void deleteTeamsByProjectId_ShouldDeleteTeams() {
        UUID projectId = UUID.randomUUID();
        ProjectDeletedEvent event = new ProjectDeletedEvent(projectId);
        Team team1 = new Team("Team 1", project);
        Team team2 = new Team("Team 2", project);
        when(teamRepository.findAllByProjectIdAndDeletedAtNull(projectId)).thenReturn(List.of(team1, team2));

        teamService.deleteTeamsByProjectId(event);

        verify(teamRepository).saveAll(anyList());
    }

    @Test
    public void existingTeamName_ShouldThrowException() {
        when(teamRepository.existsByNameAndDeletedAtNull("TEST TEAM")).thenReturn(true);

        assertThrows(CustomAlreadyExistException.class, () -> teamService.save(new TeamRequest("Test Team", projectId)));
    }

    @Test
    public void getTeamById_ShouldThrowException() {
        when(teamRepository.findByIdAndDeletedAtNull(teamId)).thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> teamService.getTeamById(teamId));
    }
}
