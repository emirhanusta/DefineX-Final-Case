package patika.defineX.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import patika.defineX.dto.request.LoginRequest;
import patika.defineX.dto.request.RefreshTokenRequest;
import patika.defineX.dto.request.UserRequest;
import patika.defineX.dto.response.TokenResponse;
import patika.defineX.dto.response.UserResponse;
import patika.defineX.model.User;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, TokenService tokenService,
                       AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(UserRequest userRequest) {
        logger.info("Registering new user with email: {}", userRequest.email());

        User user = User.builder()
                .name(userRequest.name())
                .email(userRequest.email())
                .password(passwordEncoder.encode(userRequest.password()))
                .build();

        UserResponse response = userService.save(user);
        logger.info("User registered successfully with email: {}", userRequest.email());

        return response;
    }

    public TokenResponse login(LoginRequest loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.email());

        User user = userService.findByEmail(loginRequest.email());
        logger.debug("User found for email: {}", loginRequest.email());

        try {
            logger.debug("Authenticating user with email: {}", loginRequest.email());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );
            logger.info("User authenticated successfully: {}", loginRequest.email());

            TokenResponse tokenResponse = new TokenResponse(
                    UserResponse.from(user),
                    tokenService.generateAccessToken(user.getEmail()),
                    tokenService.createRefreshToken(user)
            );

            logger.info("Login successful for email: {}", loginRequest.email());
            return tokenResponse;
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for email: {}", loginRequest.email());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        logger.info("Refreshing token with refresh token: {}", request.refreshToken());

        TokenResponse tokenResponse = tokenService.validateRefreshToken(request.refreshToken());
        logger.info("Token refreshed successfully for refresh token: {}", request.refreshToken());
        return tokenResponse;
    }

    public void logout(RefreshTokenRequest request) {
        logger.info("Logging out user with refresh token: {}", request.refreshToken());

        tokenService.deleteRefreshToken(request.refreshToken());
        logger.info("User logged out successfully for refresh token: {}", request.refreshToken());
    }
}