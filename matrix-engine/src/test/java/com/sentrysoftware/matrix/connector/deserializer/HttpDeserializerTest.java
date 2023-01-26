package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Http;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class HttpDeserializerTest {

	@Test
	/**
	 * Checks input properties for Http detection criteria
	 *
	 * @throws IOException
	 */
	void testDeserializeDoesntThrow() throws IOException {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector httpCriterion = deserializer
				.deserialize(new File("src/test/resources/test-files/connector/httpCriterion.yaml"));

		List<Criterion> expected = new ArrayList<>();

		final String headerText = "Content-Type: application/json\n"
				+ "Accept: application/json\n"
				+ "Cookie: %{AUTHENTICATIONTOKEN}";

		Http http = new Http();
		http.setMethod("GET");
		http.setUrl("test");
		http.setHeader(headerText);
		http.setAuthenticationToken("test-auth-token");
		http.setBody("test-body");
		http.setResultContent(ResultContent.ALL);
		http.setExpectedResult("result");

		expected.add(http);

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
	void testDeserializeNonNull() throws IOException {
		try {
			final ConnectorDeserializer deserializer = new ConnectorDeserializer();
			deserializer.deserialize(new File("src/test/resources/test-files/connector/httpCriterionNonNull.yaml"));
			Assert.fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("cannot be null"));
		}
	}

	@Test
	/**
	 * Checks that httpMethod if defined is GET, POST or DELETE
	 * 
	 * @throws IOException
	 */
	void testDeserializeHttpMethodEnum() throws IOException {
		// fail on not enum value
		{
			try {
				final ConnectorDeserializer deserializer = new ConnectorDeserializer();
				deserializer.deserialize(
						new File("src/test/resources/test-files/connector/httpCriterionHttpMethodEnum.yaml"));
				Assert.fail();
			} catch (IllegalArgumentException e) {
				assertEquals(String.format("HttpMethod must be one of [ {} ]", HttpMethod.values().toString()),
						e.getMessage());
			}
		}

		// pass on Enum values
		{
			for (HttpMethod method : HttpMethod.values()) {
				final ConnectorDeserializer deserializer = new ConnectorDeserializer();
				final Connector connector = deserializer.deserialize(new File(
						String.format("src/test/resources/test-files/connector/httpCriterionHttpMethodEnum{}.yaml",
								method.getName())));

				assertNotNull(connector);
				assertEquals(String.format("httpCriterionHttpMethodEnum{}", method),
						connector.getConnectorIdentity().getCompiledFilename());

				assertNotNull(connector.getConnectorIdentity().getDetection().getCriteria());
				assertEquals(1, connector.getConnectorIdentity().getDetection().getCriteria().size());

				assertEquals(method, HttpMethod.getByName(
						((Http) connector.getConnectorIdentity().getDetection().getCriteria().get(0)).getMethod()));
			}
		}
	}

	@Test
	/**
	 * Checks that resultContent if defined is httpStatus, header, body, or all
	 * 
	 * @throws IOException
	 */
	void testDeserializeResultContentEnum() throws IOException {
		// fail on not enum value
		{
			try {
				final ConnectorDeserializer deserializer = new ConnectorDeserializer();
				deserializer.deserialize(
						new File("src/test/resources/test-files/connector/httpCriterionResultContentEnum.yaml"));
				Assert.fail();
			} catch (IllegalArgumentException e) {
				assertEquals(String.format("ResultContent must be one of [ {} ]", ResultContent.values().toString()),
						e.getMessage());
			}
		}

		// pass on Enum values
		{
			for (ResultContent resultContent : ResultContent.values()) {
				final ConnectorDeserializer deserializer = new ConnectorDeserializer();
				final Connector connector = deserializer.deserialize(new File(
						String.format("src/test/resources/test-files/connector/httpCriterionResultContentEnum{}.yaml",
								resultContent.getName())));

				assertNotNull(connector);
				assertEquals(String.format("httpCriterionResultContentEnum{}", resultContent),
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
