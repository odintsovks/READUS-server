package com.readus.forum.util;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    public static UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            // В реальном приложении нужно хранить userId в UserDetails
            // Здесь заглушка - в реальности нужно извлекать из токена
            return UUID.fromString(userDetails.getUsername());
        }
        throw new IllegalStateException("User not authenticated");
    }
}
