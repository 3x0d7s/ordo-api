package com.kyut.ordo.auth.oauth2.service;

import com.kyut.ordo.auth.oauth2.dto.OAuth2TokenResponse;

public interface OAuth2Service {
    OAuth2TokenResponse exchangeCodeOnToken(String code);
}
