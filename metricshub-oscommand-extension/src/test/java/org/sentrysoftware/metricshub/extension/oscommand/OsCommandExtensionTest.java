package org.sentrysoftware.metricshub.extension.oscommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.DOWN;
import static org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy.UP;
import static org.sentrysoftware.metricshub.extension.oscommand.OsCommandExtension.SSH_UP_METRIC;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.common.exception.NoCredentialProvidedException;
import org.sentrysoftware.metricshub.engine.common.helpers.ResourceHelper;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.CommandLineCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.IpmiCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.CommandLineSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionProcessor;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceProcessor;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.HostProperties;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class OsCommandExtensionTest {

	private static final String SUCCESS_RESPONSE = "success";
	private static final String HOSTNAME = "hostname";
	public static final String COMMAND_FILE_ABSOLUTE_PATH =
		"${file::src\\test\\resources\\test-files\\embedded\\connector2\\command.txt}";
	public static final String EMPTY = "";
	public static final String ERROR = "error";
	public static final String HOST_LINUX = "host-linux";
	public static final String INVALID_SOLARIS_VERSION = "invalid";
	public static final String IPMI_RESULT_EXAMPLE =
		"Device ID                 : 3\r\n" +
		"Device Revision           : 3\r\n" +
		"Firmware Revision         : 4.10\r\n" +
		"IPMI Version              : 2.0\r\n" +
		"Manufacturer ID           : 10368\r\n" +
		"Manufacturer Name         : Fujitsu Siemens\r\n" +
		"Product ID                : 790 (0x0316)\r\n" +
		"Product Name              : Unknown (0x316)";
	public static final String IPMI_TOOL_COMMAND = "ipmitoolCommand ";
	public static final String LINUX_BUILD_IPMI_COMMAND =
		"PATH=$PATH:/usr/local/bin:/usr/sfw/bin;export PATH;ipmitool -I open bmc info";
	public static final String LIPMI = "lipmi";
	public static final String LOCALHOST = "localhost";
	public static final String MY_CONNECTOR_1_NAME = "myConnector1";
	public static final String OLD_SOLARIS_VERSION = "4.1.1B";
	public static final String OLD_SOLARIS_VERSION_MESSAGE = "Solaris version (4.1.1B) is too old";
	public static final String USERNAME = "testUser";
	public static final String PASSWORD = "testPassword";
	public static final String PATH = "PATH";
	public static final String RESULT = "result";
	public static final String SOLARIS_VERSION_NOT_IDENTIFIED_MESSAGE_TOKEN = "Could not";
	public static final String SSH_SUDO_COMMAND = "sudo pwd";
	public static final Long STRATEGY_TIMEOUT = 100L;
	public static final String SUDO_KEYWORD = "sudo";
	public static final String VALID_SOLARIS_VERSION_TEN = "5.10";
	public static final String VALID_SOLARIS_VERSION_NINE = "5.9";
	public static final String UNKNOWN_SOLARIS_VERSION = "Unknown Solaris version";
	private static final String CONNECTOR_ID = "connector";

	@Mock
	private OsCommandRequestExecutor OsCommandRequestExecutorMock;

	@Mock
	private OsCommandHelper osCommandHelper;

	@InjectMocks
	private OsCommandExtension osCommandExtension;

	static Map<String, Map<String, Monitor>> monitors;

	@Test
	void testIsValidConfiguration() {
		assertTrue(osCommandExtension.isValidConfiguration(OsCommandConfiguration.builder().build()));
		assertTrue(osCommandExtension.isValidConfiguration(SshConfiguration.builder().build()));
		assertFalse(
			osCommandExtension.isValidConfiguration(
				new IConfiguration() {
					@Override
					public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {}
				}
			)
		);
	}

	@Test
	void testGetSupportedSources() {
		assertFalse(osCommandExtension.getSupportedSources().isEmpty());
		assertTrue(osCommandExtension.getSupportedSources().containsAll(Set.of(CommandLineSource.class, IpmiSource.class)));
	}

	@Test
	void testGetSupportedCriteria() {
		assertFalse(osCommandExtension.getSupportedCriteria().isEmpty());
		assertTrue(
			osCommandExtension.getSupportedCriteria().containsAll(Set.of(CommandLineCriterion.class, IpmiCriterion.class))
		);
	}

	@Test
	void testGetConfigurationToSourceMapping() {
		assertFalse(osCommandExtension.getConfigurationToSourceMapping().isEmpty());
	}

	@Test
	void testIsSupportedConfigurationType() {
		assertTrue(osCommandExtension.isSupportedConfigurationType("ssh"));
		assertTrue(osCommandExtension.isSupportedConfigurationType("oscommand"));
		assertFalse(osCommandExtension.isSupportedConfigurationType("http"));
	}

	@Test
	void testBuildConfiguration() throws InvalidConfigurationException {
		final ObjectNode osCommandconfiguration = JsonNodeFactory.instance.objectNode();
		osCommandconfiguration.set("useSudoCommands", JsonNodeFactory.instance.arrayNode().add("sudo"));
		osCommandconfiguration.set("useSudo", BooleanNode.TRUE);
		osCommandconfiguration.set("sudoCommand", new TextNode("sudo"));
		osCommandconfiguration.set("timeout", new TextNode("120"));

		assertEquals(
			OsCommandConfiguration
				.builder()
				.sudoCommand("sudo")
				.useSudo(true)
				.useSudoCommands(Set.of("sudo"))
				.timeout(120L)
				.build(),
			osCommandExtension.buildConfiguration("oscommand", osCommandconfiguration, value -> value)
		);
		assertEquals(
			OsCommandConfiguration
				.builder()
				.sudoCommand("sudo")
				.useSudo(true)
				.useSudoCommands(Set.of("sudo"))
				.timeout(120L)
				.build(),
			osCommandExtension.buildConfiguration("oscommand", osCommandconfiguration, null)
		);

		final ObjectNode sshConfiguration = JsonNodeFactory.instance.objectNode();
		sshConfiguration.set("username", new TextNode("username"));
		sshConfiguration.set("password", new TextNode("password"));
		sshConfiguration.set("timeout", new TextNode("120"));
		sshConfiguration.set("privateKey", new TextNode("privateKey"));
		sshConfiguration.set("useSudoCommands", JsonNodeFactory.instance.arrayNode().add("sudo"));
		sshConfiguration.set("useSudo", BooleanNode.TRUE);
		sshConfiguration.set("sudoCommand", new TextNode("sudo"));
		sshConfiguration.set("timeout", new TextNode("120"));

		assertEquals(
			SshConfiguration
				.builder()
				.username("username")
				.password("password".toCharArray())
				.timeout(120L)
				.privateKey("privateKey")
				.useSudoCommands(Set.of("sudo"))
				.useSudo(true)
				.sudoCommand("sudo")
				.timeout(120L)
				.build(),
			osCommandExtension.buildConfiguration("ssh", sshConfiguration, value -> value)
		);
		assertEquals(
			SshConfiguration
				.builder()
				.username("username")
				.password("password".toCharArray())
				.timeout(120L)
				.privateKey("privateKey")
				.useSudoCommands(Set.of("sudo"))
				.useSudo(true)
				.sudoCommand("sudo")
				.timeout(120L)
				.build(),
			osCommandExtension.buildConfiguration("ssh", sshConfiguration, null)
		);
	}

	void setup() {
		Monitor hostMonitor = Monitor.builder().type(HOST.getKey()).isEndpoint(true).build();
		monitors = new HashMap<>(Map.of(HOST.getKey(), Map.of(HOSTNAME, hostMonitor)));
	}

	/**
	 * Creates and returns a TelemetryManager instance with an SSH configuration.
	 *
	 * @return A TelemetryManager instance configured with an SSH configuration.
	 */
	private TelemetryManager createTelemetryManagerWithSshConfig() {
		// Create monitors
		setup();
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
							SshConfiguration.builder().username("username").password("password".toCharArray()).timeout(60L).build()
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
		// Create monitors
		setup();
		// Create a telemetry manager
		return TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(HostConfiguration.builder().hostId(HOSTNAME).hostname(HOSTNAME).build())
			.build();
	}

	@Test
	void testCheckSshHealthLocally() {
		// Create a telemetry manager using an SSH HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSshConfig();

		// Setting SSH host properties
		telemetryManager.getHostProperties().setMustCheckSshStatus(true);
		telemetryManager.getHostProperties().setOsCommandExecutesLocally(true);

		try (MockedStatic<OsCommandHelper> staticOsCommandHelper = Mockito.mockStatic(OsCommandHelper.class)) {
			staticOsCommandHelper
				.when(() -> OsCommandHelper.runLocalCommand(anyString(), anyLong(), any()))
				.thenReturn(SUCCESS_RESPONSE);

			// Start the SSH Health Check strategy
			osCommandExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}

		try (MockedStatic<OsCommandHelper> staticOsCommandHelper = Mockito.mockStatic(OsCommandHelper.class)) {
			staticOsCommandHelper.when(() -> OsCommandHelper.runLocalCommand(anyString(), anyLong(), any())).thenReturn(null);

			// Start the SSH Health Check strategy
			osCommandExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckSshUpHealthRemotely() {
		// Create a telemetry manager using an SSH HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSshConfig();

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
			osCommandExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

			assertEquals(UP, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}

		try (MockedStatic<OsCommandHelper> staticOsCommandHelper = Mockito.mockStatic(OsCommandHelper.class)) {
			staticOsCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(anyString(), anyString(), any(SshConfiguration.class), anyLong(), any(), any())
				)
				.thenReturn(null);

			// Start the SSH Health Check strategy
			osCommandExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckSshUpHealthBothLocallyAndRemotely() {
		// Create a telemetry manager using an SSH HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSshConfig();

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
			osCommandExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

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
			osCommandExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

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
			osCommandExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

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
			osCommandExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

			assertEquals(DOWN, telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC).getValue());
		}
	}

	@Test
	void testCheckSshNoHealthWhenMustCheckFalse() {
		// Create a telemetry manager using an SSH HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSshConfig();

		// Setting SSH host properties
		telemetryManager.getHostProperties().setMustCheckSshStatus(false);
		telemetryManager.getHostProperties().setOsCommandExecutesLocally(true);
		telemetryManager.getHostProperties().setOsCommandExecutesRemotely(true);

		// Start the SSH Health Check strategy
		osCommandExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

		assertNull(telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC));
	}

	@Test
	void testCheckSshNoHealthWhenNoConfiguration() {
		// Create a telemetry manager without configuration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithoutConfig();

		// Setting SSH host properties
		telemetryManager.getHostProperties().setMustCheckSshStatus(false);
		telemetryManager.getHostProperties().setOsCommandExecutesLocally(true);
		telemetryManager.getHostProperties().setOsCommandExecutesRemotely(true);

		// Start the SSH Health Check strategy
		osCommandExtension.checkProtocol(telemetryManager, System.currentTimeMillis());

		// make sure that SSH health check is not performed if an SSH config is not present
		assertNull(telemetryManager.getEndpointHostMonitor().getMetric(SSH_UP_METRIC));
	}

	@Test
	void testProcessOsCommandSource() {
		final OsCommandConfiguration osCommandConfiguration = OsCommandConfiguration.builder().timeout(120L).build();

		final HostProperties hostProperties = HostProperties.builder().isLocalhost(true).build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(LOCALHOST)
					.hostId(LOCALHOST)
					.hostType(DeviceKind.LINUX)
					.configurations(Map.of(OsCommandConfiguration.class, osCommandConfiguration))
					.build()
			)
			.hostProperties(hostProperties)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.extensionManager(ExtensionManager.builder().build())
			.connectorId("connectorId")
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process((CommandLineSource) null));
		assertEquals(SourceTable.empty(), sourceProcessor.process(new CommandLineSource()));
		assertEquals(SourceTable.empty(), sourceProcessor.process(CommandLineSource.builder().commandLine("").build()));

		final String commandLine = "/usr/sbin/ioscan -kFC ext_bus";
		final String keepOnlyRegExp = ":ext_bus:";
		final String separators = ":";
		final String selectColumns = "2-4,5,6";

		final CommandLineSource commandSource = new CommandLineSource();
		commandSource.setCommandLine(commandLine);
		commandSource.setKeep(keepOnlyRegExp);
		commandSource.setSeparators(separators);
		commandSource.setSelectColumns(selectColumns);
		commandSource.setExecuteLocally(true);

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.runOsCommand(
						commandLine,
						telemetryManager,
						commandSource.getTimeout(),
						commandSource.getExecuteLocally(),
						hostProperties.isLocalhost()
					)
				)
				.thenThrow(NoCredentialProvidedException.class);

			assertEquals(SourceTable.empty(), sourceProcessor.process(commandSource));
		}

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.runOsCommand(
						commandLine,
						telemetryManager,
						commandSource.getTimeout(),
						commandSource.getExecuteLocally(),
						hostProperties.isLocalhost()
					)
				)
				.thenThrow(IOException.class);

			assertEquals(SourceTable.empty(), sourceProcessor.process(commandSource));
		}

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			final String result = "xxxxxx\n" + "xxxxxx\n" + "0:1:ext_bus:3:4:5:6:7:8\n" + "xxxxxx\n" + "xxxxxx\n";
			final OsCommandResult commandResult = new OsCommandResult(result, commandLine);

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.runOsCommand(
						commandLine,
						telemetryManager,
						commandSource.getTimeout(),
						commandSource.getExecuteLocally(),
						hostProperties.isLocalhost()
					)
				)
				.thenReturn(commandResult);

			final SourceTable expected = SourceTable
				.builder()
				.rawData("1;ext_bus;3;4;5")
				.table(List.of(List.of("1", "ext_bus", "3", "4", "5")))
				.build();
			assertEquals(expected, sourceProcessor.process(commandSource));
		}
	}

	@Test
	void testProcessUnixIpmiSource() {
		// classic case
		final SshConfiguration sshConfiguration = SshConfiguration
			.builder()
			.username("root")
			.password("nationale".toCharArray())
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname("localhost")
			.hostId("localhost")
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(
					HttpConfiguration.class,
					OsCommandConfiguration.builder().build(),
					SshConfiguration.class,
					sshConfiguration
				)
			)
			.build();

		final HostProperties hostProperties = HostProperties
			.builder()
			.isLocalhost(true)
			.ipmitoolCommand("ipmiCommand")
			.build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(hostProperties)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.extensionManager(ExtensionManager.builder().build())
			.build();

		// local
		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd
				.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommandfru"), anyLong(), eq(null)))
				.thenReturn("impiResultFru");
			oscmd
				.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommand-v sdr elist all"), anyLong(), eq(null)))
				.thenReturn("impiResultSdr");
			final SourceTable ipmiResult = sourceProcessor.process(new IpmiSource());
			assertEquals(SourceTable.empty(), ipmiResult);
		}

		String fru = "/data/IpmiFruBabbage";
		String sensor = "/data/IpmiSensorBabbage";
		String expected = "/data/ipmiProcessingResult";
		String fruResult = ResourceHelper.getResourceAsString(fru, this.getClass());
		String sensorResult = ResourceHelper.getResourceAsString(sensor, this.getClass());

		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd
				.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommand" + "fru"), anyLong(), any()))
				.thenReturn(fruResult);
			oscmd
				.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommand" + "-v sdr elist all"), anyLong(), any()))
				.thenReturn(sensorResult);
			final SourceTable ipmiResult = sourceProcessor.process(new IpmiSource());
			String expectedResult = ResourceHelper.getResourceAsString(expected, this.getClass());
			List<List<String>> result = new ArrayList<>();
			Stream.of(expectedResult.split("\n")).forEach(line -> result.add(Arrays.asList(line.split(";"))));
			assertEquals(result, ipmiResult.getTable());
		}

		// remote
		hostProperties.setLocalhost(false);

		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd
				.when(() -> OsCommandHelper.runSshCommand(eq("ipmiCommand" + "fru"), any(), any(), anyLong(), any(), any()))
				.thenReturn("impiResultFru");
			oscmd
				.when(() ->
					OsCommandHelper.runSshCommand(eq("ipmiCommand" + "-v sdr elist all"), any(), any(), anyLong(), any(), any())
				)
				.thenReturn("impiResultSdr");
			final SourceTable ipmiResult = sourceProcessor.process(new IpmiSource());
			assertEquals(SourceTable.empty(), ipmiResult);
		}

		// ipmiToolCommand is empty
		hostProperties.setIpmitoolCommand("");
		SourceTable ipmiResultEmpty = sourceProcessor.process(new IpmiSource());
		assertEquals(SourceTable.empty(), ipmiResultEmpty);

		// ipmiToolCommand is null
		hostProperties.setIpmitoolCommand(null);
		ipmiResultEmpty = sourceProcessor.process(new IpmiSource());
		assertEquals(SourceTable.empty(), ipmiResultEmpty);

		// osCommandConfig is null
		hostProperties.setLocalhost(true);
		hostProperties.setIpmitoolCommand("ipmiCommand");
		ipmiResultEmpty = sourceProcessor.process(new IpmiSource());
		assertEquals(SourceTable.empty(), ipmiResultEmpty);
	}

	@Test
	void testProcessIpmiLinuxWithWrongIpmitoolCommand() {
		// Init configurations
		final SshConfiguration sshConfiguration = SshConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.timeout(STRATEGY_TIMEOUT)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(HOST_LINUX)
			.hostId(HOST_LINUX)
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(
					HttpConfiguration.class,
					HttpConfiguration.builder().timeout(STRATEGY_TIMEOUT).build(),
					OsCommandConfiguration.class,
					OsCommandConfiguration.builder().timeout(STRATEGY_TIMEOUT).build()
				)
			)
			.build();
		// Create TelemetryManager instance
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(HostProperties.builder().isLocalhost(true).build())
			.build();

		hostConfiguration.setConfigurations(
			Map.of(
				HttpConfiguration.class,
				HttpConfiguration.builder().build(),
				OsCommandConfiguration.class,
				OsCommandConfiguration.builder().useSudoCommands(Sets.newSet()).timeout(STRATEGY_TIMEOUT).build(),
				SshConfiguration.class,
				sshConfiguration
			)
		);
//		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
//		doReturn(telemetryManager.getHostProperties()).when(telemetryManagerMock).getHostProperties();
		assertFalse(osCommandExtension.processCriterion(new IpmiCriterion()).isSuccess(), CONNECTOR_ID, telemetryManager);
	}

	@Test
	void testProcessIpmiLinuxWithWrongSshCommandResult() {
		// Init configurations
		final SshConfiguration sshConfiguration = SshConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.timeout(STRATEGY_TIMEOUT)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(HOST_LINUX)
			.hostId(HOST_LINUX)
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(
					HttpConfiguration.class,
					HttpConfiguration.builder().timeout(STRATEGY_TIMEOUT).build(),
					OsCommandConfiguration.class,
					OsCommandConfiguration.builder().useSudoCommands(Sets.newSet()).timeout(STRATEGY_TIMEOUT).build()
				)
			)
			.build();
		// Create TelemetryManager instance
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(HostProperties.builder().isLocalhost(false).build())
			.build();

		// Mock getHostConfiguration, getHostProperties and runSshCommand
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(telemetryManager.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (MockedStatic<OsCommandHelper> osCommandHelper = mockStatic(OsCommandHelper.class)) {
			osCommandHelper
				.when(() ->
					OsCommandHelper.runSshCommand(anyString(), eq(HOST_LINUX), eq(sshConfiguration), anyInt(), isNull(), isNull())
				)
				.thenReturn("Wrong result");
			assertFalse(criterionProcessor.process(new IpmiCriterion()).isSuccess());
		}
	}

	@Test
	void testProcessIpmiLinuxWithLocalhost() {
		// Init configurations
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(HOST_LINUX)
			.hostId(HOST_LINUX)
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(
					HttpConfiguration.class,
					HttpConfiguration.builder().timeout(STRATEGY_TIMEOUT).build(),
					OsCommandConfiguration.class,
					OsCommandConfiguration.builder().useSudoCommands(Sets.newSet()).timeout(STRATEGY_TIMEOUT).build()
				)
			)
			.build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(HostProperties.builder().isLocalhost(true).build())
			.build();

		// Mock getHostProperties and getHostConfiguration
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(telemetryManager.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (MockedStatic<OsCommandHelper> osCommand = mockStatic(OsCommandHelper.class)) {
			osCommand.when(() -> OsCommandHelper.runLocalCommand(any(), anyInt(), isNull())).thenReturn(IPMI_RESULT_EXAMPLE);
			assertEquals(
				CriterionTestResult
					.builder()
					.result(IPMI_RESULT_EXAMPLE)
					.success(true)
					.message("Successfully connected to the IPMI BMC chip with the in-band driver" + " interface.")
					.build()
					.getMessage(),
				criterionProcessor.process(new IpmiCriterion()).getMessage()
			);
		}
	}

	@Test
	void testBuildIpmiCommand() {
		final SshConfiguration sshConfiguration = SshConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.timeout(STRATEGY_TIMEOUT)
			.build();
		final OsCommandConfiguration osCommandConfiguration = OsCommandConfiguration
			.builder()
			.sudoCommand(SUDO_KEYWORD)
			.useSudoCommands(Sets.newSet())
			.timeout(STRATEGY_TIMEOUT)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(LOCALHOST)
			.hostId(LOCALHOST)
			.hostType(DeviceKind.SOLARIS)
			.configurations(Map.of(OsCommandConfiguration.class, osCommandConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(HostProperties.builder().isLocalhost(true).build())
			.build();

		doReturn(telemetryManager.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		String commandResult;

		// Test successful command
		try (MockedStatic<OsCommandHelper> osCommandHelper = mockStatic(OsCommandHelper.class)) {
			osCommandHelper
				.when(() -> OsCommandHelper.runLocalCommand(any(), eq(STRATEGY_TIMEOUT), isNull()))
				.thenReturn(VALID_SOLARIS_VERSION_TEN);
			commandResult =
					new UnixIpmiCriterionProcessor().buildIpmiCommand(
					DeviceKind.SOLARIS,
					LOCALHOST,
					sshConfiguration,
					osCommandConfiguration,
					STRATEGY_TIMEOUT.intValue()
				);
			assertNotNull(commandResult);
			assertTrue(commandResult.startsWith(PATH));
		}

		// Test failed command
		try (MockedStatic<OsCommandHelper> osCommandHelper = mockStatic(OsCommandHelper.class)) {
			osCommandHelper
				.when(() -> OsCommandHelper.runLocalCommand(any(), eq(STRATEGY_TIMEOUT), isNull()))
				.thenReturn(INVALID_SOLARIS_VERSION);
			commandResult =
				criterionProcessor.buildIpmiCommand(
					DeviceKind.SOLARIS,
					LOCALHOST,
					sshConfiguration,
					osCommandConfiguration,
					STRATEGY_TIMEOUT.intValue()
				);
			assertNotNull(commandResult);
			assertTrue(commandResult.contains(SOLARIS_VERSION_NOT_IDENTIFIED_MESSAGE_TOKEN)); // Not Successful command the response starts with Couldn't identify
		}

		// Test sudo command
		try (MockedStatic<OsCommandHelper> osCommandHelper = mockStatic(OsCommandHelper.class)) {
			osCommandConfiguration.setUseSudo(true);
			osCommandHelper
				.when(() -> OsCommandHelper.runLocalCommand(any(), eq(STRATEGY_TIMEOUT), isNull()))
				.thenReturn(VALID_SOLARIS_VERSION_TEN);
			commandResult =
				criterionProcessor.buildIpmiCommand(
					DeviceKind.SOLARIS,
					LOCALHOST,
					sshConfiguration,
					osCommandConfiguration,
					STRATEGY_TIMEOUT.intValue()
				);
			assertNotNull(commandResult);
			assertTrue(commandResult.contains(SUDO_KEYWORD)); // Successful sudo command
		}

		// Test Linux
		osCommandConfiguration.setUseSudo(false);
		commandResult =
			criterionProcessor.buildIpmiCommand(DeviceKind.LINUX, LOCALHOST, sshConfiguration, osCommandConfiguration, 120);
		assertEquals(LINUX_BUILD_IPMI_COMMAND, commandResult);
	}

	@Test
	void testGetIpmiCommandForSolaris() throws Exception {
		// Solaris Version 10 => bmc
		String commandResult = criterionProcessor.getIpmiCommandForSolaris(
			IPMI_TOOL_COMMAND,
			LOCALHOST,
			VALID_SOLARIS_VERSION_TEN
		);
		assertEquals(IPMI_TOOL_COMMAND + "bmc", commandResult);

		// Solaris version 9 => lipmi
		commandResult =
			criterionProcessor.getIpmiCommandForSolaris(IPMI_TOOL_COMMAND, LOCALHOST, VALID_SOLARIS_VERSION_NINE);
		assertEquals(IPMI_TOOL_COMMAND + LIPMI, commandResult);

		// wrong String OS version
		Exception exception = assertThrows(
			Exception.class,
			() -> {
				criterionProcessor.getIpmiCommandForSolaris(IPMI_TOOL_COMMAND, LOCALHOST, INVALID_SOLARIS_VERSION);
			}
		);

		String actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(UNKNOWN_SOLARIS_VERSION));

		// old OS version
		exception =
			assertThrows(
				Exception.class,
				() -> {
					criterionProcessor.getIpmiCommandForSolaris(IPMI_TOOL_COMMAND, LOCALHOST, OLD_SOLARIS_VERSION);
				}
			);

		actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(OLD_SOLARIS_VERSION_MESSAGE));
	}

	@Test
	void testProcessCommandLineNotExpectedResult() {
		final CommandLineCriterion commandLineCriterion = CommandLineCriterion
			.builder()
			.commandLine(SSH_SUDO_COMMAND)
			.errorMessage(EMPTY)
			.expectedResult(RESULT)
			.executeLocally(true)
			.timeout(30L)
			.build();
		final HostProperties hostProperties = HostProperties.builder().isLocalhost(true).build();

		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		final SshConfiguration sshConfiguration = new SshConfiguration();
		sshConfiguration.setUsername(USERNAME);
		sshConfiguration.setPassword(PASSWORD.toCharArray());
		configurations.put(SshConfiguration.class, sshConfiguration);

		final OsCommandConfiguration osCommandConfiguration = OsCommandConfiguration.builder().useSudo(false).build();
		configurations.put(OsCommandConfiguration.class, osCommandConfiguration);

		final HostConfiguration hostConfiguration = new HostConfiguration();
		hostConfiguration.setHostname(LOCALHOST);
		hostConfiguration.setConfigurations(configurations);

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostProperties(hostProperties)
			.hostConfiguration(hostConfiguration)
			.build();

		// The extension manager is empty because it is not involved in this test
		final ExtensionManager extensionManager = ExtensionManager.empty();

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);

		// The result is not the same as the expected result
		OsCommandResult result = new OsCommandResult(ERROR, SSH_SUDO_COMMAND);

		try (MockedStatic<OsCommandHelper> osCommandHelper = mockStatic(OsCommandHelper.class)) {
			osCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(SSH_SUDO_COMMAND, telemetryManager, 30L, true, true))
				.thenReturn(result);
			final CriterionTestResult criterionTestResult = criterionProcessor.process(commandLineCriterion);

			final String message = String.format(
				"CommandLineCriterion test ran but failed:\n" +
				"- CommandLine: %s\n" +
				"- ExecuteLocally: true\n" +
				"- ExpectedResult: %s\n" +
				"- Timeout: 30\n" +
				"\n" +
				"Actual result:\n" +
				"%s",
				SSH_SUDO_COMMAND,
				RESULT,
				ERROR
			);

			assertEquals(ERROR, criterionTestResult.getResult());
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(message, criterionTestResult.getMessage());
			assertNull(criterionTestResult.getException());
		}
	}

	@Test
	void testProcessCommandLineOK() {
		final CommandLineCriterion commandLineCriterion = CommandLineCriterion
			.builder()
			.commandLine(SSH_SUDO_COMMAND)
			.errorMessage(EMPTY)
			.expectedResult(RESULT)
			.executeLocally(true)
			.timeout(30L)
			.build();
		final HostProperties hostProperties = HostProperties.builder().isLocalhost(true).build();

		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		final SshConfiguration sshConfiguration = new SshConfiguration();
		sshConfiguration.setUsername(USERNAME);
		sshConfiguration.setPassword(PASSWORD.toCharArray());
		configurations.put(SshConfiguration.class, sshConfiguration);

		final OsCommandConfiguration osCommandConfiguration = OsCommandConfiguration.builder().useSudo(false).build();
		configurations.put(OsCommandConfiguration.class, osCommandConfiguration);

		final HostConfiguration hostConfiguration = new HostConfiguration();
		hostConfiguration.setHostname(LOCALHOST);
		hostConfiguration.setConfigurations(configurations);

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostProperties(hostProperties)
			.hostConfiguration(hostConfiguration)
			.build();

		// The extension manager is empty because it is not involved in this test
		final ExtensionManager extensionManager = ExtensionManager.empty();

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);

		OsCommandResult result = new OsCommandResult(RESULT, SSH_SUDO_COMMAND);

		try (MockedStatic<OsCommandHelper> osCommandHelper = mockStatic(OsCommandHelper.class)) {
			osCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(SSH_SUDO_COMMAND, telemetryManager, 30L, true, true))
				.thenReturn(result);
			final CriterionTestResult criterionTestResult = criterionProcessor.process(commandLineCriterion);

			final String message = String.format(
				"CommandLineCriterion test succeeded:\n" +
				"- CommandLine: %s\n" +
				"- ExecuteLocally: true\n" +
				"- ExpectedResult: %s\n" +
				"- Timeout: 30\n" +
				"\n" +
				"Result: %s",
				SSH_SUDO_COMMAND,
				RESULT,
				RESULT
			);

			assertEquals(RESULT, criterionTestResult.getResult());
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(message, criterionTestResult.getMessage());
			assertNull(criterionTestResult.getException());
		}
	}

	@Test
	void testProcessCommandLineEmbeddedFileOK() {
		final CommandLineCriterion commandLineCriterion = CommandLineCriterion
			.builder()
			.commandLine(COMMAND_FILE_ABSOLUTE_PATH)
			.errorMessage(EMPTY)
			.expectedResult(RESULT)
			.executeLocally(true)
			.timeout(120L)
			.build();
		final HostProperties hostProperties = HostProperties.builder().isLocalhost(true).build();

		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		final SshConfiguration sshConfiguration = new SshConfiguration();
		sshConfiguration.setUsername(USERNAME);
		sshConfiguration.setPassword(PASSWORD.toCharArray());
		configurations.put(SshConfiguration.class, sshConfiguration);

		final OsCommandConfiguration osCommandConfiguration = OsCommandConfiguration.builder().useSudo(false).build();
		configurations.put(OsCommandConfiguration.class, osCommandConfiguration);

		final HostConfiguration hostConfiguration = new HostConfiguration();
		hostConfiguration.setHostname(LOCALHOST);
		hostConfiguration.setConfigurations(configurations);

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostProperties(hostProperties)
			.hostConfiguration(hostConfiguration)
			.build();

		// The extension manager is empty because it is not involved in this test
		final ExtensionManager extensionManager = ExtensionManager.empty();

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);

		OsCommandResult result = new OsCommandResult(RESULT, COMMAND_FILE_ABSOLUTE_PATH);

		try (MockedStatic<OsCommandHelper> osCommandHelper = mockStatic(OsCommandHelper.class)) {
			osCommandHelper
				.when(() -> OsCommandHelper.runOsCommand(COMMAND_FILE_ABSOLUTE_PATH, telemetryManager, 120L, true, true))
				.thenReturn(result);
			final CriterionTestResult criterionTestResult = criterionProcessor.process(commandLineCriterion);

			final String message = String.format(
				"CommandLineCriterion test succeeded:\n" +
				"- CommandLine: %s\n" +
				"- ExecuteLocally: true\n" +
				"- ExpectedResult: %s\n" +
				"- Timeout: 120\n" +
				"\n" +
				"Result: %s",
				COMMAND_FILE_ABSOLUTE_PATH,
				RESULT,
				RESULT
			);

			assertEquals(RESULT, criterionTestResult.getResult());
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(message, criterionTestResult.getMessage());
			assertNull(criterionTestResult.getException());
		}
	}
}
