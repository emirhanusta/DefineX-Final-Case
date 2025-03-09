package patika.defineX.dto.request;

import patika.defineX.model.User;

public record UserRequest(
    String name,
    String surname,
    String email,
    String password
) {
    public static User from (UserRequest user) {
        return User.builder()
                .name(user.name())
                .surname(user.surname())
                .email(user.email())
                .password(user.password())
                .build();
    }
}
