package com.sentrysoftware.matrix.converter.state.source;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class TableUnionSourceConverterTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/source/tableunion";
	}

	@Test
	@Disabled("until TableUnion Source converter is up")
	void test() throws IOException {
		testConversion("discovery");
		testConversion("collect");

		testAll();
	}

}
