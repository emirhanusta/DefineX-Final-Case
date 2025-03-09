package patika.defineX.dto.response;

import patika.defineX.model.User;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String surname,
    String email
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getSurname(), user.getEmail());
    }
}
