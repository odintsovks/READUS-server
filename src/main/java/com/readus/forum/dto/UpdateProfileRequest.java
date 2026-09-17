package com.readus.forum.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 2000)
    private String bio;

    @Size(max = 255)
    private String location;
}
