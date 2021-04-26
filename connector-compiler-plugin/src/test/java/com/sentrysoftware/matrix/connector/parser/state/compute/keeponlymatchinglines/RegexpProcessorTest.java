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

class RegexpProcessorTest {

	private final RegexpProcessor regexpProcessor = new RegexpProcessor();

	private final Connector connector = new Connector();
	private static final String KEEP_ONLY_MATCHING_LINES_REGEXP_KEY = "enclosure.discovery.source(1).compute(1).regexp";
	private static final String FOO = "FOO";
	private static final String REGEXP = "^0$";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> regexpProcessor.parse(FOO, FOO, connector));

		// Key matches, no KeepOnlyMatchingLines found
		assertThrows(
				IllegalArgumentException.class,
				() -> regexpProcessor.parse(KEEP_ONLY_MATCHING_LINES_REGEXP_KEY, FOO, connector)
		);

		// Key matches, KeepOnlyMatchingLines found
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

		regexpProcessor.parse(KEEP_ONLY_MATCHING_LINES_REGEXP_KEY, REGEXP, connector);
		assertEquals(REGEXP, keepOnlyMatchingLines.getRegExp());
	}
}