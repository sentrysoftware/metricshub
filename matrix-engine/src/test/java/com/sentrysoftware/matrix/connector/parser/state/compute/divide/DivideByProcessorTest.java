package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DivideByProcessorTest {

	private final DivideByProcessor divideByProcessor = new DivideByProcessor();

	private final Connector connector = new Connector();

	private static final String DIVIDE_DIVIDE_BY_KEY = "enclosure.collect.source(1).compute(1).divideby";
	private static final String NINE = "9";

	@Test
	void testParse() {

		Divide divide = Divide
			.builder()
			.index(1)
			.build();

		SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(divide))
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

		divideByProcessor.parse(DIVIDE_DIVIDE_BY_KEY, NINE, connector);
		assertEquals(NINE, divide.getDivideBy());
	}
}