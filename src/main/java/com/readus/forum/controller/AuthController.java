package com.readus.forum.controller;

import com.readus.forum.dto.JwtResponse;
import com.readus.forum.dto.LoginRequest;
import com.readus.forum.dto.OAuthCallbackRequest;
import com.readus.forum.dto.OAuthUrlResponse;
import com.readus.forum.dto.RegisterRequest;
import com.readus.forum.service.AuthService;
import com.readus.forum.service.OAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuthService oauthService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/oauth/{provider}")
    public ResponseEntity<OAuthUrlResponse> oauthStart(@PathVariable String provider) {
        return ResponseEntity.ok(oauthService.start(provider));
    }

    @PostMapping("/oauth/{provider}/callback")
    public ResponseEntity<JwtResponse> oauthCallback(@PathVariable String provider,
                                                     @Valid @RequestBody OAuthCallbackRequest request) {
        return ResponseEntity.ok(oauthService.callback(provider, request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}