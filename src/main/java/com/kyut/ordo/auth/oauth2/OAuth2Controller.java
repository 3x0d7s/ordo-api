package com.kyut.ordo.auth.oauth2;
import com.kyut.ordo.auth.common.dto.LoginResponse;
import com.kyut.ordo.auth.oauth2.dto.OAuth2TokenResponse;
import com.kyut.ordo.auth.oauth2.dto.OAuth2UserInfo;
import com.kyut.ordo.auth.oauth2.service.GoogleOAuth2Service;

import lombok.RequiredArgsConstructor;
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

    private final GoogleOAuth2Service oAuth2Service;

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> exchangeGoogleCode(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        LoginResponse loginResponse = oAuth2Service.authenticateFromCode(code);
        return ResponseEntity.ok(loginResponse);
    }

}
