package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueListProcessorTest {

	private final ValueListProcessor valueListProcessor = new ValueListProcessor();

	private final Connector connector = new Connector();
	private static final String KEEP_ONLY_MATCHING_LINES_VALUE_LIST_KEY = "enclosure.discovery.source(1).compute(1).valuelist";
	private static final String FOO = "FOO";

	private static final String VALUE_LIST = "0,1,2";
	private static final String DOUBLE_QUOTED_VALUE_LIST = ConnectorParserConstants.DOUBLE_QUOTE
			+ VALUE_LIST
			+ ConnectorParserConstants.DOUBLE_QUOTE;

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> valueListProcessor.parse(FOO, FOO, connector));

		// Key matches, no KeepOnlyMatchingLines found
		assertThrows(
				IllegalArgumentException.class,
				() -> valueListProcessor.parse(KEEP_ONLY_MATCHING_LINES_VALUE_LIST_KEY, FOO, connector)
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

		valueListProcessor.parse(KEEP_ONLY_MATCHING_LINES_VALUE_LIST_KEY, DOUBLE_QUOTED_VALUE_LIST, connector);
		assertEquals(Arrays.asList("0", "1", "2"), keepOnlyMatchingLines.getValueList());
	}
}