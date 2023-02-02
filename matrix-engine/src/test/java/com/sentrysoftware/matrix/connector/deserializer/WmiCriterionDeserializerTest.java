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
				.deserialize(new File("src/test/resources/test-files/connector/detection/criteria/wmi/wmiCriterion.yaml"));

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
	 * Checks that the namespace field gets assigned the proper default value
	 * 
	 * @throws IOException
	 */
	void testWbemDefaultNamespace() throws IOException {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector connector = deserializer
				.deserialize(new File("src/test/resources/test-files/connector/detection/criteria/wmi/wmiCriterionDefaultNamespace.yaml"));

		final List<Criterion> expected = new ArrayList<>();

		final Wmi wmi = Wmi.builder()
				.type("wbem")
				.build();

		expected.add(wmi);
		
		assertNotNull(connector.getConnectorIdentity().getDetection());
		List<Criterion> criteria = connector.getConnectorIdentity().getDetection().getCriteria();
		assertEquals(expected, criteria);
	}

	@Test
	/**
	 * Checks that the query field throws an error when they are null or empty
	 * 
	 * @throws IOException
	 */
	void testWmiNonNull() throws IOException {
		{
			try {
				final ConnectorDeserializer deserializer = new ConnectorDeserializer();
				deserializer.deserialize(new File("src/test/resources/test-files/connector/wmi/wmiCriterionNullQuery.yaml"));
				Assert.fail();
			} catch (IllegalArgumentException e) {
				assertTrue(e.getMessage().contains("Query cannot be null."));
			}
		}

		{
			try {
				final ConnectorDeserializer deserializer = new ConnectorDeserializer();
				deserializer.deserialize(new File("src/test/resources/test-files/connector/wmi/wmiCriterionEmptyQuery.yaml"));
				Assert.fail();
			} catch (IllegalArgumentException e) {
				assertTrue(e.getMessage().contains("Query cannot be empty."));
			}
		}
	}
}
