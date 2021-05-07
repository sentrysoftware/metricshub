package com.sentrysoftware.matrix.connector.parser.state.compute.duplicatecolumn;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ColumnProcessorTest {

	private final ColumnProcessor columnProcessor = new ColumnProcessor();

	private final Connector connector = new Connector();

	private static final String DUPLICATE_COLUMN_COLUMN_KEY = "enclosure.collect.source(1).compute(1).column";
	private static final String FOO = "FOO";
	private static final String NINE = "9";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> columnProcessor.parse(FOO, FOO, connector));

		// Key matches, no DuplicateColumn found
		assertThrows(IllegalArgumentException.class,
			() -> columnProcessor.parse(DUPLICATE_COLUMN_COLUMN_KEY, FOO, connector));

		// Key matches, DuplicateColumn found, invalid value
		DuplicateColumn duplicateColumn = new DuplicateColumn();
		duplicateColumn.setIndex(1);

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
						.computes(Collections.singletonList(duplicateColumn))
						.build()))
					.build())
				.build());

		assertThrows(IllegalArgumentException.class,
			() -> columnProcessor.parse(DUPLICATE_COLUMN_COLUMN_KEY, FOO, connector));

		// Key matches, DuplicateColumn found, value is valid
		columnProcessor.parse(DUPLICATE_COLUMN_COLUMN_KEY, NINE, connector);
		assertEquals(9, duplicateColumn.getColumn());
	}
}