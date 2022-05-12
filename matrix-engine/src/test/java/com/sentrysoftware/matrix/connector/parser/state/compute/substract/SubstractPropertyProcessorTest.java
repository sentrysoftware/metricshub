package com.sentrysoftware.matrix.connector.parser.state.compute.substract;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubstractPropertyProcessorTest {

	private final SubstractPropertyProcessor substractPropertyProcessor = new SubstractPropertyProcessor();

	private final Connector connector = new Connector();

	private static final String SUBSTRACT_SUBSTRACT_KEY = "enclosure.collect.source(1).compute(1).substract";
	private static final String NINE = "9";

	@Test
	void testParse() {

		Substract substract = Substract
			.builder()
			.index(1)
			.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(substract))
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

		substractPropertyProcessor.parse(SUBSTRACT_SUBSTRACT_KEY, NINE, connector);
		assertEquals(NINE, substract.getSubstract());
	}
}