package org.sentrysoftware.metricshub.engine.connector.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.sentrysoftware.metricshub.engine.common.helpers.JsonHelper;

class EmbeddedFilesResolverTest {

	private static final ObjectMapper OBJECT_MAPPER = JsonHelper.buildYamlMapper();
	private static final String BASE_DIRECTORY = "src/test/resources/test-files/embedded";
	private static final File CONNECTOR_1_FILE = Path.of(BASE_DIRECTORY, "connector1", "connector1.yaml").toFile();
	private static final File CONNECTOR_2_FILE = Path.of(BASE_DIRECTORY, "connector2", "connector2.yaml").toFile();
	private static final Path CONNECTOR_2_DIRECTORY = Path.of(BASE_DIRECTORY, "connector2");
	private static final Path CONNECTOR_1_DIRECTORY = Path.of(BASE_DIRECTORY, "connector1");

	private static final String WINDOWS_EXPECTED_YAML = String.format(
		"""
		---
		connector:
		  detection:
		    criteria:
		    - type: productRequirements
		      kmVersion: 10.3.00
		    - type: http
		      method: GET
		      url: /redfish/v1/
		      header: "${file::%s}"
		      body: "${file::%s}"
		      expectedResult: iLO 4
		      errorMessage: Invalid credentials / not an HP iLO 4
		""",
		new File("src/test/resources/test-files/embedded/connector1/header.txt").toURI().getPath(),
		new File("src/test/resources/test-files/embedded/connector2/embedded2/body.txt").toURI().getPath()
	);

	/**
	 *  Path separator on LINUX is /
	 *  Replace the WINDOWS path separator '\' with the LINUX one '/' to get clean paths
	 *  in the LINUX expected YAML
	 */
	private static final String LINUX_EXPECTED_YAML = WINDOWS_EXPECTED_YAML
		.replace("\\\\", "/") // YAML escaped paths
		.replace("\\", "/"); // YAML not escaped paths

	@Test
	@EnabledOnOs(value = OS.WINDOWS)
	void testInternalizeWindows() throws IOException {
		assertResolvedConnector(WINDOWS_EXPECTED_YAML);
	}

	@Test
	@EnabledOnOs(value = OS.LINUX)
	void testInternalizeLinux() throws IOException {
		assertResolvedConnector(LINUX_EXPECTED_YAML);
	}

	/**
	 * Call {@link EmbeddedFilesResolver} to resolve embedded files in the
	 * connector then compare the updated connector with the given expected
	 * YAML connector.
	 *
	 * @param expectedYaml
	 * @throws IOException
	 */
	private void assertResolvedConnector(final String expectedYaml) throws IOException {
		final JsonNode connector = OBJECT_MAPPER.readTree(CONNECTOR_1_FILE);

		final Set<URI> parents = Set.of(CONNECTOR_2_DIRECTORY.toUri());

		new EmbeddedFilesResolver(connector, CONNECTOR_1_DIRECTORY, parents).internalize();

		// Verify the full JsonNode object
		assertEquals(OBJECT_MAPPER.readTree(expectedYaml), connector);
	}

	@Test
	void testInternalizeFailureOnFileNotFound() throws IOException {
		final EmbeddedFilesResolver embeddedFilesResolver = new EmbeddedFilesResolver(
			OBJECT_MAPPER.readTree(CONNECTOR_2_FILE),
			CONNECTOR_2_DIRECTORY,
			Collections.emptySet()
		);

		assertThrows(IOException.class, () -> embeddedFilesResolver.internalize());
	}

	@Test
	@EnabledOnOs(value = OS.WINDOWS)
	void testFindAbsoluteUriZipWindows() throws IOException {
		final EmbeddedFilesResolver embeddedFilesResolver = new EmbeddedFilesResolver(
			OBJECT_MAPPER.readTree(CONNECTOR_2_FILE),
			CONNECTOR_2_DIRECTORY,
			Collections.emptySet()
		);
		final Path yamlTestPath = Paths.get(
			"src",
			"test",
			"resources",
			"test-files",
			"connector",
			"zippedConnector",
			"connectors.zip"
		);
		assertEquals(
			new File("src\\test\\resources\\test-files\\connector\\zippedConnector\\connectors.zip\\AAC.yaml").toURI(),
			embeddedFilesResolver.findAbsoluteUri("AAC.yaml", yamlTestPath)
		);
	}

	@Test
	@EnabledOnOs(value = OS.WINDOWS)
	void testFindAbsoluteUriWindows() throws IOException {
		final EmbeddedFilesResolver embeddedFilesResolver = new EmbeddedFilesResolver(
			OBJECT_MAPPER.readTree(CONNECTOR_2_FILE),
			CONNECTOR_2_DIRECTORY,
			Collections.emptySet()
		);
		final Path yamlTestPath = Paths.get(
			"src",
			"test",
			"resources",
			"test-files",
			"connector",
			"connectorLibraryParser"
		);
		assertEquals(
			new File("src\\test\\resources\\test-files\\connector\\connectorLibraryParser\\AAC.yaml").toURI(),
			embeddedFilesResolver.findAbsoluteUri("AAC.yaml", yamlTestPath)
		);
	}

	@Test
	@EnabledOnOs(value = OS.LINUX)
	void testFindAbsoluteUriZipLinux() throws IOException {
		final EmbeddedFilesResolver embeddedFilesResolver = new EmbeddedFilesResolver(
			OBJECT_MAPPER.readTree(CONNECTOR_2_FILE),
			CONNECTOR_2_DIRECTORY,
			Collections.emptySet()
		);
		final Path yamlTestPath = Paths.get(
			"src",
			"test",
			"resources",
			"test-files",
			"connector",
			"zippedConnector",
			"connectors.zip"
		);
		assertEquals(
			new File("src/test/resources/test-files/connector/zippedConnector/connectors.zip/AAC.yaml").toURI(),
			embeddedFilesResolver.findAbsoluteUri("AAC.yaml", yamlTestPath)
		);
	}

	@Test
	@EnabledOnOs(value = OS.LINUX)
	void testFindAbsoluteUriLinux() throws IOException {
		final EmbeddedFilesResolver embeddedFilesResolver = new EmbeddedFilesResolver(
			OBJECT_MAPPER.readTree(CONNECTOR_2_FILE),
			CONNECTOR_2_DIRECTORY,
			Collections.emptySet()
		);
		final Path yamlTestPath = Paths.get(
			"src",
			"test",
			"resources",
			"test-files",
			"connector",
			"connectorLibraryParser"
		);
		assertEquals(
			new File("src/test/resources/test-files/connector/connectorLibraryParser/AAC.yaml").toURI(),
			embeddedFilesResolver.findAbsoluteUri("AAC.yaml", yamlTestPath)
		);
	}
}
