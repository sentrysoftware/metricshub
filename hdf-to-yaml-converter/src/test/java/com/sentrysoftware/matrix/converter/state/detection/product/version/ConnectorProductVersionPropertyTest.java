package com.sentrysoftware.matrix.converter.state.detection.product.version;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class ConnectorProductVersionPropertyTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/productVersion/";
	}
	@Test
	void testKmVersion() throws IOException {
		String input = """
				// Only for Hardware KM 11.3.00+
				Detection.Criteria(1).Type="KMVersion"
				Detection.Criteria(1).Version="11.3.00"
				""";

		testConversion(input, "kmVersion");
	}

	@Test
	void testMultipleCriteria() throws IOException {
		String input = """
				// Only for Hardware KM 11.3.00+
				Detection.Criteria(1).Type="KMVersion"
				Detection.Criteria(1).Version="11.3.00"

				// Definitely not linux
				Detection.Criteria(2).Type="OS"
				Detection.Criteria(2).Exclude="Linux"
				""";

		testConversion(input, "multipleCriteria");
	}
}