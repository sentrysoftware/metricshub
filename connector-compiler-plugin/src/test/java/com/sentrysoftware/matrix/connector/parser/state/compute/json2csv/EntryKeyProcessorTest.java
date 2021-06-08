package com.sentrysoftware.matrix.connector.parser.state.compute.json2csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Json2CSV;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;

public class EntryKeyProcessorTest {
	private final EntryKeyProcessor entryKeyProcessor = new EntryKeyProcessor();

	private final Connector connector = new Connector();

	private static final String ENTRY_KEY_KEY = "enclosure.collect.source(1).compute(1).entrykey";
	private static final String VALUE = "/";

	@Test
	void testParse() {

		Json2CSV json2CSV = Json2CSV
				.builder()
				.index(1)
				.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
				.builder()
				.index(1)
				.computes(Collections.singletonList(json2CSV))
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

		entryKeyProcessor.parse(ENTRY_KEY_KEY, VALUE, connector);
		assertEquals(VALUE, json2CSV.getEntryKey());
	}
}
