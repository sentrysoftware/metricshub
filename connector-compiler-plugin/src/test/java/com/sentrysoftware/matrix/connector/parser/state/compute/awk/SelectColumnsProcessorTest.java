package com.sentrysoftware.matrix.connector.parser.state.compute.awk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;

public class SelectColumnsProcessorTest {
	private final SelectColumnsProcessor selectColumnsProcessor = new SelectColumnsProcessor();

	private final Connector connector = new Connector();

	private static final String SELECT_COLUMNS_KEY = "enclosure.collect.source(1).compute(1).selectcolumns";
	private static final String VALUE = "2,4,5,6";
	private static final List<Integer> VALUE_RESULT = new ArrayList<>(Arrays.asList(2, 4, 5, 6));

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

		selectColumnsProcessor.parse(SELECT_COLUMNS_KEY, VALUE, connector);
		assertEquals(VALUE_RESULT, awk.getSelectColumns());
	}
}
