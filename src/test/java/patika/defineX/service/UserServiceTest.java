package patika.defineX.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import patika.defineX.dto.request.UserRequest;
import patika.defineX.dto.response.UserResponse;
import patika.defineX.exception.custom.CustomAlreadyExistException;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.User;
import patika.defineX.model.enums.Role;
import patika.defineX.repository.UserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamMemberService teamMemberService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserService userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .name("example")
                .email("example@example.com")
                .password("password")
                .authorities(new HashSet<>(List.of(Role.TEAM_MEMBER)))
                .build();
        user.setId(userId);
    }

    @Test
    void listAll_ShouldReturnUserList() {
        when(userRepository.findAllByDeletedAtNull()).thenReturn(List.of(user));

        List<UserResponse> users = userService.listAll();

        assertEquals(1, users.size());
        verify(userRepository, times(1)).findAllByDeletedAtNull();
    }

    @Test
    void getById_ShouldReturnUser() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getById(userId);

        assertEquals(user.getEmail(), response.email());
        verify(userRepository, times(1)).findByIdAndDeletedAtNull(userId);
    }

    @Test
    void getById_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> userService.getById(userId));
    }

    @Test
    void save_WhenUserAlreadyExists_ShouldThrowException() {
        when(userRepository.existsByEmailAndDeletedAtNull(user.getEmail())).thenReturn(true);

        assertThrows(CustomAlreadyExistException.class, () -> userService.save(user));
    }

    @Test
    void save_ShouldSaveUser() {
        when(userRepository.existsByEmailAndDeletedAtNull(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.save(user);

        assertEquals(user.getEmail(), response.email());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void update_ShouldUpdateUser() {
        UserRequest request = new UserRequest("Updated", "updated@example.com", "newpassword");
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.update(userId, request);

        assertEquals(request.email(), response.email());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void addRole_ShouldAddRole() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.addRole(userId, Role.TEAM_LEADER);

        assertNotNull(response);
        assertTrue(user.getAuthorities().contains(Role.TEAM_LEADER));
        verify(userRepository, times(1)).findByIdAndDeletedAtNull(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void addRole_WhenRoleAlreadyExists_ShouldNotAddRole() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.addRole(userId, Role.TEAM_MEMBER);

        assertEquals(1, response.authorities().size());
        verify(userRepository, times(0)).save(user);
    }

    @Test
    void removeRole_WhenRoleTeamMember_ShouldNotRemove() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.removeRole(userId, Role.TEAM_MEMBER);

        assertEquals(1, response.authorities().size());
    }

    @Test
    void removeRole_ShouldRemoveRole() {
        user.getAuthorities().add(Role.TEAM_LEADER);
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.removeRole(userId, Role.TEAM_LEADER);

        assertEquals(1, response.authorities().size());
        assertFalse(user.getAuthorities().contains(Role.TEAM_LEADER));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void removeRole_WhenRoleNotExists_ShouldThrowException() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));

        assertThrows(CustomNotFoundException.class, () -> userService.removeRole(userId, Role.TEAM_LEADER));
    }

    @Test
    void delete_ShouldDeleteUser() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));

        userService.delete(userId);

        verify(userRepository, times(1)).save(user);
        verify(teamMemberService, times(1)).deleteAllByUserId(userId);
        verify(tokenService, times(1)).deleteRefreshTokenByUserId(userId);
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        when(userRepository.findByEmailAndDeletedAtNull(user.getEmail())).thenReturn(Optional.of(user));

        User foundUser = userService.findByEmail(user.getEmail());

        assertEquals(user.getEmail(), foundUser.getEmail());
        verify(userRepository, times(1)).findByEmailAndDeletedAtNull(user.getEmail());
    }

    @Test
    void findByEmail_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findByEmailAndDeletedAtNull(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> userService.findByEmail(user.getEmail()));
    }

}
