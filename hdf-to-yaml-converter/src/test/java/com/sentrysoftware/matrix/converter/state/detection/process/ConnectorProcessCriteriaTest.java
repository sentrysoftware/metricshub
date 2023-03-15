package com.sentrysoftware.matrix.converter.state.detection.process;

import java.io.IOException;

import org.junit.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

public class ConnectorProcessCriteriaTest extends AbstractConnectorPropertyConverterTest {
    @Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/process";
	}

	@Test
	void test() throws IOException {

        testConversion("process");
        testConversion("test2");

		//incase an individual test was missed.
		testAll();
	}
}
