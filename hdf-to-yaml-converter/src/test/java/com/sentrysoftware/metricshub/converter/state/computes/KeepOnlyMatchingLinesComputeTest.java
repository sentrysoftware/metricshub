package com.sentrysoftware.metricshub.converter.state.computes;

import com.sentrysoftware.metricshub.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class KeepOnlyMatchingLinesComputeTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/computes/keepOnlyMatchingLines";
	}

	@Test
	void test() throws IOException {
		testAll();
	}
}
