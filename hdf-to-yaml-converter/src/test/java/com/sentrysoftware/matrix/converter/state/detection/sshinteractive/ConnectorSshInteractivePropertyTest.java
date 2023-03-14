package com.sentrysoftware.matrix.converter.state.detection.sshinteractive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.converter.ConnectorConverter;
import com.sentrysoftware.matrix.converter.PreConnector;

class ConnectorSshInteractivePropertyTest {
	@Test
	void getAvailableTest() throws IOException {
		String input = """
				// SshInteractive criterion comment
				Detection.Criteria(1).Type="TelnetInteractive"
				Detection.Criteria(1).Port=123
				Detection.Criteria(1).ExpectedResult="result"
				Detection.Criteria(1).Step(1).Type="GetAvailable"
				Detection.Criteria(1).Step(1).Capture="False"
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "SshInteractive criterion comment"
				      type: sshInteractive
				      port: 123
				      expectedResult: result
				      step:
				      - type: getAvailable
				        capture: false
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void getUntilPromptTest() throws IOException {
		String input = """
				// SshInteractive criterion comment
				Detection.Criteria(1).Type="TelnetInteractive"
				Detection.Criteria(1).Port=123
				Detection.Criteria(1).ExpectedResult="result"
				Detection.Criteria(1).Step(1).Type="GetUntilPrompt"
				Detection.Criteria(1).Step(1).Capture="False"
				Detection.Criteria(1).Step(1).Timeout=60
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "SshInteractive criterion comment"
				      type: sshInteractive
				      port: 123
				      expectedResult: result
				      step:
				      - type: getUntilPrompt
				        capture: false
				        timeout: 60
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void getSendPasswordTest() throws IOException {
		String input = """
				// SshInteractive criterion comment
				Detection.Criteria(1).Type="TelnetInteractive"
				Detection.Criteria(1).Port=123
				Detection.Criteria(1).ExpectedResult="result"
				Detection.Criteria(1).Step(1).Type="SendPassword"
				Detection.Criteria(1).Step(1).Capture="False"
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "SshInteractive criterion comment"
				      type: sshInteractive
				      port: 123
				      expectedResult: result
				      step:
				      - type: sendPassword
				        capture: false
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void getSendTextTest() throws IOException {
		String input = """
				// SshInteractive criterion comment
				Detection.Criteria(1).Type="TelnetInteractive"
				Detection.Criteria(1).Port=123
				Detection.Criteria(1).ExpectedResult="result"
				Detection.Criteria(1).Step(1).Type="SendText"
				Detection.Criteria(1).Step(1).Text="text"
				Detection.Criteria(1).Step(1).Capture="False"
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "SshInteractive criterion comment"
				      type: sshInteractive
				      port: 123
				      expectedResult: result
				      step:
				      - type: sendText
				        text: text
				        capture: false
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void getSendUsernameTest() throws IOException {
		String input = """
				// SshInteractive criterion comment
				Detection.Criteria(1).Type="TelnetInteractive"
				Detection.Criteria(1).Port=123
				Detection.Criteria(1).ExpectedResult="result"
				Detection.Criteria(1).Step(1).Type="SendUsername"
				Detection.Criteria(1).Step(1).Capture="False"
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "SshInteractive criterion comment"
				      type: sshInteractive
				      port: 123
				      expectedResult: result
				      step:
				      - type: sendUsername
				        capture: false
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void sleepTest() throws IOException {
		String input = """
				// SshInteractive criterion comment
				Detection.Criteria(1).Type="TelnetInteractive"
				Detection.Criteria(1).Port=123
				Detection.Criteria(1).ExpectedResult="result"
				Detection.Criteria(1).Step(1).Type="Sleep"
				Detection.Criteria(1).Step(1).Capture="False"
				Detection.Criteria(1).Step(1).Duration=60
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "SshInteractive criterion comment"
				      type: sshInteractive
				      port: 123
				      expectedResult: result
				      step:
				      - type: sleep
				        capture: false
				        duration: 60
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void waitForTest() throws IOException {
		String input = """
				// SshInteractive criterion comment
				Detection.Criteria(1).Type="TelnetInteractive"
				Detection.Criteria(1).Port=123
				Detection.Criteria(1).ExpectedResult="result"
				Detection.Criteria(1).Step(1).Type="WaitFor"
				Detection.Criteria(1).Step(1).Capture="False"
				Detection.Criteria(1).Step(1).Text="text"
				Detection.Criteria(1).Step(1).Duration=60
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "SshInteractive criterion comment"
				      type: sshInteractive
				      port: 123
				      expectedResult: result
				      step:
				      - type: waitFor
				        capture: false
				        text: text
				        duration: 60
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void waitForPromptTest() throws IOException {
		String input = """
				// SshInteractive criterion comment
				Detection.Criteria(1).Type="TelnetInteractive"
				Detection.Criteria(1).Port=123
				Detection.Criteria(1).ExpectedResult="result"
				Detection.Criteria(1).Step(1).Type="WaitForPrompt"
				Detection.Criteria(1).Step(1).Capture="False"
				Detection.Criteria(1).Step(1).Duration=60
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "SshInteractive criterion comment"
				      type: sshInteractive
				      port: 123
				      expectedResult: result
				      step:
				      - type: waitForPrompt
				        capture: false
				        duration: 60
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void testMany() throws IOException {
		String input = """
				// First SshInteractive criterion comment
				Detection.Criteria(1).Type="TelnetInteractive"
				Detection.Criteria(1).Port=1231
				Detection.Criteria(1).ExpectedResult="result1"
				Detection.Criteria(1).Step(1).Type="SendUsername"
				Detection.Criteria(1).Step(1).Capture="False"
				Detection.Criteria(1).Step(2).Type="SendPassword"
				Detection.Criteria(1).Step(2).Capture="False"

				// Second SshInteractive criterion comment
				Detection.Criteria(2).Type="TelnetInteractive"
				Detection.Criteria(2).Port=1232
				Detection.Criteria(2).ExpectedResult="result2"
				Detection.Criteria(2).Step(1).Type="GetAvailable"
				Detection.Criteria(2).Step(1).Capture="False"
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "First SshInteractive criterion comment"
				      type: sshInteractive
				      port: 1231
				      expectedResult: result1
				      step:
				      - type: sendUsername
				        capture: false
				      - type: sendPassword
				        capture: false
				    - _comment: "Second SshInteractive criterion comment"
				      type: sshInteractive
				      port: 1232
				      expectedResult: result2
				      step:
				      - type: GetAvailable
				        capture: false
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}
}
