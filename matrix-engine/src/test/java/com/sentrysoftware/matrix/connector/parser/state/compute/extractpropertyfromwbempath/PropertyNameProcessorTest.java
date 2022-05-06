package com.sentrysoftware.matrix.connector.parser.state.compute.extractpropertyfromwbempath;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExtractPropertyFromWbemPath;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;

public class PropertyNameProcessorTest {
	
	private final PropertyNameProcessor propertyNameProcessor = new PropertyNameProcessor();

	private final Connector connector = new Connector();

	private static final String PROPERTY_NAME_KEY = "enclosure.collect.source(1).compute(1).propertyname";
	private static final String VALUE = "Name";

	@Test
	void testParse() {

		ExtractPropertyFromWbemPath extractPropertyFromWbemPath = ExtractPropertyFromWbemPath
			.builder()
			.index(1)
			.build();

		SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource
			.builder()
			.index(1)
			.computes(Collections.singletonList(extractPropertyFromWbemPath))
			.build();

		Collect collect = Collect
			.builder()
			.sources(Collections.singletonList(snmpGetTableSource))
			.build();

		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.collect(collect)
			.build();

		connector
			.getHardwareMonitors()
			.add(hardwareMonitor);

		propertyNameProcessor.parse(PROPERTY_NAME_KEY, VALUE, connector);
		assertEquals(VALUE, extractPropertyFromWbemPath.getPropertyName());
	}
}
