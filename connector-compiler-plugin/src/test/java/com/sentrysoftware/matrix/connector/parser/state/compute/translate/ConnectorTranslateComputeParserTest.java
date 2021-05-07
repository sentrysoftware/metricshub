package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import com.sentrysoftware.matrix.connector.model.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorTranslateComputeParserTest {

	private Connector connector;

	private static final String TRANSLATE_COLLECT_TYPE_KEY = "enclosure.collect.source(1).compute(1).type";
	private static final String TRANSLATE_TYPE_VALUE = "Translate";

	@BeforeEach
	void setUp() {

		connector = new Connector();
	}

	@Test
	void testParse() {

		ConnectorTranslateComputeParser connectorTranslateComputeParser = new ConnectorTranslateComputeParser();
		connectorTranslateComputeParser.parse(TRANSLATE_COLLECT_TYPE_KEY, TRANSLATE_TYPE_VALUE, connector);

		assertNotNull(connector.getHardwareMonitors());
		assertTrue(connector.getHardwareMonitors().isEmpty());
	}
}