package com.sentrysoftware.matrix.strategy.detection;

import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.configuration.IConfiguration;
import com.sentrysoftware.matrix.configuration.IpmiConfiguration;
import com.sentrysoftware.matrix.configuration.OsCommandConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
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
import com.sentrysoftware.matrix.connector.model.identity.criterion.ServiceCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SnmpGetCriterion;
import com.sentrysoftware.matrix.matsya.HttpRequest;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.utils.CriterionProcessVisitor;
import com.sentrysoftware.matrix.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.strategy.utils.WqlDetectionHelper;
import com.sentrysoftware.matrix.telemetry.HostProperties;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
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
import java.util.concurrent.TimeoutException;

import static com.sentrysoftware.matrix.constants.Constants.BMC;
import static com.sentrysoftware.matrix.constants.Constants.CONFIGURED_OS_NT_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.CONFIGURED_OS_SOLARIS_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.EMPTY;
import static com.sentrysoftware.matrix.constants.Constants.ERROR;
import static com.sentrysoftware.matrix.constants.Constants.EXCUTE_WMI_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.EXECUTE_SNMP_GET_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.EXPECTED_SNMP_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.FAILED_OS_DETECTION;
import static com.sentrysoftware.matrix.constants.Constants.HOST_ID;
import static com.sentrysoftware.matrix.constants.Constants.HOST_LINUX;
import static com.sentrysoftware.matrix.constants.Constants.HOST_OS_IS_NOT_WINDOWS_MESSAGE;
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
import static com.sentrysoftware.matrix.constants.Constants.LIST_ALL_LINUX_PROCESSES_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.LOCALHOST;
import static com.sentrysoftware.matrix.constants.Constants.MANAGEMENT_CARD_HOST;
import static com.sentrysoftware.matrix.constants.Constants.MY_CONNECTOR_1_NAME;
import static com.sentrysoftware.matrix.constants.Constants.NEITHER_WMI_NOR_WINRM_ERROR;
import static com.sentrysoftware.matrix.constants.Constants.NO_OS_CONFIGURATION_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.NO_RUNNING_PROCESS_MATCH_REGEX_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.NO_TEST_WILL_BE_PERFORMED_AIX_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.NO_TEST_WILL_BE_PERFORMED_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.NO_TEST_WILL_BE_PERFORMED_REMOTELY_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.NO_TEST_WILL_BE_PERFORMED_UNKNOWN_OS_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.OID;
import static com.sentrysoftware.matrix.constants.Constants.OLD_SOLARIS_VERSION;
import static com.sentrysoftware.matrix.constants.Constants.OLD_SOLARIS_VERSION_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.OOB_NULL_RESULT_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.PATH;
import static com.sentrysoftware.matrix.constants.Constants.PROCESS_CRITERION_COMMAND_LINE;
import static com.sentrysoftware.matrix.constants.Constants.RESULT;
import static com.sentrysoftware.matrix.constants.Constants.RUNNING_PROCESS_MATCH_REGEX_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.SERVICE_NAME_NOT_SPECIFIED_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_CONFIGURATION_COMMUNITY;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_GET_EMPTY_RESULT_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_GET_EXCEPTION_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_GET_EXPECTED_RESULT_MATCHES_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_GET_EXPECTED_RESULT_NOT_MATCHES_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_GET_NULL_RESULT_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_GET_SUCCESS_WITH_NO_EXPECTED_RESULT_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_GET_TIMEOUT_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_VERSION;
import static com.sentrysoftware.matrix.constants.Constants.SOLARIS_VERSION_NOT_IDENTIFIED_MESSAGE_TOKEN;
import static com.sentrysoftware.matrix.constants.Constants.STRATEGY_TIMEOUT;
import static com.sentrysoftware.matrix.constants.Constants.SUCCESSFUL_OS_DETECTION;
import static com.sentrysoftware.matrix.constants.Constants.SUDO_KEYWORD;
import static com.sentrysoftware.matrix.constants.Constants.SYSTEM_POWER_UP_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.TEST;
import static com.sentrysoftware.matrix.constants.Constants.TEST_BODY;
import static com.sentrysoftware.matrix.constants.Constants.TWGIPC;
import static com.sentrysoftware.matrix.constants.Constants.UNKNOWN_SOLARIS_VERSION;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.VALID_SOLARIS_VERSION_NINE;
import static com.sentrysoftware.matrix.constants.Constants.VALID_SOLARIS_VERSION_TEN;
import static com.sentrysoftware.matrix.constants.Constants.WBEM_QUERY;
import static com.sentrysoftware.matrix.constants.Constants.WMI_NAMESPACE;
import static com.sentrysoftware.matrix.constants.Constants.WMI_QUERY_EMPTY_VALUE_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
	private TelemetryManager telemetryManager;

	private void initSNMP() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration
				.builder()
				.community(SNMP_CONFIGURATION_COMMUNITY)
				.version(SnmpConfiguration.SnmpVersion.V1)
				.port(161)
				.timeout(120L)
				.build();

		telemetryManager = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(HOST_WIN).hostId(HOST_WIN).hostType(DeviceKind.LINUX)
						.configurations(Map.of(SnmpConfiguration.class, snmpConfiguration))
						.build())
				.build();
	}

	@Test
	void testProcessSNMPGetReturnsEmptyResult() {

		initSNMP();

		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(CriterionTestResult.empty(), criterionProcessorMock.process((SnmpGetCriterion) null));
		assertNull(criterionProcessorMock.process(SnmpGetCriterion.builder().oid(OID).build()).getResult());
	}

	@Test
	void testProcessSNMPGetExpectedResultMatches() throws Exception {

		initSNMP();

		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(EXECUTE_SNMP_GET_RESULT).when(matsyaClientsExecutorMock).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionProcessorMock.process(SnmpGetCriterion.builder()
				.oid(OID).expectedResult(EXPECTED_SNMP_RESULT).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
						SNMP_GET_EXPECTED_RESULT_MATCHES_MESSAGE)
				.result(EXECUTE_SNMP_GET_RESULT)
				.success(true)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSNMPGetExpectedResultNotMatches() throws Exception {

		initSNMP();

		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(EXECUTE_SNMP_GET_RESULT).when(matsyaClientsExecutorMock).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionProcessorMock.process(SnmpGetCriterion.builder().oid(OID).expectedResult(SNMP_VERSION).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
						SNMP_GET_EXPECTED_RESULT_NOT_MATCHES_MESSAGE)
				.result(EXECUTE_SNMP_GET_RESULT)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSNMPGetSuccessWithNoExpectedResult() throws Exception {

		initSNMP();

		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(EXECUTE_SNMP_GET_RESULT).when(matsyaClientsExecutorMock).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionProcessorMock.process(SnmpGetCriterion.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
						SNMP_GET_SUCCESS_WITH_NO_EXPECTED_RESULT_MESSAGE)
				.result(EXECUTE_SNMP_GET_RESULT)
				.success(true).build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSNMPGetEmptyResult() throws Exception {

		initSNMP();

		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(EMPTY).when(matsyaClientsExecutorMock).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionProcessorMock.process(SnmpGetCriterion.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
						SNMP_GET_EMPTY_RESULT_MESSAGE)
				.result(EMPTY).build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSNMPGetNullResult() throws Exception {

		initSNMP();

		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(null).when(matsyaClientsExecutorMock).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionProcessorMock.process(SnmpGetCriterion.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
						SNMP_GET_NULL_RESULT_MESSAGE)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSNMPGetException() throws Exception {

		initSNMP();

		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doThrow(new TimeoutException(SNMP_GET_TIMEOUT_MESSAGE)).when(matsyaClientsExecutorMock).executeSNMPGet(any(),
				any(), any(), eq(false));
		final CriterionTestResult actual = criterionProcessorMock.process(SnmpGetCriterion.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(SNMP_GET_EXCEPTION_MESSAGE)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessProcessProcessNull() {
		final ProcessCriterion processCriterion = null;

		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();

		assertEquals(CriterionTestResult.empty(), criterionProcessorMock.process(processCriterion));
	}

	@Test
	void testProcessProcessCommandLineNull() {
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		final ProcessCriterion processCriterion = new ProcessCriterion();
		assertEquals(CriterionTestResult.empty(), criterionProcessorMock.process(processCriterion));
	}

	@Test
	void testProcessProcessCommandLineEmpty() {
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
		assertEquals(NO_TEST_WILL_BE_PERFORMED_MESSAGE, criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testProcessProcessNotLocalHost() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);
		final TelemetryManager engineConfiguration = TelemetryManager
				.builder()
				.hostProperties(HostProperties.builder().build())
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build())
				.build();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(engineConfiguration.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		final CriterionTestResult criterionTestResult = criterionProcessorMock.process(processCriterion);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(NO_TEST_WILL_BE_PERFORMED_REMOTELY_MESSAGE, criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testProcessProcessUnknownOS() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);

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
			assertEquals(NO_TEST_WILL_BE_PERFORMED_UNKNOWN_OS_MESSAGE, criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testProcessProcessWindowsEmptyResult() {
		MockitoAnnotations.initMocks(this);
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);

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
					WMI_QUERY_EMPTY_VALUE_MESSAGE))
					.when(wqlDetectionHelperMock)
					.performDetectionTest(any(), any(), any());

			final CriterionTestResult criterionTestResult = criterionProcessorMock.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertTrue(criterionTestResult.getMessage().contains(WMI_QUERY_EMPTY_VALUE_MESSAGE));
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	@Disabled
	void testProcessProcessWindowsOK() throws Exception {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);

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

			doReturn(EXCUTE_WMI_RESULT)
					.when(matsyaClientsExecutorMock).executeWmi(
							LOCALHOST,
							wmiConfiguration,
							WBEM_QUERY,
							WMI_NAMESPACE);

			final CriterionTestResult criterionTestResult = criterionProcessorMock.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(RUNNING_PROCESS_MATCH_REGEX_MESSAGE, criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testProcessProcessLinuxNoProcess() {
		final ProcessCriterion process = new ProcessCriterion();
		process.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);

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
					LIST_ALL_LINUX_PROCESSES_RESULT);

			final CriterionTestResult criterionTestResult = criterionProcessorMock.process(process);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					NO_RUNNING_PROCESS_MATCH_REGEX_MESSAGE,
					criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testProcessProcessLinuxOK() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);

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
			mockedCriterionProcessVisitorImpl.when(CriterionProcessVisitor::listAllLinuxProcesses).thenReturn(EXCUTE_WMI_RESULT);

			final CriterionTestResult criterionTestResult = criterionProcessorMock.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(RUNNING_PROCESS_MATCH_REGEX_MESSAGE, criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testProcessProcessNotImplementedAixOK() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);

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
			assertEquals(NO_TEST_WILL_BE_PERFORMED_AIX_MESSAGE, criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testProcessServiceCheckServiceNull() {
		final ServiceCriterion serviceCriterion = null;
		assertTrue(criterionProcessorMock.process(serviceCriterion).getMessage().contains("Malformed Service criterion."));
	}

	@Test
	void testProcessServiceCheckServiceNameNull() {
		assertTrue(criterionProcessorMock.process(new ServiceCriterion()).getMessage().contains("Malformed Service criterion."));
	}

	@Test
	void testProcessServiceCheckProtocolNull() {
		final ServiceCriterion serviceCriterion = new ServiceCriterion();
		serviceCriterion.setName(TWGIPC);

		assertTrue(criterionProcessorMock.process(serviceCriterion).getMessage().contains(NEITHER_WMI_NOR_WINRM_ERROR));
	}

	@Test
	void testProcessServiceCheckOsNull() {
		final WmiConfiguration wmiConfiguration = WmiConfiguration.builder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.timeout(STRATEGY_TIMEOUT)
				.build();
		final TelemetryManager engineConfiguration = TelemetryManager.builder()
				.hostConfiguration(HostConfiguration.builder()
						.hostname(HOST_WIN)
						.hostId(HOST_WIN)
						.configurations(Map.of(WmiConfiguration.class, wmiConfiguration))
						.build())
				.build();
		doReturn(wmiConfiguration).when(telemetryManagerMock).getWinConfiguration();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();

		final ServiceCriterion serviceCriterion = new ServiceCriterion();
		serviceCriterion.setName(TWGIPC);

		final CriterionTestResult criterionTestResult = criterionProcessorMock.process(serviceCriterion);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().contains(HOST_OS_IS_NOT_WINDOWS_MESSAGE));
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testProcessServiceCheckOsNotWindows() {
		final WmiConfiguration wmiConfiguration = WmiConfiguration.builder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.timeout(STRATEGY_TIMEOUT)
				.build();
		final ServiceCriterion serviceCriterion = new ServiceCriterion();
		serviceCriterion.setName(TWGIPC);

		final CriterionTestResult criterionTestResult = criterionProcessorMock.process(serviceCriterion);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testProcessServiceCheckServiceNameEmpty() {
		final WmiConfiguration wmiConfiguration = WmiConfiguration.builder()
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.timeout(STRATEGY_TIMEOUT)
				.build();
		final TelemetryManager engineConfiguration = TelemetryManager.builder()
				.hostConfiguration(HostConfiguration.builder()
						.hostname(LOCALHOST)
						.hostId(LOCALHOST)
						.hostType(DeviceKind.WINDOWS)
						.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
						.build())
				.build();
		doReturn(wmiConfiguration).when(telemetryManagerMock).getWinConfiguration();
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		final ServiceCriterion serviceCriterion = new ServiceCriterion();
		serviceCriterion.setName("");

		final CriterionTestResult criterionTestResult = criterionProcessorMock.process(serviceCriterion);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().contains(SERVICE_NAME_NOT_SPECIFIED_MESSAGE));
		assertNotNull(criterionTestResult.getResult());
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
	void HttpCriterionProcessHttpCriterionNullTest() {
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
	void HttpCriterionProcessHttpConfigurationNullTest() {
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
	void HttpCriterionProcessRequestWrongResultTest() {
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
	void HttpCriterionProcessOKTest() {
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

		String commandResult;

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
		// Solaris Version 10 => bmc
		String commandResult = criterionProcessorMock.getIpmiCommandForSolaris(IPMI_TOOL_COMMAND, LOCALHOST, VALID_SOLARIS_VERSION_TEN);
		assertEquals(IPMI_TOOL_COMMAND + BMC, commandResult);

		// Solaris version 9 => lipmi
		commandResult = criterionProcessorMock.getIpmiCommandForSolaris(IPMI_TOOL_COMMAND, LOCALHOST, VALID_SOLARIS_VERSION_NINE);
		assertEquals(IPMI_TOOL_COMMAND + LIPMI, commandResult);


		// wrong String OS version
		Exception exception = assertThrows(Exception.class, () -> {
			criterionProcessorMock.getIpmiCommandForSolaris(IPMI_TOOL_COMMAND, LOCALHOST, INVALID_SOLARIS_VERSION);
		});

		String actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(UNKNOWN_SOLARIS_VERSION));

		// old OS version
		exception = assertThrows(Exception.class, () -> {
			criterionProcessorMock.getIpmiCommandForSolaris(IPMI_TOOL_COMMAND, LOCALHOST, OLD_SOLARIS_VERSION);
		});

		actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(OLD_SOLARIS_VERSION_MESSAGE));

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