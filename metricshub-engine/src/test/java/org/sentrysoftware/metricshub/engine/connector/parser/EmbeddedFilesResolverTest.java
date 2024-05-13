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
import org.sentrysoftware.metricshub.engine.common.helpers.FileHelper;
import org.sentrysoftware.metricshub.engine.common.helpers.JsonHelper;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;

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
		Paths.get("src/test/resources/test-files/embedded/connector1/header.txt").toUri().toString(),
		Paths.get("src/test/resources/test-files/embedded/connector2/embedded2/body.txt").toUri().toString()
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

		new EmbeddedFilesResolver(connector, CONNECTOR_1_DIRECTORY, parents).process();

		// Verify the full JsonNode object
		assertEquals(OBJECT_MAPPER.readTree(expectedYaml), connector);
	}

	@Test
	void testProcessFailureOnFileNotFound() throws IOException {
		final EmbeddedFilesResolver embeddedFilesResolver = new EmbeddedFilesResolver(
			OBJECT_MAPPER.readTree(CONNECTOR_2_FILE),
			CONNECTOR_2_DIRECTORY,
			Collections.emptySet()
		);

		assertThrows(IllegalStateException.class, () -> embeddedFilesResolver.process());
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testFindAbsoluteUriZipWindows() throws Exception {
		testFindAbsoluteUriZip("jar:file:///");
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void testFindAbsoluteUriZipLinux() throws Exception {
		testFindAbsoluteUriZip("jar:file://");
	}

	/**
	 * Test the {@link EmbeddedFilesResolver#processFile(String, Path)} method.
	 * @param schemePrefix The scheme prefix for the URI.
	 * @throws Exception If an error occurs during the test.
	 */
	private void testFindAbsoluteUriZip(final String schemePrefix) throws Exception {
		final EmbeddedFilesResolver embeddedFilesResolver = new EmbeddedFilesResolver(
			OBJECT_MAPPER.readTree(CONNECTOR_2_FILE),
			CONNECTOR_2_DIRECTORY,
			Collections.emptySet()
		);

		final String absolutePath = Paths.get("src/test/resources").toAbsolutePath().toString().replace("\\", "/");
		final String uriStrExpected = String.format(
			"%s%s/test-files/connector/zippedConnector/connectors/connectors.zip!/hardware/DiskPart/diskPart.awk",
			schemePrefix,
			absolutePath
		);
		final String connectorDirUriStr = String.format(
			"%s%s/test-files/connector/zippedConnector/connectors/connectors.zip!/hardware/DiskPart",
			schemePrefix,
			absolutePath
		);

		final URI connectorDirUri = URI.create(connectorDirUriStr);
		final EmbeddedFile result = FileHelper.fileSystemTask(
			connectorDirUri,
			Collections.emptyMap(),
			() -> embeddedFilesResolver.processFile("diskPart.awk", Paths.get(connectorDirUri))
		);

		assertEquals(uriStrExpected, result.toString());
	}

	@Test
	void testFindAbsoluteUri() throws IOException {
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
			"connectors",
			"hardware",
			"MIB2"
		);
		assertEquals(
			new File("src/test/resources/test-files/connector/zippedConnector/connectors/hardware/MIB2/exit.txt").toURI(),
			embeddedFilesResolver.processFile("exit.txt", yamlTestPath)
		);
	}
}
