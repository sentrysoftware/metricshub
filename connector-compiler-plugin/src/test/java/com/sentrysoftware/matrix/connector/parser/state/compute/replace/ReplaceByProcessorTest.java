package com.sentrysoftware.matrix.connector.parser.state.compute.replace;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Replace;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReplaceByProcessorTest {

	private final ReplaceByProcessor divideByProcessor = new ReplaceByProcessor();

	private final Connector connector = new Connector();

	private static final String REPLACE_REPLACE_BY_KEY = "enclosure.collect.source(1).compute(1).replaceby";
	private static final String FOO = "FOO";
	private static final String NINE = "9";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> divideByProcessor.parse(FOO, FOO, connector));

		// Key matches, no Replace found
		assertThrows(IllegalArgumentException.class,
			() -> divideByProcessor.parse(REPLACE_REPLACE_BY_KEY, FOO, connector));

		// Key matches, Replace found
		Replace divide = new Replace();
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

		divideByProcessor.parse(REPLACE_REPLACE_BY_KEY, NINE, connector);
		assertEquals(NINE, divide.getReplaceBy());
	}
}