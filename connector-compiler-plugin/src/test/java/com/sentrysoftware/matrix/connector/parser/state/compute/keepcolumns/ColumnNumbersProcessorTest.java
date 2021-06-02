package com.sentrysoftware.matrix.connector.parser.state.compute.keepcolumns;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepColumns;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColumnNumbersProcessorTest {

	private final ColumnNumbersProcessor columnNumbersProcessor = new ColumnNumbersProcessor();

	private final Connector connector = new Connector();

	private static final String KEEP_COLUMNS_COLUMN_NUMBERS_KEY = "enclosure.collect.source(1).compute(1).columnnumbers";
	private static final String VALID_VALUE = "1,42,9";
	private static final String INVALID_VALUE = "1;42,9";

	@Test
	void testParse() {

		assertThrows(IllegalStateException.class,
			() -> columnNumbersProcessor.parse(KEEP_COLUMNS_COLUMN_NUMBERS_KEY, INVALID_VALUE, connector));

		KeepColumns keepColumns = KeepColumns
			.builder()
			.index(1)
			.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(keepColumns))
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

		columnNumbersProcessor.parse(KEEP_COLUMNS_COLUMN_NUMBERS_KEY, VALID_VALUE, connector);
		assertEquals(Arrays.asList(1, 42, 9), keepColumns.getColumnNumbers());
	}
}