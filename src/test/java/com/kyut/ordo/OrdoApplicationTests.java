package com.kyut.ordo;

import com.kyut.ordo.testcontainers.AbstractPostgreSQLIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Application Context Tests with PostgreSQL")
class OrdoApplicationTests extends AbstractPostgreSQLIntegrationTest {

	@Test
	@DisplayName("Spring Boot context loads successfully with PostgreSQL")
	void contextLoads() {
		// This test verifies that the application context can be loaded
		// with PostgreSQL running in a Testcontainer
	}

}
