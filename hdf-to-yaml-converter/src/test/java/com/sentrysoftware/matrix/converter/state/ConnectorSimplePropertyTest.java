package com.sentrysoftware.matrix.converter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.converter.ConnectorConverter;
import com.sentrysoftware.matrix.converter.ConnectorLibraryConverter;
import com.sentrysoftware.matrix.converter.PreConnector;

class ConnectorSimplePropertyTest {

	private static final String DETECTION = "detection";

	@Test
	void detectAndConvertDisplayName() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode(); // create root node : connector
		String expected = "Dell OpenManage Server Administrator"; // expected value for displayName
		String hdfProperty = "hdf.DisplayName"; // hdf property name
		// detect the node connector -> displayName
		boolean result = new ConnectorSimpleProperty.DisplayNameProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.DisplayNameProcessor().convert(hdfProperty, expected, connector,
				new PreConnector()); // convert/create YAML node
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		JsonNode displayNameNode = connectorNode.get("displayName");
		assertEquals(expected, displayNameNode.textValue());
	}

	@Test
	void detectAndConvertPlatforms() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "Dell PowerEdge";
		String hdfProperty = "hdf.TypicalPlatform";
		boolean result = new ConnectorSimpleProperty.TypicalPlatformProcessor().detect(hdfProperty, expected,
				connector);
		assertTrue(result);
		new ConnectorSimpleProperty.TypicalPlatformProcessor().convert(hdfProperty, expected, connector,
				new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		JsonNode platformsNode = connectorNode.get("platforms");
		assertEquals(expected, platformsNode.textValue());
	}

	@Test
	void detectAndConvertReliesOn() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "Dell OpenManage Server Administrator";
		String hdfProperty = "hdf.ReliesOn";
		boolean result = new ConnectorSimpleProperty.ReliesOnProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.ReliesOnProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		JsonNode reliesOnNode = connectorNode.get("reliesOn");
		assertEquals(expected, reliesOnNode.textValue());
	}

	@Test
	void detectAndConvertVersion() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "1.0";
		String hdfProperty = "hdf.Version";
		boolean result = new ConnectorSimpleProperty.VersionProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.VersionProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		JsonNode versionNode = connectorNode.get("version");
		assertEquals(expected, versionNode.textValue());
	}

	@Test
	void detectAndConvertComments() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "MyComments";
		String hdfProperty = "hdf.Comments";
		boolean result = new ConnectorSimpleProperty.CommentsProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.CommentsProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		JsonNode informationNode = connectorNode.get("information");
		assertEquals(expected, informationNode.textValue());
	}

	/// DETECTION

	@Test
	void detectAndConvertConnectionTypes() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "true";
		String hdfProperty = "hdf.LocalSupport";
		boolean result = new ConnectorSimpleProperty.LocalSupportProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.LocalSupportProcessor().convert(hdfProperty, expected, connector,
				new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);

		final ArrayNode connectionTypes = JsonNodeFactory.instance.arrayNode();
		connectionTypes.add("local");

		assertEquals(connectionTypes, connectorNode.get(DETECTION).get("connectionTypes"));
	}

	@Test
	void detectAndConvertConnectionTypes2() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "true";
		String hdfProperty = "hdf.RemoteSupport";
		boolean result = new ConnectorSimpleProperty.RemoteSupportProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.RemoteSupportProcessor().convert(hdfProperty, expected, connector,
				new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);

		final ArrayNode connectionTypes = JsonNodeFactory.instance.arrayNode();
		connectionTypes.add("remote");

		assertEquals(connectionTypes, connectorNode.get(DETECTION).get("connectionTypes"));
	}

	@Test
	void detectAndConvertConnectionTypes3() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String hdfPropertyRemote = "hdf.RemoteSupport";
		String hdfPropertyLocal = "hdf.LocalSupport";
		// detect
		assertTrue(new ConnectorSimpleProperty.RemoteSupportProcessor().detect(hdfPropertyRemote, "true", connector));
		assertTrue(new ConnectorSimpleProperty.LocalSupportProcessor().detect(hdfPropertyLocal, "true", connector));
		// convert
		new ConnectorSimpleProperty.RemoteSupportProcessor().convert(hdfPropertyRemote, "true", connector,
				new PreConnector());
		new ConnectorSimpleProperty.LocalSupportProcessor().convert(hdfPropertyLocal, "true", connector,
				new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);

		final ArrayNode connectionTypes = JsonNodeFactory.instance.arrayNode();
		connectionTypes.add("remote");
		connectionTypes.add("local");

		assertEquals(connectionTypes, connectorNode.get(DETECTION).get("connectionTypes"));
	}

	@Test
	void detectAndConvertConnectionTypes4() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String hdfPropertyRemote = "hdf.RemoteSupport";
		String hdfPropertyLocal = "hdf.LocalSupport";
		// detect
		assertTrue(new ConnectorSimpleProperty.RemoteSupportProcessor().detect(hdfPropertyRemote, "true", connector));
		assertTrue(new ConnectorSimpleProperty.LocalSupportProcessor().detect(hdfPropertyLocal, "false", connector));
		// convert
		new ConnectorSimpleProperty.RemoteSupportProcessor().convert(hdfPropertyRemote, "true", connector,
				new PreConnector());
		new ConnectorSimpleProperty.LocalSupportProcessor().convert(hdfPropertyLocal, "false", connector,
				new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);

		final ArrayNode connectionTypes = JsonNodeFactory.instance.arrayNode();
		connectionTypes.add("remote");

		assertEquals(connectionTypes, connectorNode.get(DETECTION).get("connectionTypes"));
	}

	@Test
	void detectAndConvertSupersedes() {
		final ObjectNode connector = JsonNodeFactory.instance.objectNode(); // create the root node
		String input = "MS_HW_IpmiTool.hdf,MS_HW_VMwareESX4i.hdf"; // value in the hdf file
		String hdfProperty = "hdf.Supersedes"; // property name as defined in the hdf file
		// check that the property is correctly detected
		boolean result = new ConnectorSimpleProperty.SupersedesProcessor().detect(hdfProperty, input, connector); 
		assertTrue(result);
		// convert the property to a JSON node 
		new ConnectorSimpleProperty.SupersedesProcessor().convert(hdfProperty, input, connector, new PreConnector()); 
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);

		final ArrayNode expected = JsonNodeFactory.instance.arrayNode();
		Stream.of(input.split(",")).map(ConnectorLibraryConverter::getConnectorFilenameNoExtension)
				.forEach(expected::add);

		final JsonNode detection = connectorNode.get(DETECTION);
		assertNotNull(detection);
		JsonNode actual = detection.get("supersedes");

		assertEquals(expected, actual);
	}

	@Test
	void detectAndConvertAppliesToOS() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode(); // root node
		String expected = "NT,Linux";
		String hdfProperty = "hdf.AppliesToOS"; // property name as defined in the hdf file
		boolean result = new ConnectorSimpleProperty.AppliesToOsProcessor().detect(hdfProperty, expected, connector); 
		assertTrue(result);
		new ConnectorSimpleProperty.AppliesToOsProcessor().convert(hdfProperty, expected, connector,
				new PreConnector()); // convert the property to a yaml node
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);

		final ArrayNode appliesToYamlNode = JsonNodeFactory.instance.arrayNode(); // prop node
		appliesToYamlNode.add("NT");
		appliesToYamlNode.add("Linux");

		assertEquals(appliesToYamlNode, connectorNode.get(DETECTION).get("appliesTo"));
	}

	@Test
	void detectAndConvertDisableAutoDetection() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "false";
		String hdfProperty = "hdf.noautodetection";
		boolean result = new ConnectorSimpleProperty.NoAutoDetectionProcessor().detect(hdfProperty, expected,
				connector);
		assertTrue(result);
		new ConnectorSimpleProperty.NoAutoDetectionProcessor().convert(hdfProperty, expected, connector,
				new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);

		BooleanNode expectedNodeValue = JsonNodeFactory.instance.booleanNode(false);

		assertEquals(expectedNodeValue.booleanValue(),
				connectorNode.get(DETECTION).get("disableAutoDetection").booleanValue());
	}

	@Test
	void detectAndConvertOnLastResort() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "false";
		String hdfProperty = "hdf.onLastResort";
		boolean result = new ConnectorSimpleProperty.OnLastResortProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.OnLastResortProcessor().convert(hdfProperty, expected, connector,
				new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		assertEquals(expected, connectorNode.get(DETECTION).get("onLastResort").textValue());
	}

	@Test
	void test() throws IOException {
		String input = """
					hdf.DisplayName="Dell OpenManage Server Administrator"
					hdf.TypicalPlatform="Dell PowerEdge"
					hdf.ReliesOn="Dell OpenManage Server Administrator"
					hdf.Version="1.0"
					hdf.Comments="This connector provides hardware monitoring through the Dell OpenManage Server Administrator SNMP agent which supports almost all Dell PowerEdge servers."
					hdf.RemoteSupport="true"
					hdf.LocalSupport="true"
					hdf.NoAutoDetection="true"
					hdf.AppliesToOS="NT,Linux"
					hdf.Supersedes="MS_HW_IpmiTool.hdf,MS_HW_VMwareESX4i.hdf,MS_HW_VMwareESXi.hdf,MS_HW_VMwareESXiDisksIPMI.hdf,MS_HW_VMwareESXiDisksStorage.hdf"
				""";

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();
		String yaml = """
				           connector:
				             displayName: Dell OpenManage Server Administrator
				             platforms: Dell PowerEdge
				             reliesOn: Dell OpenManage Server Administrator
				             version: "1.0"
				             information: This connector provides hardware monitoring through the Dell OpenManage Server Administrator SNMP agent which supports almost all Dell PowerEdge servers.

				             detection:
				                connectionTypes: [remote, local]
				                disableAutoDetection: true
				                appliesTo: [ NT,Linux ]
				                supersedes: [IpmiTool, VMwareESX4i, VMwareESXi, VMwareESXiDisksIPMI, VMwareESXiDisksStorage]
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}

}
