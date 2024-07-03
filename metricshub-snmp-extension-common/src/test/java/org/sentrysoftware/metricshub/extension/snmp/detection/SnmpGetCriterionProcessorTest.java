package org.sentrysoftware.metricshub.extension.snmp.detection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetCriterion;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.snmp.ISnmpConfiguration;
import org.sentrysoftware.metricshub.extension.snmp.ISnmpRequestExecutor;

@ExtendWith(MockitoExtension.class)
public class SnmpGetCriterionProcessorTest {

	@Mock
	private ISnmpRequestExecutor snmpRequestExecutor;

	@Mock
	private Function<TelemetryManager, ISnmpConfiguration> configurationRetriever;

	private SnmpGetCriterionProcessor snmpGetCriterionProcessor;

	@BeforeEach
	public void setUp() {
		snmpGetCriterionProcessor = new SnmpGetCriterionProcessor(snmpRequestExecutor, configurationRetriever);
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
			.configurations(Map.of())
			.build();
		TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		return telemetryManager;
	}

	// Test case for successful process
	@Test
	public void testProcess_Success() throws Exception {
		TelemetryManager telemetryManager = createTelemetryManagerWithHostConfiguration();

		ISnmpConfiguration snmpConfiguration = mock(ISnmpConfiguration.class);
		when(configurationRetriever.apply(any(TelemetryManager.class))).thenReturn(snmpConfiguration);

		String expectedOid = "1.3.6.1.2.1.1.1.0";
		String expectedResult = "TestValue";
		String expectedHostname = "hostname";

		when(snmpRequestExecutor.executeSNMPGet(expectedOid, snmpConfiguration, expectedHostname, false))
			.thenReturn(expectedResult);

		SnmpGetCriterion snmpGetCriterion = SnmpGetCriterion
			.builder()
			.oid(expectedOid)
			.expectedResult(expectedResult)
			.build();

		CriterionTestResult criterionTestResult = snmpGetCriterionProcessor.process(
			snmpGetCriterion,
			"connectorId",
			telemetryManager
		);

		assertNotNull(snmpConfiguration);
		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
	}

	// Test case when snmpGetCriterion is null
	@Test
	public void testProcess_NullCriterion() {
		TelemetryManager telemetryManager = createTelemetryManagerWithHostConfiguration();
		CriterionTestResult result = snmpGetCriterionProcessor.process(null, "connectorId", telemetryManager);
		assertFalse(result.isSuccess());
	}

	// Test case when snmpConfiguration is null
	@Test
	public void testProcess_NullSnmpConfiguration() {
		TelemetryManager telemetryManager = createTelemetryManagerWithHostConfiguration();

		when(configurationRetriever.apply(any(TelemetryManager.class))).thenReturn(null);

		SnmpGetCriterion snmpGetCriterion = SnmpGetCriterion.builder().oid("1.3.6.1.2.1.1.1.0").build();

		CriterionTestResult result = snmpGetCriterionProcessor.process(snmpGetCriterion, "connectorId", telemetryManager);

		assertFalse(result.isSuccess());
	}

	// Test case when the requestExecutor throws an exception.
	@Test
	public void testProcess_SnmpRequestException() throws Exception {
		TelemetryManager telemetryManager = createTelemetryManagerWithHostConfiguration();

		ISnmpConfiguration snmpConfiguration = mock(ISnmpConfiguration.class);
		when(configurationRetriever.apply(telemetryManager)).thenReturn(snmpConfiguration);

		when(
			snmpRequestExecutor.executeSNMPGet(
				any(String.class),
				any(ISnmpConfiguration.class),
				any(String.class),
				any(Boolean.class)
			)
		)
			.thenThrow(new InterruptedException("Test exception"));

		SnmpGetCriterion snmpGetCriterion = SnmpGetCriterion.builder().oid("1.3.6.1.2.1.1.1.0").build();

		CriterionTestResult result = snmpGetCriterionProcessor.process(snmpGetCriterion, "connectorId", telemetryManager);

		assertFalse(result.isSuccess());
	}
}
