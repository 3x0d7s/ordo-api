package com.kyut.ordo.auth.oauth2.service;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.kyut.ordo.auth.oauth2.dto.OAuth2TokenResponse;
import com.kyut.ordo.auth.oauth2.dto.OAuth2UserInfo;
import com.kyut.ordo.user.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public OAuth2TokenResponse exchangeCodeOnToken(String code)  {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        TokenResponse tokenResponse;
        try {
            tokenResponse = new AuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    new GenericUrl(tokenUrl),
                    code)
                    .setRedirectUri(redirectUri)
                    .setClientAuthentication(new ClientParametersAuthentication(
                            clientId,
                            clientSecret
                    )).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return OAuth2TokenResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .build();
    }

    public OAuth2UserInfo getUserInfo(OAuth2TokenResponse tokens) {
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(tokens.getAccessToken());
        HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                userInfoRequest,
                Map.class
        );

        Map<String, String> userInfo = userResponse.getBody();

        UserDTO user = UserDTO.builder()
                .id(userInfo.get("id"))
                .email(userInfo.get("email"))
                .name(userInfo.get("name"))
                .picture(userInfo.get("picture"))
                .build();

        return OAuth2UserInfo.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .user(user)
                .build();
    }

}
