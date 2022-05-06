package com.sentrysoftware.matrix.connector.parser.state.compute.multiply;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiplyByProcessorTest {

	private final MultiplyByProcessor multiplyByProcessor = new MultiplyByProcessor();

	private final Connector connector = new Connector();

	private static final String MULTIPLY_MULTIPLY_BY_KEY = "enclosure.collect.source(1).compute(1).multiplyby";
	private static final String NINE = "9";

	@Test
	void testParse() {

		Multiply multiply = Multiply
			.builder()
			.index(1)
			.build();

		SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(multiply))
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

		multiplyByProcessor.parse(MULTIPLY_MULTIPLY_BY_KEY, NINE, connector);
		assertEquals(NINE, multiply.getMultiplyBy());
	}
}