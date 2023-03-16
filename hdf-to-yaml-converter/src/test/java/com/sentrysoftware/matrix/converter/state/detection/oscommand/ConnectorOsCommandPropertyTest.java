package com.sentrysoftware.matrix.converter.state.detection.oscommand;

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

class ConnectorOsCommandPropertyTest {

	@Test
	void test() throws IOException {
		String input = """
				// OSCommand criterion comment
				Detection.Criteria(1).Type="OSCommand"
				Detection.Criteria(1).ExecuteLocally=1
				Detection.Criteria(1).CommandLine="echo test"
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "OSCommand criterion comment"
				      type: osCommand
				      executeLocally: true
				      commandLine: "echo test"
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void testMany() throws IOException {
		String input = """
				// First OSCommand criterion comment
				Detection.Criteria(1).Type="OSCommand"
				Detection.Criteria(1).ExecuteLocally=1
				Detection.Criteria(1).CommandLine="echo test1"
				
				// Second OSCommand criterion comment
				Detection.Criteria(2).Type="OSCommand"
				Detection.Criteria(2).ExecuteLocally=0
				Detection.Criteria(2).CommandLine="echo test2"
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "First OSCommand criterion comment"
				      type: osCommand
				      executeLocally: true
				      commandLine: "echo test1"
				    - _comment: "Second OSCommand criterion comment"
				      type: osCommand
				      executeLocally: false
				      commandLine: "echo test2"
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}
}
