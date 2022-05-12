package com.sentrysoftware.matrix.connector.parser.state.compute.extract;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Extract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SubSeparatorsProcessorTest {

	private final SubSeparatorsProcessor subSeparatorsProcessor = new SubSeparatorsProcessor();

	private final Connector connector = new Connector();

	private static final String EXTRACT_SUB_SEPARATORS_KEY = "enclosure.collect.source(1).compute(1).subseparators";
	private static final String VALUE = "|";

	@Test
	void testParse() {

		Extract extract = Extract
			.builder()
			.index(1)
			.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(extract))
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

		subSeparatorsProcessor.parse(EXTRACT_SUB_SEPARATORS_KEY, VALUE, connector);
		assertEquals(VALUE, extract.getSubSeparators());
	}
}