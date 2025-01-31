package com.kyut.ordo.auth.oauth2;
import com.kyut.ordo.auth.oauth2.dto.OAuth2TokenResponse;
import com.kyut.ordo.auth.oauth2.dto.OAuth2UserInfo;
import com.kyut.ordo.auth.oauth2.service.GoogleOAuth2Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class OAuth2Controller {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final GoogleOAuth2Service oAuth2Service;

    @PostMapping("/google")
    public ResponseEntity<OAuth2UserInfo> exchangeGoogleCode(@RequestBody Map<String, String> body) {
        String code = body.get("code");

        OAuth2TokenResponse oAuth2TokenResponse = oAuth2Service.exchangeCodeOnToken(code);
        OAuth2UserInfo oAuth2UserInfo = oAuth2Service.getUserInfo(oAuth2TokenResponse);

        return ResponseEntity.ok(oAuth2UserInfo);
    }

}
