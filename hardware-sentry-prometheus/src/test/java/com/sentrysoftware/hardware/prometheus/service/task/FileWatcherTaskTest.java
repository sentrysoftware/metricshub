package com.sentrysoftware.hardware.prometheus.service.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;

import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileWatcherTaskTest {

	@TempDir
	static Path tempDir;

	@Test
	void testRun() throws IOException, InterruptedException {

		// This test starts the watcher task then create a file in the temporary Junit test directory
		// then verifies that the watcher task performed the callback correctly on the create event

		final Path testFilePath = tempDir.resolve("testFile.tmp");
		final File testFile = testFilePath.toFile();

		final StringBuilder stringBuilder = new StringBuilder();

		// Start the file watcher
		FileWatcherTask.builder()
			.file(testFile)
			.filter(event -> {
				return event.kind() != null && event.context() != null 
						&& StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())
						&& testFile.getName().equals(event.context().toString());
			})
			.onChange(() -> stringBuilder.append("File updated"))
			.build()
			.start();

		Awaitility
			.await()
			.atMost(Durations.FIVE_HUNDRED_MILLISECONDS)
			.until(() -> testFile.createNewFile());

		Awaitility
			.await()
			.atMost(Durations.FIVE_HUNDRED_MILLISECONDS)
			.untilAsserted(() -> assertEquals("File updated", stringBuilder.toString()));

	}

}
