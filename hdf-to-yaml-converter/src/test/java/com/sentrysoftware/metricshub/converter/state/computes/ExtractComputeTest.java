package com.sentrysoftware.metricshub.converter.state.computes;

import com.sentrysoftware.metricshub.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ExtractComputeTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/computes/extract";
	}

	@Test
	void test() throws IOException {
		testAll();
	}
}
