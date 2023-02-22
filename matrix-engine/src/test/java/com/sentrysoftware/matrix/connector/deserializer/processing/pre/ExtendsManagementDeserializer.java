package com.sentrysoftware.matrix.connector.deserializer.processing.pre;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;

public class ExtendsManagementDeserializer extends DeserializerTest{
    
    final String path;

    public ExtendsManagementDeserializer(String path) {
        this.path = path;
    }

    @Override
    public String getResourcePath() {
        return "src/test/resources/test-files/extends/management/" + path + "/";
    }

    void test() throws IOException {

        final Connector test = getConnector("test");
        final Connector expected = getConnector("expected");

        assertEquals(test, expected);
    }
}
