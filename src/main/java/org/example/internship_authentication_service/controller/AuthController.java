package org.example.internship_authentication_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.internship_authentication_service.dto.*;
import org.example.internship_authentication_service.entity.Role;
import org.example.internship_authentication_service.service.AuthService;
import org.example.internship_authentication_service.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin")
    public ResponseEntity<Void> makeAdmin(@RequestParam String login) {
        authService.makeAdmin(login);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validate(@Valid @RequestBody TokenValidRequest request) {
        authService.validate(request.getToken());
        return ResponseEntity.ok().build();
    }
}
