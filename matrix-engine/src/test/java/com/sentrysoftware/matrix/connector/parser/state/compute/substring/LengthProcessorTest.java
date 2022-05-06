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

class LengthProcessorTest {

	private static final String LENGTH_KEY = "enclosure.collect.source(1).compute(1).Length";

	private static LengthProcessor lengthProcessor = new LengthProcessor();

	@Test
	void testGetMatcher() {
		assertNotNull(lengthProcessor.getMatcher(LENGTH_KEY));
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

		lengthProcessor.parse(LENGTH_KEY, "10", connector);

		assertEquals("10", substring.getLength());
	}
}
