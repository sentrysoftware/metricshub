package com.sentrysoftware.metricshub.converter.state.monitors.collect.valuetable;

import com.sentrysoftware.metricshub.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class VoltageCollectValueTableTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/collect/valueTable/voltage";
	}

	@Test
	void test() throws IOException {
		testAll();
	}
}
