package org.sentrysoftware.metricshub.extension.snmp.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetNextCriterion;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.snmp.ISnmpConfiguration;
import org.sentrysoftware.metricshub.extension.snmp.ISnmpRequestExecutor;

@ExtendWith(MockitoExtension.class)
public class SnmpGetNextCriterionProcessorTest {

	@Mock
	private ISnmpRequestExecutor snmpRequestExecutor;

	@Mock
	private Function<TelemetryManager, ISnmpConfiguration> configurationRetriever;

	private SnmpGetNextCriterionProcessor snmpGetNextCriterionProcessor;

	@BeforeEach
	public void setUp() {
		snmpGetNextCriterionProcessor = new SnmpGetNextCriterionProcessor(snmpRequestExecutor, configurationRetriever);
	}

	private TelemetryManager createMockTelemetryManagerWithHostConfiguration() {
		TelemetryManager telemetryManager = mock(TelemetryManager.class);
		HostConfiguration hostConfigurationMock = mock(HostConfiguration.class);
		when(telemetryManager.getHostConfiguration()).thenReturn(hostConfigurationMock);
		when(hostConfigurationMock.getHostname()).thenReturn("hostname");
		return telemetryManager;
	}

	// Test case for successful process
	@Test
	public void testProcess_Success() throws Exception {
		TelemetryManager telemetryManager = createMockTelemetryManagerWithHostConfiguration();

		ISnmpConfiguration snmpConfiguration = mock(ISnmpConfiguration.class);
		when(configurationRetriever.apply(telemetryManager)).thenReturn(snmpConfiguration);

		String expectedOid = "1.3.6.1.2.1.1.1.0";
		String expectedResult = "1.3.6.1.2.1.1.1.0 OID_TYPE TestValue";

		when(snmpRequestExecutor.executeSNMPGetNext(expectedOid, snmpConfiguration, false)).thenReturn(expectedResult);

		SnmpGetNextCriterion snmpGetNextCriterion = SnmpGetNextCriterion
			.builder()
			.oid(expectedOid)
			.expectedResult("TestValue")
			.build();

		CriterionTestResult criterionTestResult = snmpGetNextCriterionProcessor.process(
			snmpGetNextCriterion,
			"connectorId",
			telemetryManager
		);

		assertNotNull(snmpConfiguration);
		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(expectedResult, criterionTestResult.getResult());
	}

	// Test case when snmpGetCriterion is null
	@Test
	public void testProcess_NullCriterion() {
		TelemetryManager telemetryManager = createMockTelemetryManagerWithHostConfiguration();
		CriterionTestResult result = snmpGetNextCriterionProcessor.process(null, "connectorId", telemetryManager);
		assertFalse(result.isSuccess());
	}

	// Test case when snmpConfiguration is null
	@Test
	public void testProcess_NullSnmpConfiguration() {
		TelemetryManager telemetryManager = createMockTelemetryManagerWithHostConfiguration();

		when(configurationRetriever.apply(any(TelemetryManager.class))).thenReturn(null);

		SnmpGetNextCriterion snmpGetNextCriterion = SnmpGetNextCriterion.builder().oid("1.3.6.1.2.1.1.1.0").build();

		CriterionTestResult result = snmpGetNextCriterionProcessor.process(
			snmpGetNextCriterion,
			"connectorId",
			telemetryManager
		);

		assertFalse(result.isSuccess());
	}

	// Test case when the requestExecutor throws an exception.
	@Test
	public void testProcess_SnmpRequestException() throws Exception {
		TelemetryManager telemetryManager = createMockTelemetryManagerWithHostConfiguration();

		ISnmpConfiguration snmpConfiguration = mock(ISnmpConfiguration.class);
		when(configurationRetriever.apply(telemetryManager)).thenReturn(snmpConfiguration);

		when(snmpRequestExecutor.executeSNMPGetNext(any(String.class), any(ISnmpConfiguration.class), any(Boolean.class)))
			.thenThrow((new RuntimeException("Test exception")));

		SnmpGetNextCriterion snmpGetNextCriterion = SnmpGetNextCriterion.builder().oid("1.3.6.1.2.1.1.1.0").build();

		CriterionTestResult result = snmpGetNextCriterionProcessor.process(
			snmpGetNextCriterion,
			"connectorId",
			telemetryManager
		);

		assertFalse(result.isSuccess());
	}
}
