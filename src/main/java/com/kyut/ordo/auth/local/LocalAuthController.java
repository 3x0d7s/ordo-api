package com.kyut.ordo.auth.local;

import com.kyut.ordo.auth.local.dto.LocalLoginRequest;
import com.kyut.ordo.auth.common.dto.LoginResponse;
import com.kyut.ordo.user.dto.UserCreateDTO;
import com.kyut.ordo.user.dto.UserReadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/local")
public class LocalAuthController {
    private final LocalAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserReadDTO> register(UserCreateDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LocalLoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

}
