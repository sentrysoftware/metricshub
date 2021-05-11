package com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColumnProcessorTest {

	private final ColumnProcessor columnProcessor = new ColumnProcessor();

	private final Connector connector = new Connector();

	private static final String PER_BIT_TRANSLATION_COLUMN_KEY = "enclosure.collect.source(1).compute(1).column";
	private static final String FOO = "FOO";
	private static final String NINE = "9";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> columnProcessor.parse(FOO, FOO, connector));

		// Key matches, no PerBitTranslation found
		assertThrows(IllegalArgumentException.class,
			() -> columnProcessor.parse(PER_BIT_TRANSLATION_COLUMN_KEY, FOO, connector));

		// Key matches, PerBitTranslation found, invalid value
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

		assertThrows(IllegalArgumentException.class,
			() -> columnProcessor.parse(PER_BIT_TRANSLATION_COLUMN_KEY, FOO, connector));

		// Key matches, PerBitTranslation found, value is valid
		columnProcessor.parse(PER_BIT_TRANSLATION_COLUMN_KEY, NINE, connector);
		assertEquals(9, perBitTranslation.getColumn());
	}
}