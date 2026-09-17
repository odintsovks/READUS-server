package com.readus.forum.service;

import com.readus.forum.dto.JwtResponse;
import com.readus.forum.dto.OAuthCallbackRequest;
import com.readus.forum.dto.OAuthUrlResponse;
import com.readus.forum.entity.User;
import com.readus.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final RestClient.Builder restClientBuilder;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${app.oauth.frontend-callback-url}")
    private String frontendCallbackUrl;

    public OAuthUrlResponse start(String provider) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(provider);

        String state = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(stateKey(provider, state), state, STATE_TTL);

        String authorizationUrl = UriComponentsBuilder.fromUriString(registration.getProviderDetails().getAuthorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", registration.getClientId())
                .queryParam("redirect_uri", frontendCallbackUrl)
                .queryParam("scope", String.join(" ", registration.getScopes()))
                .queryParam("state", state)
                .build(true)
                .toUriString();

        return new OAuthUrlResponse(authorizationUrl, state);
    }

    @Transactional
    public JwtResponse callback(String provider, OAuthCallbackRequest request) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(provider);

        Boolean stateDeleted = stringRedisTemplate.delete(stateKey(provider, request.getState()));
        if (stateDeleted == null || !stateDeleted) {
            throw new RuntimeException("Invalid or expired OAuth state");
        }

        Map<String, Object> tokenResponse = restClientBuilder.build()
                .post()
                .uri(registration.getProviderDetails().getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("code", request.getCode())
                        .with("redirect_uri", frontendCallbackUrl)
                        .with("client_id", registration.getClientId())
                        .with("client_secret", registration.getClientSecret()))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        if (tokenResponse == null || tokenResponse.get("access_token") == null) {
            throw new RuntimeException("OAuth token exchange failed");
        }

        String accessToken = String.valueOf(tokenResponse.get("access_token"));

        Map<String, Object> userInfo = restClientBuilder.build()
                .get()
                .uri(registration.getProviderDetails().getUserInfoEndpoint().getUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        if (userInfo == null) {
            throw new RuntimeException("Failed to fetch OAuth user info");
        }

        User user = upsertUser(provider, userInfo);
        return authService.issueTokens(user);
    }

    private User upsertUser(String provider, Map<String, Object> attributes) {
        String defaultEmail = asString(attributes.get("default_email"));
        String email = defaultEmail != null ? defaultEmail : asString(attributes.get("email"));
        if (email == null) {
            email = provider + "_" + attributes.get("id") + "@no-email.readus";
        }
        
        final String finalEmail = email;

        String realName = asString(attributes.get("real_name"));
        String name = realName != null ? realName : asString(attributes.get("login"));
        if (name == null) {
            name = provider;
        }

        final String finalName = name;

        final String providerId = String.valueOf(attributes.get("id"));
        String avatarUrl = asString(attributes.get("avatar"));

        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(finalEmail);
                    newUser.setUsername(finalName.replaceAll("\\s+", "_").toLowerCase() + "_" + System.currentTimeMillis());
                    newUser.setProvider(provider);
                    newUser.setProviderId(providerId);
                    newUser.setAvatarUrl(avatarUrl);
                    return userRepository.save(newUser);
                });
    }

    private String stateKey(String provider, String state) {
        return "oauth:state:" + provider + ":" + state;
    }

    private static String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}
