package org.sentrysoftware.metricshub.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.connector.model.common.EmbeddedFile;

class EmbeddedFileHelperTest {

	private static final String FILE_PATH = new File("src/test/resources/test-files/embedded/connector1/header.txt")
		.getAbsolutePath()
		.replace("\\", "/");
	private static final String EMBEDDED_FILE_HEADER_REF = String.format("${file::%s}", FILE_PATH);

	@Test
	void testFindEmbeddedFiles() throws IOException {
		final Map<String, EmbeddedFile> result = EmbeddedFileHelper.findEmbeddedFiles(
			EMBEDDED_FILE_HEADER_REF + " " + EMBEDDED_FILE_HEADER_REF
		);

		final Map<String, EmbeddedFile> expected = Map.of(
			EMBEDDED_FILE_HEADER_REF,
			new EmbeddedFile(
				Files.readAllLines(Path.of(FILE_PATH)).stream().collect(Collectors.joining("\n")),
				"txt",
				EMBEDDED_FILE_HEADER_REF
			)
		);

		assertEquals(expected, result);
	}

	@Test
	void testFindEmbeddedFilesErrorOnFileNotFound() throws IOException {
		assertThrows(IOException.class, () -> EmbeddedFileHelper.findEmbeddedFiles("${file::notFound}"));
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
	void testParseEmbeddedFileFailsOnFileNotFound() {
		assertThrows(IllegalArgumentException.class, () -> EmbeddedFileHelper.parseEmbeddedFile(URI.create("notpresent")));
	}
}
