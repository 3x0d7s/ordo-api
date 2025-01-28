package com.kyut.ordo.auth.oauth2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class OAuth2TokenResponse {
    private String accessToken;
    private String refreshToken;
}
