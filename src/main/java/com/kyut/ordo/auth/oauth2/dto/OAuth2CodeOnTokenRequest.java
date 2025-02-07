package com.kyut.ordo.auth.oauth2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class OAuth2CodeOnTokenRequest {
    private String code;
}
