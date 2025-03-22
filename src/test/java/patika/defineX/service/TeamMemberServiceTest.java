package patika.defineX.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import patika.defineX.dto.response.TeamMemberResponse;
import patika.defineX.exception.custom.CustomAlreadyExistException;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.Project;
import patika.defineX.model.Team;
import patika.defineX.model.TeamMember;
import patika.defineX.model.User;
import patika.defineX.repository.TeamMemberRepository;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class TeamMemberServiceTest {

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private TeamMemberService teamMemberService;

    private Team team;
    private User user;
    private TeamMember teamMember;

    @BeforeEach
    void setUp() {
        Project project = Project.builder()
                .title("Project")
                .build();
        project.setId(UUID.randomUUID());

        team = Team.builder()
                .name("Team")
                .project(project)
                .build();
        team.setId(UUID.randomUUID());

        user = User.builder()
                .name("User")
                .email("teamMember@mail.com")
                .build();
        user.setId(UUID.randomUUID());

        teamMember = new TeamMember(team, user);
    }

    @Test
    void getAllByTeamId_ShouldReturnTeamMemberList() {
        UUID teamId = team.getId();
        when(teamMemberRepository.findAllByTeamIdAndDeletedAtNull(teamId))
                .thenReturn(Collections.singletonList(teamMember));

        List<TeamMemberResponse> response = teamMemberService.getAllByTeamId(teamId);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void getAllMembersByProjectId_ShouldReturnTeamMemberList() {
        UUID projectId = team.getProject().getId();
        when(teamMemberRepository.findAllByProjectIdAndDeletedAtNull(projectId))
                .thenReturn(Collections.singletonList(teamMember));

        List<TeamMemberResponse> response = teamMemberService.getAllMembersByProjectId(projectId);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void createTeamMember_WhenNotExists_ShouldCreate() {
        when(teamMemberRepository.findByTeamAndUserAndDeletedAtNull(team, user)).thenReturn(Optional.empty());
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);

        teamMemberService.createTeamMember(team, user);

        verify(teamMemberRepository, times(1)).save(any(TeamMember.class));
    }

    @Test
    void createTeamMember_WhenAlreadyExists_ShouldThrowException() {
        when(teamMemberRepository.findByTeamAndUserAndDeletedAtNull(team, user)).thenReturn(Optional.of(teamMember));

        CustomAlreadyExistException exception = assertThrows(CustomAlreadyExistException.class,
                () -> teamMemberService.createTeamMember(team, user));
        assertEquals("User is already a member of this team!", exception.getMessage());
    }

    @Test
    void removeTeamMember_ShouldRemove() {
        UUID teamMemberId = teamMember.getId();
        when(teamMemberRepository.findByIdAndDeletedAtNull(teamMemberId)).thenReturn(Optional.of(teamMember));

        teamMemberService.removeTeamMember(teamMemberId);

        verify(teamMemberRepository, times(1)).save(teamMember);
        assertTrue(teamMember.isDeleted());
    }

    @Test
    void removeTeamMember_WhenNotFound_ShouldThrowException() {
        UUID teamMemberId = teamMember.getId();
        when(teamMemberRepository.findByIdAndDeletedAtNull(teamMemberId)).thenReturn(Optional.empty());

        CustomNotFoundException exception = assertThrows(CustomNotFoundException.class,
                () -> teamMemberService.removeTeamMember(teamMemberId));
        assertEquals("Team member not found with id: " + teamMemberId, exception.getMessage());
    }

    @Test
    void deleteAllByTeamId_ShouldDeleteAll() {
        UUID teamId = team.getId();
        when(teamMemberRepository.findAllByTeamIdAndDeletedAtNull(teamId)).thenReturn(Collections.singletonList(teamMember));

        teamMemberService.deleteAllByTeamId(teamId);

        verify(teamMemberRepository, times(1)).saveAll(anyList());
        assertTrue(teamMember.isDeleted());
    }

    @Test
    void deleteAllByUserId_ShouldDeleteAll() {
        UUID userId = user.getId();
        when(teamMemberRepository.findAllByUserIdAndDeletedAtNull(userId)).thenReturn(Collections.singletonList(teamMember));

        teamMemberService.deleteAllByUserId(userId);

        verify(teamMemberRepository, times(1)).saveAll(anyList());
        assertTrue(teamMember.isDeleted());
    }
}
