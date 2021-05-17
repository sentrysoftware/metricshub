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

class ColumnProcessorTest {

	private final ColumnProcessor columnProcessor = new ColumnProcessor();

	private final Connector connector = new Connector();

	private static final String RIGHT_CONCAT_COLUMN_KEY = "enclosure.discovery.source(1).compute(1).column";
	private static final String FOO = "FOO";
	private static final String NINE = "9";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> columnProcessor.parse(FOO, FOO, connector));

		// Key matches, no RightConcat found
		assertThrows(IllegalArgumentException.class,
				() -> columnProcessor.parse(RIGHT_CONCAT_COLUMN_KEY, FOO, connector));

		// Key matches, RightConcat found, invalid value
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

		assertThrows(IllegalArgumentException.class,
				() -> columnProcessor.parse(RIGHT_CONCAT_COLUMN_KEY, FOO, connector));

		// Key matches, RightConcat found, value is valid
		columnProcessor.parse(RIGHT_CONCAT_COLUMN_KEY, NINE, connector);
		assertEquals(9, rightConcat.getColumn());
	}
}