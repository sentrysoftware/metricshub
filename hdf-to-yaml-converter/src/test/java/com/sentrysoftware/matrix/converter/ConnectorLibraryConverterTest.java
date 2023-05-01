package com.sentrysoftware.matrix.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;

class ConnectorLibraryConverterTest {

	private static final String YAML_IS_INVALID_FORMAT = "YAML is invalid! %s, %s";
	private static final String HDF_DIRECTORY = "src/test/resources/hdf";
	private static final String YAML_DIRECTORY = "src/test/resources/yaml";

	@TempDir
	private Path tempDir;

	@Test
	void testProcess() throws IOException {
		final ObjectMapper mapper = JsonHelper.buildYamlMapper();
		final ConnectorLibraryConverter processor = new ConnectorLibraryConverter(Path.of(HDF_DIRECTORY), tempDir);
		processor.process();

		for (File yamlFile : List.of(processor.getOutputDirectory().toFile().list()).stream()
				.map(x -> new File(processor.getOutputDirectory().toAbsolutePath() + "/" + x)).toList()) {

			final File expected = Path.of(YAML_DIRECTORY, yamlFile.getName()).toFile();

			assertTrue(expected.exists());
			assertTrue(yamlFile.exists());

			JsonNode expectedNode = null;
			JsonNode yaml = null;

			try {
				yaml = mapper.readTree(yamlFile);
			} catch (Exception e) {
				Assertions.fail(String.format(YAML_IS_INVALID_FORMAT, yamlFile, yaml));
			}

			try {
				expectedNode = mapper.readTree(expected);
			} catch (Exception e) {
				Assertions.fail(String.format(YAML_IS_INVALID_FORMAT, expected, expectedNode));
			}

			assertEquals(expectedNode, yaml);
		}
	}

	@Test
	void testProcessComments() throws Exception {
		final ConnectorLibraryConverter processor = new ConnectorLibraryConverter(Path.of(HDF_DIRECTORY), tempDir);
		processor.process();

		for (File inputFile : List.of(processor.getOutputDirectory().toFile().list()).stream()
				.map(x -> new File(processor.getOutputDirectory().toAbsolutePath() + "/" + x)).toList()) {

			final File expectedFile = Path.of(YAML_DIRECTORY, inputFile.getName()).toFile();

			assertTrue(expectedFile.exists());
			assertTrue(inputFile.exists());

			final List<String> inputLines = Files.readAllLines(inputFile.toPath());
			final List<String> expectedLines = Files.readAllLines(expectedFile.toPath());

			// compares line by line. includes comments
			assertEquals(expectedLines, inputLines);
		}
	}
}