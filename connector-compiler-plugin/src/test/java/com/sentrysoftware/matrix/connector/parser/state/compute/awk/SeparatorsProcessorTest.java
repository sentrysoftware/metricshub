package com.sentrysoftware.matrix.connector.parser.state.compute.awk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;

public class SeparatorsProcessorTest {
	private final SeparatorsProcessor separatorsProcessor = new SeparatorsProcessor();

	private final Connector connector = new Connector();

	private static final String SELECT_COLUMNS_KEY = "enclosure.collect.source(1).compute(1).separators";
	private static final String VALUE = ";";

	@Test
	void testParse() {

		Awk awk = Awk
				.builder()
				.index(1)
				.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
				.builder()
				.index(1)
				.computes(Collections.singletonList(awk))
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

		separatorsProcessor.parse(SELECT_COLUMNS_KEY, VALUE, connector);
		assertEquals(VALUE, awk.getSeparators());
	}
}
