package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegexpProcessorTest {

	private final RegexpProcessor regexpProcessor = new RegexpProcessor();

	private final Connector connector = new Connector();
	private static final String KEEP_ONLY_MATCHING_LINES_REGEXP_KEY = "enclosure.collect.source(1).compute(1).regexp";
	private static final String REGEXP = "^0$";

	@Test
	void testParse() {

		KeepOnlyMatchingLines keepOnlyMatchingLines = KeepOnlyMatchingLines
			.builder()
			.index(1)
			.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(keepOnlyMatchingLines))
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

		regexpProcessor.parse(KEEP_ONLY_MATCHING_LINES_REGEXP_KEY, REGEXP, connector);
		assertEquals(REGEXP, keepOnlyMatchingLines.getRegExp());
	}
}