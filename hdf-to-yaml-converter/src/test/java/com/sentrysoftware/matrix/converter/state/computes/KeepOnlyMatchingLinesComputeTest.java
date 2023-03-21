package com.sentrysoftware.matrix.converter.state.computes;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class KeepOnlyMatchingLinesComputeTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/computes/keepOnlyMatchingLines";
	}
	
	@Test
	@Disabled("until KeepOnlyMatchingLines compute processor is up")
	void test() throws IOException {
		
		
		testAll();
	}
}
