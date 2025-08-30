package com.kyut.ordo;

import com.kyut.ordo.auth.provider.common.AuthProvider;
import com.kyut.ordo.user.entity.UserEntity;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Тестова конфігурація для створення test beans та utilities
 */
@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Утиліта для створення тестових користувачів
     */
    public static class TestDataFactory {
        
        public static UserEntity createTestUser(String email, String name) {
            return UserEntity.builder()
                .id(1L)
                .email(email)
                .name(name)
                .password("$2a$10$test.password.hash")
                .provider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        
        public static UserEntity createTestUserWithId(Long id, String email, String name) {
            return UserEntity.builder()
                .id(id)
                .email(email)
                .name(name)
                .password("$2a$10$test.password.hash")
                .provider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
    }
}
