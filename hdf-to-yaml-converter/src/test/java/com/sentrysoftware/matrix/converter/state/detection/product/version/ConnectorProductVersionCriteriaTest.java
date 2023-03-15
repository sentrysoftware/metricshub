package com.sentrysoftware.matrix.converter.state.detection.product.version;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class ConnectorProductVersionCriteriaTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/productVersion";
	}
	@Test
	void test() throws IOException {
		testConversion("kmVersion");
		testConversion("multipleCriteria");

		testAll();
	}
}