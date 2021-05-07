package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class DivideByProcessorTest {

	private final DivideByProcessor divideByProcessor = new DivideByProcessor();

	private final Connector connector = new Connector();

	private static final String DIVIDE_DIVIDE_BY_KEY = "enclosure.collect.source(1).compute(1).divideby";
	private static final String FOO = "FOO";
	private static final String NINE = "9";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> divideByProcessor.parse(FOO, FOO, connector));

		// Key matches, no Divide found
		assertThrows(IllegalArgumentException.class,
			() -> divideByProcessor.parse(DIVIDE_DIVIDE_BY_KEY, FOO, connector));

		// Key matches, Divide found
		Divide divide = new Divide();
		divide.setIndex(1);

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
						.computes(Collections.singletonList(divide))
						.build()))
					.build())
				.build());

		divideByProcessor.parse(DIVIDE_DIVIDE_BY_KEY, NINE, connector);
		assertEquals(NINE, divide.getDivideBy());
	}
}