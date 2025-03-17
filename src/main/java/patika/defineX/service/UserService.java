package patika.defineX.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.UserRequest;
import patika.defineX.dto.response.UserResponse;
import patika.defineX.exception.custom.CustomAlreadyExistException;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.enums.Role;
import patika.defineX.model.User;
import patika.defineX.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TeamMemberService teamMemberService;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, TeamMemberService teamMemberService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.teamMemberService = teamMemberService;
        this.tokenService = tokenService;
    }

    public List<UserResponse> listAll() {
        return userRepository.findAllByDeletedAtNull().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getById(UUID id) {
        User user = findById(id);
        return UserResponse.from(user);
    }

    public UserResponse save(User user) {
        existsByEmail(user.getEmail());
        user.setAuthorities(Set.of(Role.TEAM_MEMBER));
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse update(UUID id, UserRequest userRequest) {
        User user = findById(id);
        if (!user.getEmail().equals(userRequest.email())) {
            existsByEmail(userRequest.email());
        }
        user.setName(userRequest.name());
        user.setEmail(userRequest.email());
        user.setPassword(userRequest.password());
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse addRole(UUID id, Role role) {
        User user = findById(id);
        if (user.getAuthorities().contains(role)) {
            return UserResponse.from(user);
        }
        user.getAuthorities().add(role);
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse removeRole(UUID id, Role role) {
        User user = findById(id);
        if (role == Role.TEAM_MEMBER) {
            return UserResponse.from(user);
        }
        if (!user.getAuthorities().contains(role)) {
            return UserResponse.from(user);
        }
        user.getAuthorities().remove(role);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        User user = findById(id);
        user.softDelete();
        userRepository.save(user);
        teamMemberService.deleteAllByUserId(id);
        tokenService.deleteRefreshTokenByUserId(id);
    }

    protected User findById(UUID id) {
        return userRepository.findByIdAndDeletedAtNull(id).orElseThrow(
                () -> new CustomNotFoundException("User not found with id: " + id));
    }

    protected User findByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtNull(email).orElseThrow(
                () -> new CustomNotFoundException("User not found with email: " + email));
    }

    private void existsByEmail(String email) {
        if (userRepository.existsByEmailAndDeletedAtNull(email)) {
            throw new CustomAlreadyExistException("User already exist with email: " + email);
        }
    }
}
