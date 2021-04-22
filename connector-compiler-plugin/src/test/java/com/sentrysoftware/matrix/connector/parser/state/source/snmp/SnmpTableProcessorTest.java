package com.sentrysoftware.matrix.connector.parser.state.source.snmp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPSource;

public class SnmpTableProcessorTest {

	private SnmpTableProcessor snmpTableProcessor;

	private Connector connector;

	private static final String ENCLOSURE_CRITERION_1_TYPE = "Enclosure.Discovery.Source(1).Type";
	private static final String ENCLOSURE_CRITERION_2_TYPE = "Enclosure.Discovery.Source(2).Type";
	private static final String FAN_CRITERION_1_TYPE = "Fan.Discovery.Source(1).Type";

	private static final String ENCLOSURE_CRITERION_1_OID = "Enclosure.Discovery.Source(1).SnmpTableOid";
	private static final String ENCLOSURE_CRITERION_2_OID = "Enclosure.Discovery.Source(2).SnmpTableOid";
	private static final String FAN_CRITERION_1_OID = "Fan.Discovery.Source(1).SnmpTableOid";

	private static final String ENCLOSURE_CRITERION_1_COLUMNS = "Enclosure.Discovery.Source(1).SnmpTableSelectColumns";
	private static final String ENCLOSURE_CRITERION_2_COLUMNS = "Enclosure.Discovery.Source(2).SnmpTableSelectColumns";
	private static final String FAN_CRITERION_1_COLUMNS = "Fan.Discovery.Source(1).SnmpTableSelectColumns";

	private static final String TYPE_SNMP_TABLE = "SnmpTable";

	private static final String SNMP_TABLE_OID_1 = "1.3.6.1.4.1.674.10892.1.300.10.1";
	private static final String SNMP_TABLE_OID_2 = "1.3.6.1.4.1.674.10893.1.1100.32.1";
	private static final String SNMP_TABLE_OID_3 = "1.3.6.1.4.1.674.10892.1.600.50.1";

	private static final String SNMP_TABLE_SELECT_COLUMNS_1 = "ID,9,11,49";
	private static final String SNMP_TABLE_SELECT_COLUMNS_2 = "ID,6,8";
	private static final String SNMP_TABLE_SELECT_COLUMNS_3 = "ID,3,5,7,11,13,17";

	private static final List<String> SNMP_TABLE_SELECT_COLUMNS_LIST_1 = new ArrayList<>(Arrays.asList("ID","9","11","49"));
	private static final List<String> SNMP_TABLE_SELECT_COLUMNS_LIST_2 = new ArrayList<>(Arrays.asList("ID","6","8"));
	private static final List<String> SNMP_TABLE_SELECT_COLUMNS_LIST_3 = new ArrayList<>(Arrays.asList("ID","3","5","7","11","13","17"));

	@BeforeEach
	void setUp() {
		snmpTableProcessor = new SnmpTableProcessor();
		connector = new Connector();
	}

	@Test
	void testDetect() {
		assertFalse(snmpTableProcessor.detect(null, null, null));
		assertFalse(snmpTableProcessor.detect(null, TYPE_SNMP_TABLE, null));
		assertFalse(snmpTableProcessor.detect(ENCLOSURE_CRITERION_1_TYPE, null, null));
		assertTrue(snmpTableProcessor.detect(ENCLOSURE_CRITERION_1_TYPE, TYPE_SNMP_TABLE, null));

		assertTrue(snmpTableProcessor.detect(ENCLOSURE_CRITERION_1_TYPE, TYPE_SNMP_TABLE, connector));
		assertTrue(snmpTableProcessor.detect(ENCLOSURE_CRITERION_1_OID, SNMP_TABLE_OID_1, connector));
		assertTrue(snmpTableProcessor.detect(ENCLOSURE_CRITERION_1_COLUMNS, SNMP_TABLE_SELECT_COLUMNS_1, connector));

		assertTrue(snmpTableProcessor.detect(ENCLOSURE_CRITERION_2_TYPE, TYPE_SNMP_TABLE, connector));
		assertTrue(snmpTableProcessor.detect(ENCLOSURE_CRITERION_2_OID, SNMP_TABLE_OID_2, connector));
		assertTrue(snmpTableProcessor.detect(ENCLOSURE_CRITERION_2_COLUMNS, SNMP_TABLE_SELECT_COLUMNS_2, connector));

		assertTrue(snmpTableProcessor.detect(FAN_CRITERION_1_TYPE, TYPE_SNMP_TABLE, connector));
		assertTrue(snmpTableProcessor.detect(FAN_CRITERION_1_OID, SNMP_TABLE_OID_3, connector));
		assertTrue(snmpTableProcessor.detect(FAN_CRITERION_1_COLUMNS, SNMP_TABLE_SELECT_COLUMNS_3, connector));
	}

	@Test
	void testParse() {
		/* 
		 * Parsing of :
		 * Enclosure.Discovery.Source(1).Type="SnmpTable"
		 * Enclosure.Discovery.Source(1).SnmpTableOid="1.3.6.1.4.1.674.10892.1.300.10.1"
		 * Enclosure.Discovery.Source(1).SnmpTableSelectColumns="ID,9,11,49"
		 */
		snmpTableProcessor.parse(ENCLOSURE_CRITERION_1_TYPE, TYPE_SNMP_TABLE, connector);

		snmpTableProcessor.parse(ENCLOSURE_CRITERION_1_OID, SNMP_TABLE_OID_1, connector);
		Optional<HardwareMonitor> hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.ENCLOSURE)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		Optional<Source> sourceOpt = hardwareMonitorOpt.get().getDiscovery().getSources().stream()
				.filter(src -> 1 == src.getIndex()).findFirst();

		assertTrue(sourceOpt.isPresent());

		Source source = sourceOpt.get();
		assertEquals(SNMP_TABLE_OID_1, ((SNMPSource) source).getOid());
		assertEquals(1, source.getIndex());

		snmpTableProcessor.parse(ENCLOSURE_CRITERION_1_COLUMNS, SNMP_TABLE_SELECT_COLUMNS_1, connector);

		assertEquals(SNMP_TABLE_SELECT_COLUMNS_LIST_1, ((SNMPGetTableSource) source).getSnmpTableSelectColumns());

		/* 
		 * Parsing of :
		 * Enclosure.Discovery.Source(2).Type="SnmpTable"
		 * Enclosure.Discovery.Source(2).SnmpTableOid="1.3.6.1.4.1.674.10893.1.1100.32.1"
		 * Enclosure.Discovery.Source(2).SnmpTableSelectColumns="ID,6,8"
		 */
		snmpTableProcessor.parse(ENCLOSURE_CRITERION_2_TYPE, TYPE_SNMP_TABLE, connector);

		snmpTableProcessor.parse(ENCLOSURE_CRITERION_2_OID, SNMP_TABLE_OID_2, connector);
		hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.ENCLOSURE)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		sourceOpt = hardwareMonitorOpt.get().getDiscovery().getSources().stream()
				.filter(src -> 2 == src.getIndex()).findFirst();

		assertTrue(sourceOpt.isPresent());

		source = sourceOpt.get();
		assertEquals(SNMP_TABLE_OID_2, ((SNMPSource) source).getOid());
		assertEquals(2, source.getIndex());

		snmpTableProcessor.parse(ENCLOSURE_CRITERION_2_COLUMNS, SNMP_TABLE_SELECT_COLUMNS_2, connector);
		assertEquals(SNMP_TABLE_SELECT_COLUMNS_LIST_2, ((SNMPGetTableSource) source).getSnmpTableSelectColumns());

		/* 
		 * Parsing of :
		 * Fan.Discovery.Source(1).Type="SnmpTable"
		 * Fan.Discovery.Source(1).SnmpTableOid="1.3.6.1.4.1.674.10892.1.600.50.1"
		 * Fan.Discovery.Source(1).SnmpTableSelectColumns="ID,3,5,7,11,13,17"
		 */
		snmpTableProcessor.parse(FAN_CRITERION_1_TYPE, TYPE_SNMP_TABLE, connector);

		snmpTableProcessor.parse(FAN_CRITERION_1_OID, SNMP_TABLE_OID_3, connector);
		hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.FAN)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		sourceOpt = hardwareMonitorOpt.get().getDiscovery().getSources().stream()
				.filter(src -> 1 == src.getIndex()).findFirst();

		assertTrue(sourceOpt.isPresent());

		source = sourceOpt.get();
		assertEquals(SNMP_TABLE_OID_3, ((SNMPSource) source).getOid());
		assertEquals(1, source.getIndex());

		snmpTableProcessor.parse(FAN_CRITERION_1_COLUMNS, SNMP_TABLE_SELECT_COLUMNS_3, connector);
		assertEquals(SNMP_TABLE_SELECT_COLUMNS_LIST_3, ((SNMPGetTableSource) source).getSnmpTableSelectColumns());

		// We make sure that previously parsed sources were not impacted
		hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.ENCLOSURE)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		List<Source> sources = hardwareMonitorOpt.get().getDiscovery().getSources();
		assertEquals(2, sources.size());

		sourceOpt = sources.stream().filter(src -> 1 == src.getIndex()).findFirst();
		assertTrue(sourceOpt.isPresent());

		source = sourceOpt.get();

		assertEquals(SNMP_TABLE_OID_1, ((SNMPSource) source).getOid());
		assertEquals(1, source.getIndex());
		assertEquals(SNMP_TABLE_SELECT_COLUMNS_LIST_1, ((SNMPGetTableSource) source).getSnmpTableSelectColumns());

		sourceOpt = sources.stream().filter(src -> 2 == src.getIndex()).findFirst();
		assertTrue(sourceOpt.isPresent());

		source = sourceOpt.get();

		assertEquals(SNMP_TABLE_OID_2, ((SNMPSource) source).getOid());
		assertEquals(2, source.getIndex());
		assertEquals(SNMP_TABLE_SELECT_COLUMNS_LIST_2, ((SNMPGetTableSource) source).getSnmpTableSelectColumns());
	}

	@Test
	void testParseSNMPTableEmptyConnector() {
		snmpTableProcessor.parse(ENCLOSURE_CRITERION_1_OID, SNMP_TABLE_OID_1, connector);

		Optional<HardwareMonitor> hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.ENCLOSURE)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		Optional<Source> sourceOpt = hardwareMonitorOpt.get().getDiscovery().getSources().stream()
				.filter(src -> 1 == src.getIndex()).findFirst();

		assertTrue(sourceOpt.isPresent());

		Source source = sourceOpt.get();
		assertEquals(SNMP_TABLE_OID_1, ((SNMPSource) source).getOid());
		assertEquals(1, source.getIndex());

		connector = new Connector();
		snmpTableProcessor.parse(ENCLOSURE_CRITERION_1_COLUMNS, SNMP_TABLE_SELECT_COLUMNS_1, connector);

		hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.ENCLOSURE)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		sourceOpt = hardwareMonitorOpt.get().getDiscovery().getSources().stream()
				.filter(src -> 1 == src.getIndex()).findFirst();

		assertTrue(sourceOpt.isPresent());

		source = sourceOpt.get();
		assertEquals(1, source.getIndex());

		assertEquals(SNMP_TABLE_SELECT_COLUMNS_LIST_1, ((SNMPGetTableSource) source).getSnmpTableSelectColumns());
	}
}
