package com.kyut.ordo.auth.oauth2.service;

import com.kyut.ordo.auth.oauth2.dto.OAuth2TokenResponse;
import com.kyut.ordo.auth.oauth2.dto.OAuth2UserInfo;
import com.kyut.ordo.user.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    public OAuth2TokenResponse exchangeCodeOnToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String tokenUrl = "https://oauth2.googleapis.com/token";
        String requestBody = String.format(
                "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                code, clientId, clientSecret, redirectUri
        );

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        var tokenResponse = restTemplate.postForEntity(
                tokenUrl,
                request,
                Map.class
        );

        Map<String, String> tokens = tokenResponse.getBody();

        OAuth2TokenResponse response = OAuth2TokenResponse.builder()
                .accessToken(tokens.get("access_token"))
                .refreshToken(tokens.get("refresh_token"))
                .build();

        return response;
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
