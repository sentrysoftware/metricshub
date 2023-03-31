package com.sentrysoftware.matrix.converter;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class CollectTypeProcessorTest extends AbstractConnectorPropertyConverterTest  {

	@Test
	void test() throws IOException {
		testAll();
	}

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/collectType";
	}

}
