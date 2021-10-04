package com.sentrysoftware.hardware.prometheus.service.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.function.Predicate;

import com.sentrysoftware.hardware.prometheus.configuration.ConfigHelper;

import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class FileWatcherTask extends Thread {

	@NonNull
	private File file;
	@NonNull
	private Predicate<WatchEvent<?>> filter;
	@NonNull
	private Runnable onChange;

	@Override
	public void run() {
		try {
			// Start the watcher
			watchFile();
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}

			log.error("Could not start the watcher on file: {}. Error: {}", file.getAbsolutePath(),
					e.getMessage());
			log.debug("Error: ", e);
		}
	}

	/**
	 * Watch the current file, filter then events using the provided filter then apply the action
	 * 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	void watchFile() throws IOException, InterruptedException {

		try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {

			// Continuously try to watch the file through the watcher service
			while (!Thread.currentThread().isInterrupted()) {

				final Path path = ConfigHelper.getDirectoryPath(file);

				// Registers the file located by this path with a watch service and provide
				// the events for which this object should be registered
				path.register(
						watchService, 
						StandardWatchEventKinds.ENTRY_CREATE, 
						StandardWatchEventKinds.ENTRY_DELETE,
						StandardWatchEventKinds.ENTRY_MODIFY
				);

				WatchKey key;

				// Waiting if a key is present
				while ((key = watchService.take()) != null) {
					key.pollEvents()
						.stream()
						.filter(event -> filter.test(event))
						.forEach(event -> onChange.run());

					// The key is no more valid and must be reset
					if (!key.reset()) {
						break;
					}
				}
			}
		}
	}

}
