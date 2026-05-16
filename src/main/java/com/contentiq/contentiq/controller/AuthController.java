package com.contentiq.contentiq.controller;

import com.contentiq.contentiq.dto.AuthResponse;
import com.contentiq.contentiq.dto.LoginRequest;
import com.contentiq.contentiq.dto.RegisterRequest;
import com.contentiq.contentiq.model.User;
import com.contentiq.contentiq.security.JwtUtil;
import com.contentiq.contentiq.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .userId(user.getId())
                .expiresInSeconds(jwtUtil.getExpirationSeconds())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userService.findByUsername(authentication.getName());
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .userId(user.getId())
                .expiresInSeconds(jwtUtil.getExpirationSeconds())
                .build();
        return ResponseEntity.ok(response);
    }
}
