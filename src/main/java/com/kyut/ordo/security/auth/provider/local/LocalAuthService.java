package com.kyut.ordo.security.auth.provider.local;

import com.kyut.ordo.security.auth.provider.AuthProvider;
import com.kyut.ordo.security.auth.dto.LoginResponse;
import com.kyut.ordo.security.auth.exception.AuthUsernameNotFoundException;
import com.kyut.ordo.security.auth.exception.DifferentAuthenticationProviderException;
import com.kyut.ordo.security.auth.provider.local.dto.LocalLoginRequest;
import com.kyut.ordo.security.jwt.JwtService;
import com.kyut.ordo.feature.user.mapper.UserMapper;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.feature.user.dto.UserCreateDTO;
import com.kyut.ordo.feature.user.dto.UserReadDTO;
import com.kyut.ordo.feature.user.entity.UserEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalAuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @Transactional
    public UserReadDTO register(UserCreateDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        if (request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        UserEntity user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        
        UserEntity savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public LoginResponse authenticate(LocalLoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new AuthUsernameNotFoundException("User not found")
        );

        if (!user.getProvider().equals(AuthProvider.LOCAL)) {
            throw new DifferentAuthenticationProviderException("Account exists with different authentication provider");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String jwtToken = jwtService.generateToken(authentication);
        String csrfToken = jwtService.getCsrfTokenFromJwt(jwtToken);

        user = (UserEntity) authentication.getPrincipal();

        return LoginResponse.builder()
                .accessToken(jwtToken)
                .csrfToken(csrfToken)
                .user(userMapper.toDto(user))
                .build();
    }

    public UserReadDTO getInfo(UserEntity user) {
        return userMapper.toDto(user);
    }

    public UserReadDTO getCurrentUser(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(
                () -> new AuthUsernameNotFoundException("User not found")
        );
        return userMapper.toDto(user);
    }
}
