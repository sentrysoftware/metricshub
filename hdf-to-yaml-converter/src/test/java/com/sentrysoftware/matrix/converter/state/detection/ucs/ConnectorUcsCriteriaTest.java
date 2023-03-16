package com.sentrysoftware.matrix.converter.state.detection.ucs;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

public class ConnectorUcsCriteriaTest extends AbstractConnectorPropertyConverterTest {

    @Override
    protected String getResourcePath() {
        return "src/test/resources/test-files/connector/detection/criteria/ucs";
    }
    
    @Test
	@Disabled("Until UCS Converter is up!")
    void test() throws IOException {
        testAll();
    }
}
