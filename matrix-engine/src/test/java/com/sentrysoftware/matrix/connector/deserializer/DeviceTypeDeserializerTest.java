package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OsType;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.DeviceType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class DeviceTypeDeserializerTest {

	@Test
	/**
	 * Checks input properties for device type detection criteria
	 *
	 * @throws IOException
	 */
	void testDeserializeDeviceType() throws IOException {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector deviceType = deserializer
				.deserialize(new File("src/test/resources/test-files/connector/deviceTypeCriterion.yaml"));

		List<Criterion> expected = new ArrayList<>();
		Set<OsType> keep = new HashSet<>();
		Set<OsType> exclude = new HashSet<>();

		for(OsType type : OsType.values()) {
			keep.add(type);
			exclude.add(type);
		}
		
		expected.add(new DeviceType("deviceType", false, keep, exclude));

		assertNotNull(deviceType);
		assertEquals("deviceTypeCriterion", deviceType.getConnectorIdentity().getCompiledFilename());

		assertNotNull(deviceType.getConnectorIdentity().getDetection());
		assertEquals(expected, deviceType.getConnectorIdentity().getDetection().getCriteria());
	}

	@Test
	/**
	 * Checks that non enum members are rejected
	 *
	 * @throws IOException
	 */
	void testDeviceTypeNonEnum() throws IOException {

		// Yaml contains invalid OsType so if deserializer does not throw an invalid exception, test will fail.
		try {
			final ConnectorDeserializer deserializer = new ConnectorDeserializer();
			deserializer.deserialize(new File("src/test/resources/test-files/connector/deviceTypeCriterionNonEnum.yaml"));
			Assert.fail();
		} catch (IllegalArgumentException e) {
			assertEquals(String.format("OsType must be one of [ {} ]", OsType.values().toString()), e.getMessage());
		}
	}
}
