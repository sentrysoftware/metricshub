package com.sentrysoftware.matrix.connector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;

public abstract class AbstractConnectorParserManagement extends DeserializerTest {

	final protected ConnectorParser parser;
	final protected String relativePath;
	final protected Path connectorDirectory;

	public AbstractConnectorParserManagement(String relativePath, Function<Path, ConnectorParser> parserProducer) {
		this.relativePath = relativePath;
		this.connectorDirectory = Path.of(getResourcePath());
		this.parser = parserProducer.apply(connectorDirectory);
	}

	/**
	 * Parse the test.yaml connector and test the expected connector
	 * 
	 * @throws IOException
	 */
	protected void test() throws IOException {

		final Connector test = parse("test");
		final Connector expected = getConnector("expected");

		expected.getConnectorIdentity().setCompiledFilename("test");

		assertEquals(expected, test);
	}

	protected Connector parse(String testFile) throws IOException {
		return parser.parse(connectorDirectory.resolve(testFile + ".yaml").toFile());
	}
}
