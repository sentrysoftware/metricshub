package com.sentrysoftware.matrix.converter.state.source;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class WbemSourceConverterTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/source/wbem";
	}

	@Test
	void test() throws IOException {
		testConversion("discovery");
		testConversion("collect");

		testAll();
	}
}
