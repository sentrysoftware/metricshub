package com.sentrysoftware.matrix.connector.parser.state.compute.add;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddPropertyProcessorTest {

	private final AddPropertyProcessor addPropertyProcessor = new AddPropertyProcessor();

	private final Connector connector = new Connector();

	private static final String ADD_ADD_KEY = "enclosure.collect.source(1).compute(1).add";
	private static final String FOO = "FOO";
	private static final String NINE = "9";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> addPropertyProcessor.parse(FOO, FOO, connector));

		// Key matches, no Add found
		assertThrows(IllegalArgumentException.class,
			() -> addPropertyProcessor.parse(ADD_ADD_KEY, FOO, connector));

		// Key matches, Add found
		Add add = new Add();
		add.setIndex(1);

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
						.computes(Collections.singletonList(add))
						.build()))
					.build())
				.build());

		addPropertyProcessor.parse(ADD_ADD_KEY, NINE, connector);
		assertEquals(NINE, add.getAdd());
	}
}