package com.sentrysoftware.matrix.connector.parser.state.compute.extract;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Extract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubColumnProcessorTest {

	private final SubColumnProcessor subColumnProcessor = new SubColumnProcessor();

	private final Connector connector = new Connector();

	private static final String EXTRACT_SUB_COLUMN_KEY = "enclosure.collect.source(1).compute(1).subcolumn";
	private static final String INVALID_VALUE = "FOO";
	private static final String VALID_VALUE = "9";

	@Test
	void testParse() {

		assertThrows(IllegalArgumentException.class,
			() -> subColumnProcessor.parse(EXTRACT_SUB_COLUMN_KEY, INVALID_VALUE, connector));

		Extract extract = Extract
			.builder()
			.index(1)
			.build();

		SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource
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

		subColumnProcessor.parse(EXTRACT_SUB_COLUMN_KEY, VALID_VALUE, connector);
		assertEquals(9, extract.getSubColumn());
	}
}