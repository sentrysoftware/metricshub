package com.sentrysoftware.matrix.converter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.converter.ConnectorConverter;
import com.sentrysoftware.matrix.converter.PreConnector;

class ConnectorSimplePropertyTest {

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertDisplayName() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode(); // create root node
		String expected = "Dell OpenManage Server Administrator"; // expected value for displayName
		String hdfProperty = "hdf.DisplayName"; // hdf property name
		boolean result = new ConnectorSimpleProperty.DisplayNameProcessor().detect(hdfProperty, expected, connector); // detect the node connector -> displayName
		assertTrue(result);
		new ConnectorSimpleProperty.DisplayNameProcessor().convert(hdfProperty, expected, connector, new PreConnector()); // convert/create YAML node
		JsonNode connectorNode = connector.get("connector"); 
		assertNotNull(connectorNode);
		JsonNode displayNameNode = connectorNode.get("displayName");
		assertEquals(expected, displayNameNode.textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertPlatforms() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "Dell PowerEdge";
		String hdfProperty = "hdf.TypicalPlatform";
		boolean result = new ConnectorSimpleProperty.TypicalPlatformProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.TypicalPlatformProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "platforms";
		JsonNode platformsNode = connectorNode.get(propName);
		assertEquals(expected, platformsNode.textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertReliesOn() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "Dell OpenManage Server Administrator";
		String hdfProperty = "hdf.ReliesOn";
		boolean result = new ConnectorSimpleProperty.ReliesOnProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.ReliesOnProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "reliesOn";
		JsonNode reliesOnNode = connectorNode.get(propName);
		assertEquals(expected, reliesOnNode.textValue()); 
	}


	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertVersion() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "1.0";
		String hdfProperty = "hdf.Version";
		boolean result = new ConnectorSimpleProperty.VersionProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.VersionProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "version";
		JsonNode versionNode = connectorNode.get(propName);
		assertEquals(expected, versionNode.textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertComments() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "MyComments";
		String hdfProperty = "hdf.Comments";
		boolean result = new ConnectorSimpleProperty.CommentsProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.CommentsProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "information";
		JsonNode informationNode = connectorNode.get(propName);
		assertEquals(expected, informationNode.textValue()); 
	}

	///   DETECTION

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertConnectionTypes() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "local";
		String hdfProperty = "hdf.ConnectionTypes";
		boolean result = new ConnectorSimpleProperty.LocalSupportProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.LocalSupportProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "connectionTypes";
		
		final ArrayNode connectionTypes = JsonNodeFactory.instance.arrayNode();
		connectionTypes.add(expected);
		ObjectNode detectionNode =  JsonNodeFactory.instance.objectNode();
		((ObjectNode) detectionNode ).set(propName, connectionTypes );		
		((ObjectNode) connectorNode ).set("detection", detectionNode );
		
		assertEquals(expected, connectorNode.get(propName).textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertConnectionTypes2() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "remote";
		String hdfProperty = "hdf.ConnectionTypes";
		boolean result = new ConnectorSimpleProperty.RemoteSupportProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.RemoteSupportProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "connectionTypes";
		
		final ArrayNode connectionTypes = JsonNodeFactory.instance.arrayNode();
		connectionTypes.add(expected);
		ObjectNode detectionNode =  JsonNodeFactory.instance.objectNode();
		((ObjectNode) detectionNode ).set(propName, connectionTypes );		
		((ObjectNode) connectorNode ).set("detection", detectionNode );
		
		assertEquals(expected, connectorNode.get(propName).textValue()); 
	}


	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertSupersedes() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode(); // create the root node
		String expected = "MS_HW_IpmiTool.hdf,MS_HW_VMwareESX4i.hdf"; // expected string result 
		String hdfProperty = "hdf.Supersedes"; // property name as defined in the hdf file
		boolean result = new ConnectorSimpleProperty.SupersedesProcessor().detect(hdfProperty, expected, connector); // check that the property is correctly detected
		assertTrue(result);
		new ConnectorSimpleProperty.SupersedesProcessor().convert(hdfProperty, expected, connector, new PreConnector()); // convert the property to a yaml node
		JsonNode connectorNode = connector.get("connector"); // create the connector yaml node
		assertNotNull(connectorNode);

		String propName = "supersedes"; // connector -> detection -> supersedes
		
		final ArrayNode supersedes = JsonNodeFactory.instance.arrayNode(); // supersedes node
		supersedes.add(expected);
		ObjectNode detectionNode =  JsonNodeFactory.instance.objectNode(); // detection node
		((ObjectNode) detectionNode ).set(propName, supersedes ); // detection -> supersedes		
		((ObjectNode) connectorNode ).set("detection", detectionNode ); // connector -> detection
		
		assertEquals(expected, connectorNode.get(propName).textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertAppliesToOS() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode(); // root node
		String expected = "NT,Linux";  
		String hdfProperty = "hdf.AppliesToOS"; // property name as defined in the hdf file
		boolean result = new ConnectorSimpleProperty.AppliesToOsProcessor().detect(hdfProperty, expected, connector); // check that the property is correctly detected
		assertTrue(result);
		new ConnectorSimpleProperty.AppliesToOsProcessor().convert(hdfProperty, expected, connector, new PreConnector()); // convert the property to a yaml node
		JsonNode connectorNode = connector.get("connector"); // create the connector yaml node
		assertNotNull(connectorNode);

		String propName = "appliesTo";
		
		final ArrayNode appliesToYamlNode = JsonNodeFactory.instance.arrayNode(); // prop node
		appliesToYamlNode.add(expected);
		ObjectNode detectionNode =  JsonNodeFactory.instance.objectNode(); // detection node
		((ObjectNode) detectionNode ).set(propName, appliesToYamlNode ); // detection -> prop		
		((ObjectNode) connectorNode ).set("detection", detectionNode ); // connector -> detection
		
		assertEquals(expected, connectorNode.get(propName).textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertDisableAutoDetection() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode(); 
		String expected = "false";  
		String hdfProperty = "hdf.noautodetection"; 
		boolean result = new ConnectorSimpleProperty.NoAutoDetectionProcessor().detect(hdfProperty, expected, connector); 
		assertTrue(result);
		new ConnectorSimpleProperty.NoAutoDetectionProcessor().convert(hdfProperty, expected, connector, new PreConnector()); 
		JsonNode connectorNode = connector.get("connector"); 
		assertNotNull(connectorNode);

		String propName = "disableAutoDetection";	
		BooleanNode autoDetectYamlNode = JsonNodeFactory.instance.booleanNode(false); 
		ObjectNode detectionNode =  JsonNodeFactory.instance.objectNode(); 
		((ObjectNode) detectionNode ).set(propName, autoDetectYamlNode ); 		
		((ObjectNode) connectorNode ).set("detection", detectionNode ); 
		
		assertEquals(expected, connectorNode.get(propName).textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertOnLastResort() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode(); 
		String expected = "false";  
		String hdfProperty = "hdf.onLastResort"; 
		boolean result = new ConnectorSimpleProperty.OnLastResortProcessor().detect(hdfProperty, expected, connector); 
		assertTrue(result);
		new ConnectorSimpleProperty.OnLastResortProcessor().convert(hdfProperty, expected, connector, new PreConnector()); 
		JsonNode connectorNode = connector.get("connector"); 
		assertNotNull(connectorNode);

		String propName = "onLastResort";	
		final JsonNode onLastResortYamlNode = JsonNodeFactory.instance.arrayNode(); 
		ObjectNode detectionNode =  JsonNodeFactory.instance.objectNode(); 
		((ObjectNode) detectionNode ).set(propName, onLastResortYamlNode ); 		
		((ObjectNode) connectorNode ).set("detection", detectionNode ); 
		
		assertEquals(expected, connectorNode.get(propName).textValue()); 
	}
	
	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void test() throws IOException{
		String input = """
				hdf.DisplayName="Dell OpenManage Server Administrator"
				hdf.TypicalPlatform="Dell PowerEdge"
				hdf.ReliesOn="Dell OpenManage Server Administrator"
				hdf.Version="1.0"
				hdf.Comments="This connector provides hardware monitoring through the Dell OpenManage Server Administrator SNMP agent which supports almost all Dell PowerEdge servers."
				hdf.RemoteSupport="true"
				hdf.LocalSupport="true"
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
                 version:  1.0
                 projectVersion: 1.0
                 information: This connector provides hardware monitoring through the Dell OpenManage Server Administrator SNMP agent which supports almost all Dell PowerEdge servers.
  
                 detection:
                    connectionTypes: [remote, local]
                    disableAutoDetection: false
                    appliesTo: [ LINUX, NT ]
                    supersedes: [IpmiTool, VMWareESX41, VMwareESXi, VMwareESXiDisksIPMI, VMwareESXiDisksStorage]
				""";
		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}


}
