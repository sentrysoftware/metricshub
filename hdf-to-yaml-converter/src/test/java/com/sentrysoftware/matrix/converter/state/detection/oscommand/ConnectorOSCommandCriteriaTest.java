package com.sentrysoftware.matrix.converter.state.detection.oscommand;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

public class ConnectorOSCommandCriteriaTest extends AbstractConnectorPropertyConverterTest {
	
	@Test
	@Disabled("Until OsCommand Converter is up!")
	void test() throws IOException {
		testConversion("test");
		testConversion("testMany");
		
		testAll();
	}

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/osCommand";
	}
}
