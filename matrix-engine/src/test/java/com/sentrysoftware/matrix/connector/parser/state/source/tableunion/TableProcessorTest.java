package com.sentrysoftware.matrix.connector.parser.state.source.tableunion;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableProcessorTest {

	private final TableProcessor tableProcessor = new TableProcessor();

	private final Connector connector = new Connector();

	private static final String TABLE1_KEY = "enclosure.discovery.source(3).table1";
	private static final String TABLE1_VALUE = "%enclosure.discovery.source(1)%";
	private static final String TABLE2_KEY = "enclosure.discovery.source(3).table2";
	private static final String TABLE2_VALUE = "%enclosure.discovery.source(2)%";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> tableProcessor.parse(FOO, FOO, connector));

		// Key matches, source not found
		assertThrows(IllegalArgumentException.class, () -> tableProcessor.parse(TABLE1_KEY, FOO, connector));

		// Key matches, source found, forceSerialization == 0
		TableUnionSource tableUnionSource = TableUnionSource.builder().index(3).tables(new ArrayList<>()).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(tableUnionSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(discovery)
			.build();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));
		tableProcessor.parse(TABLE1_KEY, TABLE1_VALUE, connector);
		tableProcessor.parse(TABLE2_KEY, TABLE2_VALUE, connector);
		assertNotNull(connector.getHardwareMonitors());
		assertEquals(1, connector.getHardwareMonitors().size());
		assertEquals(hardwareMonitor, connector.getHardwareMonitors().get(0));
		assertEquals(discovery, connector.getHardwareMonitors().get(0).getDiscovery());
		assertNotNull(connector.getHardwareMonitors().get(0).getDiscovery().getSources());
		assertEquals(1, connector.getHardwareMonitors().get(0).getDiscovery().getSources().size());
		assertEquals(tableUnionSource, connector.getHardwareMonitors().get(0).getDiscovery().getSources().get(0));
		List<String> tables = tableUnionSource.getTables();
		assertNotNull(tables);
		assertEquals(2, tables.size());
		assertTrue(tables.contains(TABLE1_VALUE.substring(1, TABLE1_VALUE.lastIndexOf("%"))));
		assertTrue(tables.contains(TABLE2_VALUE.substring(1, TABLE2_VALUE.lastIndexOf("%"))));
	}
}