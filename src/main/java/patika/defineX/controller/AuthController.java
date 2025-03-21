package patika.defineX.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import patika.defineX.dto.request.LoginRequest;
import patika.defineX.dto.request.RefreshTokenRequest;
import patika.defineX.dto.request.UserRequest;
import patika.defineX.dto.response.TokenResponse;
import patika.defineX.dto.response.UserResponse;
import patika.defineX.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            method = "POST",
            summary = "Login",
            description = "Login with email and password"
    )
    @PostMapping("/v1/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Operation(
            method = "POST",
            summary = "Register",
            description = "Register with email and password"
    )
    @PostMapping("/v1/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(userRequest));
    }

    @Operation(
            method = "POST",
            summary = "Refresh token",
            description = "Refresh your access token with refresh token"
    )
    @PostMapping("/v1/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(
            method = "POST",
            summary = "Logout",
            description = "Logout with refresh token"
    )
    @PostMapping("/v1/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
