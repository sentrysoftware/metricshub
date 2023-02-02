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
import com.sentrysoftware.matrix.connector.model.identity.criterion.Wbem;

class WbemCriterionDeserializerTest {

	@Test
	/**
	 * Checks that the criteria type is wbem and that the attributes match the yaml input
	 *
	 * @throws IOException
	 */
	void testDeserializeWbemCriterion() throws IOException {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector connector = deserializer
				.deserialize(new File("src/test/resources/test-files/connector/detection/criteria/wbem/wbemCriterion.yaml"));

		final List<Criterion> expected = new ArrayList<>();

		final Wbem wbem = Wbem.builder()
				.type("wbem")
				.query("testQuery")
				.namespace("testNamespace")
				.expectedResult("testExpectedResult")
				.errorMessage("testErrorMessage")
				.forceSerialization(true)
				.build();

		expected.add(wbem);

		assertNotNull(connector);
		assertEquals("wbemCriterion", connector.getConnectorIdentity().getCompiledFilename());

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
				.deserialize(new File("src/test/resources/test-files/connector/detection/criteria/wbem/wbemCriterionDefaultNamespace.yaml"));

		final List<Criterion> expected = new ArrayList<>();

		final Wbem wbem = Wbem.builder()
				.type("wbem")
				.build();

		expected.add(wbem);
		
		assertNotNull(connector.getConnectorIdentity().getDetection());
		List<Criterion> criteria = connector.getConnectorIdentity().getDetection().getCriteria();
		assertEquals(expected, criteria);
	}

	@Test
	/**
	 * Checks that the query field cannot be null or empty throw an error when they are null
	 * 
	 * @throws IOException
	 */
	void testWbemNonNull() throws IOException {
		{
			try {
				final ConnectorDeserializer deserializer = new ConnectorDeserializer();
				deserializer.deserialize(new File("src/test/resources/test-files/connector/detection/criteria/wbem/wbemCriterionNullQuery.yaml"));
				Assert.fail();
			} catch (IllegalArgumentException e) {
				assertTrue(e.getMessage().contains("Query cannot be null."));
			}
		}

		{
			try {
				final ConnectorDeserializer deserializer = new ConnectorDeserializer();
				deserializer.deserialize(new File("src/test/resources/test-files/connector/detection/criteria/wbem/wbemCriterionEmptyQuery.yaml"));
				Assert.fail();
			} catch (IllegalArgumentException e) {
				assertTrue(e.getMessage().contains("Query cannot be empty."));
			}
		}
	}
}