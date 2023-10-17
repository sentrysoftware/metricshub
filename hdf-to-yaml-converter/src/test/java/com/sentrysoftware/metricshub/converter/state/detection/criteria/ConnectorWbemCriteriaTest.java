package com.sentrysoftware.metricshub.converter.state.detection.criteria;

import com.sentrysoftware.metricshub.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ConnectorWbemCriteriaTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/wbem";
	}

	@Test
	void test() throws IOException {
		testConversion("test");
		testConversion("testMany");

		testAll();
	}
}
