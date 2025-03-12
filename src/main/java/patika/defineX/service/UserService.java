package patika.defineX.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patika.defineX.dto.request.UserRequest;
import patika.defineX.dto.response.UserResponse;
import patika.defineX.exception.custom.CustomNotFoundException;
import patika.defineX.model.enums.Role;
import patika.defineX.model.User;
import patika.defineX.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TeamMemberService teamMemberService;

    public UserService(UserRepository userRepository, TeamMemberService teamMemberService) {
        this.userRepository = userRepository;
        this.teamMemberService = teamMemberService;
    }

    public List<UserResponse> listAll() {
        return userRepository.findAllByIsDeletedFalse().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getById(UUID id) {
        User user = findById(id);
        return UserResponse.from(user);
    }

    public UserResponse save(UserRequest userRequest) {
        User user = UserRequest.from(userRequest);
        user.setRole(Role.TEAM_MEMBER);
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse update(UUID id, UserRequest userRequest) {
        User user = findById(id);
        user.setName(userRequest.name());
        user.setEmail(userRequest.email());
        user.setPassword(userRequest.password());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        User user = findById(id);
        user.setDeleted(true);
        userRepository.save(user);
        teamMemberService.deleteAllByUserId(id);
    }

    protected User findById(UUID id) {
        return userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new CustomNotFoundException("User not found with id: " + id));
    }
}
