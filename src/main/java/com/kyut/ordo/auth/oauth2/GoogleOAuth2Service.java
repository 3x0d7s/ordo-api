package com.kyut.ordo.auth.oauth2;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.kyut.ordo.auth.common.dto.LoginResponse;
import com.kyut.ordo.auth.oauth2.dto.OAuth2TokenResponse;
import com.kyut.ordo.auth.oauth2.dto.OAuth2UserInfo;
import com.kyut.ordo.common.security.jwt.JwtService;
import com.kyut.ordo.user.UserEntity;
import com.kyut.ordo.user.UserRepository;
import com.kyut.ordo.user.dto.UserReadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final RestTemplate restTemplate = new RestTemplate();

    public OAuth2TokenResponse exchangeCodeOnToken(String code)  {

        TokenResponse tokenResponse;
        try {
            tokenResponse = new AuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    new GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL),
                    code)
                    .setRedirectUri(redirectUri)
                    .setClientAuthentication(new ClientParametersAuthentication(
                            clientId,
                            clientSecret
                    )).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return OAuth2TokenResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .build();
    }

    public OAuth2UserInfo getUserInfo(OAuth2TokenResponse tokens) {
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(tokens.getAccessToken());
        HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                userInfoRequest,
                Map.class
        );

        Map<String, String> userInfo = userResponse.getBody();

        UserReadDTO user = UserReadDTO.builder()
                .id(userInfo.get("id"))
                .email(userInfo.get("email"))
                .name(userInfo.get("name"))
                .picture(userInfo.get("picture"))
                .build();

        return OAuth2UserInfo.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .user(user)
                .build();
    }

    public LoginResponse authenticate(OAuth2UserInfo userInfo) {
        UserReadDTO userReadDTO = userInfo.getUser();

        Optional<UserEntity> user = userRepository.findByEmail(userReadDTO.getEmail());
        UserEntity userEntity;

        if (user.isEmpty()) {
            userEntity = UserEntity.builder()
                    .email(userReadDTO.getEmail())
                    .username(userReadDTO.getName())
                    .imageUrl(userReadDTO.getPicture())
                    .provider(AuthProvider.GOOGLE)
                    .password(passwordEncoder.encode("test"))
                    .build();
            userRepository.save(userEntity);
        }

        else {
            if (!user.get().getProvider().equals(AuthProvider.GOOGLE)) {
                throw new RuntimeException("User already exists");
            }
            userEntity = user.get();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userEntity.getEmail(),
                        "test"
                )
        );

        String token = jwtService.generateToken(authentication);

        System.out.println("123");

        return LoginResponse.builder()
                .accessToken(token)
                .user(userInfo.getUser())
                .build();
    }

    public LoginResponse authenticateFromCode(String code) {
        OAuth2TokenResponse oAuth2TokenResponse = exchangeCodeOnToken(code);
        OAuth2UserInfo oAuth2UserInfo = getUserInfo(oAuth2TokenResponse);
        return authenticate(oAuth2UserInfo);
    }

}
