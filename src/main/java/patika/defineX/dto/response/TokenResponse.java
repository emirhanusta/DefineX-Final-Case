package patika.defineX.dto.response;

public record TokenResponse(
        UserResponse user,
        String token
) {
}
