package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProductRequirements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CriteriaProductRequirementsDeserializerTest {

	@Test
	/**
	 * Checks input properties for product requirements detection criteria
	 *
	 * @throws Exception
	 */
	void testDeserializeProductRequirementsDeserializer() throws Exception {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector productRequirements = deserializer
				.deserialize(new File("src/test/resources/test-files/connector/detection/criteria/productRequirements/productRequirementsCriterion.yaml"));

		List<Criterion> expected = new ArrayList<>();
		expected.add(new ProductRequirements("productRequirements", false, "testengineversion", "testkmversion"));

		assertNotNull(productRequirements);
		assertEquals("productRequirementsCriterion", productRequirements.getConnectorIdentity().getCompiledFilename());

		assertNotNull(productRequirements.getConnectorIdentity().getDetection());
		assertEquals(expected, productRequirements.getConnectorIdentity().getDetection().getCriteria());
	}
}
