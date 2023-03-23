package com.sentrysoftware.matrix.converter.state.source;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class SnmpSourceConverterTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/source/snmp";
	}

	@Test
	void testSnmpGet() throws IOException {
		testConversion("discoveryGet");
		testConversion("collectGet");
	}

	@Test
	void testSnmpTable() throws IOException {
		testConversion("discoveryTable");
		testConversion("collectTable");
	}

	@Test
	void test() throws IOException {
		testAll();
	}
}
