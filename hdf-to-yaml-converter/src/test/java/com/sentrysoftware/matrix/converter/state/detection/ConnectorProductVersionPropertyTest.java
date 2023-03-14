package com.sentrysoftware.matrix.converter.state.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.converter.ConnectorConverter;
import com.sentrysoftware.matrix.converter.PreConnector;

class ConnectorProductVersionPropertyTest {

	@Test
	void testKmVersion() throws IOException {
		String input = """
				// Only for Hardware KM 11.3.00+
				Detection.Criteria(1).Type="KMVersion"
				Detection.Criteria(1).Version="11.3.00"
				""";

		String yaml = """
				---
				connector:
				detection:
				  criteria:
				  - type: productRequirements
				  	_comments: Only for Hardware KM 11.3.00+
				  	kmVersion: 11.3.00
				""";
		testConversion(input, yaml);
	}

	@Test
	void testMultipleCriteria() throws IOException {
		String input = """
				// Only for Hardware KM 11.3.00+
				Detection.Criteria(1).Type="KMVersion"
				Detection.Criteria(1).Version="11.3.00"

				// Definitely not linux
				Detection.Criteria(2).Type="OS"
				Detection.Criteria(2).Exclude="Linux"
				""";

		String yaml = """
				---
				connector:
				detection:
				  criteria:
				  - type: productRequirements
				  	_comments: Only for Hardware KM 11.3.00+
				  	kmVersion: 11.3.00
				  - type: deviceType
				  	_comments: Definitely not linux
				  	exclude: [ Linux ]
				""";
		testConversion(input, yaml);
	}

	private void testConversion(String input, String yaml)
			throws IOException, JsonProcessingException, JsonMappingException {
		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();

		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}
}