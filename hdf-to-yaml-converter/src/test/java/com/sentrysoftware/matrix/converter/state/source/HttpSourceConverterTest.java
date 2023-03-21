package com.sentrysoftware.matrix.converter.state.source;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class HttpSourceConverterTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/monitors/source/http";
	}

	@Test
	@Disabled("Until HTTP Source coverter is up.")
	void test() throws IOException {
		testConversion("discovery");
		testConversion("collect");

		testAll();
	}
}