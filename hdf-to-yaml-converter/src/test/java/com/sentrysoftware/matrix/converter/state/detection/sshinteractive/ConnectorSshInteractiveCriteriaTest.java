package com.sentrysoftware.matrix.converter.state.detection.sshinteractive;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class ConnectorSshInteractiveCriteriaTest extends AbstractConnectorPropertyConverterTest {

    @Override
    protected String getResourcePath() {
        return "src/test/resources/test-files/connector/detection/criteria/sshInteractive";
    }
	
	@Test
	void test() throws IOException { 
		testConversion("getAvailableTest");
		testConversion("getUntilPromptTest");
		testConversion("getSendPasswordTest");
		testConversion("getSendTextTest");
		testConversion("getSendUsernameTest");
		testConversion("sleepTest");
		testConversion("waitForTest");
		testConversion("waitForPromptTest");
		testConversion("testMany");

		testAll();
	}
}
