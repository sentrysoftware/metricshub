package com.sentrysoftware.matrix.strategy.detection;

import static com.sentrysoftware.matrix.constants.Constants.ERROR;
import static com.sentrysoftware.matrix.constants.Constants.HOST_ID;
import static com.sentrysoftware.matrix.constants.Constants.HTTP;
import static com.sentrysoftware.matrix.constants.Constants.HTTP_GET;
import static com.sentrysoftware.matrix.constants.Constants.MY_CONNECTOR_1_NAME;
import static com.sentrysoftware.matrix.constants.Constants.RESULT;
import static com.sentrysoftware.matrix.constants.Constants.TEST;
import static com.sentrysoftware.matrix.constants.Constants.TEST_BODY;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
import com.sentrysoftware.matrix.connector.model.identity.criterion.HttpCriterion;
import com.sentrysoftware.matrix.matsya.HttpRequest;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class CriterionProcessorTest {

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

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
				matsyaClientsExecutor,
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
				matsyaClientsExecutor,
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
		doReturn(result).when(matsyaClientsExecutor).executeHttp(httpRequest, false);

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
				matsyaClientsExecutor,
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
		doReturn(RESULT).when(matsyaClientsExecutor).executeHttp(httpRequest, false);

		final CriterionProcessor criterionProcessor = new CriterionProcessor(
				matsyaClientsExecutor,
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
