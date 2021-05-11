package com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BitTranslationTableProcessorTest {

	private final BitTranslationTableProcessor bitTranslationTableProcessor = new BitTranslationTableProcessor();

	private final Connector connector = new Connector();

	private static final String PER_BIT_TRANSLATION_BIT_TRANSLATION_TABLE_KEY =
		"enclosure.collect.source(1).compute(1).bittranslationTable";

	private static final String FOO = "FOO";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> bitTranslationTableProcessor.parse(FOO, FOO, connector));

		// Key matches, no PerBitTranslation found
		assertThrows(IllegalArgumentException.class,
			() -> bitTranslationTableProcessor.parse(PER_BIT_TRANSLATION_BIT_TRANSLATION_TABLE_KEY, FOO, connector));

		// Key matches, PerBitTranslation found, no TranslationTables found
		PerBitTranslation perBitTranslation = new PerBitTranslation();
		perBitTranslation.setIndex(1);

		connector.setTranslationTables(null);

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

		assertThrows(IllegalStateException.class,
			() -> bitTranslationTableProcessor.parse(PER_BIT_TRANSLATION_BIT_TRANSLATION_TABLE_KEY, FOO, connector));

		// Key matches, PerBitTranslation found, TranslationTables found, no BitTranslationTable named FOO
		Map<String, TranslationTable> translationTables = new HashMap<>();
		connector.setTranslationTables(translationTables);
		assertThrows(IllegalStateException.class,
			() -> bitTranslationTableProcessor.parse(PER_BIT_TRANSLATION_BIT_TRANSLATION_TABLE_KEY, FOO, connector));

		// Key matches, PerBitTranslation found, TranslationTables found, BitTranslationTable named FOO found
		TranslationTable translationTable = new TranslationTable();
		translationTables.put(FOO, translationTable);
		bitTranslationTableProcessor.parse(PER_BIT_TRANSLATION_BIT_TRANSLATION_TABLE_KEY, FOO, connector);
		assertEquals(translationTable, perBitTranslation.getBitTranslationTable());
	}
}