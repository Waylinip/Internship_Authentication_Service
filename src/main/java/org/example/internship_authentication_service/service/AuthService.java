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

    public static final String USER_NOT_FOUND = "User not found";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String INVALID_LOGIN_OR_PASSWORD = "Invalid login or password";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new UserAlreadyExistsException("Login already taken");
        }

        User user = new User();
        user.setLogin(request.getLogin());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setEnabled(true);

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException(INVALID_LOGIN_OR_PASSWORD));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(INVALID_LOGIN_OR_PASSWORD);
        }

        if (!user.getEnabled()) {
            throw new DisabledException("User is deactivated");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
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
        log.info("Validate token: {}", token);
        if (!jwtService.validateToken(token)) throw new JwtException(INVALID_TOKEN);
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
