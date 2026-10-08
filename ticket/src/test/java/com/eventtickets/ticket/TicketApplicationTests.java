package com.eventtickets.ticket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;

@SpringBootTest
class TicketApplicationTests {

	@Test
	void contextLoads() {
	}

	@TestConfiguration
	static class TestJdbcConfig {
		@Bean
		public JdbcDialect jdbcDialect() {
			// Forces Spring to bypass the connection check and use this dialect
			return new JdbcPostgresDialect();
		}
	}
}
