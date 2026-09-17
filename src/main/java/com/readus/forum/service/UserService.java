package com.readus.forum.service;

import com.readus.forum.dto.UpdateProfileRequest;
import com.readus.forum.dto.UserResponse;
import com.readus.forum.entity.Profile;
import com.readus.forum.entity.User;
import com.readus.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        return toResponse(findUser(userId), true);
    }

    @Transactional
    public UserResponse updateMe(UUID userId, UpdateProfileRequest request) {
        User user = findUser(userId);

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
            user.setProfile(profile);
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }

        return toResponse(user, true);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId, UUID requesterId) {
        return toResponse(findUser(userId), requesterId.equals(userId));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserResponse toResponse(User user, boolean includeEmail) {
        Profile profile = user.getProfile();
        return UserResponse.builder()
                .id(user.getId())
                .email(includeEmail ? user.getEmail() : null)
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .bio(profile != null ? profile.getBio() : null)
                .location(profile != null ? profile.getLocation() : null)
                .build();
    }
}
