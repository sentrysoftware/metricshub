package com.sentrysoftware.matrix.converter.state.collect.valueTable;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class lunCollectValueTableTest extends AbstractConnectorPropertyConverterTest{
	
	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/collect/valueTable/lun";
	}

	@Test
	@Disabled("Until LunValueTable processor is up")
	void test() throws IOException {
		testAll();
	}
}