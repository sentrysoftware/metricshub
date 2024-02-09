package org.sentrysoftware.metricshub.engine.strategy.collect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOSTNAME;
import static org.sentrysoftware.metricshub.engine.strategy.collect.HealthCheckStrategy.SNMP_OID;
import static org.sentrysoftware.metricshub.engine.strategy.collect.HealthCheckStrategy.UP_METRIC_FORMAT;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
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
class HealthCheckStrategyTest {

	private static final long CURRENT_TIME_MILLIS = System.currentTimeMillis();

	@Mock
	private static ClientsExecutor clientsExecutorMock;

	private static final String SUCCESS_RESPONSE = "Success";
	private static final String NULL_RESPONSE = null;
	private static final Double UP_VALUE = 1.0;
	private static final Double DOWN_VALUE = 0.0;
	private static Monitor hostMonitor;
	static HttpConfiguration httpConfig;
	static Map<String, Map<String, Monitor>> monitors;
	static TelemetryManager telemetryManager;

	/**
	 * Instantiates all the necessary variables to perform the Http Health Check
	 */
	private static void setupHttp() {
		// Create the host monitor
		hostMonitor = Monitor.builder().type(HOST.getKey()).isEndpoint(true).build();

		// Create all the needed protocols configurations
		httpConfig = HttpConfiguration.builder().build();

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
						.configurations(Map.of(HttpConfiguration.class, httpConfig))
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
		// Create all the necessary variables to run the test
		setupHttp();
		// Mock HTTP protocol health check response
		doReturn(NULL_RESPONSE).when(clientsExecutorMock).executeHttp(any(HttpRequest.class), anyBoolean());

		// Create a new health check strategy
		final HealthCheckStrategy httpHealthCheckStrategy = new HealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);
		// Start the Health Check strategy
		httpHealthCheckStrategy.run();

		assertEquals(DOWN_VALUE, hostMonitor.getMetric(String.format(UP_METRIC_FORMAT, "HTTP")).getValue());
	}

	@Test
	void testCheckHttpUpHealth() {
		// Create all the necessary variables to run the test
		setupHttp();
		// Mock HTTP protocol health check response
		doReturn(SUCCESS_RESPONSE).when(clientsExecutorMock).executeHttp(any(HttpRequest.class), anyBoolean());

		// Create a new health check strategy
		final HealthCheckStrategy httpHealthCheckStrategy = new HealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);
		// Start the Health Check strategy
		httpHealthCheckStrategy.run();

		assertEquals(UP_VALUE, hostMonitor.getMetric(String.format(UP_METRIC_FORMAT, "HTTP")).getValue());
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
