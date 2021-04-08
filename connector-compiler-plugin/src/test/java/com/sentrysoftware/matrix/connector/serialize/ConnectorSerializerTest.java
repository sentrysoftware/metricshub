package com.sentrysoftware.matrix.connector.serialize;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sentrysoftware.matrix.connector.model.Connector;

class ConnectorSerializerTest {

	@TempDir
	File outputDirectory;

	@Test
	void testSerialize() throws IOException, ClassNotFoundException {

		final String expectedFilename = "MS_HW_MyConnector.connector";
		final Connector expected = Connector.builder().compiledFilename(expectedFilename).build();

		ConnectorSerializer.serialize(outputDirectory.getAbsolutePath(), expected);

		final String[] fileNames = outputDirectory.list();

		assertEquals(1, fileNames.length);
		assertEquals(expectedFilename, fileNames[0]);

		// Integrity check
		final File[] serializedConnectors = outputDirectory.listFiles((file, name) -> name.endsWith(".connector"));

		assertEquals(1, serializedConnectors.length);
		try (final FileInputStream is = new FileInputStream(serializedConnectors[0]);
				final ObjectInputStream in = new ObjectInputStream(is);) {
			assertEquals(expected, (Connector) in.readObject());
		}
	}

}
