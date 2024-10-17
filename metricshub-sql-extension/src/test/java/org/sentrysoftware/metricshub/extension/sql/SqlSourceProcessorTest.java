package org.sentrysoftware.metricshub.extension.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SqlSource;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class SqlSourceProcessorTest {

	@Mock
	private SqlRequestExecutor sqlRequestExecutor;

	private SqlSourceProcessor sqlSourceProcessor;

	@Mock
	private SqlConfiguration sqlConfiguration;

	@BeforeEach
	void setUp() {
		sqlSourceProcessor = new SqlSourceProcessor(sqlRequestExecutor, "connectorId");
	}

	// Test case when SqlSource is null
	@Test
	void testProcess_WhenSourceIsNull_ReturnsEmptySourceTable() {
		SqlSource sqlSource = null;
		TelemetryManager telemetryManager = createTelemetryManagerWithHostConfiguration();

		SourceTable result = sqlSourceProcessor.process(sqlSource, telemetryManager);
		assertEquals(SourceTable.empty(), result);
	}

	// Test case when sqlConfiguration is null
	@Test
	void testProcessWhenConfigurationIsNullReturnsEmptySourceTable() {
		Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		configurations.put(SqlConfiguration.class, null);
		SqlSource sqlSource = SqlSource.builder().query("SELECT * FROM test_table").build();

		TelemetryManager telemetryManager = createTelemetryManagerWithHostConfiguration();
		SourceTable result = sqlSourceProcessor.process(sqlSource, telemetryManager);
		assertEquals(SourceTable.empty(), result);
	}

	// Test case when the requestExecutor throws an exception
	@Test
	void testProcessWhenRequestExecutorThrowsExceptionReturnsEmptySourceTable() throws Exception {
		SqlSource sqlSource = SqlSource.builder().query("SELECT * FROM test_table").build();
		TelemetryManager telemetryManager = createTelemetryManagerWithHostConfiguration();

		doThrow(new RuntimeException("SQL execution error"))
			.when(sqlRequestExecutor)
			.executeSql("hostname", sqlConfiguration, "SELECT * FROM test_table", false);

		SourceTable result = sqlSourceProcessor.process(sqlSource, telemetryManager);
		assertEquals(SourceTable.empty(), result);
	}

	// Test case when requestExecutor returns a valid result
	@Test
	void testProcessWhenReturnsSourceTableWithResult() throws Exception {
		SqlSource sqlSource = SqlSource.builder().query("SELECT * FROM test_table").build();
		TelemetryManager telemetryManager = createTelemetryManagerWithHostConfiguration();

		List<List<String>> ExpectedResults = List.of(List.of("row1_col1", "row1_col2"));
		when(sqlRequestExecutor.executeSql("hostname", sqlConfiguration, "SELECT * FROM test_table", false))
			.thenReturn(ExpectedResults);
		when(sqlConfiguration.getHostname()).thenReturn("hostname");
		SourceTable result = sqlSourceProcessor.process(sqlSource, telemetryManager);

		assertNotNull(result);
		assertTrue(result.getTable().size() == 1);
		assertEquals("row1_col1", result.getTable().get(0).get(0));
		assertEquals("row1_col2", result.getTable().get(0).get(1));
	}

	/**
	 * Utility method to create a telemetryManager
	 *
	 * @return a configured telemetryManager instance
	 */
	private TelemetryManager createTelemetryManagerWithHostConfiguration() {
		HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname("hostname")
			.configurations(Map.of(SqlConfiguration.class, sqlConfiguration))
			.build();
		return TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
	}
}
