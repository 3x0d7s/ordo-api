package com.kyut.ordo.security;

import com.kyut.ordo.TestConfig;
import com.kyut.ordo.security.jwt.JwtService;
import com.kyut.ordo.feature.user.entity.UserEntity;
import com.kyut.ordo.feature.user.repository.UserRepository;
import com.kyut.ordo.security.auth.provider.AuthProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration тести для Security конфігурації
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Import(TestConfig.class)
@Transactional
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private MockMvc mockMvc;
    private String validJwtToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        // Створюємо користувача без встановлення ID - хай JPA сам призначить
        UserEntity testUser = UserEntity.builder()
                .email("test@example.com")
                .name("Test User")
                .password("$2a$10$test.password.hash")
                .provider(AuthProvider.LOCAL)
                .build();
        testUser = userRepository.save(testUser);

        // Створюємо валідний JWT токен для тестування
        Authentication auth = new UsernamePasswordAuthenticationToken(
            testUser.getEmail(), null, testUser.getAuthorities());
        validJwtToken = jwtService.generateToken(auth);
    }

    @Test
    @DisplayName("Доступ до публічних endpoints без аутентифікації")
    void accessPublicEndpoints() throws Exception {
        // Swagger UI
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk());

        // API docs
        mockMvc.perform(get("/api-docs"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Доступ до захищених endpoints без токена - 401")
    void accessProtectedEndpoints_WithoutToken() throws Exception {
        mockMvc.perform(get("/boards"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/workspaces"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Доступ до захищених endpoints з валідним токеном")
    void accessProtectedEndpoints_WithValidToken() throws Exception {
        mockMvc.perform(get("/workspaces")
                .header("Authorization", "Bearer " + validJwtToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Доступ з недійсним токеном - 401")
    void accessProtectedEndpoints_WithInvalidToken() throws Exception {
        mockMvc.perform(get("/workspaces")
                .header("Authorization", "Bearer invalid.jwt.token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Доступ з токеном без Bearer prefix - 401")
    void accessProtectedEndpoints_WithoutBearerPrefix() throws Exception {
        mockMvc.perform(get("/workspaces")
                .header("Authorization", validJwtToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CORS headers присутні в response")
    void corsHeadersPresent() throws Exception {
        mockMvc.perform(get("/workspaces/joined")
                .header("Authorization", "Bearer " + validJwtToken)
                .header("Origin", "http://localhost:5173"))
            .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
