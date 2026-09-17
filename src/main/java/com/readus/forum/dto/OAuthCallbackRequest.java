package com.readus.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthCallbackRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String state;
}
