package com.kyut.ordo.testcontainers;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Configuration class for PostgreSQL container used in tests.
 * This class provides a centralized configuration for all PostgreSQL containers.
 */
public class PostgreSQLContainerConfig {

    /**
     * PostgreSQL version used for testing
     */
    public static final String POSTGRES_VERSION = "postgres:17.5";

    /**
     * Database name for tests
     */
    public static final String TEST_DB_NAME = "testdb";

    /**
     * Username for test database
     */
    public static final String TEST_USERNAME = "test";

    /**
     * Password for test database
     */
    public static final String TEST_PASSWORD = "test";

    /**
     * Creates a pre-configured PostgreSQL container for testing
     * Note: Testcontainers automatically manages container lifecycle, 
     * so manual resource management is not required
     */
    @SuppressWarnings("resource")
    public static PostgreSQLContainer<?> createPostgreSQLContainer() {
        return new PostgreSQLContainer<>(POSTGRES_VERSION)
                .withDatabaseName(TEST_DB_NAME)
                .withUsername(TEST_USERNAME)
                .withPassword(TEST_PASSWORD)
                .withEnv("TZ", "UTC") // Set timezone to UTC to avoid timezone issues
                .withEnv("PGTZ", "UTC") // PostgreSQL specific timezone setting
                .withReuse(true); // Reuse container across test classes for better performance
    }

    /**
     * Creates a PostgreSQL container with custom configuration
     * Note: Testcontainers automatically manages container lifecycle, 
     * so manual resource management is not required
     */
    @SuppressWarnings("resource")
    public static PostgreSQLContainer<?> createPostgreSQLContainer(
            String databaseName, 
            String username, 
            String password) {
        return new PostgreSQLContainer<>(POSTGRES_VERSION)
                .withDatabaseName(databaseName)
                .withUsername(username)
                .withPassword(password)
                .withReuse(true);
    }
}
