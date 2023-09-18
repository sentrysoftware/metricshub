package com.sentrysoftware.matrix.converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.junit.jupiter.api.Test;

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
		testConversion("getTranslationTables");
		testConversion("testManyTranslationTables");
		testConversion("testConstantsConversion");

		testAll();
	}
}
