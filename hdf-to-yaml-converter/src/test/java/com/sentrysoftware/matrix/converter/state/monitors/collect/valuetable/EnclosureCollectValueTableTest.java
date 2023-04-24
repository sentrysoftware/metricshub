package com.sentrysoftware.matrix.converter.state.monitors.collect.valuetable;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class EnclosureCollectValueTableTest extends AbstractConnectorPropertyConverterTest {
	
	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/collect/valueTable/enclosure";
	}

	@Test
	void test() throws IOException {
		testAll();
	}
}