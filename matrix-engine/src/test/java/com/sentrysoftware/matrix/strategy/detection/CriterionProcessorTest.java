package com.sentrysoftware.matrix.strategy.detection;

import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.configuration.IConfiguration;
import com.sentrysoftware.matrix.configuration.IpmiConfiguration;
import com.sentrysoftware.matrix.configuration.OsCommandConfiguration;
import com.sentrysoftware.matrix.configuration.SshConfiguration;
import com.sentrysoftware.matrix.configuration.WmiConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
import com.sentrysoftware.matrix.connector.model.identity.criterion.DeviceTypeCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.HttpCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.IpmiCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProcessCriterion;
import com.sentrysoftware.matrix.matsya.HttpRequest;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.utils.CriterionProcessVisitor;
import com.sentrysoftware.matrix.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.strategy.utils.WqlDetectionHelper;
import com.sentrysoftware.matrix.telemetry.HostProperties;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.sentrysoftware.matrix.constants.Constants.BMC;
import static com.sentrysoftware.matrix.constants.Constants.CONFIGURED_OS_NT_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.CONFIGURED_OS_SOLARIS_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.ERROR;
import static com.sentrysoftware.matrix.constants.Constants.FAILED_OS_DETECTION;
import static com.sentrysoftware.matrix.constants.Constants.HOST_ID;
import static com.sentrysoftware.matrix.constants.Constants.HOST_LINUX;
import static com.sentrysoftware.matrix.constants.Constants.HOST_WIN;
import static com.sentrysoftware.matrix.constants.Constants.HTTP;
import static com.sentrysoftware.matrix.constants.Constants.HTTP_GET;
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
import static com.sentrysoftware.matrix.constants.Constants.MY_CONNECTOR_1_NAME;
import static com.sentrysoftware.matrix.constants.Constants.NO_OS_CONFIGURATION_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.OLD_SOLARIS_VERSION;
import static com.sentrysoftware.matrix.constants.Constants.OLD_SOLARIS_VERSION_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.OOB_NULL_RESULT_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.PATH;
import static com.sentrysoftware.matrix.constants.Constants.RESULT;
import static com.sentrysoftware.matrix.constants.Constants.SOLARIS_VERSION_NOT_IDENTIFIED_MESSAGE_TOKEN;
import static com.sentrysoftware.matrix.constants.Constants.STRATEGY_TIMEOUT;
import static com.sentrysoftware.matrix.constants.Constants.SUCCESSFUL_OS_DETECTION;
import static com.sentrysoftware.matrix.constants.Constants.SUDO_KEYWORD;
import static com.sentrysoftware.matrix.constants.Constants.SYSTEM_POWER_UP_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.TEST;
import static com.sentrysoftware.matrix.constants.Constants.TEST_BODY;
import static com.sentrysoftware.matrix.constants.Constants.UNKNOWN_SOLARIS_VERSION;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.VALID_SOLARIS_VERSION_NINE;
import static com.sentrysoftware.matrix.constants.Constants.VALID_SOLARIS_VERSION_TEN;
import static org.junit.Assert.assertNull;
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
	private MatsyaClientsExecutor matsyaClientsExecutorMock;
	@Mock
	private WqlDetectionHelper wqlDetectionHelperMock;
	@InjectMocks
	private CriterionProcessor criterionProcessorMock;

	@Mock
	private TelemetryManager telemetryManagerMock;

	@Test
	void testVisitProcessProcessNull() {
		final ProcessCriterion processCriterion = null;

		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();

		assertEquals(CriterionTestResult.empty(), criterionProcessorMock.process(processCriterion));
	}

	@Test
	void testVisitProcessCommandLineNull() {
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		final ProcessCriterion processCriterion = new ProcessCriterion();
		assertEquals(CriterionTestResult.empty(), criterionProcessorMock.process(processCriterion));
	}

	@Test
	void testVisitProcessCommandLineEmpty() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine("");

		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();

		final CriterionTestResult criterionTestResult = criterionProcessorMock.process(processCriterion);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No test will be performed.", criterionTestResult.getMessage());
		Assertions.assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitProcessNotLocalHost() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine("MBM[5-9]\\.exe");
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		final CriterionTestResult criterionTestResult = criterionProcessorMock.process(processCriterion);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No test will be performed remotely.", criterionTestResult.getMessage());
		Assertions.assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitProcessUnknownOS() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine("MBM[5-9]\\.exe");

		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.hostProperties(HostProperties.builder().isLocalhost(true).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.empty());

			final CriterionTestResult criterionTestResult = criterionProcessorMock.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals("Process presence check: OS unknown, no test will be performed.", criterionTestResult.getMessage());
			Assertions.assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitProcessWindowsEmptyResult() throws Exception {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine("MBM[5-9]\\.exe");

		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.hostProperties(HostProperties.builder().isLocalhost(true).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.of(LocalOsHandler.WINDOWS));
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOsVersion).thenReturn(Optional.of("5.1"));

			doReturn(CriterionTestResult.error(processCriterion,
					"WMI query \"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process\" returned empty value."))
					.when(wqlDetectionHelperMock)
					.performDetectionTest(any(), any(), any());

			final CriterionTestResult criterionTestResult = criterionProcessorMock.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertTrue(criterionTestResult.getMessage().contains("WMI query \"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process\"" +
					" returned empty value."));
			Assertions.assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	@Disabled
	void testVisitProcessWindowsOK() throws Exception {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine("MBM[5-9]\\.exe");

		final WmiConfiguration wmiConfiguration = WmiConfiguration.builder()
				.timeout(STRATEGY_TIMEOUT)
				.build();

		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder()
						.hostname(LOCALHOST)
						.hostId(LOCALHOST)
						.hostType(DeviceKind.LINUX)
						.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
						.build())
				.hostProperties(HostProperties.builder().isLocalhost(true).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.of(LocalOsHandler.WINDOWS));
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOsVersion).thenReturn(Optional.of("5.1"));

			doReturn(
					List.of(
							List.of("0", "System Idle Process", "0", ""),
							List.of("2", "MBM6.exe", "0", "MBM6.exe arg1 arg2"),
							List.of("10564", "eclipse.exe", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")))
					.when(matsyaClientsExecutorMock).executeWmi(
							"localhost",
							wmiConfiguration,
							"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process",
							"root\\cimv2");

			final CriterionTestResult criterionTestResult = criterionProcessorMock.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"One or more currently running processes match the following regular expression:\n- Regexp (should match with the command-line): MBM[5-9]\\.exe",
					criterionTestResult.getMessage());
			Assertions.assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitProcessLinuxNoProcess() {
		final ProcessCriterion process = new ProcessCriterion();
		process.setCommandLine("MBM[5-9]\\.exe");

		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.hostProperties(HostProperties.builder().isLocalhost(true).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();


		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
			 final MockedStatic<CriterionProcessVisitor> mockedCriterionProcessVisitorImpl = mockStatic(CriterionProcessVisitor.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.of(LocalOsHandler.LINUX));
			mockedCriterionProcessVisitorImpl.when(CriterionProcessVisitor::listAllLinuxProcesses).thenReturn(
					List.of(
							List.of("1", "ps", "root", "0", "ps -A -o pid,comm,ruser,ppid,args"),
							List.of("10564", "eclipse.exe", "user", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")));

			final CriterionTestResult criterionTestResult = criterionProcessorMock.process(process);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"No currently running processes match the following regular expression:\n" +
							"- Regexp (should match with the command-line): MBM[5-9]\\.exe\n" +
							"- Currently running process list:\n" +
							"1;ps;root;0;ps -A -o pid,comm,ruser,ppid,args\n" +
							"10564;eclipse.exe;user;11068;\"C:\\Users\\huan\\eclipse\\eclipse.exe\"",
					criterionTestResult.getMessage());
			Assertions.assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitProcessLinuxOK() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine("MBM[5-9]\\.exe");

		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.hostProperties(HostProperties.builder().isLocalhost(true).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
			 final MockedStatic<CriterionProcessVisitor> mockedCriterionProcessVisitorImpl = mockStatic(CriterionProcessVisitor.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.of(LocalOsHandler.LINUX));
			mockedCriterionProcessVisitorImpl.when(CriterionProcessVisitor::listAllLinuxProcesses).thenReturn(
					List.of(
							List.of("1", "ps", "root", "0", "ps -A -o pid,comm,ruser,ppid,args"),
							List.of("2", "MBM6.exe", "user", "0", "MBM6.exe arg1 arg2"),
							List.of("10564", "eclipse.exe", "user", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")));

			final CriterionTestResult criterionTestResult = criterionProcessorMock.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"One or more currently running processes match the following regular expression:\n- Regexp (should match with the command-line): MBM[5-9]\\.exe",
					criterionTestResult.getMessage());
			Assertions.assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitProcessNotImplementedAixOK() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine("MBM[5-9]\\.exe");

		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.hostProperties(HostProperties.builder().isLocalhost(true).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.of(LocalOsHandler.AIX));

			final CriterionTestResult criterionTestResult = criterionProcessorMock.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals("Process presence check: No tests will be performed for OS: aix.", criterionTestResult.getMessage());
			Assertions.assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testProcessDeviceTypeCriterion() {

		// Init configurations
		final TelemetryManager engineConfiguration = TelemetryManager.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.NETWORK).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();

		// Init CriterionTestResult success and failure instances
		final CriterionTestResult successfulTestResult = CriterionTestResult
				.builder()
				.message(SUCCESSFUL_OS_DETECTION)
				.result(CONFIGURED_OS_NT_MESSAGE)
				.success(true)
				.build();

		final CriterionTestResult failedTestResult = CriterionTestResult
				.builder()
				.message(FAILED_OS_DETECTION)
				.result(CONFIGURED_OS_NT_MESSAGE)
				.success(false)
				.build();

		// Test configured NETWORK OS

		final DeviceTypeCriterion deviceTypeCriterion = DeviceTypeCriterion.builder().build();
		assertEquals(successfulTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		// Include NETWORK OS

		deviceTypeCriterion.setKeep(Set.of(DeviceKind.NETWORK));
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		deviceTypeCriterion.setKeep(Collections.emptySet());
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.NETWORK));
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(failedTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		// Test Linux OS

		// Exclude only Linux OS with empty keep set
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.LINUX));
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		// Include only Linux OS with empty keep set
		deviceTypeCriterion.setKeep(Set.of(DeviceKind.LINUX));
		deviceTypeCriterion.setExclude(Collections.emptySet());
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		criterionProcessorMock.process(deviceTypeCriterion);
		assertEquals(failedTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		// Exclude only Linux with empty keep set
		deviceTypeCriterion.setKeep(Collections.emptySet());
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.LINUX));
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		// TEST SOLARIS OS

		// Prepare CriterionTestResult with specific SOLARIS CriterionTestResult instances
		successfulTestResult.setResult(CONFIGURED_OS_SOLARIS_MESSAGE);
		failedTestResult.setResult(CONFIGURED_OS_SOLARIS_MESSAGE);
		engineConfiguration.setHostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST)
				.hostType(DeviceKind.SOLARIS).build());

		// Exclude only SOLARIS OS
		deviceTypeCriterion.setKeep(Collections.emptySet());
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.SOLARIS));
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(failedTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		// Include only SOLARIS OS
		deviceTypeCriterion.setKeep(Set.of(DeviceKind.SOLARIS));
		deviceTypeCriterion.setExclude(Collections.emptySet());
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessorMock.process(deviceTypeCriterion));
	}

	@Test
	void testIsDeviceTypeIncluded() {
		// Create the device criterion instance
		final DeviceTypeCriterion deviceTypeCriterion = DeviceTypeCriterion.builder().build();

		// Prepare the device kind list
		final List<DeviceKind> deviceKindList = Arrays.asList(DeviceKind.STORAGE, DeviceKind.NETWORK, DeviceKind.LINUX);
		assertTrue(criterionProcessorMock.isDeviceKindIncluded(deviceKindList, deviceTypeCriterion));

		// Keep only Solaris OS
		deviceTypeCriterion.setKeep(Set.of(DeviceKind.SOLARIS));
		assertFalse(criterionProcessorMock.isDeviceKindIncluded(deviceKindList, deviceTypeCriterion));

		// Keep only Linux
		deviceTypeCriterion.setKeep(Set.of(DeviceKind.LINUX));
		assertTrue(criterionProcessorMock.isDeviceKindIncluded(deviceKindList, deviceTypeCriterion));

		// Exclude only Solaris and with empty keep set
		deviceTypeCriterion.setKeep(Collections.emptySet());
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.SOLARIS));
		assertTrue(criterionProcessorMock.isDeviceKindIncluded(deviceKindList, deviceTypeCriterion));

		// Exclude only Linux
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.LINUX));
		assertFalse(criterionProcessorMock.isDeviceKindIncluded(deviceKindList, deviceTypeCriterion));
	}

	@Test
	void HttpCriterionProcessHttpCriterionNullTest() throws Exception {
		final HttpCriterion httpCriterion = null;
		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostname(HOST_ID)
				.hostId(HOST_ID)
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(HttpConfiguration.class, HttpConfiguration.builder().build()))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();
		final CriterionProcessor criterionProcessor = new CriterionProcessor(
				matsyaClientsExecutorMock,
				telemetryManager,
				MY_CONNECTOR_1_NAME);

		assertEquals(CriterionTestResult.empty(), criterionProcessor.process(httpCriterion));
	}

	@Test
	void HttpCriterionProcessHttpConfigurationNullTest() throws Exception {
		final HttpCriterion httpCriterion = HttpCriterion.builder()
				.type(HTTP)
				.method(HttpMethod.GET)
				.url(TEST)
				.body(TEST_BODY)
				.resultContent(ResultContent.ALL)
				.expectedResult(RESULT)
				.errorMessage(ERROR)
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.build();
		final CriterionProcessor criterionProcessor = new CriterionProcessor(
				matsyaClientsExecutorMock,
				telemetryManager,
				MY_CONNECTOR_1_NAME);

		assertEquals(CriterionTestResult.empty(), criterionProcessor.process(httpCriterion));
	}

	@Test
	void HttpCriterionProcessRequestWrongResultTest() throws Exception {
		final HttpCriterion httpCriterion = HttpCriterion.builder()
				.type(HTTP)
				.method(HttpMethod.GET)
				.url(TEST)
				.body(TEST_BODY)
				.resultContent(ResultContent.ALL)
				.expectedResult(RESULT)
				.errorMessage(ERROR)
				.build();
		final HttpConfiguration httpConfiguration = HttpConfiguration.builder().build();
		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostname(HOST_ID)
				.hostId(HOST_ID)
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(HttpConfiguration.class, HttpConfiguration.builder().build()))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		final String result = "Something went Wrong";
		final HttpRequest httpRequest = HttpRequest
				.builder()
				.hostname(HOST_ID)
				.method(HTTP_GET)
				.url(httpCriterion.getUrl())
				.header(new StringHeader(httpCriterion.getHeader()))
				.body(new StringBody(httpCriterion.getBody()))
				.httpConfiguration(httpConfiguration)
				.resultContent(httpCriterion.getResultContent())
				.authenticationToken(httpCriterion.getAuthenticationToken())
				.build();
		doReturn(result).when(matsyaClientsExecutorMock).executeHttp(httpRequest, false);

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
				matsyaClientsExecutorMock,
				telemetryManager,
				MY_CONNECTOR_1_NAME);

		final String message = String
				.format("Hostname %s - HTTP test failed - "
								+ "The result (%s) returned by the HTTP test did not match the expected result (%s)."
								+ "Expected value: %s - returned value %s.",
						HOST_ID, result, RESULT, RESULT, result);
		final CriterionTestResult criterionTestResult = criterionProcessor.process(httpCriterion);

		assertEquals(result, criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(message, criterionTestResult.getMessage());
		assertNull(criterionTestResult.getException());
	}

	@Test
	void HttpCriterionProcessOKTest() throws Exception {
		final HttpCriterion httpCriterion = HttpCriterion.builder()
				.type(HTTP)
				.method(HttpMethod.GET)
				.url(TEST)
				.body(TEST_BODY)
				.resultContent(ResultContent.ALL)
				.expectedResult(RESULT)
				.errorMessage(ERROR)
				.build();
		final HttpConfiguration httpConfiguration = HttpConfiguration.builder().build();
		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostname(HOST_ID)
				.hostId(HOST_ID)
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(HttpConfiguration.class, HttpConfiguration.builder().build()))
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(hostConfiguration)
				.build();

		final HttpRequest httpRequest = HttpRequest
				.builder()
				.hostname(HOST_ID)
				.method(HTTP_GET)
				.url(httpCriterion.getUrl())
				.header(new StringHeader(httpCriterion.getHeader()))
				.body(new StringBody(httpCriterion.getBody()))
				.httpConfiguration(httpConfiguration)
				.resultContent(httpCriterion.getResultContent())
				.authenticationToken(httpCriterion.getAuthenticationToken())
				.build();
		doReturn(RESULT).when(matsyaClientsExecutorMock).executeHttp(httpRequest, false);

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
				matsyaClientsExecutorMock,
				telemetryManager,
				MY_CONNECTOR_1_NAME);

		final String message = "Hostname PC-120 - HTTP test succeeded. Returned result: result.";
		final CriterionTestResult criterionTestResult = criterionProcessor.process(httpCriterion);

		assertEquals(RESULT, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(message, criterionTestResult.getMessage());
		assertNull(criterionTestResult.getException());
	}

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
				.when(wqlDetectionHelperMock).performDetectionTest(any(), any(), any());

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
				.when(wqlDetectionHelperMock).performDetectionTest(any(), any(), any());

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
	void testProcessIPMIOutOfBandConfigurationNotFound() {
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
	void testProcessIPMIOutOfBand() throws Exception {
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
	void testProcessIPMIOutOfBandNullResult() throws Exception {
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