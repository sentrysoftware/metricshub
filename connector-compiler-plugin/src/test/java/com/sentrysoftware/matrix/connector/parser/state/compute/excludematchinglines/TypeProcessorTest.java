package com.sentrysoftware.matrix.connector.parser.state.compute.excludematchinglines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;

class TypeProcessorTest {

	private final TypeProcessor typeProcessor = new TypeProcessor();

	private final Connector connector = new Connector();
	private static final String EXCLUDE_MATCHING_LINES_TYPE_KEY_1 = "enclosure.discovery.source(1).compute(1).type";
	private static final String EXCLUDE_MATCHING_LINES_TYPE_KEY_2 = "enclosure.discovery.source(1).compute(2).type";
	private static final String FOO = "FOO";
	private static final String EXCLUDE_MATCHING_LINES_TYPE_VALUE = "ExcludeMatchingLines";

	@Test
	void testParse() {

		// Value is invalid
		assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, FOO, connector));

		// Value is valid, key does not match
		assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, EXCLUDE_MATCHING_LINES_TYPE_VALUE, connector));

		// Value is valid, key matches, no Source found
		typeProcessor.parse(EXCLUDE_MATCHING_LINES_TYPE_KEY_1, EXCLUDE_MATCHING_LINES_TYPE_VALUE, connector);
		assertTrue(connector.getHardwareMonitors().isEmpty());

		// Value is valid, key matches, Source found, source.getComputes() == null
		SNMPGetTableSource source = SNMPGetTableSource
				.builder()
				.index(1)
				.build();
		source.setComputes(null);

		connector
		.getHardwareMonitors()
		.add(HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(Discovery
						.builder()
						.sources(Collections.singletonList(source))
						.build())
				.build());

		typeProcessor.parse(EXCLUDE_MATCHING_LINES_TYPE_KEY_1, EXCLUDE_MATCHING_LINES_TYPE_VALUE, connector);
		assertNotNull(source.getComputes());
		assertEquals(1, source.getComputes().size());
		Compute compute = source.getComputes().get(0);
		assertTrue(compute instanceof ExcludeMatchingLines);
		assertEquals(1, compute.getIndex());

		// Value is valid, key matches, Source found, source.getComputes() != null
		typeProcessor.parse(EXCLUDE_MATCHING_LINES_TYPE_KEY_2, EXCLUDE_MATCHING_LINES_TYPE_VALUE, connector);
		assertNotNull(source.getComputes());
		assertEquals(2, source.getComputes().size());
		compute = source.getComputes().get(0);
		assertTrue(compute instanceof ExcludeMatchingLines);
		assertEquals(1, compute.getIndex());
		compute = source.getComputes().get(1);
		assertTrue(compute instanceof ExcludeMatchingLines);
		assertEquals(2, compute.getIndex());
	}
}