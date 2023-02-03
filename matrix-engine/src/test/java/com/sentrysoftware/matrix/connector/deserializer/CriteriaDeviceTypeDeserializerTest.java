package com.sentrysoftware.matrix.connector.deserializer;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OsType;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.DeviceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class CriteriaDeviceTypeDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/deviceType/";
	}

	@Test
	/**
	 * Checks input properties for device type detection criteria
	 *
	 * @throws Exception
	 */
	void testDeserializeDeviceType() throws Exception {
		
		String testResource = "deviceTypeCriterion";
		final Connector deviceType = getConnector(testResource);

		List<Criterion> expected = new ArrayList<>();
		expected.add(new DeviceType("deviceType", false, Set.of(OsType.values()), Set.of(OsType.values())));

		compareCriterion(testResource, deviceType, expected);
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
		try {
			getConnector("deviceTypeCriterionOsTypeNonEnum");
			Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
		} catch (JsonMappingException e) {
			String message = String.format("'toto' is not a supported OsType. Accepted values are: %s",
					"[ linux, windows, oob, network, storage, vms, tru64, hpux, aix, solaris ]");
			checkMessage(e, message);
		}
	}
}
