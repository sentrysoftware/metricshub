package com.sentrysoftware.matrix.connector.parser.state.compute.excludematchinglines;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValueListProcessorTest {

	private final ValueListProcessor valueListProcessor = new ValueListProcessor();

	private final Connector connector = new Connector();
	private static final String EXCLUDE_MATCHING_LINES_VALUE_LIST_KEY = "enclosure.collect.source(1).compute(1).valuelist";

	private static final String VALUE_LIST = "Unavailable,offline,OFF,0";

	@Test
	void testParse() {

		ExcludeMatchingLines excludeMatchingLines = ExcludeMatchingLines
			.builder()
			.index(1)
			.build();

		SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource
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
		final Set<String> valueSet = excludeMatchingLines.getValueSet();
		assertEquals(Set.of("Unavailable", "offline", "OFF", "0"), valueSet);
		assertFalse(valueSet.contains("1"));
		assertFalse(valueSet.contains("ON"));
		assertTrue(valueSet.contains("Unavailable"));
		assertTrue(valueSet.contains("unavailable"));
		assertTrue(valueSet.contains("UNAVAILABLE"));
		assertTrue(valueSet.contains("off"));
		assertTrue(valueSet.contains("0"));
	}
}