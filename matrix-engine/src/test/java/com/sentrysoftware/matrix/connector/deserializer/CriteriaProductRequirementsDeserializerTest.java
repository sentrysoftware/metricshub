package com.sentrysoftware.matrix.connector.deserializer;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProductRequirements;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CriteriaProductRequirementsDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/productRequirements/";
	}

	@Test
	/**
	 * Checks input properties for product requirements detection criteria
	 *
	 * @throws Exception
	 */
	void testDeserializeProductRequirementsDeserializer() throws Exception {
		final String testResource = "productRequirementsCriterion";
		final Connector productRequirements = getConnector(testResource);

		List<Criterion> expected = new ArrayList<>();
		expected.add(new ProductRequirements("productRequirements", false, "testengineversion", "testkmversion"));
		
		compareCriterion(testResource, productRequirements, expected);
	}
}
