package com.sentrysoftware.metricshub.converter.state.monitors.discovery.instance;

import com.sentrysoftware.metricshub.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class VmDiscoveryInstanceTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/discovery/instance/vm";
	}

	@Test
	void test() throws IOException {
		testAll();
	}
}