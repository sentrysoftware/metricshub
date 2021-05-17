package com.sentrysoftware.matrix.connector.parser.state.compute.rightconcat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;

class StringProcessorTest {

	private final StringProcessor stringProcessor = new StringProcessor();

	private final Connector connector = new Connector();
	private static final String RIGHT_CONCAT_STRING_KEY = "enclosure.discovery.source(1).compute(1).string";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> stringProcessor.parse(FOO, FOO, connector));

		// Key matches, no RightConcat found
		assertThrows(IllegalArgumentException.class,
				() -> stringProcessor.parse(RIGHT_CONCAT_STRING_KEY, FOO, connector));

		// Key matches, RightConcat found
		RightConcat rightConcat = new RightConcat();
		rightConcat.setIndex(1);

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
								.computes(Collections.singletonList(rightConcat))
								.build()))
						.build())
				.build());

		stringProcessor.parse(RIGHT_CONCAT_STRING_KEY, FOO, connector);
		assertEquals(FOO, rightConcat.getString());
	}
}