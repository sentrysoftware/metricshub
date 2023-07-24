package com.sentrysoftware.matrix.connector.parser;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.identity.ConnectionType;
import com.sentrysoftware.matrix.connector.model.monitor.StandardMonitorJob;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConnectorLibraryParserTest {
	public static final String ENCLOSURE = "enclosure";
	public static final String SNMP_TABLE = "snmpTable";
	public static final String DISCOVERY_MAPPING_NAME = "name";
	public static final String DISCOVERY_MAPPING_VENDOR = "vendor";
	public static final String DISCOVERY_MAPPING_VENDOR_VALUE = "$4";
	public static final String DISCOVERY_MAPPING_MODEL = "model";
	public static final String DISCOVERY_MAPPING_MODEL_VALUE = "$3";
	public static final String PHYSICAL_DISK = "physical_disk";
	public static final String DUPLICATE_COLUMN = "duplicateColumn";
	public static final String TRANSLATE = "translate";
	public static String YAML_CONNECTOR_TEST_FILE_NAME = "AAC.yaml";
	public static String SNMP_CRITERION_TYPE = "snmpGetNext";
	public static String DISK_CONTROLLER = "disk_controller";
	public static String HW_PARENT_TYPE = "hw.parent.type";
	public static String DISK_CONTROLLER_AWK_COMMAND = "${awk::sprintf(\"Disk Controller: %s (%s)\", $2, $3)}";
	public static String PHYSICAL_DISK_AWK_COMMAND = "${awk::sprintf(\"%s (%s - %s)\", $1, $4, bytes2HumanFormatBase10($6))}";
	public static String SOURCE = "source(1)";

	/**
	 * This tests the method visitFile overridden in {@link ConnectorLibraryParser}
	 *
	 * @throws IOException if the file does not exist
	 */
	@Test
	public void testVisitFile() throws IOException {
		final Path yamlTestPath = Paths.get("src", "test", "resources", "test-files", "connector", "connectorLibraryParser");
		final Map<String, Connector> connectors = new ConnectorLibraryParser().parseConnectorsFromAllYamlFiles(yamlTestPath);

		//Check connector identity retrieval
		assertTrue(connectors.get(YAML_CONNECTOR_TEST_FILE_NAME).getConnectorIdentity().getDetection().getConnectionTypes().contains(ConnectionType.LOCAL));
		assertTrue(connectors.get(YAML_CONNECTOR_TEST_FILE_NAME).getConnectorIdentity().getDetection().getConnectionTypes().contains(ConnectionType.REMOTE));
		assertTrue(connectors.get(YAML_CONNECTOR_TEST_FILE_NAME).getConnectorIdentity().getDetection().getAppliesTo().contains(DeviceKind.LINUX));
		assertTrue(connectors.get(YAML_CONNECTOR_TEST_FILE_NAME).getConnectorIdentity().getDetection().getAppliesTo().contains(DeviceKind.WINDOWS));
		assertEquals(SNMP_CRITERION_TYPE, connectors.get(YAML_CONNECTOR_TEST_FILE_NAME).getConnectorIdentity().getDetection().getCriteria().get(0).getType());

		//Check detected monitors number
		assertEquals(3, connectors.get(YAML_CONNECTOR_TEST_FILE_NAME).getMonitors().size());

		//Retrieve the disk controller monitor
		StandardMonitorJob monitorJob = (StandardMonitorJob) (connectors.get(YAML_CONNECTOR_TEST_FILE_NAME).getMonitors()
				.get(DISK_CONTROLLER));

		//Check disk controller discovery sources
		assertEquals(1, monitorJob.getDiscovery().getSources().size());
		assertEquals(SNMP_TABLE, monitorJob.getDiscovery().getSources().get(SOURCE).getType());

		//Check disk controller discovery mapping
		assertEquals(ENCLOSURE, monitorJob.getDiscovery().getMapping().getAttributes().get(HW_PARENT_TYPE));
		assertEquals(DISK_CONTROLLER_AWK_COMMAND, monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_NAME));
		assertEquals(DISCOVERY_MAPPING_MODEL_VALUE, monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_MODEL));

		//Retrieve the physical disk monitor
		monitorJob = (StandardMonitorJob) (connectors.get(YAML_CONNECTOR_TEST_FILE_NAME).getMonitors()
				.get(PHYSICAL_DISK));

		//Check physical disk discovery sources
		assertEquals(1, monitorJob.getDiscovery().getSources().size());
		assertEquals(SNMP_TABLE, monitorJob.getDiscovery().getSources().get(SOURCE).getType());

		//Check physical disk discovery mapping
		assertEquals(DISK_CONTROLLER, monitorJob.getDiscovery().getMapping().getAttributes().get(HW_PARENT_TYPE));
		assertEquals(PHYSICAL_DISK_AWK_COMMAND, monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_NAME));
		assertEquals(DISCOVERY_MAPPING_VENDOR_VALUE, monitorJob.getDiscovery().getMapping().getAttributes().get(DISCOVERY_MAPPING_VENDOR));

		//Check physical disk collection
		assertEquals(1, monitorJob.getCollect().getSources().size());
		assertEquals(4, monitorJob.getCollect().getSources().get(SOURCE).getComputes().size());
		assertEquals(DUPLICATE_COLUMN, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(0).getType());
		assertEquals(TRANSLATE, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(1).getType());
		assertEquals(TRANSLATE, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(2).getType());
		assertEquals(TRANSLATE, monitorJob.getCollect().getSources().get(SOURCE).getComputes().get(3).getType());
	}
}
