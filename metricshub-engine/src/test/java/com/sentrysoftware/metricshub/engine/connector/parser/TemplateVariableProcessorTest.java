package com.sentrysoftware.metricshub.engine.connector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TemplateVariableProcessorTest {

	@Test
	void testProcess() throws IOException {
		// Define the json test file path
		final Path jsonTestFilePath = Paths.get(
			"src",
			"test",
			"resources",
			"test-files",
			"connector",
			"templateVariable",
			"templateVariable.json"
		);

		// Create an ObjectMapper
		final ObjectMapper mapper = new ObjectMapper();

		// Retrieve the json node from the json test file
		final JsonNode connectorNode = mapper.readTree(jsonTestFilePath.toFile());

		// Init TemplateVariableProcessor with the connector variables map and a node processor
		final TemplateVariableProcessor templateVariableProcessor = TemplateVariableProcessor
			.builder()
			.connectorVariables(Map.of("snmp-get-next", "snmpGetNext"))
			.nodeProcessor(new ConstantsProcessor())
			.build();

		// Call the method process
		final JsonNode nodeProcessingResult = templateVariableProcessor.process(connectorNode);

		// Retrieve the processed connector node
		final Connector customConnector = mapper.treeToValue(nodeProcessingResult, Connector.class);

		// Check that the variable value was successfully replaced
		assertEquals("snmpGetNext", customConnector.getConnectorIdentity().getDetection().getCriteria().get(0).getType());
	}
}
