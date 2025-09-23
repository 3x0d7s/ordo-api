package com.kyut.ordo.security.auth.provider.oauth2;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.user.dto.UserReadDTO;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.security.auth.dto.LoginResponse;
import com.kyut.ordo.security.auth.exception.DifferentAuthenticationProviderException;
import com.kyut.ordo.security.auth.provider.AuthProvider;
import com.kyut.ordo.security.auth.provider.oauth2.dto.OAuth2TokenResponse;
import com.kyut.ordo.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

/**
 * Unit tests for GoogleOAuth2Service
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleOAuth2Service Unit Tests")
class GoogleOAuth2ServiceTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GoogleOAuth2Client googleOAuth2Client;

    @InjectMocks
    private GoogleOAuth2Service googleOAuth2Service;

    private UserEntity existingGoogleUser;
    private UserEntity existingLocalUser;
    private UserReadDTO userInfo;
    private OAuth2TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        existingGoogleUser = TestConfig.TestDataFactory.createTestUser("google@example.com", "Google User");
        existingGoogleUser.setProvider(AuthProvider.GOOGLE);
        existingGoogleUser.setImageUrl("https://example.com/image.jpg");
        existingGoogleUser.setCreatedAt(LocalDateTime.now());

        existingLocalUser = TestConfig.TestDataFactory.createTestUser("local@example.com", "Local User");
        existingLocalUser.setProvider(AuthProvider.LOCAL);
        existingLocalUser.setPassword("encoded-password");
        existingLocalUser.setCreatedAt(LocalDateTime.now());

        userInfo = UserReadDTO.builder()
                .email("google@example.com")
                .name("Google User")
                .imageUrl("https://example.com/image.jpg")
                .build();

        tokenResponse = OAuth2TokenResponse.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-456")
                .build();
    }

    @Test
    @DisplayName("Authenticate from code - success with existing Google user")
    void authenticateFromCode_ExistingGoogleUser_Success() {
        // Given
        String authCode = "auth-code-123";
        String jwtToken = "jwt-token-456";

        when(googleOAuth2Client.exchangeCodeForToken(authCode)).thenReturn(tokenResponse);
        when(googleOAuth2Client.getUserInfo(tokenResponse)).thenReturn(userInfo);
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(existingGoogleUser));
        when(jwtService.generateToken(any(Authentication.class))).thenReturn(jwtToken);

        // When
        LoginResponse result = googleOAuth2Service.authenticateFromCode(authCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(jwtToken);
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token-456");
        assertThat(result.getUser()).isEqualTo(userInfo);

        verify(googleOAuth2Client).exchangeCodeForToken(authCode);
        verify(googleOAuth2Client).getUserInfo(tokenResponse);
        verify(userRepository).findByEmail("google@example.com");
        verify(userRepository, never()).save(any(UserEntity.class)); // No new user creation
        verify(jwtService).generateToken(any(Authentication.class));
    }

    @Test
    @DisplayName("Authenticate from code - success with new user")
    void authenticateFromCode_NewUser_Success() {
        // Given
        String authCode = "auth-code-123";
        String jwtToken = "jwt-token-456";
        UserEntity newUser = UserEntity.builder()
                .email("google@example.com")
                .name("Google User")
                .imageUrl("https://example.com/image.jpg")
                .provider(AuthProvider.GOOGLE)
                .build();
        newUser.setId(1L);

        when(googleOAuth2Client.exchangeCodeForToken(authCode)).thenReturn(tokenResponse);
        when(googleOAuth2Client.getUserInfo(tokenResponse)).thenReturn(userInfo);
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);
        when(jwtService.generateToken(any(Authentication.class))).thenReturn(jwtToken);

        // When
        LoginResponse result = googleOAuth2Service.authenticateFromCode(authCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(jwtToken);
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token-456");
        assertThat(result.getUser()).isEqualTo(userInfo);

        verify(googleOAuth2Client).exchangeCodeForToken(authCode);
        verify(googleOAuth2Client).getUserInfo(tokenResponse);
        verify(userRepository).findByEmail("google@example.com");
        verify(userRepository).save(any(UserEntity.class)); // New user created
        verify(jwtService).generateToken(any(Authentication.class));
    }

    @Test
    @DisplayName("Authenticate from code - user exists with different provider")
    void authenticateFromCode_DifferentProvider_ThrowsException() {
        // Given
        String authCode = "auth-code-123";
        userInfo.setEmail("local@example.com"); // Same email as local user

        when(googleOAuth2Client.exchangeCodeForToken(authCode)).thenReturn(tokenResponse);
        when(googleOAuth2Client.getUserInfo(tokenResponse)).thenReturn(userInfo);
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(existingLocalUser));

        // When & Then
        assertThatThrownBy(() -> googleOAuth2Service.authenticateFromCode(authCode))
                .isInstanceOf(DifferentAuthenticationProviderException.class)
                .hasMessageContaining("Account exists with different authentication provider");

        verify(googleOAuth2Client).exchangeCodeForToken(authCode);
        verify(googleOAuth2Client).getUserInfo(tokenResponse);
        verify(userRepository).findByEmail("local@example.com");
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(jwtService, never()).generateToken(any(Authentication.class));
    }

    @Test
    @DisplayName("Create new user - sets correct properties")
    void createNewUser_SetsCorrectProperties() {
        // Given
        String authCode = "auth-code-123";
        UserEntity newUser = UserEntity.builder()
                .email("newuser@example.com")
                .name("New User")
                .imageUrl("https://example.com/newuser.jpg")
                .provider(AuthProvider.GOOGLE)
                .build();
        newUser.setId(1L);

        UserReadDTO newUserInfo = UserReadDTO.builder()
                .email("newuser@example.com")
                .name("New User")
                .imageUrl("https://example.com/newuser.jpg")
                .build();

        when(googleOAuth2Client.exchangeCodeForToken(authCode)).thenReturn(tokenResponse);
        when(googleOAuth2Client.getUserInfo(tokenResponse)).thenReturn(newUserInfo);
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
            assertThat(savedUser.getName()).isEqualTo("New User");
            assertThat(savedUser.getImageUrl()).isEqualTo("https://example.com/newuser.jpg");
            assertThat(savedUser.getProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(savedUser.getPassword()).isNull(); // OAuth2 users don't have passwords
            return newUser;
        });
        when(jwtService.generateToken(any(Authentication.class))).thenReturn("jwt-token");

        // When
        LoginResponse result = googleOAuth2Service.authenticateFromCode(authCode);

        // Then
        verify(userRepository).save(any(UserEntity.class));
        assertThat(result.getUser().getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    @DisplayName("Authentication creates correct OAuth2 authentication")
    void authenticateFromCode_CreatesCorrectOAuth2Authentication() {
        // Given
        String authCode = "auth-code-123";

        when(googleOAuth2Client.exchangeCodeForToken(authCode)).thenReturn(tokenResponse);
        when(googleOAuth2Client.getUserInfo(tokenResponse)).thenReturn(userInfo);
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(existingGoogleUser));
        when(jwtService.generateToken(any(Authentication.class))).thenAnswer(invocation -> {
            Authentication auth = invocation.getArgument(0);
            assertThat(auth.getName()).isEqualTo("google@example.com");
            assertThat(auth.getAuthorities()).isNotEmpty();
            assertThat(auth.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
            return "jwt-token";
        });

        // When
        googleOAuth2Service.authenticateFromCode(authCode);

        // Then
        verify(jwtService).generateToken(any(Authentication.class));
    }

    @Test
    @DisplayName("Handle null refresh token gracefully")
    void authenticateFromCode_NullRefreshToken_Success() {
        // Given
        String authCode = "auth-code-123";
        tokenResponse.setRefreshToken(null); // No refresh token

        when(googleOAuth2Client.exchangeCodeForToken(authCode)).thenReturn(tokenResponse);
        when(googleOAuth2Client.getUserInfo(tokenResponse)).thenReturn(userInfo);
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(existingGoogleUser));
        when(jwtService.generateToken(any(Authentication.class))).thenReturn("jwt-token");

        // When
        LoginResponse result = googleOAuth2Service.authenticateFromCode(authCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRefreshToken()).isNull();
        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("Handle null image URL gracefully")
    void authenticateFromCode_NullImageUrl_Success() {
        // Given
        String authCode = "auth-code-123";
        userInfo.setImageUrl(null); // No image URL

        when(googleOAuth2Client.exchangeCodeForToken(authCode)).thenReturn(tokenResponse);
        when(googleOAuth2Client.getUserInfo(tokenResponse)).thenReturn(userInfo);
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertThat(savedUser.getImageUrl()).isNull();
            savedUser.setId(1L);
            return savedUser;
        });
        when(jwtService.generateToken(any(Authentication.class))).thenReturn("jwt-token");

        // When
        LoginResponse result = googleOAuth2Service.authenticateFromCode(authCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUser().getImageUrl()).isNull();
    }

    @Test
    @DisplayName("OAuth2 authentication contains correct attributes")
    void createOAuth2Authentication_ContainsCorrectAttributes() {
        // Given
        String authCode = "auth-code-123";

        when(googleOAuth2Client.exchangeCodeForToken(authCode)).thenReturn(tokenResponse);
        when(googleOAuth2Client.getUserInfo(tokenResponse)).thenReturn(userInfo);
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(existingGoogleUser));
        when(jwtService.generateToken(any(Authentication.class))).thenAnswer(invocation -> {
            Authentication auth = invocation.getArgument(0);
            var principal = auth.getPrincipal();
            assertThat(principal).isInstanceOf(org.springframework.security.oauth2.core.user.OAuth2User.class);
            
            var oauth2User = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
            assertThat((String) oauth2User.getAttribute("email")).isEqualTo("google@example.com");
            assertThat((String) oauth2User.getAttribute("name")).isEqualTo("google@example.com"); // OAuth2 principal uses email as name
            assertThat((String) oauth2User.getAttribute("picture")).isEqualTo("https://example.com/image.jpg");
            
            return "jwt-token";
        });

        // When
        googleOAuth2Service.authenticateFromCode(authCode);

        // Then
        verify(jwtService).generateToken(any(Authentication.class));
    }
}
