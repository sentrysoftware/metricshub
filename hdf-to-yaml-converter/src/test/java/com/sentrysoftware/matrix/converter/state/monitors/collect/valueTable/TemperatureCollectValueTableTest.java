package com.sentrysoftware.matrix.converter.state.monitors.collect.valueTable;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class TemperatureCollectValueTableTest extends AbstractConnectorPropertyConverterTest{
	
	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/collect/valueTable/temperature";
	}

	@Test
	@Disabled("Until TemperatureValueTable processor is up")
	void test() throws IOException {
		testAll();
	}
}