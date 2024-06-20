package org.sentrysoftware.metricshub.extension.snmp.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.snmp.ISnmpConfiguration;
import org.sentrysoftware.metricshub.extension.snmp.ISnmpRequestExecutor;

@ExtendWith(MockitoExtension.class)
public class SnmpGetSourceProcessorTest {

	@Mock
	private ISnmpRequestExecutor snmpRequestExecutor;

	@Mock
	private Function<TelemetryManager, ISnmpConfiguration> configurationRetriever;

	private SnmpGetSourceProcessor snmpGetSourceProcessor;

	@BeforeEach
	public void setUp() {
		snmpGetSourceProcessor = new SnmpGetSourceProcessor(snmpRequestExecutor, configurationRetriever);
	}

	// Test case when snmpGetSource is null.
	@Test
	public void testProcess_WhenSourceIsNull_ReturnsEmptySourceTable() {
		SnmpGetSource snmpGetSource = null;
		TelemetryManager telemetryManager = mockTelemetryManager();

		SourceTable result = snmpGetSourceProcessor.process(snmpGetSource, "connectorId", telemetryManager);
		assertEquals(SourceTable.empty(), result);
	}

	// Test case for when snmpconfiguration is null."
	@Test
	public void testProcess_WhenConfigurationIsNull_ReturnsEmptySourceTable() {
		SnmpGetSource snmpGetSource = SnmpGetSource.builder().oid("test_oid").build();
		TelemetryManager telemetryManager = mockTelemetryManager();
		when(configurationRetriever.apply(telemetryManager)).thenReturn(null);

		SourceTable result = snmpGetSourceProcessor.process(snmpGetSource, "connectorId", telemetryManager);
		assertEquals(SourceTable.empty(), result);
	}

	// Test case when the requestExecutor throws an exception.
	@Test
	public void testProcess_WhenRequestExecutorThrowsException_ReturnsEmptySourceTable() throws Exception {
		SnmpGetSource snmpGetSource = SnmpGetSource.builder().oid("test_oid").build();
		TelemetryManager telemetryManager = mockTelemetryManager();
		ISnmpConfiguration snmpConfiguration = mock(ISnmpConfiguration.class);
		when(configurationRetriever.apply(telemetryManager)).thenReturn(snmpConfiguration);
		when(snmpConfiguration.getHostname()).thenReturn("hostname");
		doThrow(new InterruptedException("Test exception"))
			.when(snmpRequestExecutor)
			.executeSNMPGet("test_oid", snmpConfiguration, "hostname", true);

		SourceTable result = snmpGetSourceProcessor.process(snmpGetSource, "connectorId", telemetryManager);
		assertEquals(SourceTable.empty(), result);
	}

	//Test case when requestExecutor returns a valid result.
	@Test
	public void testProcess_WhenRequestExecutorReturnsValidResult_ReturnsSourceTableWithResult() throws Exception {
		SnmpGetSource snmpGetSource = SnmpGetSource.builder().oid("test_oid").build();
		TelemetryManager telemetryManager = mockTelemetryManager();
		ISnmpConfiguration snmpConfiguration = mock(ISnmpConfiguration.class);
		when(configurationRetriever.apply(telemetryManager)).thenReturn(snmpConfiguration);
		when(snmpConfiguration.getHostname()).thenReturn("hostname");
		when(snmpRequestExecutor.executeSNMPGet("test_oid", snmpConfiguration, "hostname", true)).thenReturn("result");

		SourceTable result = snmpGetSourceProcessor.process(snmpGetSource, "connectorId", telemetryManager);

		assertNotNull(result);
		assertTrue(result.getTable().size() == 1);
		assertEquals("result", result.getTable().get(0).get(0));
	}

	//Utility method to create a simulated TelemetryManager
	private TelemetryManager mockTelemetryManager() {
		TelemetryManager telemetryManager = mock(TelemetryManager.class);
		HostConfiguration hostConfigurationMock = mock(HostConfiguration.class);
		when(telemetryManager.getHostConfiguration()).thenReturn(hostConfigurationMock);
		return telemetryManager;
	}
}
