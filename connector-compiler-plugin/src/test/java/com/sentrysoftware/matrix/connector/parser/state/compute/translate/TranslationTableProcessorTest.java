package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Translate;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TranslationTableProcessorTest {

	private final TranslationTableProcessor translationTableProcessor = new TranslationTableProcessor();

	private final Connector connector = new Connector();

	private static final String TRANSLATE_TRANSLATION_TABLE_KEY = "enclosure.collect.source(1).compute(1).translationTable";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> translationTableProcessor.parse(FOO, FOO, connector));

		// Key matches, no Translate found
		assertThrows(IllegalArgumentException.class,
			() -> translationTableProcessor.parse(TRANSLATE_TRANSLATION_TABLE_KEY, FOO, connector));

		// Key matches, Translate found, no TranslationTables found
		Translate translate = new Translate();
		translate.setIndex(1);

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
						.computes(Collections.singletonList(translate))
						.build()))
					.build())
				.build());

		assertThrows(IllegalStateException.class,
			() -> translationTableProcessor.parse(TRANSLATE_TRANSLATION_TABLE_KEY, FOO, connector));

		// Key matches, Translate found, TranslationTables found, no TranslationTable named FOO
		Map<String, TranslationTable> translationTables = new HashMap<>();
		connector.setTranslationTables(translationTables);
		assertThrows(IllegalStateException.class,
			() -> translationTableProcessor.parse(TRANSLATE_TRANSLATION_TABLE_KEY, FOO, connector));

		// Key matches, Translate found, TranslationTables found, TranslationTable named FOO found
		TranslationTable translationTable = new TranslationTable();
		translationTables.put(FOO, translationTable);
		translationTableProcessor.parse(TRANSLATE_TRANSLATION_TABLE_KEY, FOO, connector);
		assertEquals(translationTable, translate.getTranslationTable());
	}
}