package com.sentrysoftware.matrix.strategy.detection;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import static com.sentrysoftware.matrix.constants.Constants.RESULT;
import static com.sentrysoftware.matrix.constants.Constants.SUCCESSFUL_OS_DETECTION;
import static com.sentrysoftware.matrix.constants.Constants.TEST;
import static com.sentrysoftware.matrix.constants.Constants.TEST_BODY;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class CriterionProcessorTest {

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutorMOck;

	@InjectMocks
	private CriterionProcessor criterionProcessorMock;

	@Mock
	private TelemetryManager telemetryManagerMock;

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
