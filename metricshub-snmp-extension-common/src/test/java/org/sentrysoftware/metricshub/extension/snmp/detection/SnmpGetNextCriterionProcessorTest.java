package org.sentrysoftware.metricshub.extension.snmp.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Map;
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
		when(configurationRetriever.apply(telemetryManager)).thenReturn(snmpConfiguration);

		String expectedOid = "1.3.6.1.2.1.1.1.0";
		String expectedResult = "1.3.6.1.2.1.1.1.0 OID_TYPE TestValue";
		String expectedHostname = "hostname";

		when(snmpRequestExecutor.executeSNMPGetNext(expectedOid, snmpConfiguration, expectedHostname, false))
			.thenReturn(expectedResult);

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
		TelemetryManager telemetryManager = createTelemetryManagerWithHostConfiguration();
		CriterionTestResult result = snmpGetNextCriterionProcessor.process(null, "connectorId", telemetryManager);
		assertFalse(result.isSuccess());
	}

	// Test case when snmpConfiguration is null
	@Test
	public void testProcess_NullSnmpConfiguration() {
		TelemetryManager telemetryManager = createTelemetryManagerWithHostConfiguration();

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
		TelemetryManager telemetryManager = createTelemetryManagerWithHostConfiguration();

		ISnmpConfiguration snmpConfiguration = mock(ISnmpConfiguration.class);
		when(configurationRetriever.apply(telemetryManager)).thenReturn(snmpConfiguration);

		when(
			snmpRequestExecutor.executeSNMPGetNext(
				any(String.class),
				any(ISnmpConfiguration.class),
				any(String.class),
				any(Boolean.class)
			)
		)
			.thenThrow((new RuntimeException("Test exception")));

		SnmpGetNextCriterion snmpGetNextCriterion = SnmpGetNextCriterion.builder().oid("1.3.6.1.2.1.1.1.0").build();

		CriterionTestResult result = snmpGetNextCriterionProcessor.process(
			snmpGetNextCriterion,
			"connectorId",
			telemetryManager
		);

		assertFalse(result.isSuccess());
	}

	@Test
	void testCheckSNMPGetNextExpectedValue_NullResult() throws Exception {
		String hostname = "hostname";
		String oid = "1.3.6.1.2.1.1.1.0";
		String expectedResult = "expectedResult";
		String result = null;

		Method method =
			SnmpGetNextCriterionProcessor.class.getDeclaredMethod(
					"checkSNMPGetNextExpectedValue",
					String.class,
					String.class,
					String.class,
					String.class
				);
		method.setAccessible(true);
		CriterionTestResult criterionTestResult = (CriterionTestResult) method.invoke(
			null,
			hostname,
			oid,
			expectedResult,
			result
		);

		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
			"Hostname " +
			hostname +
			" - SNMP test failed - SNMP GetNext of " +
			oid +
			" was unsuccessful due to a null result.",
			criterionTestResult.getMessage()
		);
	}

	@Test
	void testCheckSNMPGetNextExpectedValue_EmptyResult() throws Exception {
		String hostname = "hostname";
		String oid = "1.3.6.1.2.1.1.1.0";
		String expectedResult = "expectedResult";
		String result = "";

		Method method =
			SnmpGetNextCriterionProcessor.class.getDeclaredMethod(
					"checkSNMPGetNextExpectedValue",
					String.class,
					String.class,
					String.class,
					String.class
				);
		method.setAccessible(true);
		CriterionTestResult criterionTestResult = (CriterionTestResult) method.invoke(
			null,
			hostname,
			oid,
			expectedResult,
			result
		);

		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
			"Hostname " +
			hostname +
			" - SNMP test failed - SNMP GetNext of " +
			oid +
			" was unsuccessful due to an empty result.",
			criterionTestResult.getMessage()
		);
	}
}
