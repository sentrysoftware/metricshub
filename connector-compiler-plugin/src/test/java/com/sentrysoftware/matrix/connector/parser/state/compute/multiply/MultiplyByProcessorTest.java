package com.sentrysoftware.matrix.connector.parser.state.compute.multiply;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultiplyByProcessorTest {

	private final MultiplyByProcessor multiplyByProcessor = new MultiplyByProcessor();

	private final Connector connector = new Connector();

	private static final String MULTIPLY_MULTIPLY_BY_KEY = "enclosure.collect.source(1).compute(1).multiplyby";
	private static final String FOO = "FOO";
	private static final String NINE = "9";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> multiplyByProcessor.parse(FOO, FOO, connector));

		// Key matches, no Multiply found
		assertThrows(IllegalArgumentException.class,
			() -> multiplyByProcessor.parse(MULTIPLY_MULTIPLY_BY_KEY, FOO, connector));

		// Key matches, Multiply found
		Multiply multiply = new Multiply();
		multiply.setIndex(1);

		connector
			.getHardwareMonitors()
			.add(HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.collect(Collect
					.builder()
					.sources(Collections.singletonList(SNMPGetTableSource
						.builder()
						.index(1)
						.computes(Collections.singletonList(multiply))
						.build()))
					.build())
				.build());

		multiplyByProcessor.parse(MULTIPLY_MULTIPLY_BY_KEY, NINE, connector);
		assertEquals(NINE, multiply.getMultiplyBy());
	}
}