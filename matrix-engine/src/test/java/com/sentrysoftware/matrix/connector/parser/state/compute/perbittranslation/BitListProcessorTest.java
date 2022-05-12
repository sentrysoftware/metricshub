package com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BitListProcessorTest {

	private final BitListProcessor bitListProcessor = new BitListProcessor();

	private final Connector connector = new Connector();
	private static final String PER_BIT_TRANSLATION_BIT_LIST_KEY = "enclosure.collect.source(1).compute(1).bitlist";

	private static final String BIT_LIST = "0,1,2";

	@Test
	void testParse() {

		PerBitTranslation perBitTranslation = PerBitTranslation
			.builder()
			.index(1)
			.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(perBitTranslation))
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

		bitListProcessor.parse(PER_BIT_TRANSLATION_BIT_LIST_KEY, BIT_LIST, connector);
		assertEquals(Arrays.asList(0, 1, 2), perBitTranslation.getBitList());
	}
}