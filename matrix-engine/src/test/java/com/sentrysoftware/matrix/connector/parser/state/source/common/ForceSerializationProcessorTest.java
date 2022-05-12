package com.sentrysoftware.matrix.connector.parser.state.source.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForceSerializationProcessorTest {

	private final ForceSerializationProcessor forceSerializationProcessor =
		new ForceSerializationProcessor(TableUnionSource.class, "TableUnion");

	private final Connector connector = new Connector();

	private static final String FORCE_SERIALIZATION_KEY = "enclosure.discovery.source(1).forceserialization";
	private static final String FOO = "FOO";
	private static final String ZERO = "0";
	private static final String ONE = "1";

	@Test
	void testGetType() {

		assertEquals(TableUnionSource.class, new ForceSerializationProcessor(TableUnionSource.class, null).getType());
	}

	@Test
	void testGetTypeValue() {

		assertNull(new ForceSerializationProcessor(TableUnionSource.class, null).getTypeValue());
	}

	@Test
	void testDetect() {

		// value null
		assertFalse(forceSerializationProcessor.detect(null, null, null));

		// value not null, key null
		assertFalse(forceSerializationProcessor.detect(null, ONE, null));

		// value not null, key not null, key does not match
		assertFalse(forceSerializationProcessor.detect(FOO, ONE, null));

		// value not null, key not null, key matches, no source found
		Discovery discovery = Discovery.builder().build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(discovery)
			.build();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));
		assertFalse(forceSerializationProcessor.detect(FORCE_SERIALIZATION_KEY, ONE, connector));

		// value not null, key not null, key matches, different type source found
		Source source2 = SnmpGetTableSource.builder().index(2).build();
		discovery.getSources().add(source2);
		assertFalse(forceSerializationProcessor.detect(FORCE_SERIALIZATION_KEY, ONE, connector));

		// value not null, key not null, key matches, same type source found, different index
		Source source3 = TableUnionSource.builder().index(3).build();
		discovery.getSources().add(source3);
		assertFalse(forceSerializationProcessor.detect(FORCE_SERIALIZATION_KEY, ONE, connector));

		// value not null, key not null, key matches, same type source found, same index
		Source source1 = TableUnionSource.builder().index(1).build();
		discovery.getSources().add(source1);
		assertTrue(forceSerializationProcessor.detect(FORCE_SERIALIZATION_KEY, ONE, connector));
	}

	@Test
	void testParse() {

		// Key does not match
		assertThrows(IllegalArgumentException.class, () -> forceSerializationProcessor.parse(FOO, FOO, connector));

		// Key matches, no HardwareMonitor
		assertThrows(
			IllegalArgumentException.class,
			() -> forceSerializationProcessor.parse(FORCE_SERIALIZATION_KEY, FOO, connector));

		// Key matches, HardwareMonitor found, no MonitorJob
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(null)
			.build();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));
		assertThrows(
			IllegalArgumentException.class,
			() -> forceSerializationProcessor.parse(FORCE_SERIALIZATION_KEY, FOO, connector));

		// Key matches, HardwareMonitor found, MonitorJob found, monitorJob.getSources == null
		Discovery discovery = new Discovery();
		discovery.setSources(null);
		hardwareMonitor.setDiscovery(discovery);
		assertThrows(
			IllegalArgumentException.class,
			() -> forceSerializationProcessor.parse(FORCE_SERIALIZATION_KEY, FOO, connector));

		// Key matches, HardwareMonitor found, MonitorJob found, monitorJob.getSources != null, source not found
		discovery.setSources(Collections.emptyList());
		assertThrows(
			IllegalArgumentException.class,
			() -> forceSerializationProcessor.parse(FORCE_SERIALIZATION_KEY, FOO, connector));

		// Key matches, HardwareMonitor found, MonitorJob found, monitorJob.getSources != null, source found,
		// forceSerialization == 0
		Source source = TableUnionSource.builder().index(1).build();
		discovery.setSources(Collections.singletonList(source));
		forceSerializationProcessor.parse(FORCE_SERIALIZATION_KEY, ZERO, connector);
		assertNotNull(connector.getHardwareMonitors());
		assertEquals(1, connector.getHardwareMonitors().size());
		assertEquals(hardwareMonitor, connector.getHardwareMonitors().get(0));
		assertEquals(discovery, connector.getHardwareMonitors().get(0).getDiscovery());
		assertNotNull(connector.getHardwareMonitors().get(0).getDiscovery().getSources());
		assertEquals(1, connector.getHardwareMonitors().get(0).getDiscovery().getSources().size());
		assertEquals(source, connector.getHardwareMonitors().get(0).getDiscovery().getSources().get(0));
		assertFalse(source.isForceSerialization());

		// Key matches, source found, forceSerialization == 1
		forceSerializationProcessor.parse(FORCE_SERIALIZATION_KEY, ONE, connector);
		assertTrue(source.isForceSerialization());
	}
}