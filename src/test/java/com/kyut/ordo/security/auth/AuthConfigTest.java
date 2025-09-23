package com.kyut.ordo.security.auth;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.security.auth.provider.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthConfig
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthConfig Unit Tests")
class AuthConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthConfig authConfig;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        authConfig = new AuthConfig(userRepository);
        
        testUser = TestConfig.TestDataFactory.createTestUser("test@example.com", "Test User");
        testUser.setProvider(AuthProvider.LOCAL);
        testUser.setPassword("encoded-password");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("UserDetailsService bean - returns user when found")
    void userDetailsService_UserFound_ReturnsUserDetails() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetailsService userDetailsService = authConfig.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encoded-password");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("UserDetailsService bean - throws exception when user not found")
    void userDetailsService_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When
        UserDetailsService userDetailsService = authConfig.userDetailsService();

        // Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("notfound@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("notfound@example.com");
    }

    @Test
    @DisplayName("AuthenticationProvider bean - creates DaoAuthenticationProvider")
    void authenticationProvider_CreatesDaoAuthenticationProvider() {
        // When
        AuthenticationProvider authProvider = authConfig.authenticationProvider();

        // Then
        assertThat(authProvider).isNotNull();
        assertThat(authProvider).isInstanceOf(DaoAuthenticationProvider.class);
        
        DaoAuthenticationProvider daoProvider = (DaoAuthenticationProvider) authProvider;
        // Verify that the provider is properly configured (these are internal checks)
        assertThat(daoProvider).isNotNull();
    }

    @Test
    @DisplayName("AuthenticationManager bean - returns authentication manager")
    void authenticationManager_ReturnsAuthenticationManager() throws Exception {
        // Given
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        // When
        AuthenticationManager result = authConfig.authenticationManager(authenticationConfiguration);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(authenticationManager);

        verify(authenticationConfiguration).getAuthenticationManager();
    }

    @Test
    @DisplayName("PasswordEncoder bean - returns BCryptPasswordEncoder")
    void passwordEncoder_ReturnsBCryptPasswordEncoder() {
        // When
        PasswordEncoder passwordEncoder = authConfig.passwordEncoder();

        // Then
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("PasswordEncoder bean - encodes password correctly")
    void passwordEncoder_EncodesPasswordCorrectly() {
        // Given
        String rawPassword = "testPassword123";

        // When
        PasswordEncoder passwordEncoder = authConfig.passwordEncoder();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
    }

    @Test
    @DisplayName("RestTemplate bean - returns RestTemplate instance")
    void restTemplate_ReturnsRestTemplateInstance() {
        // When
        RestTemplate restTemplate = authConfig.restTemplate();

        // Then
        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate).isInstanceOf(RestTemplate.class);
    }

    @Test
    @DisplayName("UserDetailsService - handles different user providers")
    void userDetailsService_HandlesGoogleUser() {
        // Given
        UserEntity googleUser = TestConfig.TestDataFactory.createTestUser("google@example.com", "Google User");
        googleUser.setProvider(AuthProvider.GOOGLE);
        googleUser.setPassword(null); // Google users don't have passwords
        
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(googleUser));

        // When
        UserDetailsService userDetailsService = authConfig.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername("google@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("google@example.com");
        assertThat(userDetails.getPassword()).isNull();
    }

    @Test
    @DisplayName("UserDetailsService - handles empty email")
    void userDetailsService_EmptyEmail_ThrowsException() {
        // Given
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // When
        UserDetailsService userDetailsService = authConfig.userDetailsService();

        // Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("UserDetailsService - handles null email")
    void userDetailsService_NullEmail_ThrowsException() {
        // Given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When
        UserDetailsService userDetailsService = authConfig.userDetailsService();

        // Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Password encoder - different instances encode differently")
    void passwordEncoder_DifferentInstancesEncodeDifferently() {
        // Given
        String password = "samePassword";
        PasswordEncoder encoder1 = authConfig.passwordEncoder();
        PasswordEncoder encoder2 = authConfig.passwordEncoder();

        // When
        String encoded1 = encoder1.encode(password);
        String encoded2 = encoder2.encode(password);

        // Then
        assertThat(encoded1).isNotEqualTo(encoded2); // BCrypt uses salt, so each encoding is different
        assertThat(encoder1.matches(password, encoded1)).isTrue();
        assertThat(encoder2.matches(password, encoded2)).isTrue();
        assertThat(encoder1.matches(password, encoded2)).isTrue(); // Cross-validation should work
        assertThat(encoder2.matches(password, encoded1)).isTrue();
    }

    @Test
    @DisplayName("All beans are properly configured")
    void allBeans_AreProperlyConfigured() throws Exception {
        // Given
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        // When
        UserDetailsService userDetailsService = authConfig.userDetailsService();
        AuthenticationProvider authProvider = authConfig.authenticationProvider();
        AuthenticationManager authManager = authConfig.authenticationManager(authenticationConfiguration);
        PasswordEncoder passwordEncoder = authConfig.passwordEncoder();
        RestTemplate restTemplate = authConfig.restTemplate();

        // Then
        assertThat(userDetailsService).isNotNull();
        assertThat(authProvider).isNotNull();
        assertThat(authManager).isNotNull();
        assertThat(passwordEncoder).isNotNull();
        assertThat(restTemplate).isNotNull();
    }
}
