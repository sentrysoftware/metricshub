package com.sentrysoftware.matrix.connector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;

public class ConnectorParserExtendsManagement extends DeserializerTest {

	final private ConnectorParser parser;
	final private String path;
	final private Path connectorDirectory;

	public ConnectorParserExtendsManagement(String path) {
		this.path = path;
		this.connectorDirectory = Path.of(getResourcePath());
		this.parser = ConnectorParser.withNodeProcessor(connectorDirectory);
	}

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/extends/management/" + path + "/";
	}

	/**
	 * Parse the test.yaml connector and test the expected connector
	 * 
	 * @throws IOException
	 */
	public void test() throws IOException {

		final Connector test = parser.parse(connectorDirectory.resolve("test.yaml").toFile());
		final Connector expected = getConnector("expected");

		assertEquals(test, expected);
	}
}
