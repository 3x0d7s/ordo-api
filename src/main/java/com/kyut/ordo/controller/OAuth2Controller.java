package com.kyut.ordo.controller;
import com.kyut.ordo.user.UserDTO;
import com.kyut.ordo.controller.AuthResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class OAuth2Controller {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/google")
    public ResponseEntity<?> exchangeGoogleCode(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        
        // Exchange code for tokens
        String tokenUrl = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = String.format(
            "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
            code, clientId, clientSecret, redirectUri
        );

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                tokenUrl,
                request,
                Map.class
            );
            
            Map<String, String> tokens = tokenResponse.getBody();
            String accessToken = tokens.get("access_token");
            
            // Get user info using the access token
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);
            
            ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                userInfoRequest,
                Map.class
            );
            
            Map<String, String> userInfo = userResponse.getBody();
            
            // Create response with tokens and user info
            UserDTO user = UserDTO.builder()
                .id(userInfo.get("id"))
                .email(userInfo.get("email"))
                .name(userInfo.get("name"))
                .picture(userInfo.get("picture"))
                .build();

            AuthResponse response = AuthResponse.builder()
                .access_token(accessToken)
                .refresh_token(tokens.get("refresh_token"))
                .user(user)
                .build();
                
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Failed to authenticate with Google"));
        }
    }
}
