package com.sentrysoftware.matrix.connector;

import static org.junit.jupiter.api.Assertions.*;
import static com.sentrysoftware.matrix.connector.ConnectorLibrarySerializer.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.exception.ConnectorSerializationException;

class ConnectorLibrarySerializerTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testMain() {
	}

	@Test
	void testValidateOutputDirectory() throws IOException {

		// Existing dir
		{
			Path tempDir = Files.createTempDirectory("testDir");
			tempDir.toFile().deleteOnExit();
			assertDoesNotThrow(() -> validateOutputDirectory(tempDir), "Existing directory is just fine");
		}

		// File triggers exception
		{
			Path tempFile = Files.createTempFile("testFile", this.getClass().getSimpleName());
			tempFile.toFile().deleteOnExit();
			assertThrows(
					ConnectorSerializationException.class,
					() -> validateOutputDirectory(tempFile),
					"A file is not a valid directory"
			);
		}

		// Non-existing dir
		{
			Path tempDir = Files.createTempDirectory("testDir");
			tempDir.toFile().deleteOnExit();
			Path nonExistingDir = tempDir.resolve("new");
			assertFalse(Files.exists(nonExistingDir), "Tested directory must not have been created first");
			assertDoesNotThrow(() -> validateOutputDirectory(nonExistingDir), "Create new directory works");
			assertTrue(Files.exists(nonExistingDir) && Files.isDirectory(nonExistingDir), "Directory must have been created");
		}
	}

	@Test
	void testGetConnectorList() throws IOException {
		List<String> connectorList = getConnectorList(Paths.get("src", "test", "resources", "hdf"));
		assertEquals(4, connectorList.size());
	}

	@Test
	void testGetLastModifiedTime() throws IOException {

		// Non-existing file
		{
			assertEquals(0, getLastModifiedTime(Paths.get("non-existent")), "Non-existing file must have a modification time of zero");
		}

		// Existing file
		{
			long creationTime = System.currentTimeMillis();
			Path tempFile = Files.createTempFile("testFile", this.getClass().getSimpleName());
			tempFile.toFile().deleteOnExit();
			assertTrue(getLastModifiedTime(tempFile) >= creationTime - 1000, "File modification time must be greater than now");
		}

	}

	@Test
	void testSerializeConnectorSources() throws IOException {

		// Invalid sourceDirectory
		{
			assertThrows(ConnectorSerializationException.class, () -> serializeConnectorSources(Paths.get("non-existent"), Paths.get("other")));
		}

		// Valid case
		{
			Path tempDir = Files.createTempDirectory("testDir");
			tempDir.toFile().deleteOnExit();
			Path targetDir = tempDir.resolve("serialized");

			assertFalse(Files.isDirectory(targetDir));

			serializeConnectorSources(Paths.get("src", "test", "resources", "hdf"), targetDir);

			assertTrue(Files.isDirectory(targetDir));
			List<Path> resultList = Files.list(targetDir).collect(Collectors.toUnmodifiableList());
			assertEquals(4, resultList.size());
		}
	}

}
