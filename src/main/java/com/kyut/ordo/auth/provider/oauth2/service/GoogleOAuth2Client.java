package com.kyut.ordo.auth.provider.oauth2.service;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.kyut.ordo.auth.provider.oauth2.config.GoogleOAuth2Properties;
import com.kyut.ordo.auth.provider.oauth2.dto.OAuth2TokenResponse;
import com.kyut.ordo.auth.provider.oauth2.exception.GoogleOAuth2CodeForTokenExchangeException;
import com.kyut.ordo.auth.provider.oauth2.exception.GoogleOAuth2FetchUserInfoException;
import com.kyut.ordo.auth.provider.oauth2.exception.OAuth2AuthenticationException;
import com.kyut.ordo.user.dto.UserReadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuth2Client {
    private final GoogleOAuth2Properties properties;
    private final RestTemplate restTemplate;

    public OAuth2TokenResponse exchangeCodeForToken(String code) {
        try {
            TokenResponse tokenResponse = new AuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    new GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL),
                    code)
                    .setRedirectUri(properties.getRedirectUri())
                    .setClientAuthentication(new ClientParametersAuthentication(
                            properties.getClientId(),
                            properties.getClientSecret()
                    )).execute();

            return OAuth2TokenResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .build();
        } catch (IOException e) {
            log.error("Failed to exchange code for token", e);
            throw new GoogleOAuth2CodeForTokenExchangeException("Failed to exchange code for token", e);
        }
    }

    public UserReadDTO getUserInfo(OAuth2TokenResponse tokens) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokens.getAccessToken());
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    properties.getUserInfoUrl(),
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new GoogleOAuth2FetchUserInfoException("Failed to fetch user info from Google");
            }

            Map<String, String> userInfo = response.getBody();
            return UserReadDTO.builder()
                    .id(userInfo.get("id"))
                    .email(userInfo.get("email"))
                    .name(userInfo.get("name"))
                    .imageUrl(userInfo.get("picture"))
                    .build();
        }
        catch (GoogleOAuth2FetchUserInfoException e) {
            throw e;
        }
        catch (Exception e) {
            throw new OAuth2AuthenticationException("Failed to fetch user info", e);
        }
    }
}
