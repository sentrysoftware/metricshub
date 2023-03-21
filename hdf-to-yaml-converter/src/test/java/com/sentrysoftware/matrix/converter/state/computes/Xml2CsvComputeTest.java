package com.sentrysoftware.matrix.converter.state.computes;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class Xml2CsvComputeTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/computes/xml2Csv";
	}
	
	@Test
	@Disabled("until Xml2Csv compute processor is up")
	void test() throws IOException {
		
		
		testAll();
	}
}
