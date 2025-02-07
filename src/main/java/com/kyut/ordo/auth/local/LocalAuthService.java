package com.kyut.ordo.auth.local;

import com.kyut.ordo.auth.local.dto.LocalLoginRequest;
import com.kyut.ordo.auth.common.dto.LoginResponse;
import com.kyut.ordo.auth.common.AuthProvider;
import com.kyut.ordo.common.security.jwt.JwtService;
import com.kyut.ordo.user.UserMapper;
import com.kyut.ordo.user.UserRepository;
import com.kyut.ordo.user.dto.UserCreateDTO;
import com.kyut.ordo.user.dto.UserReadDTO;
import com.kyut.ordo.user.UserEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalAuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public UserReadDTO register(UserCreateDTO request) {
        if (userRepository
                .findByEmail(request.getEmail())
                .isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        UserEntity user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    public LoginResponse authenticate(LocalLoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        if (!user.getProvider().equals(AuthProvider.LOCAL)) {
            throw new RuntimeException();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String jwtToken = jwtService.generateToken(authentication);

        user = (UserEntity) authentication.getPrincipal();

        return LoginResponse.builder()
                .accessToken(jwtToken)
                .user(userMapper.toDto(user))
                .build();
    }

    public UserReadDTO getInfo(UserEntity user) {
        return userMapper.toDto(user);
    }
}
