package com.sentrysoftware.matrix.connector.parser.state.source.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeProcessorTest {

	private final TypeProcessor typeProcessor = new TypeProcessor(TableUnionSource.class, "TableUnion");

	private final Connector connector = new Connector();
	private static final String TYPE_DISCOVERY_KEY = "enclosure.discovery.source(1).type";
	private static final String TYPE_COLLECT_KEY = "enclosure.collect.source(1).type";
	private static final String TYPE_VALUE = "TableUnion";
	private static final String SOURCE_DISCOVERY_KEY = "enclosure.discovery.source(1)";
	private static final String SOURCE_COLLECT_KEY = "enclosure.collect.source(1)";
	private static final String FOO = "FOO";

	@Test
	void testGetType() {

		assertEquals(TableUnionSource.class, new TypeProcessor(TableUnionSource.class, null).getType());
	}

	@Test
	void testParse() {

		connector.setHardwareMonitors(new ArrayList<>());

		// Value is invalid
		assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, FOO, connector));

		// Value is valid, key does not match
		assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, TYPE_VALUE, connector));

		// Value is valid, key matches, no HardwareMonitor found (=> HardwareMonitor is created)
		typeProcessor.parse(TYPE_DISCOVERY_KEY, TYPE_VALUE, connector);
		assertNotNull(connector.getHardwareMonitors());
		assertEquals(1, connector.getHardwareMonitors().size());
		HardwareMonitor hardwareMonitor = connector.getHardwareMonitors().get(0);
		assertEquals(MonitorType.ENCLOSURE, hardwareMonitor.getType());
		assertNotNull(hardwareMonitor.getCollect());
		assertNotNull(hardwareMonitor.getDiscovery());
		assertNotNull(hardwareMonitor.getDiscovery().getSources());
		assertEquals(1, hardwareMonitor.getDiscovery().getSources().size());
		Source source = hardwareMonitor.getDiscovery().getSources().get(0);
		assertNotNull(source);
		assertTrue(source instanceof TableUnionSource);
		assertEquals(1, source.getIndex());
		assertEquals(SOURCE_DISCOVERY_KEY, source.getKey());

		// Value is valid, key matches, HardwareMonitor found, Source already defined
		assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(TYPE_DISCOVERY_KEY, TYPE_VALUE, connector));

		// Value is valid, key matches, HardwareMonitor found, Source not already defined
		hardwareMonitor.getDiscovery().setSources(new ArrayList<>());
		typeProcessor.parse(TYPE_DISCOVERY_KEY, TYPE_VALUE, connector);
		assertNotNull(connector.getHardwareMonitors());
		assertEquals(1, connector.getHardwareMonitors().size());
		assertEquals(hardwareMonitor, connector.getHardwareMonitors().get(0));
		assertEquals(hardwareMonitor.getDiscovery(), connector.getHardwareMonitors().get(0).getDiscovery());
		assertNotNull(hardwareMonitor.getDiscovery().getSources());
		assertEquals(1, hardwareMonitor.getDiscovery().getSources().size());
		source = hardwareMonitor.getDiscovery().getSources().get(0);
		assertNotNull(source);
		assertTrue(source instanceof TableUnionSource);
		assertEquals(1, source.getIndex());
		assertEquals(SOURCE_DISCOVERY_KEY, source.getKey());
	}

	@Test
	void testParseCollect() {

		// Collect job is null
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.collect(null)
			.build();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));
		typeProcessor.parse(TYPE_COLLECT_KEY, TYPE_VALUE, connector);
		assertNotNull(connector.getHardwareMonitors());
		assertEquals(1, connector.getHardwareMonitors().size());
		assertEquals(hardwareMonitor, connector.getHardwareMonitors().get(0));
		Collect collect = connector.getHardwareMonitors().get(0).getCollect();
		assertNotNull(collect);
		assertNotNull(collect.getSources());
		assertEquals(1, collect.getSources().size());
		Source source = collect.getSources().get(0);
		assertNotNull(source);
		assertTrue(source instanceof TableUnionSource);
		assertEquals(1, source.getIndex());
		assertEquals(SOURCE_COLLECT_KEY, source.getKey());

		// Collect job is not null
		collect = Collect.builder().sources(new ArrayList<>()).build();
		hardwareMonitor.setCollect(collect);
		typeProcessor.parse(TYPE_COLLECT_KEY, TYPE_VALUE, connector);
		assertNotNull(connector.getHardwareMonitors());
		assertEquals(1, connector.getHardwareMonitors().size());
		assertEquals(hardwareMonitor, connector.getHardwareMonitors().get(0));
		assertEquals(collect, connector.getHardwareMonitors().get(0).getCollect());
		assertNotNull(collect.getSources());
		assertEquals(1, collect.getSources().size());
		source = collect.getSources().get(0);
		assertNotNull(source);
		assertTrue(source instanceof TableUnionSource);
		assertEquals(1, source.getIndex());
		assertEquals(SOURCE_COLLECT_KEY, source.getKey());

		// Could not instantiate Source
		String snmpTable = "SnmpTable";
		TypeProcessor snmpTypeProcessor = new TypeProcessor(SnmpSource.class, snmpTable);
		assertThrows(IllegalStateException.class, () -> snmpTypeProcessor.parse(TYPE_COLLECT_KEY, snmpTable, connector));
	}
}