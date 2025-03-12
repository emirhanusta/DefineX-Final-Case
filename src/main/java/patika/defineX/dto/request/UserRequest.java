package patika.defineX.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import patika.defineX.model.User;

public record UserRequest(
    @NotBlank
    @Size(min = 3, max = 50)
    String name,
    @NotBlank
    @Email
    String email,
    @NotBlank
    @Size(min = 6, max = 50)
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
