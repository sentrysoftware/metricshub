package com.sentrysoftware.matrix.converter.state.detection.ipmi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.converter.ConnectorConverter;
import com.sentrysoftware.matrix.converter.PreConnector;

class ConnectorIpmiPropertyTest {

	@Test
	@Disabled("Until IPMI Converter is up!")
	void test() throws IOException {
		String input = """
				// IPMI criterion comment
				Detection.Criteria(1).Type="IPMI"
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "IPMI criterion comment"
				      type: ipmi
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	@Disabled("Until IPMI Converter is up!")
	void testMany() throws IOException {
		String input = """
				// First IPMI criterion comment
				Detection.Criteria(1).Type="IPMI"
				
				// Second IPMI criterion comment
				Detection.Criteria(2).Type="IPMI"
				""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "First IPMI criterion comment"
				      type: ipmi
				    - _comment: "Second IPMI criterion comment"
				      type: ipmi
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}
}
