package com.sentrysoftware.matrix.converter.state.detection.wmi;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class ConnectorWmiCriteriaTest extends AbstractConnectorPropertyConverterTest {

    @Override
    protected String getResourcePath() {
        return "src/test/resources/test-files/connector/detection/criteria/wmi";
    }

	@Test
	@Disabled("Until WMI Converter is up!")
	void test() throws IOException {
		testConversion("getAvailableTest");
		testConversion("testMany");

		testAll();
	}
}
