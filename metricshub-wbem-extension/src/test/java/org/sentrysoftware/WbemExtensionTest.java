package org.sentrysoftware;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WbemCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.wbem.WbemConfiguration;
import org.sentrysoftware.metricshub.extension.wbem.WbemExtension;
import org.sentrysoftware.metricshub.extension.wbem.WbemRequestExecutor;
import org.sentrysoftware.wbem.client.WbemClient;
import org.sentrysoftware.wmi.exceptions.WmiComException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.DOWN;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.UP;

@ExtendWith(MockitoExtension.class)
public class WbemExtensionTest {
	private static final String CONNECTOR_ID = "connector";
	private static final String HOST_NAME = "test-host" + UUID.randomUUID();
	private static final String SUCCESS_RESPONSE = "Success";
	private static final String BMC_KEY = "0x0102";
	private static final String USERNAME = "testUser";
	private static final String PASSWORD = "testPassword";
	private static final String WBEM_CRITERION_TYPE = "wbem";
	private static final String RESULT = "result";
	private static final String WBEM_TEST_NAMESPACE = "namespace";

	@Mock
	private WbemRequestExecutor wbemRequestExecutorMock;

	@InjectMocks
	private WbemExtension wbemExtension;

	private TelemetryManager telemetryManager;

	private void initWbem() {
		final Monitor hostMonitor = Monitor.builder().type(HOST.getKey()).isEndpoint(true).build();

		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
				Map.of(HOST.getKey(), Map.of(HOST_NAME, hostMonitor))
		);

		final WbemConfiguration wbemConfiguration = WbemConfiguration
				.builder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.timeout(120L)
				.build();

		telemetryManager =
				TelemetryManager
						.builder()
						.monitors(monitors)
						.hostConfiguration(
								HostConfiguration
										.builder()
										.hostname(HOST_NAME)
										.hostId(HOST_NAME)
										.hostType(DeviceKind.OOB)
										.configurations(Map.of(WbemConfiguration.class, wbemConfiguration))
										.build()
						)
						.build();
	}

	@Test
	void testCheckProtocolUp() throws ClientException {
		// Create a telemetry manager using a WMI HostConfiguration
		initWbem();

		{
			// Mock a positive WMI protocol health check response
			doReturn(SUCCESS_RESPONSE)
					.when(wbemRequestExecutorMock)
					.executeWbem(
							anyString(),
							any(WbemConfiguration.class),
							eq(WbemExtension.WBEM_TEST_QUERY),
							eq(WBEM_TEST_NAMESPACE),
							telemetryManager
					);

			// Start the WMI Health Check strategy
			wbemExtension.checkProtocol(telemetryManager);

			assertEquals(
					WbemExtension.UP,
					telemetryManager.getEndpointHostMonitor().getMetric(WbemExtension.WBEM_UP_METRIC).getValue()
			);
		}

		{
			// Mock an acceptable WMI protocol health check exception
			doThrow(new RuntimeException(new WmiComException("WBEM_E_INVALID_NAMESPACE")))
					.when(wbemRequestExecutorMock)
					.executeWbem(
							anyString(),
							any(WbemConfiguration.class),
							eq(WbemExtension.WBEM_TEST_QUERY),
							eq(WBEM_TEST_NAMESPACE),
							telemetryManager
					);

			doCallRealMethod().when(wbemRequestExecutorMock).isAcceptableException(any());

			// Start the WMI Health Check
			wbemExtension.checkProtocol(telemetryManager);

			assertEquals(
					WbemExtension.UP,
					telemetryManager.getEndpointHostMonitor().getMetric(WbemExtension.WBEM_UP_METRIC).getValue()
			);
		}
	}


	@Test
	void testCheckWbemDownHealth() throws ClientException {
		initWbem();

		// Mock null WBEM protocol health check response
		doReturn(SUCCESS_RESPONSE)
				.when(wbemRequestExecutorMock)
				.executeWbem(
						anyString(),
						any(WbemConfiguration.class),
						eq(WbemExtension.WBEM_TEST_QUERY),
						eq(WBEM_TEST_NAMESPACE),
						telemetryManager
				);

		// Start the WBEM Health Check strategy
		wbemExtension.checkProtocol(telemetryManager);

		assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(WbemExtension.WBEM_UP_METRIC).getValue());
	}

	@Test
	void testIsValidConfiguration() {
		assertTrue(wbemExtension.isValidConfiguration(WbemConfiguration.builder().build()));
		assertFalse(
				wbemExtension.isValidConfiguration(
						new IConfiguration() {
							@Override
							public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {}
						}
				)
		);
	}

	@Test
	void testGetSupportedSources() {
		assertFalse(wbemExtension.getSupportedSources().isEmpty());
		assertTrue(wbemExtension.getSupportedSources().contains(WbemSource.class));
	}

	@Test
	void testGetSupportedCriteria() {
		assertFalse(wbemExtension.getSupportedCriteria().isEmpty());
		assertTrue(wbemExtension.getSupportedCriteria().contains(WbemCriterion.class));
	}

	@Test
	void testGetConfigurationToSourceMapping() {
		assertFalse(wbemExtension.getConfigurationToSourceMapping().isEmpty());
	}

	@Test
	void testIsSupportedConfigurationType() {
		assertTrue(wbemExtension.isSupportedConfigurationType("wbem"));
		assertFalse(wbemExtension.isSupportedConfigurationType("snmp"));
	}

	@Test
	void testBuildConfiguration() throws InvalidConfigurationException {
		final ObjectNode configuration = JsonNodeFactory.instance.objectNode();
		configuration.set("username", new TextNode(USERNAME));
		configuration.set("password", new TextNode(PASSWORD));
		configuration.set("timeout", new TextNode("120"));
		configuration.set("bmcKey", new TextNode(BMC_KEY));
		configuration.set("skipAuth", BooleanNode.valueOf(false));

		assertEquals(
				WbemConfiguration
						.builder()
						.username(USERNAME)
						.password(PASSWORD.toCharArray())
						.timeout(120L)
						.build(),
				wbemExtension.buildConfiguration(WBEM_CRITERION_TYPE, configuration, value -> value)
		);

		assertEquals(
				WbemConfiguration
						.builder()
						.username(USERNAME)
						.password(PASSWORD.toCharArray())
						.timeout(120L)
						.build(),
				wbemExtension.buildConfiguration(WBEM_CRITERION_TYPE, configuration, null)
		);
	}

	@Test
	void testProcessCriterionConfigurationNullTest() {
		initWbem();

		telemetryManager.getHostConfiguration().setConfigurations(Map.of());

		final WbemCriterion wbemCriterion = WbemCriterion.builder().type(WBEM_CRITERION_TYPE).build();

		assertEquals(
				CriterionTestResult.empty(),
				wbemExtension.processCriterion(wbemCriterion, CONNECTOR_ID, telemetryManager)
		);
	}

	@Test
	void testProcessCriterionNullResultTest() throws ExecutionException, InterruptedException, TimeoutException {
		initWbem();

		final WbemCriterion wbemCriterion = WbemCriterion.builder().type(WBEM_CRITERION_TYPE).build();

		final WbemConfiguration configuration = (WbemConfiguration) telemetryManager
				.getHostConfiguration()
				.getConfigurations()
				.get(WbemConfiguration.class);

		doReturn(null).when(wbemRequestExecutorMock).executeWbemDetection(HOST_NAME, configuration);

		final String message =
				"Received <null> result after connecting to the WBEM BMC chip with the WBEM-over-LAN interface.";
		final CriterionTestResult criterionTestResult = wbemExtension.processCriterion(
				wbemCriterion,
				CONNECTOR_ID,
				telemetryManager
		);

		assertEquals(null, criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(message, criterionTestResult.getMessage());
		assertNull(criterionTestResult.getException());
	}

	@Test
	void testProcessCriterionOk() throws ExecutionException, InterruptedException, TimeoutException {
		initWbem();

		final WbemCriterion wbemCriterion = WbemCriterion.builder().type(WBEM_CRITERION_TYPE).build();

		final WbemConfiguration configuration = (WbemConfiguration) telemetryManager
				.getHostConfiguration()
				.getConfigurations()
				.get(WbemConfiguration.class);

		doReturn(RESULT).when(wbemRequestExecutorMock).executeWbemDetection(HOST_NAME, configuration);

		final String message = "Successfully connected to the WBEM BMC chip with the WBEM-over-LAN interface.";
		final CriterionTestResult criterionTestResult = wbemExtension.processCriterion(
				wbemCriterion,
				CONNECTOR_ID,
				telemetryManager
		);

		assertEquals(RESULT, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(message, criterionTestResult.getMessage());
		assertNull(criterionTestResult.getException());
	}

	@Test
	void testProcessWbemSourceOk() throws ExecutionException, InterruptedException, TimeoutException {
		initWbem();

		final WbemConfiguration configuration = (WbemConfiguration) telemetryManager
				.getHostConfiguration()
				.getConfigurations()
				.get(WbemConfiguration.class);

		doReturn(RESULT).when(wbemRequestExecutorMock).executeWbemGetSensors(HOST_NAME, configuration);
		final SourceTable actual = wbemExtension.processSource(
				WbemSource.builder().build(),
				CONNECTOR_ID,
				telemetryManager
		);

		final SourceTable expected = SourceTable.builder().rawData(RESULT).build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessWbemSourceThrowsException() throws ExecutionException, InterruptedException, TimeoutException {
		initWbem();

		final WbemConfiguration configuration = (WbemConfiguration) telemetryManager
				.getHostConfiguration()
				.getConfigurations()
				.get(WbemConfiguration.class);

		doThrow(new RuntimeException("exception"))
				.when(wbemRequestExecutorMock)
				.executeWbemGetSensors(HOST_NAME, configuration);
		final SourceTable actual = wbemExtension.processSource(
				WbemSource.builder().build(),
				CONNECTOR_ID,
				telemetryManager
		);

		final SourceTable expected = SourceTable.empty();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessWbemSourceEmptyResult() throws ExecutionException, InterruptedException, TimeoutException {
		initWbem();

		final WbemConfiguration configuration = (WbemConfiguration) telemetryManager
				.getHostConfiguration()
				.getConfigurations()
				.get(WbemConfiguration.class);

		{
			doReturn(null).when(wbemRequestExecutorMock).executeWbemGetSensors(HOST_NAME, configuration);
			final SourceTable actual = wbemExtension.processSource(
					WbemSource.builder().build(),
					CONNECTOR_ID,
					telemetryManager
			);

			final SourceTable expected = SourceTable.empty();
			assertEquals(expected, actual);
		}

		{
			doReturn("").when(wbemRequestExecutorMock).executeWbemGetSensors(HOST_NAME, configuration);
			final SourceTable actual = wbemExtension.processSource(
					WbemSource.builder().build(),
					CONNECTOR_ID,
					telemetryManager
			);

			final SourceTable expected = SourceTable.builder().rawData("").build();
			assertEquals(expected, actual);
		}
	}

	@Test
	void testProcessWbemSourceNoWbemConfiguration() {
		initWbem();

		telemetryManager.getHostConfiguration().setConfigurations(Map.of());

		assertEquals(
				SourceTable.empty(),
				wbemExtension.processSource(WbemSource.builder().build(), CONNECTOR_ID, telemetryManager)
		);
	}
}
