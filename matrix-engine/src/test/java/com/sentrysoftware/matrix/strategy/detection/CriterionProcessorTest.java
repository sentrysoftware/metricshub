package com.sentrysoftware.matrix.strategy.detection;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.configuration.IConfiguration;
import com.sentrysoftware.matrix.configuration.IpmiConfiguration;
import com.sentrysoftware.matrix.configuration.OsCommandConfiguration;
import com.sentrysoftware.matrix.configuration.SshConfiguration;
import com.sentrysoftware.matrix.configuration.WmiConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.identity.criterion.IpmiCriterion;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.strategy.utils.WqlDetectionHelper;
import com.sentrysoftware.matrix.telemetry.HostProperties;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.sentrysoftware.matrix.constants.Constants.BMC;
import static com.sentrysoftware.matrix.constants.Constants.HOST_LINUX;
import static com.sentrysoftware.matrix.constants.Constants.HOST_WIN;
import static com.sentrysoftware.matrix.constants.Constants.INVALID_SOLARIS_VERSION;
import static com.sentrysoftware.matrix.constants.Constants.INVALID_SSH_RESPONSE;
import static com.sentrysoftware.matrix.constants.Constants.IPMI_CONNECTION_SUCCESS_WITH_IMPI_OVER_LAN_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.IPMI_CONNECTION_SUCCESS_WITH_IN_BAND_DRIVER_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.IPMI_FAILURE_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.IPMI_RESULT_EXAMPLE;
import static com.sentrysoftware.matrix.constants.Constants.IPMI_SUCCESS_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.IPMI_TOOL_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.LINUX_BUILD_IPMI_COMMAND;
import static com.sentrysoftware.matrix.constants.Constants.LIPMI;
import static com.sentrysoftware.matrix.constants.Constants.LOCALHOST;
import static com.sentrysoftware.matrix.constants.Constants.MANAGEMENT_CARD_HOST;
import static com.sentrysoftware.matrix.constants.Constants.NO_OS_CONFIGURATION_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.OLD_SOLARIS_VERSION;
import static com.sentrysoftware.matrix.constants.Constants.OLD_SOLARIS_VERSION_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.OOB_NULL_RESULT_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.PATH;
import static com.sentrysoftware.matrix.constants.Constants.SOLARIS_VERSION_NOT_IDENTIFIED_MESSAGE_TOKEN;
import static com.sentrysoftware.matrix.constants.Constants.STRATEGY_TIMEOUT;
import static com.sentrysoftware.matrix.constants.Constants.SUDO_KEYWORD;
import static com.sentrysoftware.matrix.constants.Constants.SYSTEM_POWER_UP_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.UNKNOWN_SOLARIS_VERSION;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.VALID_SOLARIS_VERSION_NINE;
import static com.sentrysoftware.matrix.constants.Constants.VALID_SOLARIS_VERSION_TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

/**
 * This is a test for {@link CriterionProcessor}
 */
@ExtendWith(MockitoExtension.class)
class CriterionProcessorTest {

	@Mock
	private WqlDetectionHelper wqlDetectionHelper;
	@InjectMocks
	private CriterionProcessor criterionProcessorMock;

	@Mock
	private TelemetryManager telemetryManagerMock;

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutorMock;


	@Test
	void testProcessIpmiWindowsSuccess() {
		// Init the mocks
		MockitoAnnotations.initMocks(this);

		// Init configurations
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		final WmiConfiguration wmiProtocol = WmiConfiguration.builder()
				.namespace(HOST_WIN)
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.timeout(STRATEGY_TIMEOUT)
				.build();

		final IpmiCriterion ipmi = IpmiCriterion.builder().forceSerialization(true).build();

		// Add configurations to configurations Map
		configurations.put(wmiProtocol.getClass(), wmiProtocol);
		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostname(HOST_WIN)
				.hostId(HOST_WIN)
				.hostType(DeviceKind.WINDOWS)
				.configurations(configurations)
				.build();

		// Create a TelemetryManager instance
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		// Mock getHostConfiguration and getWinConfiguration
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(configurations.get(wmiProtocol.getClass())).when(telemetryManagerMock).getWinConfiguration();

		// Mock performDetectionTest
		doReturn(CriterionTestResult.success(ipmi, IPMI_SUCCESS_MESSAGE))
				.when(wqlDetectionHelper).performDetectionTest(any(), any(), any());

		final CriterionTestResult criterionTestResult = criterionProcessorMock.process(ipmi);

		assertNotNull(criterionTestResult);
		assertEquals(IPMI_SUCCESS_MESSAGE, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());
	}

	@Test
	void testProcessIpmiWindowsFailure() {
		// Init mocks
		MockitoAnnotations.initMocks(this);

		// Init configurations
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		final WmiConfiguration wmiProtocol = WmiConfiguration.builder()
				.namespace(HOST_WIN)
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.timeout(STRATEGY_TIMEOUT)
				.build();

		final IpmiCriterion ipmi = IpmiCriterion.builder().forceSerialization(true).build();

		// Add configurations to configurations map
		configurations.put(wmiProtocol.getClass(), wmiProtocol);

		// Create a TelemetryManager instance
		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostname(HOST_WIN)
				.hostId(HOST_WIN)
				.hostType(DeviceKind.WINDOWS)
				.configurations(configurations)
				.build();
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		// mock getHostConfiguration, getWinConfiguration and performDetectionTest
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(configurations.get(wmiProtocol.getClass())).when(telemetryManagerMock).getWinConfiguration();
		doReturn(CriterionTestResult.success(ipmi, IPMI_FAILURE_MESSAGE))
				.when(wqlDetectionHelper).performDetectionTest(any(), any(), any());

		final CriterionTestResult criterionTestResult = criterionProcessorMock.process(ipmi);

		assertNotNull(criterionTestResult);
		assertEquals(IPMI_FAILURE_MESSAGE, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());

	}

	@Test
	void testProcessIpmiLinuxWithWrongIpmitoolCommand() {
		// Init configurations
		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.timeout(STRATEGY_TIMEOUT).build();
		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostname(HOST_LINUX)
				.hostId(HOST_LINUX)
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(HttpConfiguration.class, HttpConfiguration.builder().timeout(STRATEGY_TIMEOUT).build(),
						OsCommandConfiguration.class, OsCommandConfiguration.builder()
								.timeout(STRATEGY_TIMEOUT).build()))
				.build();
		// Create TelemetryManager instance
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.hostProperties(HostProperties.builder().isLocalhost(true).build())
				.build();

		hostConfiguration.setConfigurations(Map.of(HttpConfiguration.class, HttpConfiguration.builder().build(),
				OsCommandConfiguration.class, OsCommandConfiguration.builder()
						.useSudoCommands(Sets.newSet())
						.timeout(STRATEGY_TIMEOUT).build(),
				SshConfiguration.class, sshConfiguration));
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();
		assertFalse(criterionProcessorMock.process(new IpmiCriterion()).isSuccess());

	}

	@Test
	void testProcessIpmiLinuxWithWrongSshCommandResult() {
		// Init configurations
		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.timeout(STRATEGY_TIMEOUT).build();
		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostname(HOST_LINUX)
				.hostId(HOST_LINUX)
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(HttpConfiguration.class, HttpConfiguration.builder().timeout(STRATEGY_TIMEOUT).build(),
						OsCommandConfiguration.class, OsCommandConfiguration.builder()
								.useSudoCommands(Sets.newSet())
								.timeout(STRATEGY_TIMEOUT).build()))
				.build();
		// Create TelemetryManager instance
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.hostProperties(HostProperties.builder().isLocalhost(false).build())
				.build();

		// Mock getHostConfiguration, getHostProperties and runSshCommand
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (MockedStatic<OsCommandHelper> osCommandHelper = mockStatic(OsCommandHelper.class)) {
			osCommandHelper.when(() -> OsCommandHelper.runSshCommand(anyString(), eq(HOST_LINUX), eq(sshConfiguration), anyInt(), isNull(), isNull()))
					.thenReturn(INVALID_SSH_RESPONSE);
			assertFalse(criterionProcessorMock.process(new IpmiCriterion()).isSuccess());
		}
	}


	@Test
	void testProcessIpmiLinuxWithNullOsConfiguration() {
		// Init configurations
		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostname(HOST_LINUX)
				.hostId(HOST_LINUX)
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(HttpConfiguration.class, HttpConfiguration.builder().timeout(STRATEGY_TIMEOUT).build())).build();
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.hostProperties(HostProperties.builder().isLocalhost(true).build())
				.build();

		// Mock getHostConfiguration and getHostProperties
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		assertEquals(CriterionTestResult.builder().result("").success(false)
						.message("Hostname " + HOST_LINUX + NO_OS_CONFIGURATION_MESSAGE).build(),
				criterionProcessorMock.process(new IpmiCriterion()));
	}

	@Test
	void testProcessIpmiLinuxWithLocalhost() {
		// Init configurations
		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostname(HOST_LINUX)
				.hostId(HOST_LINUX)
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(HttpConfiguration.class, HttpConfiguration.builder().timeout(STRATEGY_TIMEOUT).build(),
						OsCommandConfiguration.class, OsCommandConfiguration.builder()
								.useSudoCommands(Sets.newSet())
								.timeout(STRATEGY_TIMEOUT).build()))
				.build();
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.hostProperties(HostProperties.builder().isLocalhost(true).build())
				.build();

		// Mock getHostProperties and getHostConfiguration
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (MockedStatic<OsCommandHelper> osCommand = mockStatic(OsCommandHelper.class)) {
			osCommand.when(() -> OsCommandHelper.runLocalCommand(any(), anyInt(), isNull())).thenReturn(IPMI_RESULT_EXAMPLE);
			assertEquals(CriterionTestResult.builder().result(IPMI_RESULT_EXAMPLE).success(true)
							.message(IPMI_CONNECTION_SUCCESS_WITH_IN_BAND_DRIVER_MESSAGE).build().getMessage(),
					criterionProcessorMock.process(new IpmiCriterion()).getMessage());
		}
	}


	@Test
	void testBuildIpmiCommand() {

		final SshConfiguration sshConfiguration = SshConfiguration.sshConfigurationBuilder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.timeout(STRATEGY_TIMEOUT).build();
		final OsCommandConfiguration osCommandConfiguration = OsCommandConfiguration.builder()
				.sudoCommand(SUDO_KEYWORD)
				.useSudoCommands(Sets.newSet())
				.timeout(STRATEGY_TIMEOUT)
				.build();
		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostname(LOCALHOST)
				.hostId(LOCALHOST)
				.hostType(DeviceKind.SOLARIS)
				.configurations(Map.of(OsCommandConfiguration.class, osCommandConfiguration)).build();
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.hostProperties(HostProperties.builder().isLocalhost(true).build())
				.build();

		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		String commandResult = null;

		// Test successful command
		try (MockedStatic<OsCommandHelper> osCommandHelper = mockStatic(OsCommandHelper.class)) {
			osCommandHelper.when(() -> OsCommandHelper.runLocalCommand(any(), eq(STRATEGY_TIMEOUT), isNull())).thenReturn(VALID_SOLARIS_VERSION_TEN);
			commandResult = criterionProcessorMock.buildIpmiCommand(DeviceKind.SOLARIS, LOCALHOST, sshConfiguration,
					osCommandConfiguration, STRATEGY_TIMEOUT.intValue());
			assertNotNull(commandResult);
			assertTrue(commandResult.startsWith(PATH));
		}

		// Test failed command
		try (MockedStatic<OsCommandHelper> osCommandHelper = mockStatic(OsCommandHelper.class)) {
			osCommandHelper.when(() -> OsCommandHelper.runLocalCommand(any(), eq(STRATEGY_TIMEOUT), isNull())).thenReturn(INVALID_SOLARIS_VERSION);
			commandResult = criterionProcessorMock.buildIpmiCommand(DeviceKind.SOLARIS, LOCALHOST, sshConfiguration,
					osCommandConfiguration, STRATEGY_TIMEOUT.intValue());
			assertNotNull(commandResult);
			assertTrue(commandResult.contains(SOLARIS_VERSION_NOT_IDENTIFIED_MESSAGE_TOKEN)); // Not Successful command the response starts with Couldn't identify
		}

		// Test sudo command
		try (MockedStatic<OsCommandHelper> osCommandHelper = mockStatic(OsCommandHelper.class)) {
			osCommandConfiguration.setUseSudo(true);
			osCommandHelper.when(() -> OsCommandHelper.runLocalCommand(any(), eq(STRATEGY_TIMEOUT), isNull())).thenReturn(VALID_SOLARIS_VERSION_TEN);
			commandResult = criterionProcessorMock.buildIpmiCommand(DeviceKind.SOLARIS, LOCALHOST, sshConfiguration,
					osCommandConfiguration, STRATEGY_TIMEOUT.intValue());
			assertNotNull(commandResult);
			assertTrue(commandResult.contains(SUDO_KEYWORD)); // Successful sudo command
		}

		// Test Linux
		osCommandConfiguration.setUseSudo(false);
		commandResult = criterionProcessorMock
				.buildIpmiCommand(DeviceKind.LINUX, LOCALHOST, sshConfiguration, osCommandConfiguration, 120);
		assertEquals(LINUX_BUILD_IPMI_COMMAND, commandResult);


	}

	@Test
	void testGetIpmiCommandForSolaris() throws Exception {
		{ // Solaris Version 10 => bmc
			final String commandResult = criterionProcessorMock.getIpmiCommandForSolaris(IPMI_TOOL_COMMAND, LOCALHOST, VALID_SOLARIS_VERSION_TEN);
			assertEquals(IPMI_TOOL_COMMAND + BMC, commandResult);
		}
		{ // Solaris version 9 => lipmi
			final String commandResult = criterionProcessorMock.getIpmiCommandForSolaris(IPMI_TOOL_COMMAND, LOCALHOST, VALID_SOLARIS_VERSION_NINE);
			assertEquals(IPMI_TOOL_COMMAND + LIPMI, commandResult);
		}


		{// wrong String OS version
			final Exception exception = assertThrows(Exception.class, () -> {
				criterionProcessorMock.getIpmiCommandForSolaris(IPMI_TOOL_COMMAND, LOCALHOST, INVALID_SOLARIS_VERSION);
			});

			final String actualMessage = exception.getMessage();

			assertTrue(actualMessage.contains(UNKNOWN_SOLARIS_VERSION));
		}
		{// old OS version
			final Exception exception = assertThrows(Exception.class, () -> {
				criterionProcessorMock.getIpmiCommandForSolaris(IPMI_TOOL_COMMAND, LOCALHOST, OLD_SOLARIS_VERSION);
			});

			final String actualMessage = exception.getMessage();

			assertTrue(actualMessage.contains(OLD_SOLARIS_VERSION_MESSAGE));
		}
	}


	@Test
	void testVisitIPMIOutOfBandConfigurationNotFound() {
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder()
						.hostId(MANAGEMENT_CARD_HOST).hostType(DeviceKind.OOB).hostname(MANAGEMENT_CARD_HOST)
						.configurations(Collections.emptyMap())
						.build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(CriterionTestResult.empty(), criterionProcessorMock.process(new IpmiCriterion()));
	}


	@Test
	void testVisitIPMIOutOfBand() throws Exception {
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder()
						.hostId(MANAGEMENT_CARD_HOST).hostType(DeviceKind.OOB).hostname(MANAGEMENT_CARD_HOST)
						.configurations(Map.of(IpmiConfiguration.class, IpmiConfiguration
								.builder()
								.username(USERNAME)
								.password(PASSWORD.toCharArray()).build()))
						.build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(SYSTEM_POWER_UP_MESSAGE).when(matsyaClientsExecutorMock)
				.executeIpmiDetection(eq(MANAGEMENT_CARD_HOST), any(IpmiConfiguration.class));
		assertEquals(CriterionTestResult
				.builder()
				.result(SYSTEM_POWER_UP_MESSAGE)
				.message(IPMI_CONNECTION_SUCCESS_WITH_IMPI_OVER_LAN_MESSAGE)
				.success(true)
				.build(), criterionProcessorMock.process(new IpmiCriterion()));
	}

	@Test
	void testVisitIPMIOutOfBandNullResult() throws Exception {
		final IpmiConfiguration ipmiConfiguration = IpmiConfiguration
				.builder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray()).build();
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();

		configurations.put(IpmiConfiguration.class, ipmiConfiguration);
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
				.hostId(MANAGEMENT_CARD_HOST)
				.hostType(DeviceKind.OOB)
				.hostname(MANAGEMENT_CARD_HOST)
				.configurations(configurations)
				.build();
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostProperties(HostProperties.builder().isLocalhost(true).build())
				.hostConfiguration(hostConfiguration)
				.build();

		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(null).when(matsyaClientsExecutorMock)
				.executeIpmiDetection(eq(MANAGEMENT_CARD_HOST), any(IpmiConfiguration.class));
		assertEquals(CriterionTestResult
				.builder()
				.message(OOB_NULL_RESULT_MESSAGE)
				.build().getMessage(), criterionProcessorMock.process(new IpmiCriterion()).getMessage());
	}
}
