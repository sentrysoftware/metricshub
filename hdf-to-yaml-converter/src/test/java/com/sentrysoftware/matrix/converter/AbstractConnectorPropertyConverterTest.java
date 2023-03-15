package com.sentrysoftware.matrix.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;

public abstract class AbstractConnectorPropertyConverterTest {

	private static final String EXPECTED = "expected";
	private static final String INPUT = "input";
	private static final String NAME = "name";
	private static final String EXPECTED_YAML = "/expected.yaml";

	protected abstract String getResourcePath();

	/**
	 * finds the specified key in the %{EXPECTED} file and compares the input to the
	 * expected result.
	 */
	protected void testConversion(String key)
			throws IOException, JsonProcessingException, JsonMappingException {

		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		ArrayNode expectedArray = (ArrayNode) mapper.readTree(new File(getResourcePath() + EXPECTED_YAML));
		expectedArray.elements().forEachRemaining(x -> {
			if (x.get(NAME).asText().equals(key)) {

				PreConnector preConnector = new PreConnector();
				try {
					preConnector.load(new ByteArrayInputStream(x.get(INPUT).asText().getBytes()));
				} catch (IOException e) {
					Assert.fail(String.format("input for %s not found", key));
				}
				ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
				JsonNode connector = connectorConverter.convert();

				assertEquals(x.get(EXPECTED), connector);
			}
		});
	}

	/**
	 * Checks all keys in %{EXPECTED} file and compares the input against the expected result.
	 * 
	 * @throws IOException
	 */
	protected void testAll() throws IOException {
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		ArrayNode expectedArray = (ArrayNode) mapper.readTree(new File(getResourcePath() + EXPECTED_YAML));
		expectedArray.elements().forEachRemaining(x -> {

			PreConnector preConnector = new PreConnector();
			try {
				preConnector.load(new ByteArrayInputStream(x.get(INPUT).asText().getBytes()));
			} catch (IOException e) {
				Assert.fail("input for test was not found");
			}

			ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
			JsonNode connector = connectorConverter.convert();

			assertEquals(x.get(EXPECTED), connector);

		});
	}
}
