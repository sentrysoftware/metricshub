package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColumnProcessorTest {

	private final ColumnProcessor columnProcessor = new ColumnProcessor();

	private final Connector connector = new Connector();

	private static final String KEEP_ONLY_MATCHING_LINES_COLUMN_KEY = "enclosure.discovery.source(1).compute(1).column";
	private static final String FOO = "FOO";
	private static final String NINE = "9";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> columnProcessor.parse(FOO, FOO, connector));

		// Key matches, no KeepOnlyMatchingLines found
		assertThrows(
				IllegalArgumentException.class,
				() -> columnProcessor.parse(KEEP_ONLY_MATCHING_LINES_COLUMN_KEY, FOO, connector)
		);

		// Key matches, KeepOnlyMatchingLines found, invalid value
		KeepOnlyMatchingLines keepOnlyMatchingLines = new KeepOnlyMatchingLines();
		keepOnlyMatchingLines.setIndex(1);

		connector
				.getHardwareMonitors()
				.add(
						HardwareMonitor
								.builder()
								.type(MonitorType.ENCLOSURE)
								.discovery(
										Discovery
												.builder()
												.sources(
														Collections.singletonList(
																SNMPGetTableSource
																		.builder()
																		.index(1)
																		.computes(
																				Collections.singletonList(
																						keepOnlyMatchingLines
																				)
																		)
																		.build()
														)
												)
												.build()
								)
								.build()
				);

		assertThrows(
				IllegalArgumentException.class,
				() -> columnProcessor.parse(KEEP_ONLY_MATCHING_LINES_COLUMN_KEY, FOO, connector)
		);

		// Key matches, KeepOnlyMatchingLines found, value is valid
		columnProcessor.parse(KEEP_ONLY_MATCHING_LINES_COLUMN_KEY, NINE, connector);
		assertEquals(9, keepOnlyMatchingLines.getColumn());
	}
}