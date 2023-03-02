package com.sentrysoftware.matrix.connector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;

public abstract class AbstractConnectorParserManagement extends DeserializerTest {

	final protected ConnectorParser parser;
	final protected String relativePath;
	final protected Path connectorDirectory;

	public AbstractConnectorParserManagement(String relativePath) {
		this.relativePath = relativePath;
		this.connectorDirectory = Path.of(getResourcePath());
		this.parser = ConnectorParser.withNodeProcessor(connectorDirectory);
	}

	/**
	 * Parse the test.yaml connector and test the expected connector
	 * 
	 * @throws IOException
	 */
	protected void test() throws IOException {

		final Connector test = parser.parse(connectorDirectory.resolve("test.yaml").toFile());
		final Connector expected = getConnector("expected");

		expected.getConnectorIdentity().setCompiledFilename("test");

		assertEquals(expected, test);
	}
}
