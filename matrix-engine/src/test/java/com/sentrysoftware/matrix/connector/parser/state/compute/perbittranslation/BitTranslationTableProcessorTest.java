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

		// connector.getTranslationTables() == null
		connector.setTranslationTables(null);
		assertThrows(IllegalStateException.class,
			() -> bitTranslationTableProcessor.parse(PER_BIT_TRANSLATION_BIT_TRANSLATION_TABLE_KEY, FOO, connector));

		// connector.getTranslationTables() != null, translationTables.get(tableName) == null
		connector.setTranslationTables(Collections.emptyMap());
		assertThrows(IllegalStateException.class,
			() -> bitTranslationTableProcessor.parse(PER_BIT_TRANSLATION_BIT_TRANSLATION_TABLE_KEY, FOO, connector));

		// connector.getTranslationTables() != null, translationTables.get(tableName) != null
		TranslationTable translationTable = new TranslationTable();

		Map<String, TranslationTable> translationTables = new HashMap<>();
		translationTables.put(FOO, translationTable);

		connector.setTranslationTables(translationTables);

		bitTranslationTableProcessor.parse(PER_BIT_TRANSLATION_BIT_TRANSLATION_TABLE_KEY, FOO, connector);
		assertEquals(translationTable, perBitTranslation.getBitTranslationTable());
	}
}