package com.sentrysoftware.matrix.converter.state.detection.criteria;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class ConnectorOsCommandCriteriaTest extends AbstractConnectorPropertyConverterTest {

	@Test
	void test() throws IOException {
		testConversion("test");
		testConversion("testMany");
		
		testAll();
	}

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/osCommand";
	}
}
