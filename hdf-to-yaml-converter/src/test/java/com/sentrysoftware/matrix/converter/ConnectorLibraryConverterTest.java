package com.sentrysoftware.matrix.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConnectorLibraryConverterTest {

	private static final String HDF_DIRECTORY = "src/test/resources/hdf";

	@TempDir
	private Path tempDir;

	@Test
	void testProcess() throws IOException {
		final ConnectorLibraryConverter processor = new ConnectorLibraryConverter(Path.of(HDF_DIRECTORY), tempDir);
		processor.process();
		final File file = tempDir.resolve("DellOpenManage.yaml").toFile();
		assertTrue(file.exists());
	}

}
