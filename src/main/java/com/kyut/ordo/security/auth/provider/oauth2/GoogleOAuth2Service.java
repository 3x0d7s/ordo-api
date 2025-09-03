package com.kyut.ordo.security.auth.provider.oauth2;


import com.kyut.ordo.security.auth.provider.oauth2.dto.OAuth2TokenResponse;
import com.kyut.ordo.security.auth.provider.AuthProvider;
import com.kyut.ordo.security.auth.dto.LoginResponse;
import com.kyut.ordo.security.auth.exception.DifferentAuthenticationProviderException;
import com.kyut.ordo.security.jwt.JwtService;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.feature.user.dto.UserReadDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {
    private final JwtService jwtService;
    private final UserRepository userRepository;
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

        Authentication authentication = createOAuth2Authentication(userEntity);
        String jwtToken = jwtService.generateToken(authentication);

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
            throw new DifferentAuthenticationProviderException(
                    "Account exists with different authentication provider"
            );
        }
        return user;
    }

    private UserEntity createNewUser(UserReadDTO userInfo) {
        UserEntity newUser = UserEntity.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .imageUrl(userInfo.getImageUrl())
                .provider(AuthProvider.GOOGLE)
                .build();

        log.debug("Creating new Google user: {}", userInfo.getEmail());
        return userRepository.save(newUser);
    }

    private Authentication createOAuth2Authentication(UserEntity user) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", user.getEmail());
        attributes.put("name", user.getUsername());
        attributes.put("picture", user.getImageUrl());

        OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        return new OAuth2AuthenticationToken(
                oauth2User,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                "google"
        );
    }

}
