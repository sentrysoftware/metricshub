package com.sentrysoftware.matrix.connector.parser.state.source.snmpget;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;

public class SnmpGetOidProcessorTest {
	private static final String SNMP_GET_OID_KEY = "enclosure.discovery.source(1).snmpoid";
	private static final String VALUE = "1.3.6.1.2.1.1.1.0";
	
	private SnmpGetOidProcessor snmpGetOidProcessor;
	private Connector connector;

	@BeforeEach
	void setUp() {
		connector = new Connector();
		snmpGetOidProcessor = new SnmpGetOidProcessor();
	}

	@Test
	void testDetect() {
		assertFalse(snmpGetOidProcessor.detect(null, null, null));
		assertFalse(snmpGetOidProcessor.detect(null, VALUE, null));
		assertFalse(snmpGetOidProcessor.detect(SNMP_GET_OID_KEY, null, null));
		assertThrows(IllegalArgumentException.class, () -> {snmpGetOidProcessor.detect(SNMP_GET_OID_KEY, VALUE, null);});

		SNMPGetSource source = SNMPGetSource.builder().index(1).build();
		
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(source)).build();
		
		HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();
		
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));
		assertTrue(snmpGetOidProcessor.detect(SNMP_GET_OID_KEY, VALUE, connector));
	}
	
	@Test
	void testParse() {
		SNMPGetSource snmpGetSource = SNMPGetSource.builder().index(1).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(snmpGetSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(discovery)
			.build();

		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		new SnmpGetOidProcessor().parse(SNMP_GET_OID_KEY, VALUE, connector);
		assertEquals(VALUE, snmpGetSource.getOid());
	}
}
