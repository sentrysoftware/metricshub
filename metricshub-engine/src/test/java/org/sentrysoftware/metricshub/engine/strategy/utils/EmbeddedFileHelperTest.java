package org.sentrysoftware.metricshub.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;

class EmbeddedFileHelperTest {

	private static final String FILE_REF_FORMAT = "${file::%s}";
	private static final Path FILE_PATH = Paths.get("src/test/resources/test-files/embedded/connector1/header.txt");
	private static final String EMBEDDED_FILE_HEADER_REF = String.format(FILE_REF_FORMAT, FILE_PATH.toUri().toString());

	@Test
	void testFindEmbeddedFiles() throws IOException {
		final Map<String, EmbeddedFile> result = EmbeddedFileHelper.findEmbeddedFiles(
			EMBEDDED_FILE_HEADER_REF + " " + EMBEDDED_FILE_HEADER_REF
		);

		final Map<String, EmbeddedFile> expected = Map.of(
			EMBEDDED_FILE_HEADER_REF,
			new EmbeddedFile(
				Files.readAllLines(FILE_PATH).stream().collect(Collectors.joining("\n")),
				"txt",
				EMBEDDED_FILE_HEADER_REF
			)
		);

		assertEquals(expected, result);
	}

	@Test
	void testFindEmbeddedFilesErrorOnFileNotFound() throws IOException {
		assertThrows(IOException.class, () -> EmbeddedFileHelper.findEmbeddedFiles("${file::file:///notFound}"));
	}

	@Test
	void testFindEmbeddedFilesEmptyMapOnUnmatchedRef() throws IOException {
		assertEquals(Collections.emptyMap(), EmbeddedFileHelper.findEmbeddedFiles("value"));
	}

	@Test
	void testFindExtensionNullOnNoExtension() {
		assertNull(EmbeddedFileHelper.findExtension("file"));
	}

	@Test
	void testFindEmbeddedFilesIllegalArgumentExceptionOnNullValue() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> EmbeddedFileHelper.findEmbeddedFiles(null));
	}

	@Test
	void testFindEmbeddedFilesInZip() throws IOException {
		final String absolutePath = Paths.get("src/test/resources").toAbsolutePath().toString().replace("\\", "/");
		final String uriStr = String.format(
			"jar:file:///%s/test-files/connector/zippedConnector/connectors/connectors.zip!/hardware/DiskPart/listVolume.txt",
			absolutePath
		);
		final String awkInZipRef = String.format(FILE_REF_FORMAT, uriStr);
		final Map<String, EmbeddedFile> result = EmbeddedFileHelper.findEmbeddedFiles(awkInZipRef);

		final Map<String, EmbeddedFile> expected = Map.of(
			awkInZipRef,
			new EmbeddedFile(
				"""
				list volume
				exit""",
				"txt",
				awkInZipRef
			)
		);

		assertEquals(expected, result);
	}
}
