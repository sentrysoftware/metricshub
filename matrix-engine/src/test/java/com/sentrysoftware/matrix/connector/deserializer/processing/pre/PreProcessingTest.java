package com.sentrysoftware.matrix.connector.deserializer.processing.pre;

import java.io.IOException;
import org.junit.Test;

public class PreProcessingTest {

    @Test
    public void testExtendsManagementArrayObjectsMerge() throws IOException {
        new ExtendsManagementDeserializer("arrayObjectsMerge").test();
    }

    @Test
    public void testExtendsManagementMergeObjects() throws IOException {
        new ExtendsManagementDeserializer("mergeObjects").test();
    }

    @Test
    public void testExtendsManagementOverwriteArraysSimpleValues() throws IOException {
        new ExtendsManagementDeserializer("overwriteArraysSimpleValues").test();
    }
}
