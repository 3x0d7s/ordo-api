package com.kyut.ordo.auth.oauth2.service;

import com.kyut.ordo.auth.common.AuthProvider;
import com.kyut.ordo.auth.common.dto.LoginResponse;
import com.kyut.ordo.auth.oauth2.dto.OAuth2TokenResponse;
import com.kyut.ordo.auth.oauth2.exception.OAuth2AuthenticationException;
import com.kyut.ordo.common.security.jwt.JwtService;
import com.kyut.ordo.user.UserEntity;
import com.kyut.ordo.user.UserRepository;
import com.kyut.ordo.user.dto.UserReadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final GoogleOAuth2Client googleOAuth2Client;

    public LoginResponse authenticateFromCode(String code) {
        OAuth2TokenResponse tokens = googleOAuth2Client.exchangeCodeForToken(code);
        UserReadDTO userInfo = googleOAuth2Client.getUserInfo(tokens);
        return authenticateUser(userInfo, tokens);
    }

    private LoginResponse authenticateUser(UserReadDTO userInfo, OAuth2TokenResponse tokens) {
        UserEntity userEntity = userRepository.findByEmail(userInfo.getEmail())
                .map(this::validateExistingUser)
                .orElseGet(() -> createNewUser(userInfo));

        Authentication authentication = authenticateWithRandomPassword(userEntity);
        String jwtToken = jwtService.generateToken(authentication);

//        log.debug("Successfully authenticated Google user: {}", userInfo.getEmail());

        return LoginResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(tokens.getRefreshToken())
                .user(userInfo)
                .build();
    }

    private UserEntity validateExistingUser(UserEntity user) {
        if (!user.getProvider().equals(AuthProvider.GOOGLE)) {
            log.error("User {} exists with different auth provider: {}", 
                    user.getEmail(), user.getProvider());
            throw new OAuth2AuthenticationException(
                    "Account exists with different authentication provider"
            );
        }
        return user;
    }

    private UserEntity createNewUser(UserReadDTO userInfo) {
        UserEntity newUser = UserEntity.builder()
                .email(userInfo.getEmail())
                .username(userInfo.getUsername())
                .imageUrl(userInfo.getImageUrl())
                .provider(AuthProvider.GOOGLE)
//                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .password(passwordEncoder.encode("test"))
                .build();

        log.debug("Creating new Google user: {}", userInfo.getEmail());
        return userRepository.save(newUser);
    }

    private Authentication authenticateWithRandomPassword(UserEntity user) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
//                        UUID.randomUUID().toString()
                        "test"
                )
        );
    }

}
