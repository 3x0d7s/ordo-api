package com.kyut.ordo.auth.oauth2.controller;

import com.kyut.ordo.auth.oauth2.service.GoogleOAuth2Service;
import com.kyut.ordo.auth.oauth2.dto.OAuth2CodeOnTokenRequest;
import com.kyut.ordo.auth.provider.dto.LoginResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/oauth2")
public class OAuth2Controller {

    private final GoogleOAuth2Service oAuth2Service;

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> exchangeGoogleCode(@RequestBody OAuth2CodeOnTokenRequest request) {
        LoginResponse loginResponse = oAuth2Service.authenticateFromCode(request.getCode());
        return ResponseEntity.ok(loginResponse);
    }

}
