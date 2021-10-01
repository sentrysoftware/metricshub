package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringProcessorTest {

	private final StringProcessor stringProcessor = new StringProcessor();

	private final Connector connector = new Connector();
	private static final String LEFT_CONCAT_STRING_KEY = "enclosure.collect.source(1).compute(1).string";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		LeftConcat leftConcat = LeftConcat
			.builder()
			.index(1)
			.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(leftConcat))
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

		stringProcessor.parse(LEFT_CONCAT_STRING_KEY, FOO, connector);
		assertEquals(FOO, leftConcat.getString());
	}
}