package com.sentrysoftware.matrix.converter.state.detection.criteria;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class ConnectorProductVersionCriteriaTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/productVersion";
	}
	@Test
	@Disabled("Until ProductVersion Converter is up!")
	void test() throws IOException {
		testConversion("kmVersion");
		testConversion("multipleCriteria");

		testAll();
	}
}