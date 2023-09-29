package com.sentrysoftware.matrix.converter.state.monitors.discovery.instance;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class BladeDiscoveryInstanceTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/discovery/instance/blade";
	}

	@Test
	void test() throws IOException {
		testAll();
	}
}
