package com.readus.forum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class OAuthUrlResponse {

    @JsonProperty("authorization_url")
    private final String authorizationUrl;

    private final String state;

    public OAuthUrlResponse(String authorizationUrl, String state) {
        this.authorizationUrl = authorizationUrl;
        this.state = state;
    }
}
