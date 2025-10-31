package com.kyut.ordo.security.auth.provider.oauth2;

import com.kyut.ordo.security.auth.provider.oauth2.dto.OAuth2CodeOnTokenRequest;
import com.kyut.ordo.security.auth.dto.LoginResponse;

import com.kyut.ordo.security.jwt.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
    private final JwtProperties jwtProperties;

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> exchangeGoogleCode(@RequestBody OAuth2CodeOnTokenRequest request,
                                                            HttpServletResponse response) {
        LoginResponse loginResponse = oAuth2Service.authenticateFromCode(request.getCode());
        
        // Set JWT in HttpOnly cookie
        Cookie jwtCookie = new Cookie("jwt", loginResponse.getAccessToken());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);  // Only over HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) (jwtProperties.getExpirationMs() / 1000));
        jwtCookie.setAttribute("SameSite", "Strict");
        response.addCookie(jwtCookie);
        
        // Return CSRF token in header
        response.setHeader("X-CSRF-Token", loginResponse.getCsrfToken());
        
        return ResponseEntity.ok(loginResponse);
    }

}
