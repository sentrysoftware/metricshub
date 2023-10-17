package com.sentrysoftware.metricshub.converter.state.monitors.collect.valuetable;

import com.sentrysoftware.metricshub.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

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
