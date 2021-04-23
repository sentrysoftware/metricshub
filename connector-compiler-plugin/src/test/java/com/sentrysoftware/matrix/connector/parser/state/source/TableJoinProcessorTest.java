package com.sentrysoftware.matrix.connector.parser.state.source;

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
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;

public class TableJoinProcessorTest {
	private SnmpTableProcessor snmpTableProcessor;
	private TableJoinProcessor tableJoinProcessor;

	private Connector connector;

	private static final String ENCLOSURE_1_TYPE = "Enclosure.Discovery.Source(1).Type";
	private static final String ENCLOSURE_2_TYPE = "Enclosure.Discovery.Source(2).Type";

	private static final String ENCLOSURE_1_OID = "Enclosure.Discovery.Source(1).SnmpTableOid";
	private static final String ENCLOSURE_2_OID = "Enclosure.Discovery.Source(2).SnmpTableOid";

	private static final String ENCLOSURE_1_COLUMNS = "Enclosure.Discovery.Source(1).SnmpTableSelectColumns";
	private static final String ENCLOSURE_2_COLUMNS = "Enclosure.Discovery.Source(2).SnmpTableSelectColumns";

	private static final String TYPE_SNMP_TABLE = "SnmpTable";

	private static final String SNMP_TABLE_OID_1 = "1.3.6.1.4.1.674.10892.1.300.10.1";
	private static final String SNMP_TABLE_OID_2 = "1.3.6.1.4.1.674.10893.1.1100.32.1";

	private static final String SNMP_TABLE_SELECT_COLUMNS_1 = "ID,9,11,49";
	private static final String SNMP_TABLE_SELECT_COLUMNS_2 = "ID,6,8";

	private static final String ENCLOSURE_3_TYPE = "enclosure.discovery.source(3).type";
	private static final String ENCLOSURE_3_LEFT_TABLE = "enclosure.discovery.source(3).lefttable";
	private static final String ENCLOSURE_3_RIGHT_TABLE = "enclosure.discovery.source(3).righttable";
	private static final String ENCLOSURE_3_LEFT_KEY_COLUMN = "enclosure.discovery.source(3).leftkeycolumn";
	private static final String ENCLOSURE_3_RIGHT_KEY_COLUMN = "enclosure.discovery.source(3).rightkeycolumn";
	private static final String ENCLOSURE_3_DEFAULT_RIGHT_LINE = "enclosure.discovery.source(3).defaultrightline";

	private static final String ENCLOSURE_3_SOURCE_KEY = "enclosure.discovery.source(3)";

	private static final String TYPE_TABLE_JOINT = "tablejoint";
	private static final String LEFT_TABLE = "%Enclosure.Discovery.Source(1)%";
	private static final String RIGHT_TABLE = "%Enclosure.Discovery.Source(2)%";
	private static final String LEFT_KEY_COLUMN = "1";
	private static final String RIGHT_KEY_COLUMN = "1";
	private static final String DEFAULT_RIGHT_LINE = ";;";

	private static final int LEFT_KEY_COLUMN_RESULT = 1;
	private static final int RIGHT_KEY_COLUMN_RESULT = 1;
	private static final List<String> DEFAULT_RIGHT_LINE_RESULT = new ArrayList<>(Arrays.asList("",""));

	@BeforeEach
	void setUp() {
		snmpTableProcessor = new SnmpTableProcessor();
		tableJoinProcessor = new TableJoinProcessor();
		connector = new Connector();
	}

	@Test
	void testDetect() {
		assertFalse(tableJoinProcessor.detect(null, null, null));
		assertFalse(tableJoinProcessor.detect(null, TYPE_TABLE_JOINT, null));
		assertFalse(tableJoinProcessor.detect(ENCLOSURE_3_TYPE, null, null));
		assertTrue(tableJoinProcessor.detect(ENCLOSURE_3_TYPE, TYPE_TABLE_JOINT, null));

		assertTrue(tableJoinProcessor.detect(ENCLOSURE_3_TYPE, TYPE_TABLE_JOINT, connector));
		assertTrue(tableJoinProcessor.detect(ENCLOSURE_3_LEFT_TABLE, LEFT_TABLE, connector));
		assertTrue(tableJoinProcessor.detect(ENCLOSURE_3_RIGHT_TABLE, RIGHT_TABLE, connector));
		assertTrue(tableJoinProcessor.detect(ENCLOSURE_3_LEFT_KEY_COLUMN, LEFT_KEY_COLUMN, connector));
		assertTrue(tableJoinProcessor.detect(ENCLOSURE_3_RIGHT_KEY_COLUMN, RIGHT_KEY_COLUMN, connector));
		assertTrue(tableJoinProcessor.detect(ENCLOSURE_3_DEFAULT_RIGHT_LINE, DEFAULT_RIGHT_LINE, connector));
	}

	@Test
	void testParse() {
		/* 
		 * Parsing of :
		 * Enclosure.Discovery.Source(1).Type="SnmpTable"
		 * Enclosure.Discovery.Source(1).SnmpTableOid="1.3.6.1.4.1.674.10892.1.300.10.1"
		 * Enclosure.Discovery.Source(1).SnmpTableSelectColumns="ID,9,11,49"
		 */
		snmpTableProcessor.parse(ENCLOSURE_1_TYPE, TYPE_SNMP_TABLE, connector);
		snmpTableProcessor.parse(ENCLOSURE_1_OID, SNMP_TABLE_OID_1, connector);
		snmpTableProcessor.parse(ENCLOSURE_1_COLUMNS, SNMP_TABLE_SELECT_COLUMNS_1, connector);

		/* 
		 * Parsing of :
		 * Enclosure.Discovery.Source(2).Type="SnmpTable"
		 * Enclosure.Discovery.Source(2).SnmpTableOid="1.3.6.1.4.1.674.10893.1.1100.32.1"
		 * Enclosure.Discovery.Source(2).SnmpTableSelectColumns="ID,6,8"
		 */
		snmpTableProcessor.parse(ENCLOSURE_2_TYPE, TYPE_SNMP_TABLE, connector);
		snmpTableProcessor.parse(ENCLOSURE_2_OID, SNMP_TABLE_OID_2, connector);
		snmpTableProcessor.parse(ENCLOSURE_2_COLUMNS, SNMP_TABLE_SELECT_COLUMNS_2, connector);

		/* 
		 * Parsing of :
		 * Enclosure.Discovery.Source(3).Type="TableJoint"
		 * Enclosure.Discovery.Source(3).LeftTable=%Enclosure.Discovery.Source(1)%
		 * Enclosure.Discovery.Source(3).RightTable=%Enclosure.Discovery.Source(2)%
		 * Enclosure.Discovery.Source(3).LeftKeyColumn=1
		 * Enclosure.Discovery.Source(3).RightKeyColumn=1
		 * Enclosure.Discovery.Source(3).DefaultRightLine=";;"
		 */
		tableJoinProcessor.parse(ENCLOSURE_3_TYPE, TYPE_TABLE_JOINT, connector);
		Optional<HardwareMonitor> hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.ENCLOSURE)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		List<Source> sources = hardwareMonitorOpt.get().getDiscovery().getSources();
		assertEquals(3, sources.size());

		Optional<Source> sourceOpt = sources.stream()
				.filter(src -> 3 == src.getIndex()).findFirst();

		Source source = sourceOpt.get();
		assertEquals(ENCLOSURE_3_SOURCE_KEY, source.getKey());

		tableJoinProcessor.parse(ENCLOSURE_3_LEFT_TABLE, LEFT_TABLE, connector);

		assertEquals(LEFT_TABLE, ((TableJoinSource) source).getLeftTable());
		assertEquals(3, source.getIndex());

		tableJoinProcessor.parse(ENCLOSURE_3_RIGHT_TABLE, RIGHT_TABLE, connector);
		assertEquals(RIGHT_TABLE, ((TableJoinSource) source).getRightTable());

		tableJoinProcessor.parse(ENCLOSURE_3_LEFT_KEY_COLUMN, LEFT_KEY_COLUMN, connector);
		assertEquals(LEFT_KEY_COLUMN_RESULT, ((TableJoinSource) source).getLeftKeyColumn());

		tableJoinProcessor.parse(ENCLOSURE_3_RIGHT_KEY_COLUMN, RIGHT_KEY_COLUMN, connector);
		assertEquals(RIGHT_KEY_COLUMN_RESULT, ((TableJoinSource) source).getRightKeyColumn());

		tableJoinProcessor.parse(ENCLOSURE_3_DEFAULT_RIGHT_LINE, DEFAULT_RIGHT_LINE, connector);
		assertEquals(DEFAULT_RIGHT_LINE_RESULT, ((TableJoinSource) source).getDefaultRightLine());
	}

	@Test
	void testParseTableJoinEmptyConnector() {

		tableJoinProcessor.parse(ENCLOSURE_3_LEFT_TABLE, LEFT_TABLE, connector);
		assertEquals(0, connector.getHardwareMonitors().size());

		connector = new Connector();

		//We need to parse this first so the left table exist in the connector
		snmpTableProcessor.parse(ENCLOSURE_1_TYPE, TYPE_SNMP_TABLE, connector);

		tableJoinProcessor.parse(ENCLOSURE_3_LEFT_TABLE, LEFT_TABLE, connector);

		Optional<HardwareMonitor> hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.ENCLOSURE)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		List<Source> sources = hardwareMonitorOpt.get().getDiscovery().getSources();
		assertEquals(2, sources.size());

		Optional<Source> sourceOpt = sources.stream()
				.filter(src -> 3 == src.getIndex()).findFirst();

		Source source = sourceOpt.get();
		assertEquals(LEFT_TABLE, ((TableJoinSource) source).getLeftTable());
		assertEquals(3, source.getIndex());

		connector = new Connector();

		tableJoinProcessor.parse(ENCLOSURE_3_RIGHT_TABLE, RIGHT_TABLE, connector);
		assertEquals(0, connector.getHardwareMonitors().size());

		connector = new Connector();

		//We need to parse this first so the right table exist in the connector
		snmpTableProcessor.parse(ENCLOSURE_2_TYPE, TYPE_SNMP_TABLE, connector);

		tableJoinProcessor.parse(ENCLOSURE_3_RIGHT_TABLE, RIGHT_TABLE, connector);

		hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.ENCLOSURE)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		sources = hardwareMonitorOpt.get().getDiscovery().getSources();
		assertEquals(2, sources.size());

		sourceOpt = sources.stream()
				.filter(src -> 3 == src.getIndex()).findFirst();

		source = sourceOpt.get();
		assertEquals(RIGHT_TABLE, ((TableJoinSource) source).getRightTable());

		connector = new Connector();
		tableJoinProcessor.parse(ENCLOSURE_3_LEFT_KEY_COLUMN, LEFT_KEY_COLUMN, connector);

		hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.ENCLOSURE)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		sources = hardwareMonitorOpt.get().getDiscovery().getSources();
		assertEquals(1, sources.size());

		sourceOpt = sources.stream()
				.filter(src -> 3 == src.getIndex()).findFirst();

		source = sourceOpt.get();
		assertEquals(LEFT_KEY_COLUMN_RESULT, ((TableJoinSource) source).getLeftKeyColumn());

		connector = new Connector();
		tableJoinProcessor.parse(ENCLOSURE_3_RIGHT_KEY_COLUMN, RIGHT_KEY_COLUMN, connector);

		hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.ENCLOSURE)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		sources = hardwareMonitorOpt.get().getDiscovery().getSources();
		assertEquals(1, sources.size());

		sourceOpt = sources.stream()
				.filter(src -> 3 == src.getIndex()).findFirst();

		source = sourceOpt.get();
		assertEquals(RIGHT_KEY_COLUMN_RESULT, ((TableJoinSource) source).getRightKeyColumn());

		connector = new Connector();
		tableJoinProcessor.parse(ENCLOSURE_3_DEFAULT_RIGHT_LINE, DEFAULT_RIGHT_LINE, connector);

		hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().equals(MonitorType.ENCLOSURE)).findFirst();

		assertTrue(hardwareMonitorOpt.isPresent());

		sources = hardwareMonitorOpt.get().getDiscovery().getSources();
		assertEquals(1, sources.size());

		sourceOpt = sources.stream()
				.filter(src -> 3 == src.getIndex()).findFirst();

		source = sourceOpt.get();
		assertEquals(DEFAULT_RIGHT_LINE_RESULT, ((TableJoinSource) source).getDefaultRightLine());
	}
}

