package com.kyut.ordo.testcontainers;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests that use PostgreSQL with Testcontainers.
 * Uses a JVM-wide singleton container to avoid restarts and port changes between test classes.
 */
@SpringBootTest
@ActiveProfiles("testcontainers")
public abstract class AbstractPostgreSQLIntegrationTest {

    protected static final PostgreSQLContainer<?> postgres = PostgreSQLContainerConfig.createPostgreSQLContainer();

    static {
        // Start container once for the whole JVM. Do NOT stop it manually.
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
