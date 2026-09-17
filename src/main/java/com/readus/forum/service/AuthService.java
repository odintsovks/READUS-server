package com.readus.forum.service;

import com.readus.forum.dto.JwtResponse;
import com.readus.forum.dto.LoginRequest;
import com.readus.forum.dto.RegisterRequest;
import com.readus.forum.entity.Session;
import com.readus.forum.entity.User;
import com.readus.forum.repository.SessionRepository;
import com.readus.forum.repository.UserRepository;
import com.readus.forum.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setProvider("local");

        user = userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        return issueTokens(user);
    }

    @Transactional
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            sessionRepository.findByToken(token).ifPresent(sessionRepository::delete);
        }
    }

    public JwtResponse issueTokens(User user) {
        String accessToken = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = UUID.randomUUID().toString();
        createSession(user, accessToken, refreshToken);

        return new JwtResponse(accessToken, refreshToken, expirationMs / 1000);
    }

    private void createSession(User user, String token, String refreshToken) {
        Session session = new Session();
        session.setUser(user);
        session.setToken(token);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(Instant.now().plusMillis(expirationMs));
        sessionRepository.save(session);
    }

    @Transactional
    public User getCurrentUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}