package com.sentrysoftware.matrix.converter.state.detection.snmp;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

public class ConnectorSnmpGetCriteriaTest extends AbstractConnectorPropertyConverterTest {

    @Override
    protected String getResourcePath() {
        return "src/test/resources/test-files/connector/detection/criteria/snmp";
    }

	@Test
	@Disabled("Until SnmpGet Converter is up!")
	void test() throws IOException {
		testConversion("snmpGetTest");
		testConversion("snmpGetNextTest");
		testConversion("testMany");

		testAll();
	}
}
