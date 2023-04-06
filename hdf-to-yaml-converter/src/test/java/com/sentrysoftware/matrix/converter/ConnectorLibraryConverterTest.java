package com.sentrysoftware.matrix.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;

class ConnectorLibraryConverterTest {

	private static final String HDF_DIRECTORY = "src/test/resources/hdf";
	private static final String YAML_DIRECTORY = "src/test/resources/yaml";

	@TempDir
	private Path tempDir;

	@Test
	void testProcess() throws IOException {
		final ConnectorLibraryConverter processor = new ConnectorLibraryConverter(Path.of(HDF_DIRECTORY), tempDir);
		processor.process();
		final File file = tempDir.resolve("DellOpenManage.yaml").toFile();
		assertTrue(file.exists());
	}

	@Test
	@Disabled("Until comments processor is up.")
	void testProcessComments() throws IOException {
		final ObjectMapper mapper = JsonHelper.buildYamlMapper();
		
		final ConnectorLibraryConverter processor = new ConnectorLibraryConverter(Path.of(HDF_DIRECTORY), tempDir);
		processor.process();

		final Path expectedPath = Path.of(YAML_DIRECTORY, "DellOpenManage.yaml");

		final File inputFile = tempDir.resolve("DellOpenManage.yaml").toFile();
		final File expectedFile = expectedPath.toFile();
		
		assertTrue(inputFile.exists());
		assertTrue(expectedFile.exists());

		assertEquals(mapper.readTree(expectedFile), mapper.readTree(inputFile));

		final List<String> inputLines = Files.readAllLines(Path.of(inputFile.getAbsolutePath()));
		final List<String> expectedLines = Files.readAllLines(expectedPath);

		assertEquals(expectedLines, inputLines);
	}

}
