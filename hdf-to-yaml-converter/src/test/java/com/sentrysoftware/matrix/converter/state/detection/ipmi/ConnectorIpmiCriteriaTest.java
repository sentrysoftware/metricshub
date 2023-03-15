package com.sentrysoftware.matrix.converter.state.detection.ipmi;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

public class ConnectorIpmiCriteriaTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/ipmi";
	}

	@Test
	@Disabled("until IPMI converter is up!")
	void test() throws IOException {
		testConversion("test");
		testConversion("testMany");
		testAll();
	}
}