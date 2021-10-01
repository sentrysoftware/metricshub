package com.sentrysoftware.matrix.connector.parser.state.source.tablejoin;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LeftKeyColumnProcessorTest {

	private static final String LEFT_KEY_COLUMN_KEY = "enclosure.discovery.source(1).leftkeycolumn";
	private static final String FOO = "FOO";
	private static final String NINE = "9";

	@Test
	void testParse() {

		// Invalid value
		TableJoinSource tableJoinSource = TableJoinSource.builder().index(1).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(tableJoinSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(discovery)
			.build();
		Connector connector = new Connector();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));
		LeftKeyColumnProcessor leftKeyColumnProcessor = new LeftKeyColumnProcessor();
		assertThrows(
			IllegalArgumentException.class,
			() -> leftKeyColumnProcessor.parse(LEFT_KEY_COLUMN_KEY, FOO, connector));

		// Valid value
		leftKeyColumnProcessor.parse(LEFT_KEY_COLUMN_KEY, NINE, connector);
		assertEquals(9, tableJoinSource.getLeftKeyColumn());
	}
}