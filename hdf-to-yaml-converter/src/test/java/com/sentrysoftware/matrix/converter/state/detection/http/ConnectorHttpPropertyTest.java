package com.sentrysoftware.matrix.converter.state.detection.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.converter.ConnectorConverter;
import com.sentrysoftware.matrix.converter.PreConnector;

class ConnectorHttpPropertyTest {

	@Test
	void test() throws IOException {
		String input = """
				// IGNORED COMMENT
				
				// HTTP criterion comment
				Detection.Criteria(1).Type="HTTP"
				Detection.Criteria(1).Method="GET"
				Detection.Criteria(1).Url="test"
				Detection.Criteria(1).Header="header"
				Detection.Criteria(1).AuthenticationToken="authenticationToken"
				Detection.Criteria(1).Body="body"
				Detection.Criteria(1).ExpectedResult="result"
				Detection.Criteria(1).ErrorMessage="error"
				""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "HTTP criterion comment"
				      type: http
				      method: GET
				      url: test
				      header: header
				      authenticationToken: authenticationToken
				      body: body
				      expectedResult: result
				      errorMessage: error
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void testMany() throws IOException {
		String input = """
				// IGNORED COMMENT
				
				// First HTTP criterion comment
				Detection.Criteria(1).Type="HTTP"
				Detection.Criteria(1).Method="GET"
				Detection.Criteria(1).Url="test1"
				Detection.Criteria(1).Header="header1"
				Detection.Criteria(1).AuthenticationToken="authenticationToken1"
				Detection.Criteria(1).Body="body1"
				Detection.Criteria(1).ExpectedResult="result1"
				Detection.Criteria(1).ErrorMessage="error1"
				
				// IGNORED COMMENT
				
				// Second HTTP criterion comment1
				// Second HTTP criterion comment2
				Detection.Criteria(2).Type="HTTP"
				Detection.Criteria(2).Method="GET"
				Detection.Criteria(2).Url="test2"
				Detection.Criteria(2).Header="header2"
				Detection.Criteria(2).AuthenticationToken="authenticationToken2"
				Detection.Criteria(2).Body="body2"
				Detection.Criteria(2).ExpectedResult="result2"
				Detection.Criteria(2).ErrorMessage="error2"
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				---
				connector:
				  detection:
				    criteria:
				    - _comment: First HTTP criterion comment
				      type: http
				      method: GET
				      url: test1
				      header: header1
				      authenticationToken: authenticationToken1
				      body: body1
				      expectedResult: result1
				      errorMessage: error1
				    - _comment: "Second HTTP criterion comment1\\nSecond HTTP criterion comment2"
				      type: http
				      method: GET
				      url: test2
				      header: header2
				      authenticationToken: authenticationToken2
				      body: body2
				      expectedResult: result2
				      errorMessage: error2
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
		
	}
}
