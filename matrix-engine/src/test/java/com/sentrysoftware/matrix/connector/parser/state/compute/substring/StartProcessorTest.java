package com.sentrysoftware.matrix.connector.parser.state.compute.substring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substring;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;

class StartProcessorTest {

	private static final String START_KEY = "enclosure.collect.source(1).compute(1).Start";

	private static StartProcessor startProcessor = new StartProcessor();

	@Test
	void testGetMatcher() {
		assertNotNull(startProcessor.getMatcher(START_KEY));
	}

	@Test
	void testParse() {
		final Connector connector = new Connector();

		final Substring substring = Substring
				.builder()
				.index(1)
				.build();

		final SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(substring))
			.build();

		final Collect collect = Collect
			.builder()
			.sources(Collections.singletonList(snmpGetTableSource))
			.build();

		final HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.collect(collect)
			.build();

		connector
			.getHardwareMonitors()
			.add(hardwareMonitor);

		startProcessor.parse(START_KEY, "1", connector);

		assertEquals("1", substring.getStart());
	}

}
