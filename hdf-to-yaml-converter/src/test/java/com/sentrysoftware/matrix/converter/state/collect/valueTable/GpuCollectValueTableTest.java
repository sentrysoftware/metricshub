package com.sentrysoftware.matrix.converter.state.collect.valueTable;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class GpuCollectValueTableTest extends AbstractConnectorPropertyConverterTest{
	
	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/collect/valueTable/gpu";
	}

	@Test
	@Disabled("Until GpuValueTable processor is up")
	void test() throws IOException {
		testAll();
	}
}