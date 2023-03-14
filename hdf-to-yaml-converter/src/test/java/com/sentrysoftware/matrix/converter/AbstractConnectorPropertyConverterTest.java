package com.sentrysoftware.matrix.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;

public abstract class AbstractConnectorPropertyConverterTest {

	protected abstract String getResourcePath();

	protected void testConversion(String input, String key)
			throws IOException, JsonProcessingException, JsonMappingException {

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();

		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		ArrayNode expectedArray = (ArrayNode) mapper.readTree(new File(getResourcePath() + "expected.yaml"));
		expectedArray.elements().forEachRemaining(x -> {
			if (x.get("name").asText().equals(key)) {
				assertEquals(x.get("value"), connector);
			}
		});
	}
}
