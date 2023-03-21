package com.sentrysoftware.matrix.converter.state.computes;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class KeepColumnsComputeTest extends AbstractConnectorPropertyConverterTest {

    @Override
    protected String getResourcePath() {
        return "src/test/resources/test-files/connector/computes/keepColumns";
    }
    
    @Test
    @Disabled("until KeepColumns compute processor is up")
    void test() throws IOException {
        
        
        testAll();
    }
}
