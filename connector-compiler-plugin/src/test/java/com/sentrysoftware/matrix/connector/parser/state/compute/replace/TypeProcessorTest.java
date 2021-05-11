package com.sentrysoftware.matrix.connector.parser.state.compute.replace;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Replace;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeProcessorTest {

	private final TypeProcessor typeProcessor = new TypeProcessor();

	private final Connector connector = new Connector();
	private static final String REPLACE_TYPE_KEY_1 = "enclosure.collect.source(1).compute(1).type";
	private static final String REPLACE_TYPE_KEY_2 = "enclosure.collect.source(1).compute(2).type";
	private static final String FOO = "FOO";
	private static final String REPLACE_TYPE_VALUE = "Replace";

	@Test
	void testParse() {

		// Value is invalid
		assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, FOO, connector));

		// Value is valid, key does not match
		assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, REPLACE_TYPE_VALUE, connector));

		// Value is valid, key matches, no Source found
		typeProcessor.parse(REPLACE_TYPE_KEY_1, REPLACE_TYPE_VALUE, connector);
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
				.collect(Collect
					.builder()
					.sources(Collections.singletonList(source))
					.build())
				.build());

		typeProcessor.parse(REPLACE_TYPE_KEY_1, REPLACE_TYPE_VALUE, connector);
		assertNotNull(source.getComputes());
		assertEquals(1, source.getComputes().size());
		Compute compute = source.getComputes().get(0);
		assertTrue(compute instanceof Replace);
		assertEquals(1, compute.getIndex());

		// Value is valid, key matches, Source found, source.getComputes() != null
		typeProcessor.parse(REPLACE_TYPE_KEY_2, REPLACE_TYPE_VALUE, connector);
		assertNotNull(source.getComputes());
		assertEquals(2, source.getComputes().size());
		compute = source.getComputes().get(0);
		assertTrue(compute instanceof Replace);
		assertEquals(1, compute.getIndex());
		compute = source.getComputes().get(1);
		assertTrue(compute instanceof Replace);
		assertEquals(2, compute.getIndex());
	}
}