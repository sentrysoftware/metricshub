package com.sentrysoftware.metricshub.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sentrysoftware.metricshub.engine.common.helpers.JsonHelper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;

public abstract class AbstractConnectorPropertyConverterTest {

	private static final String EXPECTED = "expected";
	private static final String INPUT = "input";
	private static final String NAME = "name";
	private static final String EXPECTED_YAML = ".yaml";

	protected abstract String getResourcePath();

	/**
	 * finds the specified key in the %{EXPECTED} file and compares the input to the
	 * expected result.
	 */
	protected void testConversion(String key) throws IOException, JsonProcessingException, JsonMappingException {
		final ObjectMapper mapper = JsonHelper.buildYamlMapper();
		final ArrayNode expectedArray = (ArrayNode) mapper.readTree(new File(getResourcePath() + EXPECTED_YAML));
		final AtomicBoolean found = new AtomicBoolean(false);
		expectedArray
			.elements()
			.forEachRemaining(x -> {
				if (x.get(NAME).asText().equals(key)) {
					final PreConnector preConnector = new PreConnector();
					try {
						preConnector.load(new ByteArrayInputStream(x.get(INPUT).asText().getBytes()));
					} catch (IOException e) {
						Assertions.fail(String.format("input for %s not found", key));
					}
					final ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
					final JsonNode connector = connectorConverter.convert();

					assertEquals(x.get(EXPECTED), connector);
					found.set(true);
				}
			});
		assertTrue(found.get(), "Cannot find test for: " + key);
	}

	/**
	 * Checks all keys in %{EXPECTED} file and compares the input against the expected result.
	 *
	 * @throws IOException
	 */
	protected void testAll() throws IOException {
		final ObjectMapper mapper = JsonHelper.buildYamlMapper();
		final ArrayNode expectedArray = (ArrayNode) mapper.readTree(new File(getResourcePath() + EXPECTED_YAML));
		expectedArray
			.elements()
			.forEachRemaining(x -> {
				final PreConnector preConnector = new PreConnector();
				try {
					preConnector.load(new ByteArrayInputStream(x.get(INPUT).asText().getBytes()));
				} catch (IOException e) {
					Assertions.fail("input for test was not found");
				}

				final ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
				final JsonNode connector = connectorConverter.convert();

				assertEquals(x.get(EXPECTED), connector);
			});
	}
}
