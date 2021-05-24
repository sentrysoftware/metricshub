package com.sentrysoftware.matrix.connector.parser.state.compute.common;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnProcessorTest {

	private final ColumnProcessor columnProcessor = new ColumnProcessor(Add.class, "Add");

	private final Connector connector = new Connector();

	private static final String COLUMN_KEY = "enclosure.discovery.source(1).compute(1).column";
	private static final String FOO = "FOO";
	private static final String ONE = "1";

	@Test
	void testGetType() {

		assertEquals(Add.class, new ColumnProcessor(Add.class, null).getType());
	}

	@Test
	void testGetTypeValue() {

		assertNull(new ColumnProcessor(Add.class, null).getTypeValue());
	}

	@Test
	void testDetect() {

		// value null
		assertFalse(columnProcessor.detect(null, null, null));

		// value not null, key null
		assertFalse(columnProcessor.detect(null, ONE, null));

		// value not null, key not null, key does not match
		assertFalse(columnProcessor.detect(FOO, ONE, null));

		// value not null, key not null, key matches, no source found
		Discovery discovery = Discovery.builder().build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(discovery)
			.build();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));
		assertFalse(columnProcessor.detect(COLUMN_KEY, ONE, connector));

		// value not null, key not null, key matches, different type source found
		Source source2 = SNMPGetTableSource.builder().index(2).build();
		discovery.getSources().add(source2);
		assertFalse(columnProcessor.detect(COLUMN_KEY, ONE, connector));

		// value not null, key not null, key matches, same type source found, different index
		Source source3 = TableUnionSource.builder().index(3).build();
		discovery.getSources().add(source3);
		assertFalse(columnProcessor.detect(COLUMN_KEY, ONE, connector));

		// value not null, key not null, key matches, same type source found, same index, source.getComputes() == null
		Source source1 = TableUnionSource.builder().index(1).build();
		source1.setComputes(null);
		discovery.getSources().add(source1);
		assertFalse(columnProcessor.detect(COLUMN_KEY, ONE, connector));

		// value not null, key not null, key matches, same type source found, same index, source.getComputes() != null,
		// no same type compute
		source1.setComputes(Collections.singletonList(Divide.builder().index(1).build()));
		assertFalse(columnProcessor.detect(COLUMN_KEY, ONE, connector));

		// value not null, key not null, key matches, same type source found, same index, source.getComputes() != null,
		// same type compute found, different index
		source1.setComputes(Collections.singletonList(Add.builder().index(2).build()));
		assertFalse(columnProcessor.detect(COLUMN_KEY, ONE, connector));

		// value not null, key not null, key matches, same type source found, same index, source.getComputes() != null,
		// same type compute found, same index
		source1.setComputes(Collections.singletonList(Add.builder().index(1).build()));
		assertTrue(columnProcessor.detect(COLUMN_KEY, ONE, connector));
	}

	@Test
	void testParse() {

		// Invalid value
		assertThrows(IllegalArgumentException.class, () -> columnProcessor.parse(FOO, FOO, connector));

		// Value is valid, key does not match
		assertThrows(IllegalArgumentException.class, () -> columnProcessor.parse(FOO, ONE, connector));

		// Value is valid, key matches, no compute found
		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
			.builder()
			.index(1)
			.computes(Collections.emptyList())
			.build();
		Discovery discovery = Discovery
			.builder()
			.sources(Collections.singletonList(snmpGetTableSource))
			.build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(discovery)
			.build();
		connector.getHardwareMonitors().add(hardwareMonitor);
		assertThrows(IllegalArgumentException.class, () -> columnProcessor.parse(COLUMN_KEY, ONE, connector));

		// Value is valid, key matches, compute found
		Add add = Add.builder().index(1).build();
		snmpGetTableSource.setComputes(Collections.singletonList(add));
		columnProcessor.parse(COLUMN_KEY, ONE, connector);
		assertEquals(1, add.getColumn());
	}
}