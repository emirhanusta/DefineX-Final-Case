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
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTitle("Project");

        team = new Team();
        team.setId(UUID.randomUUID());
        team.setName("Team");
        team.setProject(project);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("teamMember@mail.com");

        teamMember = new TeamMember(team, user);
    }

    @Test
    void testGetAllByTeamId() {
        // Arrange
        UUID teamId = team.getId();
        when(teamMemberRepository.findAllByTeamIdAndDeletedAtNull(teamId))
                .thenReturn(Collections.singletonList(teamMember));

        // Act
        List<TeamMemberResponse> response = teamMemberService.getAllByTeamId(teamId);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testCreateTeamMember_WhenNotExist() {
        // Arrange
        when(teamMemberRepository.findByTeamAndUser(team, user)).thenReturn(Optional.empty());

        // Act
        teamMemberService.createTeamMember(team, user);

        // Assert
        verify(teamMemberRepository, times(1)).save(any(TeamMember.class));
    }

    @Test
    void testCreateTeamMember_WhenAlreadyExists() {
        // Arrange
        when(teamMemberRepository.findByTeamAndUser(team, user)).thenReturn(Optional.of(teamMember));

        // Act & Assert
        CustomAlreadyExistException exception = assertThrows(CustomAlreadyExistException.class,
                () -> teamMemberService.createTeamMember(team, user));
        assertEquals("User is already a member of this team!", exception.getMessage());
    }

    @Test
    void testCreateTeamMember_WhenAlreadyDeleted() {
        // Arrange
        teamMember.softDelete();
        when(teamMemberRepository.findByTeamAndUser(team, user)).thenReturn(Optional.of(teamMember));

        // Act
        teamMemberService.createTeamMember(team, user);

        // Assert
        verify(teamMemberRepository, times(1)).save(teamMember);
        assertFalse(teamMember.isDeleted());
    }

    @Test
    void testRemoveTeamMember() {
        // Arrange
        UUID teamMemberId = teamMember.getId();
        when(teamMemberRepository.findByIdAndDeletedAtNull(teamMemberId)).thenReturn(Optional.of(teamMember));

        // Act
        teamMemberService.removeTeamMember(teamMemberId);

        // Assert
        verify(teamMemberRepository, times(1)).save(teamMember);
        assertTrue(teamMember.isDeleted());
    }

    @Test
    void testRemoveTeamMember_WhenNotFound() {
        // Arrange
        UUID teamMemberId = teamMember.getId();
        when(teamMemberRepository.findByIdAndDeletedAtNull(teamMemberId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomNotFoundException exception = assertThrows(CustomNotFoundException.class,
                () -> teamMemberService.removeTeamMember(teamMemberId));
        assertEquals("Team member not found with id: " + teamMemberId, exception.getMessage());
    }

    @Test
    void testDeleteAllByTeamId() {
        // Arrange
        UUID teamId = team.getId();
        when(teamMemberRepository.findAllByTeamIdAndDeletedAtNull(teamId)).thenReturn(Collections.singletonList(teamMember));

        // Act
        teamMemberService.deleteAllByTeamId(teamId);

        // Assert
        verify(teamMemberRepository, times(1)).saveAll(anyList());
        assertTrue(teamMember.isDeleted());
    }

    @Test
    void testDeleteAllByUserId() {
        // Arrange
        UUID userId = user.getId();
        when(teamMemberRepository.findAllByUserIdAndDeletedAtNull(userId)).thenReturn(Collections.singletonList(teamMember));

        // Act
        teamMemberService.deleteAllByUserId(userId);

        // Assert
        verify(teamMemberRepository, times(1)).saveAll(anyList());
        assertTrue(teamMember.isDeleted());
    }
}
