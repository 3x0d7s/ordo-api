package com.kyut.ordo.security.auth.provider.local;

import com.kyut.ordo.security.auth.dto.LoginResponse;
import com.kyut.ordo.feature.user.dto.UserCreateDTO;
import com.kyut.ordo.feature.user.dto.UserReadDTO;

import com.kyut.ordo.security.auth.provider.local.dto.LocalLoginRequest;
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

    @PostMapping("/register")
    public ResponseEntity<UserReadDTO> register(@Valid @RequestBody UserCreateDTO request) {
        UserReadDTO user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LocalLoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<UserReadDTO> verifyToken(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserReadDTO user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }
}
