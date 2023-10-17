package com.sentrysoftware.metricshub.converter.state.monitors.collect.valuetable;

import com.sentrysoftware.metricshub.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class BatteryCollectValueTableTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/collect/valueTable/battery";
	}

	@Test
	void test() throws IOException {
		testAll();
	}
}
