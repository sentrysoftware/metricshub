package com.sentrysoftware.matrix.connector.parser.state.compute.arraytranslate;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslationTableProcessorTest {

	private final TranslationTableProcessor translationTableProcessor = new TranslationTableProcessor();

	private final Connector connector = new Connector();

	private static final String ARRAY_TRANSLATE_TRANSLATION_TABLE_KEY =
		"enclosure.collect.source(1).compute(1).translationTable";

	private static final String FOO = "FOO";

	@Test
	void testParse() {

		ArrayTranslate arrayTranslate = ArrayTranslate
			.builder()
			.index(1)
			.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(arrayTranslate))
			.build();

		Collect collect = Collect
			.builder()
			.sources(Collections.singletonList(snmpGetTableSource))
			.build();

		TranslationTable translationTable = new TranslationTable();

		Map<String, TranslationTable> translationTables = new HashMap<>();
		translationTables.put(FOO, translationTable);

		connector.setTranslationTables(translationTables);

		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.collect(collect)
			.build();

		connector
			.getHardwareMonitors()
			.add(hardwareMonitor);

		translationTableProcessor.parse(ARRAY_TRANSLATE_TRANSLATION_TABLE_KEY, FOO, connector);
		assertEquals(translationTable, arrayTranslate.getTranslationTable());
	}
}