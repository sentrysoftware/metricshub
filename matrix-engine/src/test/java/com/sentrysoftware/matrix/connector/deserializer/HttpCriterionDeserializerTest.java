package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
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

class HttpCriterionDeserializerTest {

	@Test
	/**
	 * Checks input properties for Http detection criteria
	 *
	 * @throws IOException
	 */
	void testDeserializeHttpCriterion() throws Exception {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector httpCriterion = deserializer
				.deserialize(new File("src/test/resources/test-files/connector/httpCriterion.yaml"));

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

		assertNotNull(httpCriterion);
		assertEquals("httpCriterion", httpCriterion.getConnectorIdentity().getCompiledFilename());

		assertNotNull(httpCriterion.getConnectorIdentity().getDetection());
		assertEquals(expected, httpCriterion.getConnectorIdentity().getDetection().getCriteria());
	}

	@Test
	/**
	 * Checks that fields that cannot be null throw an error when they are null
	 * 
	 * @throws IOException
	 */
	void testDeserializeNonNull() throws Exception {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final File connectorFile = new File("src/test/resources/test-files/connector/httpCriterionNonNull.yaml");
		try {
			deserializer.deserialize(connectorFile);
			Assert.fail("Expected an MismatchedInputException to be thrown");
		} catch (MismatchedInputException e) {
			final String message = "Missing required creator property 'url' (index 3)";
			assertTrue(
				e.getMessage().contains(message),
				() -> "Expected exception contains: " + message + ". But got: " + e.getMessage()
			);
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
			final ConnectorDeserializer deserializer = new ConnectorDeserializer();
			final File connectorFile = new File("src/test/resources/test-files/connector/httpCriterionHttpMethodEnum.yaml");
			try {
				deserializer.deserialize(connectorFile);
				Assert.fail("Expected an JsonMappingException to be thrown");
			} catch (JsonMappingException e) {
				String message = "not one of the values accepted for Enum class: [POST, DELETE, GET]";
				assertTrue(
					e.getMessage().contains(message),
					() -> "Expected exception contains: " + message + ". But got: " + e.getMessage()
				);
			}
		}

		// pass on Enum values
		{
			for (HttpMethod method : HttpMethod.values()) {
				final ConnectorDeserializer deserializer = new ConnectorDeserializer();
				final File connectorFile = new File(
					String.format(
						"src/test/resources/test-files/connector/httpCriterionHttpMethodEnum%s.yaml",
						method
					)
				);
				final Connector connector = deserializer.deserialize(
						connectorFile);

				assertNotNull(connector);
				assertEquals(
					String.format("httpCriterionHttpMethodEnum%s", method),
					connector.getConnectorIdentity().getCompiledFilename()
				);

				assertNotNull(connector.getConnectorIdentity().getDetection().getCriteria());
				assertEquals(1, connector.getConnectorIdentity().getDetection().getCriteria().size());

				assertEquals(method, ((Http) connector.getConnectorIdentity().getDetection().getCriteria().get(0)).getMethod());
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
			final ConnectorDeserializer deserializer = new ConnectorDeserializer();
			final File connectorFile = new File("src/test/resources/test-files/connector/httpCriterionResultContentEnum.yaml");
			try {
				deserializer.deserialize(connectorFile);
				Assert.fail("Expected an JsonMappingException to be thrown");
			} catch (JsonMappingException e) {
				String message = "'toto' is not a supported ResultContent";
				assertTrue(
					e.getMessage().contains(message),
					() -> "Expected exception contains: " + message + ". But got: " + e.getMessage()
				);
			}
		}

		// pass on Enum values
		{
			for (ResultContent resultContent : ResultContent.values()) {
				final ConnectorDeserializer deserializer = new ConnectorDeserializer();
				final File connectorFile = new File(
					String.format(
						"src/test/resources/test-files/connector/httpCriterionResultContentEnum%s.yaml",
						resultContent
					)
				);
				final Connector connector = deserializer.deserialize(connectorFile);

				assertNotNull(connector);
				assertEquals(
					String.format("httpCriterionResultContentEnum%s", resultContent),
					connector.getConnectorIdentity().getCompiledFilename()
				);

				assertNotNull(connector.getConnectorIdentity().getDetection().getCriteria());
				assertEquals(1, connector.getConnectorIdentity().getDetection().getCriteria().size());

				assertEquals(resultContent,
						((Http) connector.getConnectorIdentity().getDetection().getCriteria().get(0))
								.getResultContent());
			}
		}
	}
}
