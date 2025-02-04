package com.kyut.ordo.auth.oauth2;
import com.kyut.ordo.auth.common.dto.LoginResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/oauth2")
public class OAuth2Controller {

    private final GoogleOAuth2Service oAuth2Service;

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> exchangeGoogleCode(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        LoginResponse loginResponse = oAuth2Service.authenticateFromCode(code);
        return ResponseEntity.ok(loginResponse);
    }

}
