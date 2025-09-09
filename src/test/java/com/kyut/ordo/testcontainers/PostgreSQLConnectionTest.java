package com.kyut.ordo.testcontainers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify PostgreSQL connection and basic functionality with Testcontainers
 */
@Import(PostgreSQLTestDataBuilder.class)
@DisplayName("PostgreSQL Connection Tests with Testcontainers")
@Transactional
class PostgreSQLConnectionTest extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("PostgreSQL container is running and accessible")
    void postgresqlContainerIsRunning() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.getJdbcUrl()).isNotNull();
        assertThat(postgres.getUsername()).isEqualTo("test");
        assertThat(postgres.getDatabaseName()).isEqualTo("testdb");
    }

    @Test
    @DisplayName("Can connect to PostgreSQL and execute queries")
    void canConnectAndExecuteQueries() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(5)).isTrue();

            // Test basic SQL query
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT version()");
                assertThat(resultSet.next()).isTrue();
                String version = resultSet.getString(1);
                assertThat(version).contains("PostgreSQL");
            }
        }
    }

    @Test
    @DisplayName("Flyway migrations are applied correctly")
    void flywayMigrationsApplied() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // Check if Flyway history table exists
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'flyway_schema_history'"
                );
                assertThat(resultSet.next()).isTrue();
                int count = resultSet.getInt(1);
                assertThat(count).isEqualTo(1);
            }

            // Check if our tables were created by migrations
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('users', 'boards', 'lists')"
                );
                assertThat(resultSet.next()).isTrue();
                int tableCount = resultSet.getInt(1);
                assertThat(tableCount).isGreaterThan(0);
            }
        }
    }
}
