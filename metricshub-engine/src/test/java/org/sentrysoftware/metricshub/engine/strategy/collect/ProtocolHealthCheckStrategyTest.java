package org.sentrysoftware.metricshub.engine.strategy.collect;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOSTNAME;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.DOWN;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.IPMI_UP_METRIC;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.SSH_UP_METRIC;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.UP;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.WBEM_TEST_QUERY;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.WBEM_UP_METRIC;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.WINRM_UP_METRIC;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.WMI_AND_WINRM_TEST_NAMESPACE;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.WMI_AND_WINRM_TEST_QUERY;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.WMI_UP_METRIC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.sentrysoftware.ipmi.client.IpmiClient;
import org.sentrysoftware.ipmi.client.IpmiClientConfiguration;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IpmiConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SshConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WbemConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WinRmConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.extension.TestConfiguration;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandHelper;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.wbem.javax.wbem.WBEMException;
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
	 * Creates and returns a TelemetryManager instance with an IPMI configuration.
	 *
	 * @return A TelemetryManager instance configured with an IPMI configuration.
	 */
	private TelemetryManager createTelemetryManagerWithIpmiConfig() {
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
							IpmiConfiguration.class,
							IpmiConfiguration
								.builder()
								.username("username")
								.password("password".toCharArray())
								.bmcKey(null)
								.skipAuth(false)
								.timeout(60L)
								.build()
						)
					)
					.build()
			)
			.build();
	}

	/**
	 * Creates and returns a TelemetryManager instance with an WBEM configuration.
	 *
	 * @return A TelemetryManager instance configured with an WBEM configuration.
	 */
	private TelemetryManager createTelemetryManagerWithWbemConfig() {
		// Create a telemetry manager
		return TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOSTNAME)
					.hostname(HOSTNAME)
					.configurations(Map.of(WbemConfiguration.class, WbemConfiguration.builder().build()))
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
			.hostConfiguration(HostConfiguration.builder().hostId(HOSTNAME).hostname(HOSTNAME).build())
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
	void testCheckSshHealthLocally() {
		// Create a telemetry manager using an SSH HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSshConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy sshHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock,
			ExtensionManager.empty()
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
			clientsExecutorMock,
			ExtensionManager.empty()
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
			clientsExecutorMock,
			ExtensionManager.empty()
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
			clientsExecutorMock,
			ExtensionManager.empty()
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
		// Create a telemetry manager without configuration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithoutConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy sshHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock,
			ExtensionManager.empty()
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

	@Test
	void testCheckIpmiUpHealth() {
		// Create a telemetry manager using an IPMI HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithIpmiConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy ipmiHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock,
			ExtensionManager.empty()
		);

		// Mock successful IPMI protocol health check response
		try (MockedStatic<IpmiClient> staticIpmiClient = Mockito.mockStatic(IpmiClient.class)) {
			staticIpmiClient
				.when(() -> IpmiClient.getChassisStatusAsStringResult(any(IpmiClientConfiguration.class)))
				.thenReturn(SUCCESS_RESPONSE);

			// Start the IPMI Health Check strategy
			ipmiHealthCheckStrategy.run();

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(IPMI_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckIpmiDownHealth() {
		// Create a telemetry manager using an IPMI HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithIpmiConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy ipmiHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock,
			ExtensionManager.empty()
		);

		// Mock null IPMI protocol health check response
		try (MockedStatic<IpmiClient> staticIpmiClient = Mockito.mockStatic(IpmiClient.class)) {
			staticIpmiClient
				.when(() -> IpmiClient.getChassisStatusAsStringResult(any(IpmiClientConfiguration.class)))
				.thenReturn(null);

			// Start the IPMI Health Check strategy
			ipmiHealthCheckStrategy.run();

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(IPMI_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckWbemUpHealthNamespaces() throws ClientException {
		// Create a telemetry manager using a WBEM HostConfiguration
		final TelemetryManager telemetryManager = createTelemetryManagerWithWbemConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy wbemHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock,
			ExtensionManager.empty()
		);

		{
			// Mock a positive response for every WBEM protocol health check test namespace
			doReturn(WQL_SUCCESS_RESPONSE)
				.when(clientsExecutorMock)
				.executeWbem(anyString(), any(WbemConfiguration.class), eq(WBEM_TEST_QUERY), anyString());

			// Start the WBEM Health Check strategy
			wbemHealthCheckStrategy.run();

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(WBEM_UP_METRIC).getValue());
		}

		{
			final Map<String, Answer<?>> answers = Map.of(
				"root/Interop",
				answer -> null,
				"interop",
				answer -> null,
				"root/PG_Interop",
				answer -> null,
				"PG_Interop",
				answer -> {
					throw new RuntimeException(new WBEMException(WBEMException.CIM_ERR_INVALID_NAMESPACE));
				}
			);

			for (Entry<String, Answer<?>> answerEntry : answers.entrySet()) {
				// Mock a positive response for every WBEM protocol health check test namespace
				doAnswer(answerEntry.getValue())
					.when(clientsExecutorMock)
					.executeWbem(anyString(), any(WbemConfiguration.class), eq(WBEM_TEST_QUERY), eq(answerEntry.getKey()));
			}

			// Start the WBEM Health Check strategy
			wbemHealthCheckStrategy.run();

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(WBEM_UP_METRIC).getValue());
		}

		{
			final Map<String, Answer<?>> answers = Map.of(
				"root/Interop",
				answer -> null,
				"interop",
				answer -> WQL_SUCCESS_RESPONSE
			);

			for (Entry<String, Answer<?>> answerEntry : answers.entrySet()) {
				// Mock a positive response for every WBEM protocol health check test namespace
				doAnswer(answerEntry.getValue())
					.when(clientsExecutorMock)
					.executeWbem(anyString(), any(WbemConfiguration.class), eq(WBEM_TEST_QUERY), eq(answerEntry.getKey()));
			}

			// Start the WBEM Health Check strategy
			wbemHealthCheckStrategy.run();

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(WBEM_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckWbemDownHealth() throws ClientException {
		// Create a telemetry manager using a WBEM HostConfiguration
		final TelemetryManager telemetryManager = createTelemetryManagerWithWbemConfig();

		// Create a new protocol health check strategy
		final ProtocolHealthCheckStrategy wbemHealthCheckStrategy = new ProtocolHealthCheckStrategy(
			telemetryManager,
			CURRENT_TIME_MILLIS,
			clientsExecutorMock,
			ExtensionManager.empty()
		);

		{
			// Mock a null WBEM protocol health check response
			doReturn(null)
				.when(clientsExecutorMock)
				.executeWbem(anyString(), any(WbemConfiguration.class), eq(WBEM_TEST_QUERY), anyString());

			// Start the WBEM Health Check strategy
			wbemHealthCheckStrategy.run();

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(WBEM_UP_METRIC).getValue());
		}

		{
			final Map<String, Answer<?>> answers = Map.of(
				"root/Interop",
				answer -> null,
				"interop",
				answer -> null,
				"root/PG_Interop",
				answer -> null,
				"PG_Interop",
				answer -> {
					throw new RuntimeException(new WBEMException(WBEMException.CIM_ERR_FAILED));
				}
			);

			for (Entry<String, Answer<?>> answerEntry : answers.entrySet()) {
				// Mock a negative response for every WBEM protocol health check test namespace
				doAnswer(answerEntry.getValue())
					.when(clientsExecutorMock)
					.executeWbem(anyString(), any(WbemConfiguration.class), eq(WBEM_TEST_QUERY), eq(answerEntry.getKey()));
			}

			// Start the WBEM Health Check strategy
			wbemHealthCheckStrategy.run();

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(WBEM_UP_METRIC).getValue());
		}
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
