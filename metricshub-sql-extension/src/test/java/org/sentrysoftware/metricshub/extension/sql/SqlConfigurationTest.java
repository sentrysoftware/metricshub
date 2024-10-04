package org.sentrysoftware.metricshub.extension.sql;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;

public class SqlConfigurationTest {

	@Test
	public void testGenerateUrl() {
		SqlConfiguration sqlConfiguration = SqlConfiguration
			.builder()
			.type("mysql")
			.hostname("localhost")
			.database("testdb")
			.port(3306)
			.build();

		char[] expectedUrl = "jdbc:mysql://localhost:3306/testdb".toCharArray();
		assertArrayEquals(expectedUrl, sqlConfiguration.generateUrl());
	}

	@Test
	public void testValidateConfigurationMissingUsername() {
		SqlConfiguration sqlConfiguration = SqlConfiguration
			.builder()
			.type("mysql")
			.database("testdb")
			.hostname("localhost")
			.build();

		Exception exception = assertThrows(
			InvalidConfigurationException.class,
			() -> sqlConfiguration.validateConfiguration("resourceKey")
		);

		assertEquals(
			"Resource resourceKey - No username configured for SQL. " + "This resource will not be monitored.",
			exception.getMessage()
		);
	}

	@Test
	public void testValidateConfigurationInvalidTimeout() {
		SqlConfiguration sqlConfiguration = SqlConfiguration
			.builder()
			.username("testUser")
			.type("mysql")
			.database("testdb")
			.hostname("localhost")
			.timeout(-10L)
			.build();

		Exception exception = assertThrows(
			InvalidConfigurationException.class,
			() -> sqlConfiguration.validateConfiguration("resourceKey")
		);

		assertEquals(
			"Resource resourceKey - Timeout value is invalid for protocol SQL. " +
			"Timeout value returned: -10. This resource will not be monitored. " +
			"Please verify the configured timeout value.",
			exception.getMessage()
		);
	}

	@Test
	public void testValidateConfigurationInvalidPort() {
		SqlConfiguration sqlConfiguration = SqlConfiguration
			.builder()
			.username("testUser")
			.type("mysql")
			.database("testdb")
			.hostname("localhost")
			.port(-1)
			.build();

		Exception exception = assertThrows(
			InvalidConfigurationException.class,
			() -> sqlConfiguration.validateConfiguration("resourceKey")
		);

		assertEquals(
			"Resource resourceKey - Invalid port configured for protocol SQL. " +
			"Port value returned: -1. This resource will not be monitored. " +
			"Please verify the configured port value.",
			exception.getMessage()
		);
	}

	@Test
	public void testValidateConfigurationGeneratesUrlWhenNull() {
		SqlConfiguration sqlConfiguration = SqlConfiguration
			.builder()
			.username("testUser")
			.type("mysql")
			.database("testdb")
			.hostname("localhost")
			.port(3306)
			.url(null)
			.build();

		assertDoesNotThrow(() -> sqlConfiguration.validateConfiguration("resourceKey"));
		assertNotNull(sqlConfiguration.getUrl());
		assertEquals("jdbc:mysql://localhost:3306/testdb", new String(sqlConfiguration.getUrl()));
	}

	@Test
	public void testValidateConfigurationWithEmptyUrl() {
		SqlConfiguration sqlConfiguration = SqlConfiguration
			.builder()
			.username("testUser")
			.type("unsupported_db")
			.database("testdb")
			.hostname("localhost")
			.port(3306)
			.build();

		Exception exception = assertThrows(
			InvalidConfigurationException.class,
			() -> sqlConfiguration.validateConfiguration("resourceKey")
		);

		assertEquals(
			"Resource resourceKey - Invalid URL generated for protocol SQL. " +
			"URL value returned: . This resource will not be monitored.",
			exception.getMessage()
		);
	}

	@Test
	public void testValidateConfigurationWithPredefinedUrl() {
		SqlConfiguration sqlConfiguration = SqlConfiguration
			.builder()
			.username("testUser")
			.type("mysql")
			.database("testdb")
			.hostname("localhost")
			.port(3306)
			.url("jdbc:mysql://custom-host:3306/customdb".toCharArray())
			.build();

		assertDoesNotThrow(() -> sqlConfiguration.validateConfiguration("resourceKey"));
		assertEquals("jdbc:mysql://custom-host:3306/customdb", new String(sqlConfiguration.getUrl()));
	}

	@Test
	public void testValidateConfigurationGeneratesUrlWhenEmpty() {
		SqlConfiguration sqlConfiguration = SqlConfiguration
			.builder()
			.username("testUser")
			.type("mysql")
			.database("testdb")
			.hostname("localhost")
			.port(3306)
			.url(new char[0]) // URL is empty
			.build();

		assertDoesNotThrow(() -> sqlConfiguration.validateConfiguration("resourceKey"));
		assertNotNull(sqlConfiguration.getUrl());
		assertEquals("jdbc:mysql://localhost:3306/testdb", new String(sqlConfiguration.getUrl()));
	}

	@Test
	public void testValidateConfigurationAssignsDefaultPort() {
		SqlConfiguration sqlConfiguration = SqlConfiguration
			.builder()
			.username("testUser")
			.type("mysql")
			.database("testdb")
			.hostname("localhost")
			.port(null)
			.build();

		assertDoesNotThrow(() -> sqlConfiguration.validateConfiguration("resourceKey"));
		assertEquals(3306, sqlConfiguration.getPort());
	}

	@Test
	public void testCopyConfigurationWithAllFields() {
		SqlConfiguration sqlConfiguration = SqlConfiguration
			.builder()
			.username("testUser")
			.password("password".toCharArray())
			.type("mysql")
			.database("testdb")
			.port(3306)
			.url("jdbc:mysql://localhost:3306/testdb".toCharArray())
			.timeout(200L)
			.build();
		final IConfiguration sqlConfigurationCopy = sqlConfiguration.copy();

		// Verify that the copied configuration has the same values as the original configuration
		assertEquals(sqlConfiguration, sqlConfigurationCopy);

		// Ensure that the copied configuration is a distinct object
		assert (sqlConfiguration != sqlConfigurationCopy);
	}
}
