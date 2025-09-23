package com.kyut.ordo.security.auth.provider.local;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.feature.user.dto.UserCreateDTO;
import com.kyut.ordo.feature.user.dto.UserReadDTO;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.mapper.UserMapper;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.security.auth.dto.LoginResponse;
import com.kyut.ordo.security.auth.exception.AuthUsernameNotFoundException;
import com.kyut.ordo.security.auth.exception.DifferentAuthenticationProviderException;
import com.kyut.ordo.security.auth.provider.AuthProvider;
import com.kyut.ordo.security.auth.provider.local.dto.LocalLoginRequest;
import com.kyut.ordo.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LocalAuthService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LocalAuthService Unit Tests")
class LocalAuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtService jwtService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private LocalAuthService localAuthService;

    private UserEntity testUser;
    private UserCreateDTO userCreateDto;
    private UserReadDTO userReadDto;
    private LocalLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = TestConfig.TestDataFactory.createTestUser("test@example.com", "Test User");
        testUser.setProvider(AuthProvider.LOCAL);
        testUser.setPassword("encoded-password");
        testUser.setCreatedAt(LocalDateTime.now());

        userCreateDto = new UserCreateDTO();
        userCreateDto.setEmail("test@example.com");
        userCreateDto.setName("Test User");
        userCreateDto.setPassword("password123");

        userReadDto = UserReadDTO.builder()
                .id("1")
                .email("test@example.com")
                .name("Test User")
                .build();

        loginRequest = LocalLoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    @DisplayName("Register user - success")
    void register_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(userCreateDto)).thenReturn(testUser);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(userReadDto);

        // When
        UserReadDTO result = localAuthService.register(userCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("Test User");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toDto(testUser);
    }

    @Test
    @DisplayName("Register user - email already exists")
    void register_EmailAlreadyExists() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> localAuthService.register(userCreateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User with this email already exists");

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Register user - empty name")
    void register_EmptyName() {
        // Given
        userCreateDto.setName("   "); // Empty name with spaces
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> localAuthService.register(userCreateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be empty");
    }

    @Test
    @DisplayName("Register user - null name")
    void register_NullName() {
        // Given
        userCreateDto.setName(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> localAuthService.register(userCreateDto))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Authenticate user - success")
    void authenticate_Success() {
        // Given
        String jwtToken = "jwt-token-12345";
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtService.generateToken(authentication)).thenReturn(jwtToken);
        when(userMapper.toDto(testUser)).thenReturn(userReadDto);

        // When
        LoginResponse result = localAuthService.authenticate(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(jwtToken);
        assertThat(result.getUser()).isEqualTo(userReadDto);
        assertThat(result.getRefreshToken()).isNull(); // Local auth doesn't use refresh tokens

        verify(userRepository).findByEmail("test@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(authentication);
        verify(userMapper).toDto(testUser);
    }

    @Test
    @DisplayName("Authenticate user - user not found")
    void authenticate_UserNotFound() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> localAuthService.authenticate(loginRequest))
                .isInstanceOf(AuthUsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Authenticate user - different authentication provider")
    void authenticate_DifferentAuthProvider() {
        // Given
        testUser.setProvider(AuthProvider.GOOGLE); // User registered with Google
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> localAuthService.authenticate(loginRequest))
                .isInstanceOf(DifferentAuthenticationProviderException.class)
                .hasMessageContaining("Account exists with different authentication provider");

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Get current user - success")
    void getCurrentUser_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(userReadDto);

        // When
        UserReadDTO result = localAuthService.getCurrentUser("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("Test User");

        verify(userRepository).findByEmail("test@example.com");
        verify(userMapper).toDto(testUser);
    }

    @Test
    @DisplayName("Get current user - user not found")
    void getCurrentUser_UserNotFound() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> localAuthService.getCurrentUser("test@example.com"))
                .isInstanceOf(AuthUsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Get info - success")
    void getInfo_Success() {
        // Given
        when(userMapper.toDto(testUser)).thenReturn(userReadDto);

        // When
        UserReadDTO result = localAuthService.getInfo(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(userReadDto);

        verify(userMapper).toDto(testUser);
    }

    @Test
    @DisplayName("Authentication sets correct provider")
    void register_SetsCorrectProvider() {
        // Given
        UserEntity capturedUser = new UserEntity();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(userCreateDto)).thenReturn(capturedUser);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertThat(savedUser.getProvider()).isEqualTo(AuthProvider.LOCAL);
            return savedUser;
        });
        when(userMapper.toDto(any(UserEntity.class))).thenReturn(userReadDto);

        // When
        localAuthService.register(userCreateDto);

        // Then
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Authentication password gets encoded")
    void register_PasswordGetsEncoded() {
        // Given
        UserEntity capturedUser = new UserEntity();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(userCreateDto)).thenReturn(capturedUser);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
            return savedUser;
        });
        when(userMapper.toDto(any(UserEntity.class))).thenReturn(userReadDto);

        // When
        localAuthService.register(userCreateDto);

        // Then
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Authenticate creates correct authentication token")
    void authenticate_CreatesCorrectAuthenticationToken() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenAnswer(invocation -> {
                    UsernamePasswordAuthenticationToken token = invocation.getArgument(0);
                    assertThat(token.getName()).isEqualTo("test@example.com");
                    assertThat(token.getCredentials()).isEqualTo("password123");
                    return authentication;
                });
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtService.generateToken(authentication)).thenReturn("token");
        when(userMapper.toDto(testUser)).thenReturn(userReadDto);

        // When
        localAuthService.authenticate(loginRequest);

        // Then
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
