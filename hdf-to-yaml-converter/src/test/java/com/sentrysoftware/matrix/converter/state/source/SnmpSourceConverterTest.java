package com.sentrysoftware.matrix.converter.state.source;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class SnmpSourceConverterTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/source/snmp";
	}

	@Test
	@Disabled("until SNMP Source converter is up")
	void test() throws IOException {
		testConversion("discoveryTable");
		testConversion("collectTable");

		testConversion("discoveryGet");
		testConversion("collectGet");

		testAll();
	}
}
