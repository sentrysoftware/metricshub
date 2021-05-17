package com.sentrysoftware.matrix.connector.parser.state.source.snmptable;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnmpTableOidProcessorTest {

	private static final String SNMP_TABLE_OID_KEY = "enclosure.discovery.source(1).snmptableoid";
	private static final String VALUE = "1.3.6.1.4.1.674.10892.1.600.60.1";

	@Test
	void testParse() {

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource.builder().index(1).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(snmpGetTableSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(discovery)
			.build();
		Connector connector = new Connector();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		new SnmpTableOidProcessor().parse(SNMP_TABLE_OID_KEY, VALUE, connector);
		assertEquals(VALUE, snmpGetTableSource.getOid());
	}
}