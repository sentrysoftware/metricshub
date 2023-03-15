package com.sentrysoftware.matrix.converter.state.detection.http;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class ConnectorHttpCriteriaTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/http";
	}

	@Test
	@Disabled("until HTTP converter is up")
	void test() throws IOException {
		testConversion("test");
		testConversion("testMany");
		testAll();
	}
}