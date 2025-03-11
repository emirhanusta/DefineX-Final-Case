package patika.defineX.dto.request;

import patika.defineX.model.User;

public record UserRequest(
    String name,
    String email,
    String password
) {
    public static User from (UserRequest user) {
        return User.builder()
                .name(user.name())
                .email(user.email())
                .password(user.password())
                .build();
    }
}
