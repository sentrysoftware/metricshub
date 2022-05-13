package com.sentrysoftware.matrix.connector.parser.state.compute.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.AbstractConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeProcessorTest {

	private final TypeProcessor typeProcessor = new TypeProcessor(Add.class, "Add");

	private final Connector connector = new Connector();
	private static final String TYPE_DISCOVERY_KEY = "enclosure.discovery.source(1).compute(1).type";
	private static final String TYPE_VALUE = "Add";
	private static final String FOO = "FOO";

	@Test
	void testGetType() {

		assertEquals(Add.class, new TypeProcessor(Add.class, null).getType());
	}

	@Test
	void testDetect() {

		assertFalse(typeProcessor.detect(TYPE_DISCOVERY_KEY, FOO, null));
		assertTrue(typeProcessor.detect(TYPE_DISCOVERY_KEY, TYPE_VALUE, null));
	}

	@Test
	void testParse() {

		connector.setHardwareMonitors(new ArrayList<>());

		// Value is invalid
		assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, FOO, connector));

		// Value is valid, key does not match
		assertThrows(IllegalArgumentException.class, () -> typeProcessor.parse(FOO, TYPE_VALUE, connector));

		// Value is valid, key matches, no source found
		Discovery discovery = Discovery
			.builder()
			.sources(Collections.emptyList())
			.build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(discovery)
			.build();
		connector
			.getHardwareMonitors()
			.add(hardwareMonitor);
		typeProcessor.parse(TYPE_DISCOVERY_KEY, TYPE_VALUE, connector);
		assertTrue(discovery.getSources().isEmpty());

		// Value is valid, key matches, source found, sources.getComputes() == null
		SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource
			.builder()
			.index(1)
			.build();
		snmpGetTableSource.setComputes(null);
		discovery.setSources(Collections.singletonList(snmpGetTableSource));
		typeProcessor.parse(TYPE_DISCOVERY_KEY, TYPE_VALUE, connector);
		List<Compute> computes = snmpGetTableSource.getComputes();
		assertNotNull(computes);
		assertEquals(1, computes.size());
		Compute compute = computes.get(0);
		assertTrue(compute instanceof Add);
		assertEquals(1, compute.getIndex());

		// Value is valid, key matches, sources.getComputes() != null
		snmpGetTableSource.setComputes(new ArrayList<>());
		typeProcessor.parse(TYPE_DISCOVERY_KEY, TYPE_VALUE, connector);
		computes = snmpGetTableSource.getComputes();
		assertNotNull(computes);
		assertEquals(1, computes.size());
		compute = computes.get(0);
		assertTrue(compute instanceof Add);
		assertEquals(1, compute.getIndex());

		// Value is valid, key matches, sources.getComputes() != null, Compute instantiation is not possible
		LeftConcat leftConcat = LeftConcat.builder().index(1).build();
		snmpGetTableSource.setComputes(Collections.singletonList(leftConcat));
		TypeProcessor abstractConcatTypeProcessor = new TypeProcessor(AbstractConcat.class, "LeftConcat");
		assertThrows(IllegalStateException.class,
			() -> abstractConcatTypeProcessor.parse(TYPE_DISCOVERY_KEY, "LeftConcat", connector));
	}
}