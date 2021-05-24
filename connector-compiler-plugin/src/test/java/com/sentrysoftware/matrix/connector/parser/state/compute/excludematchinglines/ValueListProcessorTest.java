package com.sentrysoftware.matrix.connector.parser.state.compute.excludematchinglines;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueListProcessorTest {

	private final ValueListProcessor valueListProcessor = new ValueListProcessor();

	private final Connector connector = new Connector();
	private static final String EXCLUDE_MATCHING_LINES_VALUE_LIST_KEY = "enclosure.collect.source(1).compute(1).valuelist";

	private static final String VALUE_LIST = "0,1,2";

	@Test
	void testParse() {

		ExcludeMatchingLines excludeMatchingLines = ExcludeMatchingLines
			.builder()
			.index(1)
			.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(excludeMatchingLines))
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

		valueListProcessor.parse(EXCLUDE_MATCHING_LINES_VALUE_LIST_KEY, VALUE_LIST, connector);
		assertEquals(Arrays.asList("0", "1", "2"), excludeMatchingLines.getValueList());
	}
}