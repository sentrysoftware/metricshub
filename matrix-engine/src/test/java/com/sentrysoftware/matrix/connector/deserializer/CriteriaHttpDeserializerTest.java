package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Http;

class CriteriaHttpDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/http/";
	}

	@Test
	/**
	 * Checks input properties for Http detection criteria
	 *
	 * @throws IOException
	 */
	void testDeserializeHttpCriterion() throws Exception {
		final String testResource = "httpCriterion";
		final Connector httpCriterion = getConnector(testResource);

		final List<Criterion> expected = new ArrayList<>();

		final Http http1 = Http.builder()
				.type("http")
				.method(HttpMethod.GET) // The connector doesn't define the method (default: Get)
				.url("test")
				.header("$embedded.http-header")
				.authenticationToken("$embedded.authenticationToken")
				.body("test-body")
				.resultContent(ResultContent.ALL)
				.expectedResult("result")
				.errorMessage("error")
				.build();
		final Http http2 = Http.builder()
				.type("http")
				.method(HttpMethod.GET) // The connector doesn't define the method (default: Get)
				.url("test/path1")
				.header("$embedded.http-header")
				.authenticationToken("$embedded.authenticationToken")
				.body("test-body")
				.resultContent(ResultContent.BODY) // The connector doesn't define the resultConent
				.expectedResult("result")
				.errorMessage("error")
				.build();
		final Http http3 = Http.builder()
				.type("http")
				.method(HttpMethod.POST)
				.url("test/path2")
				.header("$embedded.http-header")
				.authenticationToken("$embedded.authenticationToken")
				.body("test-body")
				.resultContent(ResultContent.BODY) // The Connector defines a null resultContent
				.expectedResult("result")
				.errorMessage("error")
				.build();

		expected.addAll(List.of(http1, http2, http3));

		compareCriterion(testResource, httpCriterion, expected);
	}

	@Test
	/**
	 * Checks that fields that cannot be null throw an error when they are null
	 * 
	 * @throws IOException
	 */
	void testDeserializeNonNull() throws Exception {
		try {
			getConnector("httpCriterionNonNull");
			Assert.fail("Expected an MismatchedInputException to be thrown");
		} catch (MismatchedInputException e) {
			final String message = "Missing required creator property 'url' (index 3)";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that httpMethod if defined is GET, POST or DELETE
	 * 
	 * @throws IOException
	 */
	void testDeserializeHttpMethodEnum() throws Exception {
		// fail on not enum value
		{
			try {
				getConnector("httpCriterionHttpMethodEnum");
				Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
			} catch (JsonMappingException e) {
				String message = "not one of the values accepted for Enum class: [POST, DELETE, GET]";
				checkMessage(e, message);
			}
		}

		// pass on Enum values
		{
			for (HttpMethod method : HttpMethod.values()) {
				String testResource = String.format("httpCriterionHttpMethodEnum%s", method);
				Connector connector = getConnector(testResource);

				assertNotNull(connector);
				assertEquals(
						testResource,
						connector.getConnectorIdentity().getCompiledFilename());

				assertNotNull(connector.getConnectorIdentity().getDetection().getCriteria());
				assertEquals(1, connector.getConnectorIdentity().getDetection().getCriteria().size());

				assertEquals(method,
						((Http) connector.getConnectorIdentity().getDetection().getCriteria().get(0)).getMethod());
			}
		}
	}

	@Test
	/**
	 * Checks that resultContent if defined is httpStatus, header, body, or all
	 * 
	 * @throws IOException
	 */
	void testDeserializeResultContentEnum() throws Exception {
		// fail on not enum value
		{
			try {
				getConnector("httpCriterionResultContentEnum");
				Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
			} catch (JsonMappingException e) {
				String message = "'toto' is not a supported ResultContent";
				checkMessage(e, message);
			}
		}

		// pass on Enum values
		{
			for (ResultContent resultContent : ResultContent.values()) {
				String testResource = String.format("httpCriterionResultContentEnum%s", resultContent);
				Connector connector = getConnector(testResource);

				assertNotNull(connector);
				assertEquals(
						testResource,
						connector.getConnectorIdentity().getCompiledFilename());

				assertNotNull(connector.getConnectorIdentity().getDetection().getCriteria());
				assertEquals(1, connector.getConnectorIdentity().getDetection().getCriteria().size());

				assertEquals(resultContent,
						((Http) connector.getConnectorIdentity().getDetection().getCriteria().get(0))
								.getResultContent());
			}
		}
	}
}
