package com.sentrysoftware.matrix.converter.state.detection.criteria;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

public class ConnectorProcessCriteriaTest extends AbstractConnectorPropertyConverterTest {
    @Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/process";
	}

	@Test
	@Disabled("Until Process Converter is up!")
	void test() throws IOException {

        testConversion("process");
        testConversion("test2");

		//incase an individual test was missed.
		testAll();
	}
}
