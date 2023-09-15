package com.sentrysoftware.matrix.converter.state.detection.criteria;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ConnectorServiceCriteriaTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/service";
	}

	@Test
	void test() throws IOException {
		testConversion("service");
		testConversion("testEscapePipe");
		testConversion("testPipe");

		testAll();
	}
}
