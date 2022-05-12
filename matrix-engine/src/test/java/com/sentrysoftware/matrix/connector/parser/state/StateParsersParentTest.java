package com.sentrysoftware.matrix.connector.parser.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.parser.state.source.ConnectorSourceProperty;

class StateParsersParentTest {

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
	void testDetect() {

		final StateParsersParent parent = new StateParsersParent(ConnectorSourceProperty.getConnectorProperties());
		assertTrue(parent.detect(CONNECTOR_SOURCE_PARSER_DISCOVERY_TYPE_KEY, CONNECTOR_SOURCE_PARSER_TYPE_VALUE, connector));
	}

	@Test
	void testParse() {

		final StateParsersParent parent = new StateParsersParent(ConnectorSourceProperty.getConnectorProperties());
		parent.parse(CONNECTOR_SOURCE_PARSER_DISCOVERY_TYPE_KEY, CONNECTOR_SOURCE_PARSER_TYPE_VALUE, connector);

		final List<HardwareMonitor> hardwareMonitors = connector.getHardwareMonitors();
		assertNotNull(hardwareMonitors);
		assertEquals(1, hardwareMonitors.size());

		final HardwareMonitor hardwareMonitor = hardwareMonitors.get(0);
		assertNotNull(hardwareMonitor);

		final Discovery discovery = hardwareMonitor.getDiscovery();
		assertNotNull(discovery);

		final List<Source> sources = discovery.getSources();
		assertNotNull(sources);
		assertEquals(1, sources.size());

		final Source source = sources.get(0);
		assertTrue(source instanceof SNMPGetTableSource);

		final SNMPGetTableSource snmpGetTableSource = (SNMPGetTableSource) source;
		assertEquals(1, snmpGetTableSource.getIndex());
		assertNull(snmpGetTableSource.getComputes());
		assertNotNull(snmpGetTableSource.getSnmpTableSelectColumns());
		assertTrue(snmpGetTableSource.getSnmpTableSelectColumns().isEmpty());
		assertNotNull(snmpGetTableSource.getKey());
		assertEquals(CONNECTOR_SOURCE_PARSER_DISCOVERY_SOURCE_KEY, snmpGetTableSource.getKey());
		assertNull(snmpGetTableSource.getOid());
	}

}
