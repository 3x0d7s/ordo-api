# Testing with Testcontainers PostgreSQL

This document explains how to run integration tests using PostgreSQL with Testcontainers in the Ordo project.

## Overview

The project now supports two types of integration testing:

1. **H2 Integration Tests** (faster, profile: `test`) - Uses in-memory H2 database
2. **PostgreSQL Integration Tests** (more realistic, profile: `testcontainers`) - Uses PostgreSQL in Docker container

## Prerequisites

- Docker must be installed and running on your system
- Java 21 or higher
- Maven 3.6+ 

## Running Tests

### Run all tests with PostgreSQL Testcontainers
```bash
mvn test -Dspring.profiles.active=testcontainers
```

### Run all tests with H2 (default)
```bash
mvn test
```

### Run specific test class with PostgreSQL
```bash
mvn test -Dtest=ListRepositoryTest -Dspring.profiles.active=testcontainers
```

### Run only unit tests (skip integration tests)
```bash
mvn test -Dtest="*Test" -Dspring.profiles.active=testcontainers
```

## Test Structure

### Unit Tests
- Use Mockito for mocking dependencies
- No database connection required
- Examples: `ListServiceTest`, `ListEventListenerTest`

### Integration Tests with H2
- Profile: `test`
- Fast execution
- In-memory database
- Configuration: `application-test.yml`

### Integration Tests with PostgreSQL Testcontainers
- Profile: `testcontainers`
- More realistic environment
- Real PostgreSQL database in Docker
- Configuration: `application-testcontainers.yml`
- Base class: `AbstractPostgreSQLIntegrationTest`

## Key Components

### AbstractPostgreSQLIntegrationTest
Base class for all PostgreSQL integration tests. Provides:
- Shared PostgreSQL container (reused across test classes)
- Dynamic configuration of datasource properties
- Automatic container lifecycle management

### PostgreSQLContainerConfiguration
Utility class for configuring PostgreSQL containers:
- Centralized container configuration
- Default settings for test database
- Support for custom configurations

### PostgreSQLTestDataBuilder
Helper class for creating test data:
- Simplified test data creation
- Transaction management
- Data cleanup utilities

### PostgreSQLConnectionTest
Demonstrates basic PostgreSQL connectivity and Flyway migrations

## Configuration Files

### application-testcontainers.yml
- Enables Flyway migrations (tests real migration scenarios)
- PostgreSQL-specific Hibernate dialect
- Enhanced logging for debugging
- OAuth2 test configuration

### application-test.yml
- H2 in-memory database
- Flyway disabled (uses Hibernate schema generation)
- Faster test execution

## Benefits of PostgreSQL Testcontainers

1. **Production-like Environment**: Tests run against the same database type as production
2. **Migration Testing**: Flyway migrations are tested as part of integration tests
3. **SQL Dialect Compatibility**: Ensures PostgreSQL-specific features work correctly
4. **Isolation**: Each test run gets a fresh database instance
5. **CI/CD Ready**: Works in any environment with Docker support

## Performance Considerations

- PostgreSQL tests are slower than H2 tests due to Docker container startup
- Container reuse (`withReuse(true)`) minimizes overhead across test classes
- Use H2 tests for rapid development feedback
- Use PostgreSQL tests for comprehensive validation before deployment

## Troubleshooting

### Docker Issues
- Ensure Docker daemon is running
- Check Docker permissions for your user
- On Linux, ensure user is in `docker` group

### Container Startup Issues
- Check available disk space
- Verify no port conflicts (PostgreSQL default port 5432)
- Check Docker logs: `docker logs <container_id>`

### Test Failures
- Enable debug logging by setting `logging.level.org.testcontainers=DEBUG`
- Check application logs for database connection issues
- Verify Flyway migrations are compatible with test scenarios

## Example Usage

```java
@DisplayName("My Integration Test with PostgreSQL")
class MyIntegrationTest extends AbstractPostgreSQLIntegrationTest {
    
    @Autowired
    private PostgreSQLTestDataBuilder testDataBuilder;
    
    @Test
    void myTest() {
        // Use real PostgreSQL database for testing
        UserEntity user = testDataBuilder.createTestUser("test@example.com", "Test User");
        // ... test logic
    }
}
```

## IDE Configuration

For IntelliJ IDEA or other IDEs, you can set up run configurations with the testcontainers profile:
- VM options: `-Dspring.profiles.active=testcontainers`
- Ensure Docker integration is enabled in your IDE
