package com.sentrysoftware.matrix.connector.parser.state.source.snmptable;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnmpTableSelectColumnsProcessorTest {

	private static final String SNMP_TABLE_SELECT_COLUMNS_KEY = "enclosure.discovery.source(1).snmptableselectcolumns";
	private static final String VALUE = "1,7";
	private static final List<String> RESULT = Arrays.asList("1", "7");

	@Test
	void testParse() {

		SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource.builder().index(1).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(snmpGetTableSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(discovery)
			.build();
		Connector connector = new Connector();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		new SnmpTableSelectColumnsProcessor().parse(SNMP_TABLE_SELECT_COLUMNS_KEY, VALUE, connector);
		assertEquals(RESULT, snmpGetTableSource.getSnmpTableSelectColumns());
	}
}