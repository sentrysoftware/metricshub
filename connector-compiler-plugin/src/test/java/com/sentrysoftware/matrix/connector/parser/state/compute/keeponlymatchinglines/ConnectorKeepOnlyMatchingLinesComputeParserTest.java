package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorKeepOnlyMatchingLinesComputeParserTest {

	private Connector connector;

	private static final String KEEP_ONLY_MATCHING_LINES_DISCOVERY_TYPE_KEY =
			"enclosure.discovery.source(1).compute(1).type";

	private static final String KEEP_ONLY_MATCHING_LINES_TYPE_VALUE = "KeepOnlyMatchingLines";

	@BeforeEach
	void setUp() {

		connector = new Connector();
	}

	@Test
	void testParse() {

		ConnectorKeepOnlyMatchingLinesComputeParser connectorKeepOnlyMatchingLinesComputeParser =
			new ConnectorKeepOnlyMatchingLinesComputeParser();

		connectorKeepOnlyMatchingLinesComputeParser.parse(
			KEEP_ONLY_MATCHING_LINES_DISCOVERY_TYPE_KEY,
			KEEP_ONLY_MATCHING_LINES_TYPE_VALUE,
			connector);

		assertNotNull(connector.getHardwareMonitors());
		assertTrue(connector.getHardwareMonitors().isEmpty());
	}
}