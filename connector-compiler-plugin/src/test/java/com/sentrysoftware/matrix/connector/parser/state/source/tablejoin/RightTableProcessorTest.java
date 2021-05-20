package com.sentrysoftware.matrix.connector.parser.state.source.tablejoin;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class RightTableProcessorTest {

	private static final String RIGHT_TABLE_KEY = "enclosure.discovery.source(3).righttable";
	private static final String VALUE = "%enclosure.discovery.source(1)%";
	private static final String RESULT = "enclosure.discovery.source(1)";

	@Test
	void testParse() {

		TableJoinSource tableJoinSource = TableJoinSource.builder().index(3).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(tableJoinSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(discovery)
			.build();
		Connector connector = new Connector();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		new RightTableProcessor().parse(RIGHT_TABLE_KEY, VALUE, connector);
		assertEquals(RESULT, tableJoinSource.getRightTable());
	}
}