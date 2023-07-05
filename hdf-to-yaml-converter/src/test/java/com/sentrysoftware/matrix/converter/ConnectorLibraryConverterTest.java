package com.sentrysoftware.matrix.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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

		final AtomicInteger fileCounter = new AtomicInteger(0);
		Files.walkFileTree(processor.getOutputDirectory(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				final String filename = file.getFileName().toString();
				if (!Files.isDirectory(file) && filename.endsWith(".yaml")) {

					fileCounter.incrementAndGet();

					final File yamlFile = file.toFile();
					final File expected = Path.of(YAML_DIRECTORY, filename.substring(0, filename.lastIndexOf('.')), filename).toFile();

					assertTrue(expected.exists());
					assertTrue(yamlFile.exists());

					JsonNode expectedNode = null;
					JsonNode yaml = null;

					try {
						yaml = mapper.readTree(yamlFile);

						if (yaml.findValues("_comment").size() != 0) {
							Assertions.fail(String.format("_comment node found! in %s", yamlFile.getAbsolutePath()));
						}

					} catch (Exception e) {
						Assertions.fail(String.format(YAML_IS_INVALID_FORMAT, yamlFile, yaml));
					}

					try {
						expectedNode = mapper.readTree(expected);

						if (expectedNode.findValues("_comment").size() != 0) {
							Assertions.fail(String.format("_comment node found! in %s", expected.getAbsolutePath()));
						}

					} catch (Exception e) {
						Assertions.fail(String.format(YAML_IS_INVALID_FORMAT, expected, expectedNode));
					}

					assertEquals(expectedNode, yaml, () -> "Assertion failed on file: " + yamlFile.getName());
				}

				return FileVisitResult.CONTINUE;
			}
		});

		final int expectedFileCounter = 286;
		assertEquals(expectedFileCounter, fileCounter.get());
	}

	@Test
	void testProcessComments() throws Exception {
		final ConnectorLibraryConverter processor = new ConnectorLibraryConverter(Path.of(HDF_DIRECTORY), tempDir);
		processor.process();

		Files.walkFileTree(processor.getOutputDirectory(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				final String filename = file.getFileName().toString();
				if (!Files.isDirectory(file) && filename.endsWith(".yaml")) {

					final File yamlFile = file.toFile();
					final File expected = Path.of(YAML_DIRECTORY, filename.substring(0, filename.lastIndexOf('.')), filename).toFile();

					assertTrue(expected.exists());
					assertTrue(yamlFile.exists());

					final List<String> inputLines = readLinesSafe(yamlFile);
					final List<String> expectedLines = readLinesSafe(expected);

					// Sanity
					if(inputLines.contains("_comment")) {
						Assertions.fail("_comment node not processed!");
					}

					assertEquals(expectedLines.size(), inputLines.size());

					for (int i = 0; i < expectedLines.size(); i++) {
						final int index = i;
						assertEquals(expectedLines.get(i), inputLines.get(i), () -> "Assertion failed at line " + index + ". File: " + expected.getName());
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static final Pattern EMBEDDED_FILE_PATTERN = Pattern.compile(
		"\\$\\{file::(\\S+)\\}",
		Pattern.CASE_INSENSITIVE
	);

	@Test
	void testProcessEmbeddedFiles() throws Exception {
		final ConnectorLibraryConverter processor = new ConnectorLibraryConverter(Path.of(HDF_DIRECTORY), tempDir);
		processor.process();

		Files.walkFileTree(processor.getOutputDirectory(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				final String filename = file.getFileName().toString();
				if (!Files.isDirectory(file) && filename.endsWith(".yaml")) {

					final File yamlFile = file.toFile();

					assertTrue(yamlFile.exists());

					final List<String> inputLines = readLinesSafe(yamlFile);

					for (String line : inputLines) {
						// Skip comments
						if (line.trim().startsWith("#")) {
							continue;
						}

						final Matcher matcher = EMBEDDED_FILE_PATTERN.matcher(line);
						while (matcher.find()) {
							final String embeddedFileRelativePath = matcher.group(1);
							final Path resolvedPath = file.getParent().resolve(embeddedFileRelativePath);
							assertTrue(
								Files.exists(
									resolvedPath
								) || filename.endsWith("-header.yaml"),
								// Headers reference embedded files defined in HDFs, The code will not produce a relative path here
								// because the HHDF could be included in several connectors where each connector defines its 
								// embedded file.
								() -> resolvedPath + " doesn't exist for: " + filename
							);
						}
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Read all lines from a file.
	 * 
	 * @param file
	 * @return The lines from the file as a {@code List}
	 */
	private List<String> readLinesSafe(final File file) {
		try {
			return Files.readAllLines(file.toPath());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	/**
	 * Run this test using JVM argument
	 * -Dhc.project.path=path_to_<hardware-connectors>_project E.g.
	 * -Dhc.project.path=/opt/workspace/hardware-connectors
	 * 
	 */
	@Disabled("The test is disabled because it's supposed to be launched on the developer's machine only.")
	void produceYamlConnectors() {
		try {
			final String hcProjectPath = System.getProperty("hc.project.path");

			assertNotNull(hcProjectPath);

			new ConnectorLibraryConverter(
				Path.of(hcProjectPath, "src", "main", "hdf"),
				Path.of(hcProjectPath, "src", "main", "connector")
			)
			.process();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Conversion error detected.");
		}
	}

}