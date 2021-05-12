package com.sentrysoftware.matrix.connector.parser.state.compute.excludematchinglines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;

class ColumnProcessorTest {

	private final ColumnProcessor columnProcessor = new ColumnProcessor();

	private final Connector connector = new Connector();

	private static final String EXCLUDE_MATCHING_LINES_COLUMN_KEY = "enclosure.discovery.source(1).compute(1).column";
	private static final String FOO = "FOO";
	private static final String NINE = "9";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> columnProcessor.parse(FOO, FOO, connector));

		// Key matches, no ExcludeMatchingLines found
		assertThrows(IllegalArgumentException.class,
				() -> columnProcessor.parse(EXCLUDE_MATCHING_LINES_COLUMN_KEY, FOO, connector));

		// Key matches, ExcludeMatchingLines found, invalid value
		ExcludeMatchingLines excludeMatchingLines = new ExcludeMatchingLines();
		excludeMatchingLines.setIndex(1);

		connector
		.getHardwareMonitors()
		.add(HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(Discovery
						.builder()
						.sources(Collections.singletonList(SNMPGetTableSource
								.builder()
								.index(1)
								.computes(Collections.singletonList(excludeMatchingLines))
								.build()))
						.build())
				.build());

		assertThrows(IllegalArgumentException.class,
				() -> columnProcessor.parse(EXCLUDE_MATCHING_LINES_COLUMN_KEY, FOO, connector));

		// Key matches, ExcludeMatchingLines found, value is valid
		columnProcessor.parse(EXCLUDE_MATCHING_LINES_COLUMN_KEY, NINE, connector);
		assertEquals(9, excludeMatchingLines.getColumn());
	}
}