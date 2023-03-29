package com.sentrysoftware.matrix.converter.state.monitors.discovery.instance;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

public class DiskControllerDiscoveryInstanceTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/discovery/instance/diskcontroller";
	}

	@Test
	@Disabled("Until DiskControllerInstance processor is up")
	void test() throws IOException {
		testAll();
	}
}
