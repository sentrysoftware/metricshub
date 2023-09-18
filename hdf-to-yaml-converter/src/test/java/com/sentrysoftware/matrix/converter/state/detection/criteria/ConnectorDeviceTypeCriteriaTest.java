package com.sentrysoftware.matrix.converter.state.detection.criteria;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ConnectorDeviceTypeCriteriaTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/deviceType";
	}

	@Test
	void test() throws IOException {
		testConversion("keep");
		testConversion("multipleCriteria");
		testConversion("keepMultiple");
		testConversion("exclude");
		testConversion("excludeKeep");

		testAll();
	}
}
