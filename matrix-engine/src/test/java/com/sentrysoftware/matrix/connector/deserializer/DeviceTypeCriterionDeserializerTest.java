package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OsType;
import com.sentrysoftware.matrix.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.DeviceType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class DeviceTypeCriterionDeserializerTest {

	@Test
	/**
	 * Checks input properties for device type detection criteria
	 *
	 * @throws Exception
	 */
	void testDeserializeDeviceType() throws Exception {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector deviceType = deserializer
				.deserialize(new File("src/test/resources/test-files/connector/deviceTypeCriterion.yaml"));

		List<Criterion> expected = new ArrayList<>();
		expected.add(new DeviceType("deviceType", false, Set.of(OsType.values()), Set.of(OsType.values())));

		assertNotNull(deviceType);

		final ConnectorIdentity connectorIdentity = deviceType.getConnectorIdentity();
		assertEquals("deviceTypeCriterion", connectorIdentity.getCompiledFilename());

		assertNotNull(connectorIdentity.getDetection());
		assertEquals(expected, connectorIdentity.getDetection().getCriteria());
	}

	@Test
	/**
	 * Checks that non enum members are rejected
	 *
	 * @throws Exception
	 */
	void testDeserializeOsTypeEnum() throws Exception {

		// Yaml contains invalid OsType so if deserializer does not throw an invalid
		// exception, test will fail.

		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final File connectorFile = new File(
				"src/test/resources/test-files/connector/deviceTypeCriterionOsTypeNonEnum.yaml");
		try {
			deserializer.deserialize(connectorFile);
			Assert.fail("Expected an JsonMappingException to be thrown");
		} catch (JsonMappingException e) {
			String message = String.format("not one of the values accepted for Enum class: %s",
					"[SUNOS, OOB, LINUX, NT, HP, STORAGE, VMS, NETWORK, OSF1, RS6000, SOLARIS]");
			assertTrue(
					e.getMessage().contains(message),
					() -> "Expected exception contains: " + message + ". But got: " + e.getMessage());
		}
	}
}
