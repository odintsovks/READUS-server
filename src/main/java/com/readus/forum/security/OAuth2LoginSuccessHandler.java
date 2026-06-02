package com.readus.forum.security;

import com.readus.forum.entity.User;
import com.readus.forum.repository.UserRepository;
import com.readus.forum.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String defaultEmail = (String)attributes.get("default_email");
        final String email = defaultEmail != null ? defaultEmail : (String)attributes.get("email");

        String realName = (String)attributes.get("real_name");
        final String name = realName != null ? realName : (String)attributes.get("login");

        final String providerId = String.valueOf(attributes.get("id"));

        User user = userRepository.findByProviderAndProviderId("yandex", providerId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(name.replaceAll("\\s+", "_").toLowerCase() + "_" + System.currentTimeMillis());
                    newUser.setProvider("yandex");
                    newUser.setProviderId(providerId);
                    return userRepository.save(newUser);
                });

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        response.sendRedirect("http://localhost:5173/oauth2/redirect?token=" + token);
    }
}
