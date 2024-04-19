package org.sentrysoftware.metricshub.engine.strategy.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.WMI_PROCESS_QUERY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.CONFIGURED_OS_NT_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.CONFIGURED_OS_SOLARIS_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.CRITERION_WMI_NAMESPACE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ERROR;
import static org.sentrysoftware.metricshub.engine.constants.Constants.EXCUTE_WBEM_RESULT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.EXECUTE_WMI_RESULT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.FAILED_OS_DETECTION;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HIGH_VERSION_NUMBER;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST_ID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST_OS_IS_NOT_WINDOWS_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST_WIN;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HTTP;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HTTP_GET;
import static org.sentrysoftware.metricshub.engine.constants.Constants.IPMI_CONNECTION_SUCCESS_WITH_IMPI_OVER_LAN_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.IPMI_FAILURE_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.IPMI_SUCCESS_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.LIST_ALL_LINUX_PROCESSES_RESULT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.LOCALHOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.LOW_VERSION_NUMBER;
import static org.sentrysoftware.metricshub.engine.constants.Constants.MANAGEMENT_CARD_HOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.MY_CONNECTOR_1_NAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NEITHER_WMI_NOR_WINRM_ERROR;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NO_RUNNING_PROCESS_MATCH_REGEX_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NO_TEST_WILL_BE_PERFORMED_AIX_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NO_TEST_WILL_BE_PERFORMED_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NO_TEST_WILL_BE_PERFORMED_REMOTELY_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.NO_TEST_WILL_BE_PERFORMED_UNKNOWN_OS_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.OOB_NULL_RESULT_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PASSWORD;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PROCESS_CRITERION_COMMAND_LINE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.RESULT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.RUNNING_PROCESS_MATCH_REGEX_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SERVICE_NAME_NOT_SPECIFIED_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.STRATEGY_TIMEOUT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SUCCESSFUL_OS_DETECTION;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SYSTEM_POWER_UP_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.TEST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.TEST_BODY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.TWGIPC;
import static org.sentrysoftware.metricshub.engine.constants.Constants.USERNAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_CREDENTIALS_NOT_CONFIGURED;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_CRITERION_NO_RESULT_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_CRITERION_UNEXPECTED_RESULT_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_MALFORMED_CRITERION_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_QUERY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WEBM_CRITERION_FAILURE_EXPECTED_RESULT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WEBM_CRITERION_SUCCESS_EXPECTED_RESULT;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WMI_CREDENTIALS_NOT_CONFIGURED;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WMI_CRITERION_TEST_SUCCEED_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WMI_CRITERION_UNEXPECTED_RESULT_MESSAGE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WMI_QUERY_EMPTY_VALUE_MESSAGE;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.client.http.HttpRequest;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.common.helpers.LocalOsHandler;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IpmiConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.OsCommandTestConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SshTestConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.TestConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WbemConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.common.HttpMethod;
import org.sentrysoftware.metricshub.engine.connector.model.common.ResultContent;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.CommandLineCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.DeviceTypeCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.HttpCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.IpmiCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ProcessCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ProductRequirementsCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ServiceCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetNextCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WbemCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WmiCriterion;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.strategy.utils.CriterionProcessVisitor;
import org.sentrysoftware.metricshub.engine.strategy.utils.WqlDetectionHelper;
import org.sentrysoftware.metricshub.engine.telemetry.HostProperties;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * This is a test for {@link CriterionProcessor}
 */
@ExtendWith(MockitoExtension.class)
class CriterionProcessorTest {

	@Mock
	private ClientsExecutor clientsExecutorMock;

	@Mock
	private WqlDetectionHelper wqlDetectionHelperMock;

	@Mock
	private TelemetryManager telemetryManagerMock;

	@InjectMocks
	private CriterionProcessor criterionProcessor;

	private TelemetryManager telemetryManager;
	private WmiConfiguration wmiConfiguration;
	private WbemConfiguration wbemConfiguration;

	private void initWbem() {
		wbemConfiguration = WbemConfiguration.builder().build();
		telemetryManager =
			TelemetryManager
				.builder()
				.hostConfiguration(
					HostConfiguration
						.builder()
						.hostname(LOCALHOST)
						.hostId(LOCALHOST)
						.hostType(DeviceKind.LINUX)
						.configurations(Map.of(WbemConfiguration.class, wbemConfiguration))
						.build()
				)
				.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
	}

	private void initSnmp() {
		final TestConfiguration snmpConfiguration = new TestConfiguration();

		telemetryManager =
			TelemetryManager
				.builder()
				.hostConfiguration(
					HostConfiguration
						.builder()
						.hostname(LOCALHOST)
						.hostId(LOCALHOST)
						.hostType(DeviceKind.LINUX)
						.configurations(Map.of(TestConfiguration.class, snmpConfiguration))
						.build()
				)
				.build();
	}

	@Test
	void testProcessSnmpGetCriterion() {
		initSnmp();

		final IProtocolExtension protocolExtensionMock = spy(IProtocolExtension.class);

		final ExtensionManager extensionManager = ExtensionManager
			.builder()
			.withProtocolExtensions(List.of(protocolExtensionMock))
			.build();

		doReturn(true)
			.when(protocolExtensionMock)
			.isValidConfiguration(telemetryManager.getHostConfiguration().getConfigurations().get(TestConfiguration.class));

		doReturn(Set.of(SnmpGetCriterion.class, SnmpGetNextCriterion.class))
			.when(protocolExtensionMock)
			.getSupportedCriteria();

		final SnmpGetCriterion snmpGetCriterion = SnmpGetCriterion.builder().oid("1.2.3.4.5.6").build();

		final CriterionTestResult expected = CriterionTestResult.builder().success(true).message("success").build();

		doReturn(expected)
			.when(protocolExtensionMock)
			.processCriterion(snmpGetCriterion, MY_CONNECTOR_1_NAME, telemetryManager);

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);
		assertEquals(expected, criterionProcessor.process(snmpGetCriterion));
	}

	@Test
	void testProcessSnmpGetNextCriterion() {
		initSnmp();

		final IProtocolExtension protocolExtensionMock = spy(IProtocolExtension.class);

		final ExtensionManager extensionManager = ExtensionManager
			.builder()
			.withProtocolExtensions(List.of(protocolExtensionMock))
			.build();

		doReturn(true)
			.when(protocolExtensionMock)
			.isValidConfiguration(telemetryManager.getHostConfiguration().getConfigurations().get(TestConfiguration.class));

		doReturn(Set.of(SnmpGetCriterion.class, SnmpGetNextCriterion.class))
			.when(protocolExtensionMock)
			.getSupportedCriteria();

		final SnmpGetNextCriterion snmpGetNextCriterion = SnmpGetNextCriterion.builder().oid("1.2.3.4.5").build();

		final CriterionTestResult expected = CriterionTestResult.builder().success(true).message("success").build();

		doReturn(expected)
			.when(protocolExtensionMock)
			.processCriterion(snmpGetNextCriterion, MY_CONNECTOR_1_NAME, telemetryManager);

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);
		assertEquals(expected, criterionProcessor.process(snmpGetNextCriterion));
	}

	@Test
	void testProcessWbemCriterionSuccess() throws Exception {
		initWbem();
		doReturn(EXCUTE_WBEM_RESULT).when(clientsExecutorMock).executeWql(any(), eq(wbemConfiguration), any(), any());
		final WbemCriterion wbemCriterion = WbemCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_SUCCESS_EXPECTED_RESULT)
			.build();

		final CriterionTestResult result = criterionProcessor.process(wbemCriterion);
		assertTrue(result.isSuccess());
	}

	@Test
	void testProcessWbemCriterionActualResultIsNotExpectedResult() throws Exception {
		initWbem();
		doReturn(EXCUTE_WBEM_RESULT).when(clientsExecutorMock).executeWql(any(), eq(wbemConfiguration), any(), any());
		final WbemCriterion wbemCriterion = WbemCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_FAILURE_EXPECTED_RESULT)
			.build();
		final CriterionTestResult result = criterionProcessor.process(wbemCriterion);
		assertFalse(result.isSuccess());
		assertTrue(result.getMessage().contains(WBEM_CRITERION_UNEXPECTED_RESULT_MESSAGE));
	}

	@Test
	void testProcessWbemCriterionMalformedCriterion() throws Exception {
		final CriterionTestResult result = criterionProcessor.process((WbemCriterion) null);
		assertFalse(result.isSuccess());
		assertTrue(result.getMessage().contains(WBEM_MALFORMED_CRITERION_MESSAGE));
	}

	@Test
	void testProcessWbemEmptyQueryResult() throws Exception {
		initWbem();
		doReturn(List.of()).when(clientsExecutorMock).executeWql(any(), eq(wbemConfiguration), any(), any());
		final WbemCriterion wbemCriterion = WbemCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_SUCCESS_EXPECTED_RESULT)
			.build();

		final CriterionTestResult result = criterionProcessor.process(wbemCriterion);
		assertFalse(result.isSuccess());
		assertEquals(WBEM_CRITERION_NO_RESULT_MESSAGE, result.getResult());
	}

	@Test
	void testProcessWbemCriterionWithNullWbemConfiguration() throws Exception {
		wbemConfiguration = null;
		telemetryManager =
			TelemetryManager
				.builder()
				.hostConfiguration(
					HostConfiguration
						.builder()
						.hostname(LOCALHOST)
						.hostId(LOCALHOST)
						.hostType(DeviceKind.LINUX)
						.configurations(Map.of())
						.build()
				)
				.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		final WbemCriterion wbemCriterion = WbemCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_SUCCESS_EXPECTED_RESULT)
			.build();

		final CriterionTestResult result = criterionProcessor.process(wbemCriterion);
		assertFalse(result.isSuccess());
		assertTrue(result.getMessage().contains(WBEM_CREDENTIALS_NOT_CONFIGURED));
	}

	@Test
	void testProcessWbemCriterionWithClientException() throws Exception {
		initWbem();
		doThrow(ClientException.class).when(clientsExecutorMock).executeWql(any(), eq(wbemConfiguration), any(), any());
		final WbemCriterion wbemCriterion = WbemCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_SUCCESS_EXPECTED_RESULT)
			.build();
		final CriterionTestResult result = criterionProcessor.process(wbemCriterion);
		assertFalse(result.isSuccess());
		assertTrue(result.getException() instanceof ClientException);
	}

	@Test
	void testProcessProcessProcessNull() {
		final ProcessCriterion processCriterion = null;

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build()
			)
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();

		assertEquals(CriterionTestResult.empty(), criterionProcessor.process(processCriterion));
	}

	@Test
	void testProcessProcessCommandLineEmpty() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine("");

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build()
			)
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();

		final CriterionTestResult criterionTestResult = criterionProcessor.process(processCriterion);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(NO_TEST_WILL_BE_PERFORMED_MESSAGE, criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testProcessProcessNotLocalHost() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostProperties(HostProperties.builder().build())
			.hostConfiguration(
				HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build()
			)
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(telemetryManager.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		final CriterionTestResult criterionTestResult = criterionProcessor.process(processCriterion);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(NO_TEST_WILL_BE_PERFORMED_REMOTELY_MESSAGE, criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testProcessProcessUnknownOS() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build()
			)
			.hostProperties(HostProperties.builder().isLocalhost(true).build())
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(telemetryManager.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOS).thenReturn(Optional.empty());

			final CriterionTestResult criterionTestResult = criterionProcessor.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(NO_TEST_WILL_BE_PERFORMED_UNKNOWN_OS_MESSAGE, criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testProcessProcessWindowsEmptyResult() {
		// Init the mocks
		MockitoAnnotations.openMocks(this);

		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build()
			)
			.hostProperties(HostProperties.builder().isLocalhost(true).build())
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(telemetryManager.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOS).thenReturn(Optional.of(LocalOsHandler.WINDOWS));
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOsVersion).thenReturn(Optional.of("5.1"));

			doReturn(CriterionTestResult.error(processCriterion, WMI_QUERY_EMPTY_VALUE_MESSAGE))
				.when(wqlDetectionHelperMock)
				.performDetectionTest(any(), any(), any());

			final CriterionTestResult criterionTestResult = criterionProcessor.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertTrue(criterionTestResult.getMessage().contains(WMI_QUERY_EMPTY_VALUE_MESSAGE));
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testProcessProcessWindowsOK() throws Exception {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);

		final WmiConfiguration wmiConfiguration = WmiConfiguration.builder().timeout(STRATEGY_TIMEOUT).build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(LOCALHOST)
					.hostId(LOCALHOST)
					.hostType(DeviceKind.WINDOWS)
					.configurations(Map.of(WmiConfiguration.class, wmiConfiguration))
					.build()
			)
			.hostProperties(HostProperties.builder().isLocalhost(true).build())
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(telemetryManager.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOS).thenReturn(Optional.of(LocalOsHandler.WINDOWS));
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOsVersion).thenReturn(Optional.of("5.1"));

			final WmiConfiguration localWmiConfiguration = WmiConfiguration
				.builder()
				.username(null)
				.password(null)
				.timeout(30L)
				.build();

			doReturn(EXECUTE_WMI_RESULT)
				.when(clientsExecutorMock)
				.executeWql(LOCALHOST, localWmiConfiguration, WMI_PROCESS_QUERY, CRITERION_WMI_NAMESPACE);

			final CriterionTestResult criterionTestResult = criterionProcessor.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(WMI_CRITERION_TEST_SUCCEED_MESSAGE, criterionTestResult.getMessage());
			assertEquals("MBM6.exe", criterionTestResult.getResult());
		}
	}

	@Test
	void testProcessProcessLinuxNoProcess() {
		final ProcessCriterion process = new ProcessCriterion();
		process.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build()
			)
			.hostProperties(HostProperties.builder().isLocalhost(true).build())
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(telemetryManager.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (
			final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
			final MockedStatic<CriterionProcessVisitor> mockedCriterionProcessVisitorImpl = mockStatic(
				CriterionProcessVisitor.class
			)
		) {
			mockedLocalOSHandler.when(LocalOsHandler::getOS).thenReturn(Optional.of(LocalOsHandler.LINUX));
			mockedCriterionProcessVisitorImpl
				.when(CriterionProcessVisitor::listAllLinuxProcesses)
				.thenReturn(LIST_ALL_LINUX_PROCESSES_RESULT);

			final CriterionTestResult criterionTestResult = criterionProcessor.process(process);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(NO_RUNNING_PROCESS_MATCH_REGEX_MESSAGE, criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testProcessProcessLinuxOK() {
		final ProcessCriterion processCriterion = new ProcessCriterion();
		processCriterion.setCommandLine(PROCESS_CRITERION_COMMAND_LINE);

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build()
			)
			.hostProperties(HostProperties.builder().isLocalhost(true).build())
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(telemetryManager.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (
			final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
			final MockedStatic<CriterionProcessVisitor> mockedCriterionProcessVisitorImpl = mockStatic(
				CriterionProcessVisitor.class
			)
		) {
			mockedLocalOSHandler.when(LocalOsHandler::getOS).thenReturn(Optional.of(LocalOsHandler.LINUX));
			mockedCriterionProcessVisitorImpl
				.when(CriterionProcessVisitor::listAllLinuxProcesses)
				.thenReturn(EXECUTE_WMI_RESULT);

			final CriterionTestResult criterionTestResult = criterionProcessor.process(processCriterion);

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

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.LINUX).build()
			)
			.hostProperties(HostProperties.builder().isLocalhost(true).build())
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(telemetryManager.getHostProperties()).when(telemetryManagerMock).getHostProperties();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOS).thenReturn(Optional.of(LocalOsHandler.AIX));

			final CriterionTestResult criterionTestResult = criterionProcessor.process(processCriterion);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(NO_TEST_WILL_BE_PERFORMED_AIX_MESSAGE, criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testProcessServiceCheckServiceNull() {
		final ServiceCriterion serviceCriterion = null;
		assertTrue(criterionProcessor.process(serviceCriterion).getMessage().contains("Malformed Service criterion."));
	}

	@Test
	void testProcessServiceCheckProtocolNull() {
		final ServiceCriterion serviceCriterion = new ServiceCriterion();
		serviceCriterion.setName(TWGIPC);

		assertTrue(criterionProcessor.process(serviceCriterion).getMessage().contains(NEITHER_WMI_NOR_WINRM_ERROR));
	}

	@Test
	void testProcessServiceCheckOsNull() {
		final WmiConfiguration wmiConfiguration = WmiConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.timeout(STRATEGY_TIMEOUT)
			.build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(HOST_WIN)
					.hostId(HOST_WIN)
					.configurations(Map.of(WmiConfiguration.class, wmiConfiguration))
					.build()
			)
			.build();
		doReturn(wmiConfiguration).when(telemetryManagerMock).getWinConfiguration();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();

		final ServiceCriterion serviceCriterion = new ServiceCriterion();
		serviceCriterion.setName(TWGIPC);

		final CriterionTestResult criterionTestResult = criterionProcessor.process(serviceCriterion);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().contains(HOST_OS_IS_NOT_WINDOWS_MESSAGE));
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testProcessServiceCheckOsNotWindows() {
		final ServiceCriterion serviceCriterion = new ServiceCriterion();
		serviceCriterion.setName(TWGIPC);

		final CriterionTestResult criterionTestResult = criterionProcessor.process(serviceCriterion);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testProcessServiceCheckServiceNameEmpty() {
		final WmiConfiguration wmiConfiguration = WmiConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.timeout(STRATEGY_TIMEOUT)
			.build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(LOCALHOST)
					.hostId(LOCALHOST)
					.hostType(DeviceKind.WINDOWS)
					.configurations(Map.of(wmiConfiguration.getClass(), wmiConfiguration))
					.build()
			)
			.build();
		doReturn(wmiConfiguration).when(telemetryManagerMock).getWinConfiguration();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		final ServiceCriterion serviceCriterion = new ServiceCriterion();
		serviceCriterion.setName("");

		final CriterionTestResult criterionTestResult = criterionProcessor.process(serviceCriterion);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().contains(SERVICE_NAME_NOT_SPECIFIED_MESSAGE));
		assertNotNull(criterionTestResult.getResult());
	}

	@Test
	void testProcessDeviceTypeCriterion() {
		// Init configurations
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.NETWORK).build()
			)
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();

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
		assertEquals(successfulTestResult, criterionProcessor.process(deviceTypeCriterion));

		// Include NETWORK OS

		deviceTypeCriterion.setKeep(Set.of(DeviceKind.NETWORK));
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessor.process(deviceTypeCriterion));

		deviceTypeCriterion.setKeep(Collections.emptySet());
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.NETWORK));
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(failedTestResult, criterionProcessor.process(deviceTypeCriterion));

		// Test Linux OS

		// Exclude only Linux OS with empty keep set
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.LINUX));
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessor.process(deviceTypeCriterion));

		// Include only Linux OS with empty keep set
		deviceTypeCriterion.setKeep(Set.of(DeviceKind.LINUX));
		deviceTypeCriterion.setExclude(Collections.emptySet());
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		criterionProcessor.process(deviceTypeCriterion);
		assertEquals(failedTestResult, criterionProcessor.process(deviceTypeCriterion));

		// Exclude only Linux with empty keep set
		deviceTypeCriterion.setKeep(Collections.emptySet());
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.LINUX));
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessor.process(deviceTypeCriterion));

		// TEST SOLARIS OS

		// Prepare CriterionTestResult with specific SOLARIS CriterionTestResult instances
		successfulTestResult.setResult(CONFIGURED_OS_SOLARIS_MESSAGE);
		failedTestResult.setResult(CONFIGURED_OS_SOLARIS_MESSAGE);
		telemetryManager.setHostConfiguration(
			HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.SOLARIS).build()
		);

		// Exclude only SOLARIS OS
		deviceTypeCriterion.setKeep(Collections.emptySet());
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.SOLARIS));
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(failedTestResult, criterionProcessor.process(deviceTypeCriterion));

		// Include only SOLARIS OS
		deviceTypeCriterion.setKeep(Set.of(DeviceKind.SOLARIS));
		deviceTypeCriterion.setExclude(Collections.emptySet());
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessor.process(deviceTypeCriterion));
	}

	@Test
	void testIsDeviceTypeIncluded() {
		// Create the device criterion instance
		final DeviceTypeCriterion deviceTypeCriterion = DeviceTypeCriterion.builder().build();

		// Prepare the device kind list
		final List<DeviceKind> deviceKindList = Arrays.asList(DeviceKind.STORAGE, DeviceKind.NETWORK, DeviceKind.LINUX);
		assertTrue(criterionProcessor.isDeviceKindIncluded(deviceKindList, deviceTypeCriterion));

		// Keep only Solaris OS
		deviceTypeCriterion.setKeep(Set.of(DeviceKind.SOLARIS));
		assertFalse(criterionProcessor.isDeviceKindIncluded(deviceKindList, deviceTypeCriterion));

		// Keep only Linux
		deviceTypeCriterion.setKeep(Set.of(DeviceKind.LINUX));
		assertTrue(criterionProcessor.isDeviceKindIncluded(deviceKindList, deviceTypeCriterion));

		// Exclude only Solaris and with empty keep set
		deviceTypeCriterion.setKeep(Collections.emptySet());
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.SOLARIS));
		assertTrue(criterionProcessor.isDeviceKindIncluded(deviceKindList, deviceTypeCriterion));

		// Exclude only Linux
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.LINUX));
		assertFalse(criterionProcessor.isDeviceKindIncluded(deviceKindList, deviceTypeCriterion));
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

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		// The extension manager is empty because it is not involved in this test
		final ExtensionManager extensionManager = ExtensionManager.empty();

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);

		assertEquals(CriterionTestResult.empty(), criterionProcessor.process(httpCriterion));
	}

	@Test
	void HttpCriterionProcessHttpConfigurationNullTest() {
		final HttpCriterion httpCriterion = HttpCriterion
			.builder()
			.type(HTTP)
			.method(HttpMethod.GET)
			.url(TEST)
			.body(TEST_BODY)
			.resultContent(ResultContent.ALL)
			.expectedResult(RESULT)
			.errorMessage(ERROR)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().build();

		// The extension manager is empty because it is not involved in this test
		final ExtensionManager extensionManager = ExtensionManager.empty();

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);

		assertEquals(CriterionTestResult.empty(), criterionProcessor.process(httpCriterion));
	}

	@Test
	void HttpCriterionProcessRequestWrongResultTest() throws IOException {
		final HttpCriterion httpCriterion = HttpCriterion
			.builder()
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

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		final String result = "Something went Wrong";
		final HttpRequest httpRequest = HttpRequest
			.builder()
			.hostname(HOST_ID)
			.method(HTTP_GET)
			.url(httpCriterion.getUrl())
			.header(httpCriterion.getHeader(), MY_CONNECTOR_1_NAME, HOST_ID)
			.body(httpCriterion.getBody(), MY_CONNECTOR_1_NAME, HOST_ID)
			.httpConfiguration(httpConfiguration)
			.resultContent(httpCriterion.getResultContent())
			.authenticationToken(httpCriterion.getAuthenticationToken())
			.build();
		doReturn(result).when(clientsExecutorMock).executeHttp(httpRequest, false);

		// The extension manager is empty because it is not involved in this test
		final ExtensionManager extensionManager = ExtensionManager.empty();

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);

		final String message = String.format(
			"Hostname %s - HTTP test failed - " +
			"The result (%s) returned by the HTTP test did not match the expected result (%s)." +
			"Expected value: %s - returned value %s.",
			HOST_ID,
			result,
			RESULT,
			RESULT,
			result
		);
		final CriterionTestResult criterionTestResult = criterionProcessor.process(httpCriterion);

		assertEquals(result, criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(message, criterionTestResult.getMessage());
		assertNull(criterionTestResult.getException());
	}

	@Test
	void HttpCriterionProcessOKTest() throws IOException {
		final HttpCriterion httpCriterion = HttpCriterion
			.builder()
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

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		final HttpRequest httpRequest = HttpRequest
			.builder()
			.hostname(HOST_ID)
			.method(HTTP_GET)
			.url(httpCriterion.getUrl())
			.header(httpCriterion.getHeader(), MY_CONNECTOR_1_NAME, HOST)
			.body(httpCriterion.getBody(), MY_CONNECTOR_1_NAME, HOST_ID)
			.httpConfiguration(httpConfiguration)
			.resultContent(httpCriterion.getResultContent())
			.authenticationToken(httpCriterion.getAuthenticationToken())
			.build();
		doReturn(RESULT).when(clientsExecutorMock).executeHttp(httpRequest, false);

		// The extension manager is empty because it is not involved in this test
		final ExtensionManager extensionManager = ExtensionManager.empty();

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);

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
		MockitoAnnotations.openMocks(this);

		// Init configurations
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		final WmiConfiguration wmiProtocol = WmiConfiguration
			.builder()
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
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		// Mock getHostConfiguration and getWinConfiguration
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(configurations.get(wmiProtocol.getClass())).when(telemetryManagerMock).getWinConfiguration();

		// Mock performDetectionTest
		doReturn(CriterionTestResult.success(ipmi, IPMI_SUCCESS_MESSAGE))
			.when(wqlDetectionHelperMock)
			.performDetectionTest(any(), any(), any());

		final CriterionTestResult criterionTestResult = criterionProcessor.process(ipmi);

		assertNotNull(criterionTestResult);
		assertEquals(IPMI_SUCCESS_MESSAGE, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());
	}

	@Test
	void testProcessIpmiWindowsFailure() {
		// Init the mocks
		MockitoAnnotations.openMocks(this);

		// Init configurations
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		final WmiConfiguration wmiProtocol = WmiConfiguration
			.builder()
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
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		// mock getHostConfiguration, getWinConfiguration and performDetectionTest
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(configurations.get(wmiProtocol.getClass())).when(telemetryManagerMock).getWinConfiguration();
		doReturn(CriterionTestResult.success(ipmi, IPMI_FAILURE_MESSAGE))
			.when(wqlDetectionHelperMock)
			.performDetectionTest(any(), any(), any());

		final CriterionTestResult criterionTestResult = criterionProcessor.process(ipmi);

		assertNotNull(criterionTestResult);
		assertEquals(IPMI_FAILURE_MESSAGE, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());
	}

	@Test
	void testProcessIPMIOutOfBandConfigurationNotFound() {
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(MANAGEMENT_CARD_HOST)
					.hostType(DeviceKind.OOB)
					.hostname(MANAGEMENT_CARD_HOST)
					.configurations(Collections.emptyMap())
					.build()
			)
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(CriterionTestResult.empty(), criterionProcessor.process(new IpmiCriterion()));
	}

	@Test
	void testProcessIPMIOutOfBand() throws Exception {
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(MANAGEMENT_CARD_HOST)
					.hostType(DeviceKind.OOB)
					.hostname(MANAGEMENT_CARD_HOST)
					.configurations(
						Map.of(
							IpmiConfiguration.class,
							IpmiConfiguration.builder().username(USERNAME).password(PASSWORD.toCharArray()).build()
						)
					)
					.build()
			)
			.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(SYSTEM_POWER_UP_MESSAGE)
			.when(clientsExecutorMock)
			.executeIpmiDetection(eq(MANAGEMENT_CARD_HOST), any(IpmiConfiguration.class));
		assertEquals(
			CriterionTestResult
				.builder()
				.result(SYSTEM_POWER_UP_MESSAGE)
				.message(IPMI_CONNECTION_SUCCESS_WITH_IMPI_OVER_LAN_MESSAGE)
				.success(true)
				.build(),
			criterionProcessor.process(new IpmiCriterion())
		);
	}

	@Test
	void testProcessIPMIOutOfBandNullResult() throws Exception {
		final IpmiConfiguration ipmiConfiguration = IpmiConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.build();
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();

		configurations.put(IpmiConfiguration.class, ipmiConfiguration);
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(MANAGEMENT_CARD_HOST)
			.hostType(DeviceKind.OOB)
			.hostname(MANAGEMENT_CARD_HOST)
			.configurations(configurations)
			.build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostProperties(HostProperties.builder().isLocalhost(true).build())
			.hostConfiguration(hostConfiguration)
			.build();

		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(null)
			.when(clientsExecutorMock)
			.executeIpmiDetection(eq(MANAGEMENT_CARD_HOST), any(IpmiConfiguration.class));
		assertEquals(
			CriterionTestResult.builder().message(OOB_NULL_RESULT_MESSAGE).build().getMessage(),
			criterionProcessor.process(new IpmiCriterion()).getMessage()
		);
	}

	@Test
	void testVisitCommandLineLineEmpty() {
		final CommandLineCriterion commandLineCriterion = new CommandLineCriterion();
		commandLineCriterion.setCommandLine("");
		commandLineCriterion.setExpectedResult("Agent Rev:");
		commandLineCriterion.setExecuteLocally(true);
		commandLineCriterion.setErrorMessage("Unable to connect using Navisphere");

		final TelemetryManager telemetryManager = TelemetryManager.builder().build();

		// The extension manager is empty because it is not involved in this test
		final ExtensionManager extensionManager = ExtensionManager.empty();

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);

		final CriterionTestResult criterionTestResult = criterionProcessor.process(commandLineCriterion);

		assertNotNull(criterionTestResult);
	}

	@Test
	void productRequirementsCriterionProcessCriterionNullTest() {
		final ProductRequirementsCriterion productRequirementsCriterion = null;

		assertTrue(new CriterionProcessor().process(productRequirementsCriterion).isSuccess());
	}

	@Test
	void productRequirementsCriterionProcessCriterionNullVersionTest() {
		final ProductRequirementsCriterion productRequirementsCriterion = ProductRequirementsCriterion.builder().build();

		assertTrue(new CriterionProcessor().process(productRequirementsCriterion).isSuccess());
	}

	@Test
	void productRequirementsCriterionProcessCriterionEmptyVersionTest() {
		final ProductRequirementsCriterion productRequirementsCriterion = ProductRequirementsCriterion
			.builder()
			.engineVersion("")
			.build();

		assertTrue(new CriterionProcessor().process(productRequirementsCriterion).isSuccess());
	}

	@Test
	void productRequirementsCriterionProcessCriterionOKTest() {
		final ProductRequirementsCriterion productRequirementsCriterion = ProductRequirementsCriterion
			.builder()
			.engineVersion(LOW_VERSION_NUMBER)
			.build();
		assertTrue(new CriterionProcessor().process(productRequirementsCriterion).isSuccess());
	}

	@Test
	void productRequirementsCriterionProcessCriterionNOKTest() {
		final ProductRequirementsCriterion productRequirementsCriterion = ProductRequirementsCriterion
			.builder()
			.engineVersion(HIGH_VERSION_NUMBER) // We will need to update the test once we reach metricshub-engine version 1000
			.build();

		assertFalse(new CriterionProcessor().process(productRequirementsCriterion).isSuccess());
	}

	private void initWmi() {
		wmiConfiguration = WmiConfiguration.builder().build();
		telemetryManager =
			TelemetryManager
				.builder()
				.hostConfiguration(
					HostConfiguration
						.builder()
						.hostname(LOCALHOST)
						.hostId(LOCALHOST)
						.hostType(DeviceKind.WINDOWS)
						.configurations(Map.of(WmiConfiguration.class, wmiConfiguration))
						.build()
				)
				.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		doReturn(telemetryManager.getHostConfiguration().getConfigurations().get(WmiConfiguration.class))
			.when(telemetryManagerMock)
			.getWinConfiguration();
	}

	@Test
	void testProcessWmiCriterionSuccess() throws Exception {
		initWmi();
		doReturn(EXCUTE_WBEM_RESULT).when(clientsExecutorMock).executeWql(any(), eq(wmiConfiguration), any(), any());
		final WmiCriterion wmiCriterion = WmiCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_SUCCESS_EXPECTED_RESULT)
			.build();

		final CriterionTestResult result = criterionProcessor.process(wmiCriterion);
		assertTrue(result.isSuccess());
	}

	@Test
	void testProcessWmiCriterionActualResultIsNotExpectedResult() throws Exception {
		initWmi();
		doReturn(EXCUTE_WBEM_RESULT).when(clientsExecutorMock).executeWql(any(), eq(wmiConfiguration), any(), any());
		final WmiCriterion wmiCriterion = WmiCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_FAILURE_EXPECTED_RESULT)
			.build();
		final CriterionTestResult result = criterionProcessor.process(wmiCriterion);
		assertFalse(result.isSuccess());
		assertTrue(result.getMessage().contains(WMI_CRITERION_UNEXPECTED_RESULT_MESSAGE));
	}

	@Test
	void testProcessWmiCriterionMalformedCriterion() throws Exception {
		final CriterionTestResult result = criterionProcessor.process((WmiCriterion) null);
		assertFalse(result.isSuccess());
		assertTrue(result.getMessage().contains(WBEM_MALFORMED_CRITERION_MESSAGE));
	}

	@Test
	void testProcessWmiEmptyQueryResult() throws Exception {
		initWmi();
		doReturn(List.of()).when(clientsExecutorMock).executeWql(any(), eq(wmiConfiguration), any(), any());
		final WmiCriterion wmiCriterion = WmiCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_SUCCESS_EXPECTED_RESULT)
			.build();

		final CriterionTestResult result = criterionProcessor.process(wmiCriterion);
		assertFalse(result.isSuccess());
		assertEquals(WBEM_CRITERION_NO_RESULT_MESSAGE, result.getResult());
	}

	@Test
	void testProcessWmiCriterionWithNullWmiConfiguration() throws Exception {
		wmiConfiguration = null;
		telemetryManager =
			TelemetryManager
				.builder()
				.hostConfiguration(
					HostConfiguration
						.builder()
						.hostname(LOCALHOST)
						.hostId(LOCALHOST)
						.hostType(DeviceKind.LINUX)
						.configurations(Map.of())
						.build()
				)
				.build();
		doReturn(telemetryManager.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		final WmiCriterion wmiCriterion = WmiCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_SUCCESS_EXPECTED_RESULT)
			.build();

		final CriterionTestResult result = criterionProcessor.process(wmiCriterion);
		assertFalse(result.isSuccess());
		assertTrue(result.getMessage().contains(WMI_CREDENTIALS_NOT_CONFIGURED));
	}

	@Test
	void testProcessWmiCriterionWithClientException() throws Exception {
		initWmi();
		doThrow(ClientException.class).when(clientsExecutorMock).executeWql(any(), eq(wmiConfiguration), any(), any());
		final WmiCriterion wmiCriterion = WmiCriterion
			.builder()
			.query(WBEM_QUERY)
			.expectedResult(WEBM_CRITERION_SUCCESS_EXPECTED_RESULT)
			.build();
		final CriterionTestResult result = criterionProcessor.process(wmiCriterion);
		assertFalse(result.isSuccess());
		assertTrue(result.getException() instanceof ClientException);
	}

	@Test
	@EnabledOnOs(LINUX)
	void testVisitCommandLineLinuxError() {
		final CommandLineCriterion commandLineCriterion = new CommandLineCriterion();
		commandLineCriterion.setCommandLine("sleep 5");
		commandLineCriterion.setExpectedResult(" ");
		commandLineCriterion.setExecuteLocally(true);
		commandLineCriterion.setErrorMessage("No date.");
		commandLineCriterion.setTimeout(1L);

		final SshTestConfiguration sshConfiguration = SshTestConfiguration
			.builder()
			.username(" ")
			.password("pwd".toCharArray())
			.build();

		final OsCommandTestConfiguration osCommandConfiguration = new OsCommandTestConfiguration();
		osCommandConfiguration.setTimeout(1L);

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId("id")
			.hostname("localhost")
			.hostType(DeviceKind.WINDOWS)
			.configurations(
				Map.of(sshConfiguration.getClass(), sshConfiguration, osCommandConfiguration.getClass(), osCommandConfiguration)
			)
			.build();

		final HostProperties hostProperties = HostProperties.builder().isLocalhost(true).build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(hostProperties)
			.build();

		// The extension manager is empty because it is not involved in this test
		final ExtensionManager extensionManager = ExtensionManager.empty();

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);

		final CriterionTestResult criterionTestResult = criterionProcessor.process(commandLineCriterion);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
			"Error in CommandLineCriterion test:\n" +
			commandLineCriterion.toString() +
			"\n\n" +
			"TimeoutException: Command \"sleep 5\" execution has timed out after 1 s",
			criterionTestResult.getMessage()
		);
		assertNull(criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(LINUX)
	void testVisitCommandLineLocalLinuxFailedToMatchCriteria() {
		final String result = "Test";

		final CommandLineCriterion commandLineCriterion = new CommandLineCriterion();
		commandLineCriterion.setCommandLine("echo Test");
		commandLineCriterion.setExpectedResult("Nothing");
		commandLineCriterion.setExecuteLocally(true);
		commandLineCriterion.setErrorMessage("No display.");

		final SshTestConfiguration sshConfiguration = SshTestConfiguration
			.builder()
			.username(" ")
			.password("pwd".toCharArray())
			.timeout(1L)
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId("id")
			.hostname("localhost")
			.hostType(DeviceKind.LINUX)
			.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
			.build();

		final HostProperties hostProperties = HostProperties.builder().isLocalhost(true).build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(hostProperties)
			.build();

		// The extension manager is empty because it is not involved in this test
		final ExtensionManager extensionManager = ExtensionManager.empty();

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);

		final CriterionTestResult criterionTestResult = criterionProcessor.process(commandLineCriterion);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
			"CommandLineCriterion test ran but failed:\n" +
			commandLineCriterion.toString() +
			"\n\n" +
			"Actual result:\n" +
			result,
			criterionTestResult.getMessage()
		);
		assertEquals(result, criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(LINUX)
	void testVisitCommandLineLocalLinux() {
		final String result = "Test";

		final CommandLineCriterion commandLineCriterion = new CommandLineCriterion();
		commandLineCriterion.setCommandLine("echo Test");
		commandLineCriterion.setExpectedResult(result);
		commandLineCriterion.setExecuteLocally(true);
		commandLineCriterion.setErrorMessage("No display.");

		final SshTestConfiguration sshConfiguration = SshTestConfiguration
			.builder()
			.username(" ")
			.password("pwd".toCharArray())
			.timeout(1L)
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId("id")
			.hostname("localhost")
			.hostType(DeviceKind.LINUX)
			.configurations(Map.of(sshConfiguration.getClass(), sshConfiguration))
			.build();

		final HostProperties hostProperties = HostProperties.builder().isLocalhost(true).build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(hostProperties)
			.build();

		// The extension manager is empty because it is not involved in this test
		final ExtensionManager extensionManager = ExtensionManager.empty();

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			clientsExecutorMock,
			telemetryManager,
			MY_CONNECTOR_1_NAME,
			extensionManager
		);

		final CriterionTestResult criterionTestResult = criterionProcessor.process(commandLineCriterion);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
			"CommandLineCriterion test succeeded:\n" + commandLineCriterion.toString() + "\n\n" + "Result: " + result,
			criterionTestResult.getMessage()
		);
		assertEquals(result, criterionTestResult.getResult());
	}
}
