package com.sentrysoftware.matrix.connector.parser.state.source;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorSourceParserTest {

	private Connector connector;

	private static final String CONNECTOR_SOURCE_PARSER_DISCOVERY_SOURCE_KEY = "enclosure.discovery.source(1)";

	private static final String CONNECTOR_SOURCE_PARSER_DISCOVERY_TYPE_KEY =
			CONNECTOR_SOURCE_PARSER_DISCOVERY_SOURCE_KEY
					+ ".type";

	private static final String CONNECTOR_SOURCE_PARSER_TYPE_VALUE = "SnmpTable";

	@BeforeEach
	void setUp() {

		connector = new Connector();
	}

	@Test
	void testGetConnectorStateProcessor() {

		assertTrue(ConnectorSourceParser.SourceSnmpProperty.SNMP_TABLE.getConnectorStateProcessor() instanceof SnmpTableProcessor);
	}

	@Test
	void testParse() {

		ConnectorSourceParser connectorSourceParser = new ConnectorSourceParser();
		connectorSourceParser.parse(CONNECTOR_SOURCE_PARSER_DISCOVERY_TYPE_KEY, CONNECTOR_SOURCE_PARSER_TYPE_VALUE, connector);

		List<HardwareMonitor> hardwareMonitors = connector.getHardwareMonitors();
		assertNotNull(hardwareMonitors);
		assertEquals(1, hardwareMonitors.size());
		HardwareMonitor hardwareMonitor = hardwareMonitors.get(0);
		assertNotNull(hardwareMonitor);
		Discovery discovery = hardwareMonitor.getDiscovery();
		assertNotNull(discovery);
		List<Source> sources = discovery.getSources();
		assertNotNull(sources);
		assertEquals(1, sources.size());
		Source source = sources.get(0);
		assertTrue(source instanceof SNMPGetTableSource);
		SNMPGetTableSource snmpGetTableSource = (SNMPGetTableSource) source;
		assertEquals(1, snmpGetTableSource.getIndex());
		assertNull(snmpGetTableSource.getComputes());
		assertNotNull(snmpGetTableSource.getSnmpTableSelectColumns());
		assertTrue(snmpGetTableSource.getSnmpTableSelectColumns().isEmpty());
		assertNotNull(snmpGetTableSource.getKey());
		assertEquals(CONNECTOR_SOURCE_PARSER_DISCOVERY_SOURCE_KEY, snmpGetTableSource.getKey());
		assertNull(snmpGetTableSource.getOid());
	}
}