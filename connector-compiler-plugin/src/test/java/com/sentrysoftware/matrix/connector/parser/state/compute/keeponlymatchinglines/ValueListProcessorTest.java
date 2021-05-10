package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;

class ValueListProcessorTest {

	private final ValueListProcessor valueListProcessor = new ValueListProcessor();

	private final Connector connector = new Connector();
	private static final String KEEP_ONLY_MATCHING_LINES_VALUE_LIST_KEY = "enclosure.discovery.source(1).compute(1).valuelist";
	private static final String FOO = "FOO";

	private static final String VALUE_LIST = "0,1,2";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> valueListProcessor.parse(FOO, FOO, connector));

		// Key matches, no KeepOnlyMatchingLines found
		assertThrows(IllegalArgumentException.class,
			() -> valueListProcessor.parse(KEEP_ONLY_MATCHING_LINES_VALUE_LIST_KEY, FOO, connector));

		// Key matches, KeepOnlyMatchingLines found
		KeepOnlyMatchingLines keepOnlyMatchingLines = new KeepOnlyMatchingLines();
		keepOnlyMatchingLines.setIndex(1);

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
						.computes(Collections.singletonList(keepOnlyMatchingLines))
						.build()))
					.build())
				.build());

		valueListProcessor.parse(KEEP_ONLY_MATCHING_LINES_VALUE_LIST_KEY, VALUE_LIST, connector);
		assertEquals(Arrays.asList("0", "1", "2"), keepOnlyMatchingLines.getValueList());
	}
}