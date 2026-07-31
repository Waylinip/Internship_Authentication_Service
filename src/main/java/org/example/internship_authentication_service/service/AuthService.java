package org.example.internship_authentication_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internship_authentication_service.dto.LoginRequest;
import org.example.internship_authentication_service.dto.RegisterRequest;
import org.example.internship_authentication_service.dto.TokenResponse;
import org.example.internship_authentication_service.dto.UserResponse;
import org.example.internship_authentication_service.entity.Role;
import org.example.internship_authentication_service.entity.User;
import org.example.internship_authentication_service.exception.UserAlreadyExistsException;
import org.example.internship_authentication_service.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid login or password";
    private static final String USER_DISABLED = "User is deactivated";
    private static final String USER_ALREADY_EXISTS = "Login already taken";
    private static final String INVALID_TOKEN = "Invalid token";
    private static final String INVALID_REFRESH_TOKEN = "Invalid or expired refresh token";
    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {
        log.info("New registration request received for login '{}'", request.getLogin());
        if (userRepository.existsByLogin(request.getLogin())) {
            log.warn("Registration rejected: login '{}' is already in use", request.getLogin());
            throw new UserAlreadyExistsException(USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setLogin(request.getLogin());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setEnabled(true);

        userRepository.save(user);
        log.info("User '{}' has been successfully registered", user.getLogin());
    }

    public TokenResponse login(LoginRequest request) {
        log.info("User '{}' is trying to sign in", request.getLogin());
        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> {
                    log.warn("Sign in failed: user '{}' was not found", request.getLogin());
                    return new BadCredentialsException(INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Sign in failed: invalid password for user '{}'", user.getLogin());
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }

        if (!user.getEnabled()) {
            log.warn("Sign in failed: user '{}' account is disabled", user.getLogin());
            throw new DisabledException(USER_DISABLED);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User '{}' signed in successfully", user.getLogin());
        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refresh(String refreshToken) {
        log.info("Refreshing authentication tokens");
        if (!jwtService.validateToken(refreshToken)) {
            log.warn("Token refresh failed: refresh token is invalid or expired");
            throw new BadCredentialsException(INVALID_REFRESH_TOKEN);
        }

        Claims claims = jwtService.parseToken(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException(USER_NOT_FOUND));

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
    public void validate(String token) {
        log.info("Validating JWT token");
        if (!jwtService.validateToken(token)) {
            log.warn("JWT validation failed: token is invalid");
            throw new JwtException(INVALID_TOKEN);
        }
    }
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(user.getId(), user.getLogin(), user.getRole(), user.getEnabled()))
                .toList();
    }

    public void activate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadCredentialsException(USER_NOT_FOUND));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void deactivate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadCredentialsException(USER_NOT_FOUND));
        user.setEnabled(false);
        userRepository.save(user);
    }
}
