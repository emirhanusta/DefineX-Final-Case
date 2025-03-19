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
        user = new User();
        user.setId(userId);
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user.setPassword("password");
        user.setAuthorities(new HashSet<>(List.of(Role.TEAM_MEMBER)));    }

    @Test
    void testListAll() {
        when(userRepository.findAllByDeletedAtNull()).thenReturn(List.of(user));

        List<UserResponse> users = userService.listAll();

        assertEquals(1, users.size());
        verify(userRepository, times(1)).findAllByDeletedAtNull();
    }

    @Test
    void testGetById_UserExists() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getById(userId);

        assertEquals(user.getEmail(), response.email());
        verify(userRepository, times(1)).findByIdAndDeletedAtNull(userId);
    }

    @Test
    void testGetById_UserNotFound() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> userService.getById(userId));
    }

    @Test
    void testSave_UserAlreadyExists() {
        when(userRepository.existsByEmailAndDeletedAtNull(user.getEmail())).thenReturn(true);

        assertThrows(CustomAlreadyExistException.class, () -> userService.save(user));
    }

    @Test
    void testSave_Success() {
        when(userRepository.existsByEmailAndDeletedAtNull(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.save(user);

        assertEquals(user.getEmail(), response.email());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdate_UserExists() {
        UserRequest request = new UserRequest("John Updated", "johnupdated@example.com", "newpassword");
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.update(userId, request);

        assertEquals(request.email(), response.email());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testAddRole_Success() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.addRole(userId, Role.TEAM_LEADER);

        assertNotNull(response);
        assertTrue(user.getAuthorities().contains(Role.TEAM_LEADER));
        verify(userRepository, times(1)).findByIdAndDeletedAtNull(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testAddRole_UserAlreadyHasRole() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.addRole(userId, Role.TEAM_MEMBER);

        assertEquals(1, response.authorities().size());
        verify(userRepository, times(0)).save(user);
    }

    @Test
    void testRemoveRole_CannotRemoveTeamMember() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.removeRole(userId, Role.TEAM_MEMBER);

        assertEquals(1, response.authorities().size());
    }

    @Test
    void testRemoveRole_Success() {
        user.getAuthorities().add(Role.TEAM_LEADER);
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.removeRole(userId, Role.TEAM_LEADER);

        assertEquals(1, response.authorities().size());
        assertFalse(user.getAuthorities().contains(Role.TEAM_LEADER));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testRemoveRole_roleNotFound() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));

        assertThrows(CustomNotFoundException.class, () -> userService.removeRole(userId, Role.TEAM_LEADER));
    }

    @Test
    void testDelete_UserExists() {
        when(userRepository.findByIdAndDeletedAtNull(userId)).thenReturn(Optional.of(user));

        userService.delete(userId);

        verify(userRepository, times(1)).save(user);
        verify(teamMemberService, times(1)).deleteAllByUserId(userId);
        verify(tokenService, times(1)).deleteRefreshTokenByUserId(userId);
    }

    @Test
    void testFindByEmail_UserExists() {
        when(userRepository.findByEmailAndDeletedAtNull(user.getEmail())).thenReturn(Optional.of(user));

        User foundUser = userService.findByEmail(user.getEmail());

        assertEquals(user.getEmail(), foundUser.getEmail());
        verify(userRepository, times(1)).findByEmailAndDeletedAtNull(user.getEmail());
    }

    @Test
    void testFindByEmail_UserNotFound() {
        when(userRepository.findByEmailAndDeletedAtNull(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(CustomNotFoundException.class, () -> userService.findByEmail(user.getEmail()));
    }

}
