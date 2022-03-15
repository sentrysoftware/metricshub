package com.sentrysoftware.hardware.agent.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;

@SpringBootTest
class ConnectorsLoaderServiceTest {

	@Autowired
	private ConnectorsLoaderService service;

	@Test
	void testLoad() {
		assertDoesNotThrow(() -> service.load());
	}

	@Test
	void testLoadConnectors() throws IOException {

		service.loadConnectors(Paths.get("src/test/resources/hdf"));

		final Connector simpleTest = ConnectorStore.getInstance().getConnectors().get("SimpleTest");

		assertNotNull(simpleTest);
		assertEquals("SimpleTest", simpleTest.getCompiledFilename());
		assertNotNull(simpleTest.getDetection());
		assertNotNull(simpleTest.getHardwareMonitors());

		final Connector invalid = ConnectorStore.getInstance().getConnectors().get("Invalid");

		assertNotNull(invalid);
		assertFalse(invalid.getProblemList().isEmpty());
	}

}
