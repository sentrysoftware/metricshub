package com.sentrysoftware.matrix.connector.parser.state.compute.arraytranslate;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.COMMA;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArraySeparatorProcessorTest {

	private final ArraySeparatorProcessor arraySeparatorProcessor = new ArraySeparatorProcessor();

	private final Connector connector = new Connector();

	private static final String ARRAY_TRANSLATE_ARRAY_SEPARATOR_KEY =
		"enclosure.collect.source(1).compute(1).arrayseparator";

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

		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.collect(collect)
			.build();

		connector
			.getHardwareMonitors()
			.add(hardwareMonitor);

		arraySeparatorProcessor.parse(ARRAY_TRANSLATE_ARRAY_SEPARATOR_KEY, COMMA, connector);
		assertEquals(COMMA, arrayTranslate.getArraySeparator());
	}
}