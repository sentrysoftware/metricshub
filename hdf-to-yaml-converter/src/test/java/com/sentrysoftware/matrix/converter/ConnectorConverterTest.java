package com.sentrysoftware.matrix.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

class ConnectorConverterTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connectorConverter";
	}

	@Test
	void testConvert() throws IOException {
		final PreConnector preConnector = new PreConnector();
		preConnector.load(PreConnectorTest.DELL_OPEN_MANAGE_HDFS);
		final JsonNode node = new ConnectorConverter(preConnector).convert();
		assertNotNull(node);
	}

	@Test
	void test() throws IOException {
		testConversion("getEmbeddedFilesTest");
		testConversion("testManyEmbeddedFiles");
		testConversion("getTranslationTables");
		testConversion("testManyTranslationTables");

		testAll();
	}
}