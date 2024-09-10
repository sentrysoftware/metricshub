package org.sentrysoftware.metricshub.agent.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.configuration.ConnectorVariables;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;

class ConnectorTemplateLibraryParserTest {

	private static final String CONNECTOR_ID = "templateVariable";

	@Test
	void testParse() throws IOException {
		// Define the yaml test files path
		final Path yamlTestPath = Paths.get("src", "test", "resources", "connectorTemplateLibraryParser");

		// Call ConnectorTemplateLibraryParser to parse the custom connectors files using the connectorVariables map and the connector id
		final ConnectorTemplateLibraryParser connectorTemplateLibraryParser = new ConnectorTemplateLibraryParser();
		final ConnectorVariables connectorVariables = new ConnectorVariables(Map.of("snmp-get-next", "snmpGetNext"));
		final Map<String, Connector> customConnectorsMap = connectorTemplateLibraryParser.parse(
			yamlTestPath,
			Map.of(CONNECTOR_ID, connectorVariables)
		);

		// Check that only the connector containing variables is returned in the map
		assertEquals(1, customConnectorsMap.size());

		// Check that the connector variable value was successfully replaced
		final Connector customConnector = customConnectorsMap.get(CONNECTOR_ID);
		assertEquals("snmpGetNext", customConnector.getConnectorIdentity().getDetection().getCriteria().get(0).getType());
	}
}
