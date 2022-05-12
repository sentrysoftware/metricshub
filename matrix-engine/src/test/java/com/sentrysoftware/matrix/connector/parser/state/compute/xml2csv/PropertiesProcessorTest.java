package com.sentrysoftware.matrix.connector.parser.state.compute.xml2csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Xml2Csv;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;

class PropertiesProcessorTest {

	private static final PropertiesProcessor PROPERTIES_PROCESSOR = new PropertiesProcessor();
	private static final String KEY = "enclosure.discovery.source(1).compute(1).Properties";
	private static final String VALUE = "elementA/propertyA;../elementB/propertyB;/elementC>attributeC";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(Xml2Csv.class, PROPERTIES_PROCESSOR.getType());
	}

	@Test
	void testGetValue() {
		assertEquals("Xml2Csv", PROPERTIES_PROCESSOR.getTypeValue());
	}

	@Test
	void testParse() {
		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> PROPERTIES_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> PROPERTIES_PROCESSOR.parse(KEY, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> PROPERTIES_PROCESSOR.parse(KEY, VALUE, null));

		// check criterion not found
		assertThrows(IllegalArgumentException.class, () -> PROPERTIES_PROCESSOR.parse(KEY, VALUE, CONNECTOR));

		final Xml2Csv xml2Csv = Xml2Csv.builder().index(1).build();

		final SNMPGetTableSource snmpGetTableSource =
				SNMPGetTableSource.builder().index(1).computes(Collections.singletonList(xml2Csv)).build();

		final Discovery discovery = Discovery.builder().sources(Collections.singletonList(snmpGetTableSource)).build();

		final HardwareMonitor hardwareMonitor =
				HardwareMonitor.builder().type(MonitorType.ENCLOSURE).discovery(discovery).build();

		CONNECTOR.getHardwareMonitors().add(hardwareMonitor);

		PROPERTIES_PROCESSOR.parse(KEY, VALUE, CONNECTOR);

		assertEquals(VALUE, xml2Csv.getProperties());
	}

}
