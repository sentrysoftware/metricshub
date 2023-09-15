package com.sentrysoftware.matrix.converter.state.computes;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class DuplicateColumnComputeTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/computes/duplicateColumn";
	}

	@Test
	void test() throws IOException {
		testAll();
	}
}
