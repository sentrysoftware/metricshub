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

class RegexpProcessorTest {

	private final RegexpProcessor regexpProcessor = new RegexpProcessor();

	private final Connector connector = new Connector();
	private static final String EXCLUDE_MATCHING_LINES_REGEXP_KEY = "enclosure.discovery.source(1).compute(1).regexp";
	private static final String FOO = "FOO";
	private static final String REGEXP = "^0$";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> regexpProcessor.parse(FOO, FOO, connector));

		// Key matches, no ExcludeMatchingLines found
		assertThrows(IllegalArgumentException.class,
				() -> regexpProcessor.parse(EXCLUDE_MATCHING_LINES_REGEXP_KEY, FOO, connector));

		// Key matches, ExcludeMatchingLines found
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
				.build()
				);

		regexpProcessor.parse(EXCLUDE_MATCHING_LINES_REGEXP_KEY, REGEXP, connector);
		assertEquals(REGEXP, excludeMatchingLines.getRegExp());
	}
}