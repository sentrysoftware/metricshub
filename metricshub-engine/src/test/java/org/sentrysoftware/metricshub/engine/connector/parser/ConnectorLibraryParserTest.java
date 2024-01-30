package org.sentrysoftware.metricshub.engine.connector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sentrysoftware.metricshub.engine.constants.Constants.AAC_CONNECTOR_ID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.DISCOVERY_MAPPING_MODEL;
import static org.sentrysoftware.metricshub.engine.constants.Constants.DISCOVERY_MAPPING_NAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.DISCOVERY_MAPPING_VENDOR;
import static org.sentrysoftware.metricshub.engine.constants.Constants.DISK_CONTROLLER;
import static org.sentrysoftware.metricshub.engine.constants.Constants.DISK_CONTROLLER_AWK_COMMAND;
import static org.sentrysoftware.metricshub.engine.constants.Constants.DOLLAR_3;
import static org.sentrysoftware.metricshub.engine.constants.Constants.DOLLAR_4;
import static org.sentrysoftware.metricshub.engine.constants.Constants.DUPLICATE_COLUMN;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ENCLOSURE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HW_PARENT_TYPE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PHYSICAL_DISK;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PHYSICAL_DISK_AWK_COMMAND;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_CRITERION_TYPE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_TABLE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SOURCE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.TRANSLATE;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.identity.ConnectionType;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.StandardMonitorJob;

class ConnectorLibraryParserTest {

	/**
	 * This tests the method visitFile overridden in {@link ConnectorLibraryParser}
	 *
	 * @throws IOException if the file does not exist
	 */
	@Test
	void testVisitFile() throws IOException {
		final Path yamlTestPath = Paths.get(
			"src",
			"test",
			"resources",
			"test-files",
			"connector",
			"connectorLibraryParser"
		);
		final Map<String, Connector> connectors = new ConnectorLibraryParser()
			.parseConnectorsFromAllYamlFiles(yamlTestPath);

		//Check connector identity retrieval
		assertTrue(
			connectors
				.get(AAC_CONNECTOR_ID)
				.getConnectorIdentity()
				.getDetection()
				.getConnectionTypes()
				.contains(ConnectionType.LOCAL)
		);
		assertTrue(
			connectors
				.get(AAC_CONNECTOR_ID)
				.getConnectorIdentity()
				.getDetection()
				.getConnectionTypes()
				.contains(ConnectionType.REMOTE)
		);
		assertTrue(
			connectors.get(AAC_CONNECTOR_ID).getConnectorIdentity().getDetection().getAppliesTo().contains(DeviceKind.LINUX)
		);
		assertTrue(
			connectors.get(AAC_CONNECTOR_ID).getConnectorIdentity().getDetection().getAppliesTo().contains(DeviceKind.WINDOWS)
		);
		assertEquals(
			SNMP_CRITERION_TYPE,
			connectors.get(AAC_CONNECTOR_ID).getConnectorIdentity().getDetection().getCriteria().get(0).getType()
		);

		//Check detected monitors number
		assertEquals(3, connectors.get(AAC_CONNECTOR_ID).getMonitors().size());

		//Retrieve the disk controller monitor
		StandardMonitorJob monitorJob = (StandardMonitorJob) (connectors
				.get(AAC_CONNECTOR_ID)
				.getMonitors()
				.get(DISK_CONTROLLER));

		//Check disk controller discovery sources
		assertEquals(1, monitorJob.getDiscovery().getSources().size());
		assertEquals(SNMP_TABLE, monitorJob.getDiscovery().getSources().get(SOURCE).getType());

		//Check disk controller discovery mapping
		assertEquals(ENCLOSURE, monitorJob.getDiscovery().getMapping().getAttributes().get(HW_PARENT_TYPE));
		assertEquals(
			DISK_CONTROLLER_AWK_COMMAND,
			monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_NAME)
		);
		assertEquals(DOLLAR_3, monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_MODEL));

		//Retrieve the physical disk monitor
		monitorJob = (StandardMonitorJob) (connectors.get(AAC_CONNECTOR_ID).getMonitors().get(PHYSICAL_DISK));

		//Check physical disk discovery sources
		assertEquals(1, monitorJob.getDiscovery().getSources().size());
		assertEquals(SNMP_TABLE, monitorJob.getDiscovery().getSources().get(SOURCE).getType());

		//Check physical disk discovery mapping
		assertEquals(DISK_CONTROLLER, monitorJob.getDiscovery().getMapping().getAttributes().get(HW_PARENT_TYPE));
		assertEquals(
			PHYSICAL_DISK_AWK_COMMAND,
			monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_NAME)
		);
		assertEquals(DOLLAR_4, monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_VENDOR));

		//Check physical disk collection
		assertEquals(1, monitorJob.getCollect().getSources().size());
		assertEquals(4, monitorJob.getCollect().getSources().get(SOURCE).getComputes().size());
		assertEquals(DUPLICATE_COLUMN, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(0).getType());
		assertEquals(TRANSLATE, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(1).getType());
		assertEquals(TRANSLATE, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(2).getType());
		assertEquals(TRANSLATE, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(3).getType());
	}

	@Test
	void testVisitZipFile() throws IOException {
		final Path yamlTestPath = Paths.get(
			"src",
			"test",
			"resources",
			"test-files",
			"connector",
			"zippedConnector",
			"connectors"
		);
		final Map<String, Connector> connectors = new ConnectorLibraryParser()
			.parseConnectorsFromAllYamlFiles(yamlTestPath);

		//Check connector identity retrieval
		assertTrue(
			connectors
				.get(AAC_CONNECTOR_ID)
				.getConnectorIdentity()
				.getDetection()
				.getConnectionTypes()
				.contains(ConnectionType.LOCAL)
		);
		assertTrue(
			connectors
				.get(AAC_CONNECTOR_ID)
				.getConnectorIdentity()
				.getDetection()
				.getConnectionTypes()
				.contains(ConnectionType.REMOTE)
		);
		assertTrue(
			connectors.get(AAC_CONNECTOR_ID).getConnectorIdentity().getDetection().getAppliesTo().contains(DeviceKind.LINUX)
		);
		assertTrue(
			connectors.get(AAC_CONNECTOR_ID).getConnectorIdentity().getDetection().getAppliesTo().contains(DeviceKind.WINDOWS)
		);
		assertEquals(
			SNMP_CRITERION_TYPE,
			connectors.get(AAC_CONNECTOR_ID).getConnectorIdentity().getDetection().getCriteria().get(0).getType()
		);

		//Check detected monitors number
		assertEquals(3, connectors.get(AAC_CONNECTOR_ID).getMonitors().size());

		//Retrieve the disk controller monitor
		StandardMonitorJob monitorJob = (StandardMonitorJob) (connectors
				.get(AAC_CONNECTOR_ID)
				.getMonitors()
				.get(DISK_CONTROLLER));

		//Check disk controller discovery sources
		assertEquals(1, monitorJob.getDiscovery().getSources().size());
		assertEquals(SNMP_TABLE, monitorJob.getDiscovery().getSources().get(SOURCE).getType());

		//Check disk controller discovery mapping
		assertEquals(ENCLOSURE, monitorJob.getDiscovery().getMapping().getAttributes().get(HW_PARENT_TYPE));
		assertEquals(
			DISK_CONTROLLER_AWK_COMMAND,
			monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_NAME)
		);
		assertEquals(DOLLAR_3, monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_MODEL));

		//Retrieve the physical disk monitor
		monitorJob = (StandardMonitorJob) (connectors.get(AAC_CONNECTOR_ID).getMonitors().get(PHYSICAL_DISK));

		//Check physical disk discovery sources
		assertEquals(1, monitorJob.getDiscovery().getSources().size());
		assertEquals(SNMP_TABLE, monitorJob.getDiscovery().getSources().get(SOURCE).getType());

		//Check physical disk discovery mapping
		assertEquals(DISK_CONTROLLER, monitorJob.getDiscovery().getMapping().getAttributes().get(HW_PARENT_TYPE));
		assertEquals(
			PHYSICAL_DISK_AWK_COMMAND,
			monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_NAME)
		);
		assertEquals(DOLLAR_4, monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_VENDOR));

		//Check physical disk collection
		assertEquals(1, monitorJob.getCollect().getSources().size());
		assertEquals(4, monitorJob.getCollect().getSources().get(SOURCE).getComputes().size());
		assertEquals(DUPLICATE_COLUMN, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(0).getType());
		assertEquals(TRANSLATE, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(1).getType());
		assertEquals(TRANSLATE, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(2).getType());
		assertEquals(TRANSLATE, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(3).getType());
	}
}
