package com.sentrysoftware.metricshub.converter.state.source;

import com.sentrysoftware.metricshub.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class WmiSourceConverterTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/source/wmi";
	}

	@Test
	void test() throws IOException {
		testConversion("discovery");
		testConversion("collect");
		testConversion("executeForEachEntryOf");

		testAll();
	}
}