package com.readus.forum.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String username;
    private String avatarUrl;
    private String bio;
    private String location;
}
