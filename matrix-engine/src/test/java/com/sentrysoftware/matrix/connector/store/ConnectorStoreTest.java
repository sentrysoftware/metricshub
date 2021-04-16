package com.sentrysoftware.matrix.connector.store;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConnectorStoreTest {

    @Test
    void testGetInstance() {

        assertNotNull(ConnectorStore.getInstance());
    }

    @Test
    void testDeserializeConnectors() {

        ConnectorStore connectorStore = ConnectorStore.getInstance();

        // Connectors relative path is null
        connectorStore.setConnectorsRelativePath(null);
        assertThrows(IllegalArgumentException.class, connectorStore::deserializeConnectors);
    }
}