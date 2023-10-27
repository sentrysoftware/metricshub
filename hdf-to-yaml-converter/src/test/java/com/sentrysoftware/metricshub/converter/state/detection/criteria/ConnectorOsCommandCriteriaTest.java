package com.sentrysoftware.metricshub.converter.state.detection.criteria;

import com.sentrysoftware.metricshub.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

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