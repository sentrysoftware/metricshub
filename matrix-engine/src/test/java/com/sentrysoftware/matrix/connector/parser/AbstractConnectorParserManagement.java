package com.sentrysoftware.matrix.connector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

public abstract class AbstractConnectorParserManagement extends DeserializerTest {

	protected final ConnectorParser parser;
	protected final String relativePath;
	protected final Path connectorDirectory;

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
