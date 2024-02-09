package org.sentrysoftware.metricshub.engine.strategy.collect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOSTNAME;
import static org.sentrysoftware.metricshub.engine.strategy.collect.HealthCheckStrategy.SNMP_OID;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.DOWN;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.UP;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.UP_METRIC_FORMAT;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.client.http.HttpRequest;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class ProtocolHealthCheckStrategyTest {

	private static final long CURRENT_TIME_MILLIS = System.currentTimeMillis();

	@Mock
	private static ClientsExecutor clientsExecutorMock;

	private static final String SUCCESS_RESPONSE = "Success";
	private static final String NULL_RESPONSE = null;
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
	 * Creates and returns a TelemetryManager instance with an HTTP configuration.
	 *
	 * @return A TelemetryManager instance configured with an HTTP configuration.
	 */
	private TelemetryManager createTelemetryManagerWithHttpConfig() {
		// Create a telemetry manager
		return TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOSTNAME)
					.hostname(HOSTNAME)
					.sequential(false)
					.configurations(Map.of(HttpConfiguration.class, HttpConfiguration.builder().build()))
					.build()
			)
			.build();
	}

	/**
	 * Instantiates all the necessary variables to perform the Http Health Check
	 */
	private static void setupSnmp() {
		// Create the host monitor
		hostMonitor = Monitor.builder().type(HOST.getKey()).isEndpoint(true).build();

		// Create all the needed protocols configurations
		final SnmpConfiguration snmpConfig = SnmpConfiguration.builder().community("public").build();

		// Create a map of monitors
		monitors = new HashMap<>(Map.of(HOST.getKey(), Map.of(HOSTNAME, hostMonitor)));

		// Create a telemetry manager
		telemetryManager =
			TelemetryManager
				.builder()
				.monitors(monitors)
				.hostConfiguration(
					HostConfiguration
						.builder()
						.hostId(HOSTNAME)
						.hostname(HOSTNAME)
						.sequential(false)
						.configurations(Map.of(SnmpConfiguration.class, snmpConfig))
						.build()
				)
				.build();
	}

	@Test
	void testCheckHttpDownHealth() {
		// Create a telemetry manager using an HTTP HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithHttpConfig();
		// Mock HTTP protocol health check response
		doReturn(NULL_RESPONSE).when(clientsExecutorMock).executeHttp(any(HttpRequest.class), anyBoolean());

		// Create a new health check strategy
		final ProtocolHealthCheckStrategy httpHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);
		// Start the Health Check strategy
		httpHealthCheckStrategy.run();

		assertEquals(
			DOWN,
			telemetryManager.getEndpointHostMonitor().getMetric(String.format(UP_METRIC_FORMAT, "HTTP")).getValue()
		);
	}

	@Test
	void testCheckHttpUpHealth() {
		// Create a telemetry manager using an HTTP HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithHttpConfig();
		// Mock HTTP protocol health check response
		doReturn(SUCCESS_RESPONSE).when(clientsExecutorMock).executeHttp(any(HttpRequest.class), anyBoolean());

		// Create a new health check strategy
		final ProtocolHealthCheckStrategy httpHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);
		// Start the Health Check strategy
		httpHealthCheckStrategy.run();

		assertEquals(
			UP,
			telemetryManager.getEndpointHostMonitor().getMetric(String.format(UP_METRIC_FORMAT, "HTTP")).getValue()
		);
	}

	@Test
	void testCheckSnmpUpHealth() throws InterruptedException, ExecutionException, TimeoutException {
		// Create all the necessary variables to run the test
		setupSnmp();
		// Mock HTTP protocol health check response
		doReturn(SUCCESS_RESPONSE)
			.when(clientsExecutorMock)
			.executeSNMPGetNext(eq(SNMP_OID), any(SnmpConfiguration.class), anyString(), anyBoolean());

		// Create a new health check strategy
		final HealthCheckStrategy snmpHealthCheckStrategy = new HealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);
		// Start the Health Check strategy
		snmpHealthCheckStrategy.run();

		assertEquals(UP_VALUE, hostMonitor.getMetric(String.format(UP_METRIC_FORMAT, "SNMP")).getValue());
	}

	@Test
	void testCheckSnmpDownHealth() throws InterruptedException, ExecutionException, TimeoutException {
		// Create all the necessary variables to run the test
		setupSnmp();
		// Mock SNMP protocol health check response
		doReturn(NULL_RESPONSE)
			.when(clientsExecutorMock)
			.executeSNMPGetNext(eq(SNMP_OID), any(SnmpConfiguration.class), anyString(), anyBoolean());

		// Create a new health check strategy
		final HealthCheckStrategy snmpHealthCheckStrategy = new HealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);
		// Start the SNMP Health Check strategy
		snmpHealthCheckStrategy.run();

		assertEquals(DOWN_VALUE, hostMonitor.getMetric(String.format(UP_METRIC_FORMAT, "SNMP")).getValue());
	}
}
