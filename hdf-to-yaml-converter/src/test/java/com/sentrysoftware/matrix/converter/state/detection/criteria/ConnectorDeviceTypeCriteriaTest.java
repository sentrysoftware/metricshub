package com.sentrysoftware.matrix.converter.state.detection.criteria;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class ConnectorDeviceTypeCriteriaTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/deviceType";
	}

	@Test
	@Disabled("until DeviceTypeConverter is up")
	void test() throws IOException {
		
		testConversion("keep");
		testConversion("multipleCriteria");
		testConversion("keepMultiple");
		testConversion("exclude");
		testConversion("excludeKeep");

		//incase an individual test was missed.
		testAll();
	}
}