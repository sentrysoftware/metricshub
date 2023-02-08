package com.sentrysoftware.matrix.connector.deserializer.criterion;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProductRequirementsCriterion;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductRequirementsCriterionDeserializerTest extends DeserializerTest {

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
	void testDeserializeProductRequirementsDeserializer() throws Exception { // NOSONAR compareCriterion performs assertion
		final String testResource = "productRequirementsCriterion";
		final Connector productRequirements = getConnector(testResource);

		List<Criterion> expected = new ArrayList<>();
		expected.add(new ProductRequirementsCriterion("productRequirements", false, "testengineversion", "testkmversion"));
		
		compareCriterion(testResource, productRequirements, expected);
	}
}
