package org.sentrysoftware.metricshub.engine.strategy.collect;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOSTNAME;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.DOWN;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.UP;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.WINRM_UP_METRIC;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.WMI_AND_WINRM_TEST_NAMESPACE;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.WMI_AND_WINRM_TEST_QUERY;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.WMI_UP_METRIC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WinRmConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.extension.TestConfiguration;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.winrm.exceptions.WqlQuerySyntaxException;
import org.sentrysoftware.wmi.exceptions.WmiComException;

@ExtendWith(MockitoExtension.class)
class ProtocolHealthCheckStrategyTest {

	private static final long CURRENT_TIME_MILLIS = System.currentTimeMillis();

	@Mock
	private static ClientsExecutor clientsExecutorMock;

	@Mock
	private static IProtocolExtension protocolExtensionMock;

	private static final String SUCCESS_RESPONSE = "Success";

	private static final List<List<String>> WQL_SUCCESS_RESPONSE = List.of(List.of(SUCCESS_RESPONSE));
	static Map<String, Map<String, Monitor>> monitors;

	/**
	 * Sets up the test environment before each test method is executed.
	 * Creates a endpoint host monitor with specific properties and initializes the monitors map.
	 */
	@BeforeEach
	void setup() {
		Monitor hostMonitor = Monitor.builder().type(HOST.getKey()).isEndpoint(true).build();
		monitors = new HashMap<>(Map.of(HOST.getKey(), Map.of(HOSTNAME, hostMonitor)));
	}

	/**
	 * Creates and returns a TelemetryManager instance with a Test configuration.
	 *
	 * @return A TelemetryManager instance configured with a Test configuration.
	 */
	private TelemetryManager createTelemetryManagerWithTestConfig() {
		// Create a telemetry manager
		return TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOSTNAME)
					.hostname(HOSTNAME)
					.configurations(Map.of(TestConfiguration.class, TestConfiguration.builder().build()))
					.build()
			)
			.build();
	}

	/**
	 * Creates and returns a TelemetryManager instance with an WMI configuration.
	 *
	 * @return A TelemetryManager instance configured with an WMI configuration.
	 */
	private TelemetryManager createTelemetryManagerWithWmiConfig() {
		// Create a telemetry manager
		return TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOSTNAME)
					.hostname(HOSTNAME)
					.configurations(
						Map.of(
							WmiConfiguration.class,
							WmiConfiguration.builder().username("username").password("password".toCharArray()).timeout(60L).build()
						)
					)
					.build()
			)
			.build();
	}

	/**
	 * Creates and returns a TelemetryManager instance with an WinRM configuration.
	 *
	 * @return A TelemetryManager instance configured with an WinRM configuration.
	 */
	private TelemetryManager createTelemetryManagerWithWinRmConfig() {
		// Create a telemetry manager
		return TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOSTNAME)
					.hostname(HOSTNAME)
					.configurations(
						Map.of(
							WinRmConfiguration.class,
							WinRmConfiguration.builder().username("username").password("password".toCharArray()).timeout(60L).build()
						)
					)
					.build()
			)
			.build();
	}

	@Test
	void testCheckHealth() throws Exception {
		// Create a telemetry manager using a test configuration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithTestConfig();

		// Create the Extension Manager
		final ExtensionManager extensionManager = ExtensionManager
			.builder()
			.withProtocolExtensions(List.of(protocolExtensionMock))
			.build();

		doReturn(true)
			.when(protocolExtensionMock)
			.isValidConfiguration(telemetryManager.getHostConfiguration().getConfigurations().get(TestConfiguration.class));

		doNothing().when(protocolExtensionMock).checkProtocol(any(TelemetryManager.class));

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy healthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock,
			extensionManager
		);
		assertDoesNotThrow(() -> healthCheckStrategy.run());
	}

	@Test
	void testCheckWmiUpHealth() throws ClientException {
		// Create a telemetry manager using a WMI HostConfiguration
		final TelemetryManager telemetryManager = createTelemetryManagerWithWmiConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy wmiHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock,
			ExtensionManager.empty()
		);

		{
			// Mock a positive WMI protocol health check response
			doReturn(WQL_SUCCESS_RESPONSE)
				.when(clientsExecutorMock)
				.executeWmi(
					anyString(),
					any(WmiConfiguration.class),
					eq(WMI_AND_WINRM_TEST_QUERY),
					eq(WMI_AND_WINRM_TEST_NAMESPACE)
				);

			// Start the WMI Health Check strategy
			wmiHealthCheckStrategy.run();

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(WMI_UP_METRIC).getValue());
		}

		{
			// Mock an acceptable WMI protocol health check exception
			doThrow(new RuntimeException(new WmiComException("WBEM_E_INVALID_NAMESPACE")))
				.when(clientsExecutorMock)
				.executeWmi(
					anyString(),
					any(WmiConfiguration.class),
					eq(WMI_AND_WINRM_TEST_QUERY),
					eq(WMI_AND_WINRM_TEST_NAMESPACE)
				);

			// Start the WMI Health Check strategy
			wmiHealthCheckStrategy.run();

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(WMI_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckWmiDownHealth() throws ClientException {
		// Create a telemetry manager using a WMI HostConfiguration
		final TelemetryManager telemetryManager = createTelemetryManagerWithWmiConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy wmiHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock,
			ExtensionManager.empty()
		);

		// Mock a null WMI protocol health check response
		doReturn(null)
			.when(clientsExecutorMock)
			.executeWmi(
				anyString(),
				any(WmiConfiguration.class),
				eq(WMI_AND_WINRM_TEST_QUERY),
				eq(WMI_AND_WINRM_TEST_NAMESPACE)
			);

		// Start the WMI Health Check strategy
		wmiHealthCheckStrategy.run();

		assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(WMI_UP_METRIC).getValue());
	}

	@Test
	void testCheckWinRmUpHealth() throws ClientException {
		// Create a telemetry manager using a WinRM HostConfiguration
		final TelemetryManager telemetryManager = createTelemetryManagerWithWinRmConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy winRmHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock,
			ExtensionManager.empty()
		);

		// Mock a positive WinRM protocol health check response
		doReturn(WQL_SUCCESS_RESPONSE)
			.when(clientsExecutorMock)
			.executeWqlThroughWinRm(
				anyString(),
				any(WinRmConfiguration.class),
				eq(WMI_AND_WINRM_TEST_QUERY),
				eq(WMI_AND_WINRM_TEST_NAMESPACE)
			);

		// Start the WinRM Health Check strategy
		winRmHealthCheckStrategy.run();

		assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(WINRM_UP_METRIC).getValue());

		{
			// Mock an acceptable WinRM protocol health check exception
			doThrow(new RuntimeException(new WqlQuerySyntaxException("WQL Quert Syntax Exception")))
				.when(clientsExecutorMock)
				.executeWqlThroughWinRm(
					anyString(),
					any(WinRmConfiguration.class),
					eq(WMI_AND_WINRM_TEST_QUERY),
					eq(WMI_AND_WINRM_TEST_NAMESPACE)
				);

			// Start the WinRM Health Check strategy
			winRmHealthCheckStrategy.run();

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(WINRM_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckWinRmDownHealth() throws ClientException {
		// Create a telemetry manager using a WinRM HostConfiguration
		final TelemetryManager telemetryManager = createTelemetryManagerWithWinRmConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy winRmHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock,
			ExtensionManager.empty()
		);

		// Mock a null WinRM protocol health check response
		doReturn(null)
			.when(clientsExecutorMock)
			.executeWqlThroughWinRm(
				anyString(),
				any(WinRmConfiguration.class),
				eq(WMI_AND_WINRM_TEST_QUERY),
				eq(WMI_AND_WINRM_TEST_NAMESPACE)
			);

		// Start the WinRM Health Check strategy
		winRmHealthCheckStrategy.run();

		assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(WINRM_UP_METRIC).getValue());
	}
}
