package org.sentrysoftware.metricshub.extension.snmp.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.snmp.ISnmpConfiguration;
import org.sentrysoftware.metricshub.extension.snmp.ISnmpRequestExecutor;

@ExtendWith(MockitoExtension.class)
public class SnmpTableSourceProcessorTest {

	@Mock
	private ISnmpRequestExecutor snmpRequestExecutor;

	@Mock
	private Function<TelemetryManager, ISnmpConfiguration> configurationRetriever;

	private SnmpTableSourceProcessor snmpTableSourceProcessor;

	@BeforeEach
	public void setUp() {
		snmpTableSourceProcessor = new SnmpTableSourceProcessor(snmpRequestExecutor, configurationRetriever);
	}

	// Test when snmpTableSource is null
	@Test
	public void testProcess_WhenSourceIsNull_ReturnsEmptySourceTable() {
		SnmpTableSource snmpTableSource = null;
		TelemetryManager telemetryManager = mockTelemetryManager();

		SourceTable result = snmpTableSourceProcessor.process(snmpTableSource, "connectorId", telemetryManager);

		assertEquals(SourceTable.empty(), result);
	}

	// Test when snmpConfiguration is null
	@Test
	public void testProcess_WhenConfigurationIsNull_ReturnsEmptySourceTable() {
		SnmpTableSource snmpTableSource = SnmpTableSource
			.builder()
			.oid("test_oid")
			.selectColumns("column1, column2")
			.build();
		TelemetryManager telemetryManager = mockTelemetryManager();
		when(configurationRetriever.apply(telemetryManager)).thenReturn(null);

		SourceTable result = snmpTableSourceProcessor.process(snmpTableSource, "connectorId", telemetryManager);

		assertEquals(SourceTable.empty(), result);
	}

	// Test when the selected columns are empty
	@Test
	public void testProcess_WhenSelectedColumnsAreBlank_ReturnsEmptySourceTable() {
		SnmpTableSource snmpTableSource = SnmpTableSource.builder().oid("test_oid").selectColumns("").build();
		TelemetryManager telemetryManager = mockTelemetryManager();

		SourceTable result = snmpTableSourceProcessor.process(snmpTableSource, "connectorId", telemetryManager);

		assertEquals(SourceTable.empty(), result);
	}

	// Test when the executor throws an exception
	@Test
	public void testProcess_WhenExecutorThrowsException_ReturnsEmptySourceTable() throws Exception {
		SnmpTableSource snmpTableSource = SnmpTableSource
			.builder()
			.oid("test_oid")
			.selectColumns("column1, column2")
			.build();
		TelemetryManager telemetryManager = mockTelemetryManager();
		ISnmpConfiguration snmpConfiguration = mock(ISnmpConfiguration.class);
		when(configurationRetriever.apply(telemetryManager)).thenReturn(snmpConfiguration);

		doThrow(new InterruptedException("Test exception"))
			.when(snmpRequestExecutor)
			.executeSNMPTable("test_oid", new String[] { "column1", "column2" }, snmpConfiguration, true);

		SourceTable result = snmpTableSourceProcessor.process(snmpTableSource, "connectorId", telemetryManager);

		assertEquals(SourceTable.empty(), result);
	}

	// Test when the executor returns a valid result
	@Test
	public void testProcess_WhenExecutorReturnsValidResult_ReturnsSourceTableWithResult() throws Exception {
		SnmpTableSource snmpTableSource = SnmpTableSource
			.builder()
			.oid("test_oid")
			.selectColumns("column1, column2")
			.build();
		TelemetryManager telemetryManager = mockTelemetryManager();
		ISnmpConfiguration snmpConfiguration = mock(ISnmpConfiguration.class);
		when(configurationRetriever.apply(telemetryManager)).thenReturn(snmpConfiguration);
		List<List<String>> expectedResult = Arrays.asList(
			Arrays.asList("value1", "value2"),
			Arrays.asList("value3", "value4")
		);
		when(
			snmpRequestExecutor.executeSNMPTable("test_oid", new String[] { "column1", "column2" }, snmpConfiguration, true)
		)
			.thenReturn(expectedResult);

		SourceTable result = snmpTableSourceProcessor.process(snmpTableSource, "connectorId", telemetryManager);

		assertEquals(expectedResult, result.getTable());
	}

	// Utility method to simulate a TelemetryManager
	private TelemetryManager mockTelemetryManager() {
		TelemetryManager telemetryManager = mock(TelemetryManager.class);
		HostConfiguration hostConfigurationMock = mock(HostConfiguration.class);
		when(telemetryManager.getHostConfiguration()).thenReturn(hostConfigurationMock);
		when(hostConfigurationMock.getHostname()).thenReturn("hostname");
		return telemetryManager;
	}
}
