package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Wmi;

class WmiCriterionDeserializerTest {

	@Test
	/**
	 * Checks that the criteria type is wmi and that the attributes match
	 *
	 * @throws IOException
	 */
	void testDeserializeWmiCriterion() throws IOException {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector connector = deserializer
				.deserialize(new File("src/test/resources/test-files/connector/wmiCriterion.yaml"));

		final List<Criterion> expected = new ArrayList<>();

		final Wmi wmi = Wmi.builder()
				.type("wmi")
				.query("testQuery")
				.namespace("testNamespace")
				.expectedResult("testExpectedResult")
				.errorMessage("testErrorMessage")
				.forceSerialization(true)
				.build();

		expected.add(wmi);

		assertNotNull(connector);
		assertEquals("wmiCriterion", connector.getConnectorIdentity().getCompiledFilename());

		assertNotNull(connector.getConnectorIdentity().getDetection());
		List<Criterion> criteria = connector.getConnectorIdentity().getDetection().getCriteria();
		assertEquals(expected, criteria);
	}

	@Test
	/**
	 * Checks that fields that cannot be null throw an error when they are null
	 * 
	 * @throws IOException
	 */
	void testWmiNonNull() throws IOException {
		try {
			final ConnectorDeserializer deserializer = new ConnectorDeserializer();
			deserializer.deserialize(new File("src/test/resources/test-files/connector/wmiCriterionNonNull.yaml"));
			Assert.fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("cannot be null"));
		}
	}
}
