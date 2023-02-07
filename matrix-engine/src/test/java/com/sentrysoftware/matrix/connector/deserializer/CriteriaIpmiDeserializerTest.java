package com.sentrysoftware.matrix.connector.deserializer;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Ipmi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CriteriaIpmiDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/ipmi/";
	}

	@Test
	/**
	 * Checks that the criteria type is ipmi
	 *
	 * @throws IOException
	 */
	void testDeserializeDoesntThrow() throws IOException {
		final String testResource = "ipmiCriterion";
		final Connector connector = getConnector(testResource);

		List<Criterion> expected = new ArrayList<>();
		expected.add(new Ipmi("ipmi", true));

		compareCriterion(testResource, connector, expected);
	}
}
