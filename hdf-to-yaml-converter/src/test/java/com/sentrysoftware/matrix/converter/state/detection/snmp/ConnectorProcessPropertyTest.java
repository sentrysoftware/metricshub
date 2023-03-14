package com.sentrysoftware.matrix.converter.state.detection.snmp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.converter.ConnectorConverter;
import com.sentrysoftware.matrix.converter.PreConnector;

public class ConnectorProcessPropertyTest {
	@Test
	void snmpGetTest() throws IOException {
		String input = """
				// SNMPGet criterion comment
				Detection.Criteria(1).Type="SNMP"
				Detection.Criteria(1).SNMPGet="1.3.6.1.3.94.1.6.1.3"
				Detection.Criteria(1).ExpectedResult="result"
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "SNMPGet criterion comment"
				      type: snmpGet
				      oid: "1.3.6.1.3.94.1.6.1.3"
				      expectedResult: result
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void snmpGetNextTest() throws IOException {
		String input = """
				// SNMPGetNext criterion comment
				Detection.Criteria(1).Type="SNMP"
				Detection.Criteria(1).SNMPGetNext="1.3.6.1.3.94.1.6.1.3"
				Detection.Criteria(1).ExpectedResult="result"
			""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "SNMPGetNext criterion comment"
				      type: snmpGetNext
				      oid: "1.3.6.1.3.94.1.6.1.3"
				      expectedResult: result
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

	@Test
	void testMany() throws IOException {
		String input = """
				// SNMPGet criterion comment
				Detection.Criteria(1).Type="SNMP"
				Detection.Criteria(1).SNMPGet="1.3.6.1.3.94.1.6.1.3"
				Detection.Criteria(1).ExpectedResult="result1"

				// SNMPGet Next criterion comment
				Detection.Criteria(2).Type="SNMP"
				Detection.Criteria(2).SNMPGetNext="1.3.6.1.3.94.1.6.1.4"
				Detection.Criteria(2).ExpectedResult="result2"
				""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				connector:
				  detection:
				    criteria:
				    - _comment: "SNMPGet criterion comment"
				      type: snmpGet
				      oid: "1.3.6.1.3.94.1.6.1.3"
				      expectedResult: result1
				    - _comment: "SNMPGetNext criterion comment"
				      type: snmpGetNext
				      oid: "1.3.6.1.3.94.1.6.1.4"
				      expectedResult: result2
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}
}
