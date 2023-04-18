package com.sentrysoftware.matrix.converter.state.monitors.collect.valuetable;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class TapeDriveCollectValueTableTest extends AbstractConnectorPropertyConverterTest{
	
	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/collect/valueTable/tapeDrive";
	}

	@Test
	@Disabled("Until TapeDriveValueTable processor is up")
	void test() throws IOException {
		testAll();
	}
}