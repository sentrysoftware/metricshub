package com.sentrysoftware.matrix.connector.parser.state.compute.xml2csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Xml2Csv;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;

class RecordTagProcessorTest {

	private static final RecordTagProcessor RECORD_TAG_PROCESSOR = new RecordTagProcessor();
	private static final String KEY = "enclosure.collect.source(1).compute(1).RecordTag";
	private static final String VALUE = "Document/Datas";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(Xml2Csv.class, RECORD_TAG_PROCESSOR.getType());
	}

	@Test
	void testGetValue() {
		assertEquals("Xml2Csv", RECORD_TAG_PROCESSOR.getTypeValue());
	}

	@Test
	void testParse() {
		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> RECORD_TAG_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> RECORD_TAG_PROCESSOR.parse(KEY, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> RECORD_TAG_PROCESSOR.parse(KEY, VALUE, null));

		// check criterion not found
		assertThrows(IllegalArgumentException.class, () -> RECORD_TAG_PROCESSOR.parse(KEY, VALUE, CONNECTOR));

		final Xml2Csv xml2Csv = Xml2Csv.builder().index(1).build();

		final SnmpGetTableSource snmpGetTableSource =
				SnmpGetTableSource.builder().index(1).computes(Collections.singletonList(xml2Csv)).build();

		final Collect collect = Collect.builder().sources(Collections.singletonList(snmpGetTableSource)).build();

		final HardwareMonitor hardwareMonitor =
				HardwareMonitor.builder().type(MonitorType.ENCLOSURE).collect(collect).build();

		CONNECTOR.getHardwareMonitors().add(hardwareMonitor);

		RECORD_TAG_PROCESSOR.parse(KEY, VALUE, CONNECTOR);

		assertEquals(VALUE, xml2Csv.getRecordTag());
	}
}
