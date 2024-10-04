package org.sentrysoftware.metricshub.extension.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.apache.derby.tools.sysinfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SqlCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WbemCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SqlSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class SqlExtensionTest {

	private static final String HOST_NAME = "test-host";
	private static final String CONNECTOR_ID = "connector_id";
	private static final char[] PASSWORD = "password".toCharArray();
	private static final String USERNAME = "user";
	private static final String SQL_QUERY = "SELECT 1";

	@Mock
	private SqlRequestExecutor sqlRequestExecutorMock;

	@InjectMocks
	private SqlExtension sqlExtension;

	private TelemetryManager telemetryManager;

	/**
	 * Creates and returns a TelemetryManager instance with an SQL configuration.
	 *
	 * @return A TelemetryManager instance configured with an SQL configuration.
	 */
	private void initSql() {
		final Monitor hostMonitor = Monitor.builder().type("HOST").isEndpoint(true).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(Map.of("HOST", Map.of(HOST_NAME, hostMonitor)));

		SqlConfiguration sqlConfiguration = SqlConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD)
			.timeout(30L)
			.build();

		final Connector connector = Connector.builder().build();
		final Map<String, Connector> store = Map.of(CONNECTOR_ID, connector);
		final ConnectorStore connectorStore = new ConnectorStore();
		connectorStore.setStore(store);

		telemetryManager =
			TelemetryManager
				.builder()
				.monitors(monitors)
				.hostConfiguration(
					HostConfiguration
						.builder()
						.hostname(HOST_NAME)
						.configurations(Map.of(SqlConfiguration.class, sqlConfiguration))
						.build()
				)
				.connectorStore(connectorStore)
				.strategyTime(System.currentTimeMillis())
				.build();
	}

	@Test
	void testCheckSqlUpHealth() throws Exception {
		initSql();

		doReturn(List.of(List.of("success")))
			.when(sqlRequestExecutorMock)
			.executeSql(anyString(), any(SqlConfiguration.class), eq(SQL_QUERY), anyBoolean());

		// Start the SQL protocol check
		Optional<Boolean> result = sqlExtension.checkProtocol(telemetryManager);

		assertTrue(result.get());
	}

	@Test
	void testCheckSqlDownHealth() throws Exception {
		initSql();

		doReturn(null)
			.when(sqlRequestExecutorMock)
			.executeSql(anyString(), any(SqlConfiguration.class), eq(SQL_QUERY), anyBoolean());

		// Start the SQL protocol check
		Optional<Boolean> result = sqlExtension.checkProtocol(telemetryManager);

		assertFalse(result.get());
	}

	@Test
	void testIsValidConfiguration() {
		assertTrue(sqlExtension.isValidConfiguration(SqlConfiguration.builder().build()));

		assertFalse(
			sqlExtension.isValidConfiguration(
				new IConfiguration() {
					@Override
					public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {}

					@Override
					public String getHostname() {
						return null;
					}

					@Override
					public void setHostname(String hostname) {}

					@Override
					public IConfiguration copy() {
						return null;
					}
				}
			)
		);
	}

	@Test
	void testGetSupportedSources() {
		assertEquals(Set.of(SqlSource.class), sqlExtension.getSupportedSources());
	}

	@Test
	void testGetSupportedCriteria() {
		assertEquals(Set.of(SqlCriterion.class), sqlExtension.getSupportedCriteria());
	}

	@Test
	void testSqlCriterionReturnsSuccessWithResults() throws Exception {
		initSql();
		final SqlCriterion sqlCriterion = SqlCriterion.builder().query(SQL_QUERY).build();

		doReturn(List.of(List.of("1")))
			.when(sqlRequestExecutorMock)
			.executeSql(anyString(), any(SqlConfiguration.class), anyString(), anyBoolean());

		CriterionTestResult result = sqlExtension.processCriterion(sqlCriterion, CONNECTOR_ID, telemetryManager);

		assertTrue(result.isSuccess());
	}

	@Test
	void testSqlCriterionFailureNoResults() throws Exception {
		initSql();
		final SqlCriterion sqlCriterion = SqlCriterion.builder().query(SQL_QUERY).build();

		doReturn(List.of())
			.when(sqlRequestExecutorMock)
			.executeSql(anyString(), any(SqlConfiguration.class), anyString(), anyBoolean());

		CriterionTestResult result = sqlExtension.processCriterion(sqlCriterion, CONNECTOR_ID, telemetryManager);

		assertFalse(result.isSuccess());
		assertEquals("No results returned by the query.", result.getResult());
	}

	@Test
	void testSqlCriterionFailureWithException() throws Exception {
		initSql();
		final SqlCriterion sqlCriterion = SqlCriterion.builder().query(SQL_QUERY).build();

		doThrow(new ClientException("SQL query failed"))
			.when(sqlRequestExecutorMock)
			.executeSql(anyString(), any(SqlConfiguration.class), anyString(), anyBoolean());

		CriterionTestResult result = sqlExtension.processCriterion(sqlCriterion, CONNECTOR_ID, telemetryManager);

		assertFalse(result.isSuccess());
	}

	@Test
	void testSqlCriterionFailureWithNullCriterion() {
		initSql();

		SqlCriterion sqlCriterion = null;

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> {
				sqlExtension.processCriterion(sqlCriterion, CONNECTOR_ID, telemetryManager);
			}
		);

		assertEquals("Hostname test-host - Cannot process criterion <null>.", exception.getMessage());
	}

	@Test
	void testSqlCriterionSuccessWithCsvFormatting() throws Exception {
		initSql();
		final SqlCriterion sqlCriterion = SqlCriterion.builder().query(SQL_QUERY).build();

		doReturn(List.of(List.of("value1", "value2")))
			.when(sqlRequestExecutorMock)
			.executeSql(anyString(), any(SqlConfiguration.class), anyString(), anyBoolean());

		CriterionTestResult result = sqlExtension.processCriterion(sqlCriterion, CONNECTOR_ID, telemetryManager);

		assertTrue(result.isSuccess());
		assertEquals("value1;value2;", result.getResult().trim());
	}

	@Test
	void testProcessSourceThrowsIllegalArgumentException() {
		initSql();
		final WbemSource wbemSource = WbemSource.builder().query("SELECT Name FROM testDb").build();
		assertThrows(
			IllegalArgumentException.class,
			() -> sqlExtension.processSource(wbemSource, CONNECTOR_ID, telemetryManager)
		);
	}

	@Test
	void testProcessCriterionThrowsIllegalArgumentException() {
		initSql();

		final WbemCriterion wbemCriterion = WbemCriterion.builder().query("SELECT Name FROM testDB").build();

		assertThrows(
			IllegalArgumentException.class,
			() -> {
				sqlExtension.processCriterion(wbemCriterion, CONNECTOR_ID, telemetryManager);
			}
		);
	}

	@Test
	void testBuildConfigurationWithDecrypt() throws InvalidConfigurationException {
		final ObjectNode configuration = JsonNodeFactory.instance.objectNode();
		configuration.set("password", new TextNode("testPassword"));
		configuration.set("url", new TextNode("jdbc:mysql://localhost:3306/dbname"));
		configuration.set("timeout", new TextNode("2m"));
		configuration.set("username", new TextNode("testUser"));
		configuration.set("port", new IntNode(161));

		assertEquals(
			SqlConfiguration
				.builder()
				.password("testPassword".toCharArray())
				.url("jdbc:mysql://localhost:3306/dbname".toCharArray())
				.timeout(120L)
				.port(161)
				.username("testUser")
				.build(),
			sqlExtension.buildConfiguration("sql", configuration, value -> value)
		);
		assertEquals(
			SqlConfiguration
				.builder()
				.password("testPassword".toCharArray())
				.url("jdbc:mysql://localhost:3306/dbname".toCharArray())
				.timeout(120L)
				.port(161)
				.username("testUser")
				.build(),
			sqlExtension.buildConfiguration("sql", configuration, null)
		);
		assertEquals(
			SqlConfiguration
				.builder()
				.password("testPassword".toCharArray())
				.url("jdbc:mysql://localhost:3306/dbname".toCharArray())
				.timeout(120L)
				.port(161)
				.username("testUser")
				.build(),
			sqlExtension.buildConfiguration("sql", configuration, null)
		);
	}

	@Test
	void testGetIdentifier() {
		assertEquals("sql", sqlExtension.getIdentifier());
	}

	@Test
	void testProcessSourceWithNullSource() {
		initSql();

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> sqlExtension.processSource(null, CONNECTOR_ID, telemetryManager)
		);

		assertEquals("Hostname test-host - Cannot process source <null>.", exception.getMessage());
	}

	@Test
	void testIsSupportedConfigurationTypeForUnsupportedType() {
		assertFalse(sqlExtension.isSupportedConfigurationType("unsupported_type"));
	}
}
