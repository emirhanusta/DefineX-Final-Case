package patika.defineX.service;

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

    public UserResponse register (UserRequest userRequest) {
        User user = User.builder()
                .name(userRequest.name())
                .email(userRequest.email())
                .password(passwordEncoder.encode(userRequest.password()))
                .build();
        return userService.save(user);
    }

    public TokenResponse login (LoginRequest loginRequest) {
        User user = userService.findByEmail(loginRequest.email());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
            return new TokenResponse(
                    UserResponse.from(user),
                    tokenService.generateAccessToken(user.getEmail()),
                    tokenService.createRefreshToken(user)
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    public  TokenResponse refresh (RefreshTokenRequest request) {
        return tokenService.validateRefreshToken(request.refreshToken());
    }
}
