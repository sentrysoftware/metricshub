package org.sentrysoftware.metricshub.engine.strategy.collect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOSTNAME;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.DOWN;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.HTTP_UP_METRIC;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.SNMP_OID;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.SNMP_UP_METRIC;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.SSH_UP_METRIC;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.UP;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.client.http.HttpRequest;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SshConfiguration;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandHelper;
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
	 * Creates and returns a TelemetryManager instance with an SNMP configuration.
	 *
	 * @return A TelemetryManager instance configured with an SNMP configuration.
	 */
	private TelemetryManager createTelemetryManagerWithSnmpConfig() {
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
					.configurations(Map.of(SnmpConfiguration.class, SnmpConfiguration.builder().community("public").build()))
					.build()
			)
			.build();
	}

	/**
	 * Creates and returns a TelemetryManager instance with an SSH configuration.
	 *
	 * @return A TelemetryManager instance configured with an SSH configuration.
	 */
	private TelemetryManager createTelemetryManagerWithSshConfig() {
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
					.configurations(
						Map.of(
							SshConfiguration.class,
							SshConfiguration
								.sshConfigurationBuilder()
								.username("username")
								.password("password".toCharArray())
								.timeout(60L)
								.build()
						)
					)
					.build()
			)
			.build();
	}

	/**
	 * Creates and returns a TelemetryManager instance without any configuration.
	 *
	 * @return A TelemetryManager instance.
	 */
	private TelemetryManager createTelemetryManagerWithoutConfig() {
		// Create a telemetry manager
		return TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(HostConfiguration.builder().hostId(HOSTNAME).hostname(HOSTNAME).sequential(false).build())
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

		assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(HTTP_UP_METRIC).getValue());
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

		assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(HTTP_UP_METRIC).getValue());
	}

	@Test
	void testCheckSnmpUpHealth() throws InterruptedException, ExecutionException, TimeoutException {
		// Create a telemetry manager using an SNMP HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSnmpConfig();

		// Mock SNMP protocol health check response
		doReturn(SUCCESS_RESPONSE)
			.when(clientsExecutorMock)
			.executeSNMPGetNext(eq(SNMP_OID), any(SnmpConfiguration.class), anyString(), anyBoolean());

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy snmpHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);

		// Start the SNMP Health Check strategy
		snmpHealthCheckStrategy.run();

		assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(SNMP_UP_METRIC).getValue());
	}

	@Test
	void testCheckSnmpDownHealth() throws InterruptedException, ExecutionException, TimeoutException {
		// Create a telemetry manager using an SNMP HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSnmpConfig();

		// Mock SNMP protocol health check response
		doReturn(NULL_RESPONSE)
			.when(clientsExecutorMock)
			.executeSNMPGetNext(eq(SNMP_OID), any(SnmpConfiguration.class), anyString(), anyBoolean());

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy snmpHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);

		// Start the SNMP Health Check strategy
		snmpHealthCheckStrategy.run();

		assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(SNMP_UP_METRIC).getValue());
	}

	@Test
	void testCheckSshHealthLocally() {
		// Create a telemetry manager using an SSH HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSshConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy sshHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);

		// Setting SSH host properties
		telemetryManager.getHostProperties().setMustCheckSshStatus(true);
		telemetryManager.getHostProperties().setOsCommandExecutesLocally(true);

		try (MockedStatic<OsCommandHelper> staticOsCommandHelper = Mockito.mockStatic(OsCommandHelper.class)) {
			staticOsCommandHelper
				.when(() -> OsCommandHelper.runLocalCommand(anyString(), anyLong(), any()))
				.thenReturn(SUCCESS_RESPONSE);

			// Start the SSH Health Check strategy
			sshHealthCheckStrategy.run();

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}

		try (MockedStatic<OsCommandHelper> staticOsCommandHelper = Mockito.mockStatic(OsCommandHelper.class)) {
			staticOsCommandHelper.when(() -> OsCommandHelper.runLocalCommand(anyString(), anyLong(), any())).thenReturn(null);

			// Start the SSH Health Check strategy
			sshHealthCheckStrategy.run();

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckSshUpHealthRemotely() {
		// Create a telemetry manager using an SSH HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSshConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy sshHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);

		// Setting SSH host properties
		telemetryManager.getHostProperties().setMustCheckSshStatus(true);
		telemetryManager.getHostProperties().setOsCommandExecutesLocally(false);
		telemetryManager.getHostProperties().setOsCommandExecutesRemotely(true);

		try (MockedStatic<OsCommandHelper> staticOsCommandHelper = Mockito.mockStatic(OsCommandHelper.class)) {
			staticOsCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(anyString(), anyString(), any(SshConfiguration.class), anyLong(), any(), any())
				)
				.thenReturn(SUCCESS_RESPONSE);

			// Start the SSH Health Check strategy
			sshHealthCheckStrategy.run();

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}

		try (MockedStatic<OsCommandHelper> staticOsCommandHelper = Mockito.mockStatic(OsCommandHelper.class)) {
			staticOsCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(anyString(), anyString(), any(SshConfiguration.class), anyLong(), any(), any())
				)
				.thenReturn(null);

			// Start the SSH Health Check strategy
			sshHealthCheckStrategy.run();

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckSshUpHealthBothLocallyAndRemotely() {
		// Create a telemetry manager using an SSH HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSshConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy sshHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);

		// Setting SSH host properties
		telemetryManager.getHostProperties().setMustCheckSshStatus(true);
		telemetryManager.getHostProperties().setOsCommandExecutesLocally(true);
		telemetryManager.getHostProperties().setOsCommandExecutesRemotely(true);

		// Both local and remote commands working fine
		try (MockedStatic<OsCommandHelper> staticOsCommandHelper = Mockito.mockStatic(OsCommandHelper.class)) {
			staticOsCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(anyString(), anyString(), any(SshConfiguration.class), anyLong(), any(), any())
				)
				.thenReturn(SUCCESS_RESPONSE);

			staticOsCommandHelper
				.when(() -> OsCommandHelper.runLocalCommand(anyString(), anyLong(), any()))
				.thenReturn(SUCCESS_RESPONSE);

			// Start the SSH Health Check strategy
			sshHealthCheckStrategy.run();

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}

		// Local commands not working
		try (MockedStatic<OsCommandHelper> staticOsCommandHelper = Mockito.mockStatic(OsCommandHelper.class)) {
			staticOsCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(anyString(), anyString(), any(SshConfiguration.class), anyLong(), any(), any())
				)
				.thenReturn(SUCCESS_RESPONSE);

			staticOsCommandHelper.when(() -> OsCommandHelper.runLocalCommand(anyString(), anyLong(), any())).thenReturn(null);

			// Start the SSH Health Check strategy
			sshHealthCheckStrategy.run();

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}
		// remote command not working
		try (MockedStatic<OsCommandHelper> staticOsCommandHelper = Mockito.mockStatic(OsCommandHelper.class)) {
			staticOsCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(anyString(), anyString(), any(SshConfiguration.class), anyLong(), any(), any())
				)
				.thenReturn(null);

			staticOsCommandHelper
				.when(() -> OsCommandHelper.runLocalCommand(anyString(), anyLong(), any()))
				.thenReturn(SUCCESS_RESPONSE);

			// Start the SSH Health Check strategy
			sshHealthCheckStrategy.run();

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}

		// Both local and remote commands not working, but not throwing exceptions
		try (MockedStatic<OsCommandHelper> staticOsCommandHelper = Mockito.mockStatic(OsCommandHelper.class)) {
			staticOsCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(anyString(), anyString(), any(SshConfiguration.class), anyLong(), any(), any())
				)
				.thenReturn(null);

			staticOsCommandHelper.when(() -> OsCommandHelper.runLocalCommand(anyString(), anyLong(), any())).thenReturn(null);

			// Start the SSH Health Check strategy
			sshHealthCheckStrategy.run();

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckSshNoHealthWhenMustCheckFalse() {
		// Create a telemetry manager using an SSH HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSshConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy sshHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);

		// Setting SSH host properties
		telemetryManager.getHostProperties().setMustCheckSshStatus(false);
		telemetryManager.getHostProperties().setOsCommandExecutesLocally(true);
		telemetryManager.getHostProperties().setOsCommandExecutesRemotely(true);

		// Start the SSH Health Check strategy
		sshHealthCheckStrategy.run();

		assertNull(telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC));
	}

	@Test
	void testCheckSshNoHealthWhenNoConfiguration() {
		// Create a telemetry manager without HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithoutConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy sshHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock
		);

		// Setting SSH host properties
		telemetryManager.getHostProperties().setMustCheckSshStatus(false);
		telemetryManager.getHostProperties().setOsCommandExecutesLocally(true);
		telemetryManager.getHostProperties().setOsCommandExecutesRemotely(true);

		// Start the SSH Health Check strategy
		sshHealthCheckStrategy.run();

		// make sure that SSH health check is not performed if an SSH config is not present
		assertNull(telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC));
	}
}
