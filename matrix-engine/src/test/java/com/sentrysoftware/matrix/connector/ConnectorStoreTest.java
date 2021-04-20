package com.sentrysoftware.matrix.connector;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ConnectorStoreTest {

    @Test
    void testGetInstance() {

        assertNotNull(ConnectorStore.getInstance());
    }

}