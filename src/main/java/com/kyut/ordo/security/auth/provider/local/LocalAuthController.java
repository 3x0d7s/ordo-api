package com.kyut.ordo.security.auth.provider.local;

import com.kyut.ordo.security.auth.dto.LoginResponse;
import com.kyut.ordo.feature.user.dto.UserCreateDTO;
import com.kyut.ordo.feature.user.dto.UserReadDTO;

import com.kyut.ordo.security.auth.provider.local.dto.LocalLoginRequest;
import com.kyut.ordo.security.jwt.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/local")
public class LocalAuthController {
    private final LocalAuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public ResponseEntity<UserReadDTO> register(@Valid @RequestBody UserCreateDTO request) {
        UserReadDTO user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LocalLoginRequest request,
                                               HttpServletResponse response) {
        LoginResponse loginResponse = authService.authenticate(request);
        
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

    @GetMapping("/verify")
    public ResponseEntity<UserReadDTO> verifyToken(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserReadDTO user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Clear JWT cookie
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);  // Delete cookie immediately
        response.addCookie(jwtCookie);
        
        return ResponseEntity.ok().build();
    }
}
