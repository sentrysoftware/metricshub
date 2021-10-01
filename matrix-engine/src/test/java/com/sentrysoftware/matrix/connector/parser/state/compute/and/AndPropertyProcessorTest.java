package com.sentrysoftware.matrix.connector.parser.state.compute.and;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;

public class AndPropertyProcessorTest {

	private final AndPropertyProcessor andPropertyProcessor = new AndPropertyProcessor();

	private final Connector connector = new Connector();

	private static final String AND_KEY = "enclosure.collect.source(1).compute(1).and";
	private static final String NINE = "9";

	@Test
	void testParse() {

		And and = And
			.builder()
			.index(1)
			.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(and))
			.build();

		Collect collect = Collect
			.builder()
			.sources(Collections.singletonList(snmpGetTableSource))
			.build();

		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.collect(collect)
			.build();

		connector
			.getHardwareMonitors()
			.add(hardwareMonitor);

		andPropertyProcessor.parse(AND_KEY, NINE, connector);
		assertEquals(NINE, and.getAnd());
	}
}
