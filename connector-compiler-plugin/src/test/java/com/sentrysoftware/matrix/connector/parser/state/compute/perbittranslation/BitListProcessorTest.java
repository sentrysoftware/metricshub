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
import static org.junit.jupiter.api.Assertions.assertThrows;

class BitListProcessorTest {

	private final BitListProcessor bitListProcessor = new BitListProcessor();

	private final Connector connector = new Connector();
	private static final String PER_BIT_TRANSLATION_BIT_LIST_KEY = "enclosure.collect.source(1).compute(1).bitlist";
	private static final String FOO = "FOO";

	private static final String BIT_LIST = "0,1,2";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> bitListProcessor.parse(FOO, FOO, connector));

		// Key matches, no PerBitTranslation found
		assertThrows(IllegalArgumentException.class,
			() -> bitListProcessor.parse(PER_BIT_TRANSLATION_BIT_LIST_KEY, FOO, connector));

		// Key matches, PerBitTranslation found
		PerBitTranslation perBitTranslation = new PerBitTranslation();
		perBitTranslation.setIndex(1);

		connector
			.getHardwareMonitors()
			.add(HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.collect(Collect
					.builder()
					.sources(Collections.singletonList(SNMPGetTableSource
						.builder()
						.index(1)
						.computes(Collections.singletonList(perBitTranslation))
						.build()))
					.build())
				.build());

		bitListProcessor.parse(PER_BIT_TRANSLATION_BIT_LIST_KEY, BIT_LIST, connector);
		assertEquals(Arrays.asList(0, 1, 2), perBitTranslation.getBitList());
	}
}