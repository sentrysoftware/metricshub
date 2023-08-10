package com.sentrysoftware.matrix.strategy.detection;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.configuration.IConfiguration;
import com.sentrysoftware.matrix.configuration.WmiConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
import com.sentrysoftware.matrix.connector.model.identity.criterion.DeviceTypeCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.HttpCriterion;
import com.sentrysoftware.matrix.matsya.HttpRequest;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.sentrysoftware.matrix.constants.Constants.CONFIGURED_OS_NT_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.CONFIGURED_OS_SOLARIS_MESSAGE;
import static com.sentrysoftware.matrix.constants.Constants.ERROR;
import static com.sentrysoftware.matrix.constants.Constants.FAILED_OS_DETECTION;
import static com.sentrysoftware.matrix.constants.Constants.HOST_ID;
import static com.sentrysoftware.matrix.constants.Constants.HTTP;
import static com.sentrysoftware.matrix.constants.Constants.HTTP_GET;
import static com.sentrysoftware.matrix.constants.Constants.LOCALHOST;
import static com.sentrysoftware.matrix.constants.Constants.MY_CONNECTOR_1_NAME;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.RESULT;
import static com.sentrysoftware.matrix.constants.Constants.STRATEGY_TIMEOUT;
import static com.sentrysoftware.matrix.constants.Constants.SUCCESSFUL_OS_DETECTION;
import static com.sentrysoftware.matrix.constants.Constants.TEST;
import static com.sentrysoftware.matrix.constants.Constants.TEST_BODY;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class CriterionProcessorTest {

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutorMOck;

	@Mock
	private CriterionProcessor criterionProcessorMock;

	@Mock
	private TelemetryManager telemetryManagerMock;

	@Test
	void testProcessDeviceTypeCriterion() {
		// Init the mocks
		MockitoAnnotations.initMocks(this);

		// Init configurations
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		final WmiConfiguration wmiProtocol = WmiConfiguration.builder()
				.namespace(LOCALHOST)
				.username(USERNAME)
				.password(PASSWORD.toCharArray())
				.timeout(STRATEGY_TIMEOUT)
				.build();
		// Add configurations to configurations Map
		configurations.put(wmiProtocol.getClass(), wmiProtocol);
		final HostConfiguration hostConfiguration = HostConfiguration
				.builder()
				.hostname(LOCALHOST)
				.hostId(LOCALHOST)
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

		final DeviceTypeCriterion deviceTypeCriterion = DeviceTypeCriterion.builder().build();
		assertEquals(successfulTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		deviceTypeCriterion.setKeep(Set.of(DeviceKind.NETWORK));
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		deviceTypeCriterion.setKeep(Collections.emptySet());
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.NETWORK));
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(failedTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		deviceTypeCriterion.setExclude(Set.of(DeviceKind.LINUX));
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		deviceTypeCriterion.setKeep(Set.of(DeviceKind.LINUX));
		deviceTypeCriterion.setExclude(Collections.emptySet());
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		criterionProcessorMock.process(deviceTypeCriterion);
		assertEquals(failedTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		successfulTestResult.setResult(CONFIGURED_OS_SOLARIS_MESSAGE);
		failedTestResult.setResult(CONFIGURED_OS_SOLARIS_MESSAGE);
		engineConfiguration.setHostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST)
				.hostType(DeviceKind.SOLARIS).build());

		deviceTypeCriterion.setKeep(Set.of(DeviceKind.LINUX));
		deviceTypeCriterion.setExclude(Collections.emptySet());
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(failedTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		deviceTypeCriterion.setKeep(Set.of(DeviceKind.SOLARIS));
		deviceTypeCriterion.setExclude(Collections.emptySet());
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		deviceTypeCriterion.setKeep(Collections.emptySet());
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.LINUX));
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(successfulTestResult, criterionProcessorMock.process(deviceTypeCriterion));

		deviceTypeCriterion.setKeep(Collections.emptySet());
		deviceTypeCriterion.setExclude(Set.of(DeviceKind.SOLARIS));
		doReturn(engineConfiguration.getHostConfiguration()).when(telemetryManagerMock).getHostConfiguration();
		assertEquals(failedTestResult, criterionProcessorMock.process(deviceTypeCriterion));
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
				matsyaClientsExecutorMOck,
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
				matsyaClientsExecutorMOck,
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
		doReturn(result).when(matsyaClientsExecutorMOck).executeHttp(httpRequest, false);

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
				matsyaClientsExecutorMOck,
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
		doReturn(RESULT).when(matsyaClientsExecutorMOck).executeHttp(httpRequest, false);

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
				matsyaClientsExecutorMOck,
				telemetryManager,
				MY_CONNECTOR_1_NAME);

		final String message = "Hostname PC-120 - HTTP test succeeded. Returned result: result.";
		final CriterionTestResult criterionTestResult = criterionProcessor.process(httpCriterion);

		assertEquals(RESULT, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(message, criterionTestResult.getMessage());
		assertNull(criterionTestResult.getException());
	}
}
