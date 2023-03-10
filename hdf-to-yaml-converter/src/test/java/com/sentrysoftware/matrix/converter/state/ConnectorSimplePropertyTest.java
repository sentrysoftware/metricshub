package com.sentrysoftware.matrix.converter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;

class ConnectorSimplePropertyTest {

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertDispalyName() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "Dell OpenManage Server Administrator";
		String hdfProperty = "hdf.DisplayName";
		boolean result = new ConnectorSimpleProperty.DisplayNameProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.DisplayNameProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		JsonNode displayNameNode = connectorNode.get("displayName");
		assertEquals(expected, displayNameNode.textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertTypicalPlatform() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "Dell PowerEdge";
		String hdfProperty = "hdf.TypicalPlatform";
		boolean result = new ConnectorSimpleProperty.DisplayNameProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.DisplayNameProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "typicalPlatform";
		JsonNode displayNameNode = connectorNode.get(propName);
		assertEquals(expected, displayNameNode.textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertReliesOn() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "Dell OpenManage Server Administrator";
		String hdfProperty = "hdf.ReliesOn";
		boolean result = new ConnectorSimpleProperty.DisplayNameProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.DisplayNameProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "reliesOn";
		JsonNode displayNameNode = connectorNode.get(propName);
		assertEquals(expected, displayNameNode.textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertVersion() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "1.0";
		String hdfProperty = "hdf.Version";
		boolean result = new ConnectorSimpleProperty.DisplayNameProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.DisplayNameProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "version";
		JsonNode displayNameNode = connectorNode.get(propName);
		assertEquals(expected, displayNameNode.textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertComments() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "MyComments";
		String hdfProperty = "hdf.Comments";
		boolean result = new ConnectorSimpleProperty.DisplayNameProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.DisplayNameProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "comments";
		JsonNode displayNameNode = connectorNode.get(propName);
		assertEquals(expected, displayNameNode.textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertRemoteSupport() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "true";
		String hdfProperty = "hdf.RemoteSupport";
		boolean result = new ConnectorSimpleProperty.DisplayNameProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.DisplayNameProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "remoteSupport";
		JsonNode displayNameNode = connectorNode.get(propName);
		assertEquals(expected, displayNameNode.textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertAppliesToOS() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "NT,Linux";
		String hdfProperty = "hdf.AppliesToOS";
		boolean result = new ConnectorSimpleProperty.DisplayNameProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.DisplayNameProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "appliesToOs";
		JsonNode displayNameNode = connectorNode.get(propName);
		assertEquals(expected, displayNameNode.textValue()); 
	}

	@Test
	@Disabled("Until ConnectorSimpleProperty is up!")
	void detectAndConvertSupersedes() {
		ObjectNode connector = JsonNodeFactory.instance.objectNode();
		String expected = "MS_HW_IpmiTool.hdf,MS_HW_VMwareESX4i.hdf";
		String hdfProperty = "hdf.Supersedes";
		boolean result = new ConnectorSimpleProperty.DisplayNameProcessor().detect(hdfProperty, expected, connector);
		assertTrue(result);
		new ConnectorSimpleProperty.DisplayNameProcessor().convert(hdfProperty, expected, connector, new PreConnector());
		JsonNode connectorNode = connector.get("connector");
		assertNotNull(connectorNode);
		String propName = "supersedes";
		JsonNode displayNameNode = connectorNode.get(propName);
		assertEquals(expected, displayNameNode.textValue()); 
	}
	


}
