package com.sentrysoftware.metricshub.agent.service.task;

import com.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.function.Predicate;
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

	private long await;

	@NonNull
	private String checksum;

	@Override
	public void run() {
		try {
			// Start the watcher
			watchFile();
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}

			log.error("The watcher on file could not be started: {}. Error: {}.", file.getAbsolutePath(), e.getMessage());
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
		try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
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
					key
						.pollEvents()
						.stream()
						.filter(event -> filter.test(event))
						.forEach(event -> {
							final String newChecksum = ConfigHelper.calculateMD5Checksum(file);
							if (checksum != null && !checksum.equals(newChecksum)) {
								checksum = newChecksum;
								performAction();
							}
						});

					// The key is no more valid and must be reset
					if (!key.reset()) {
						break;
					}
				}
			}
		}
	}

	/**
	 * Perform the onChange action and await before running the action if requested
	 */
	private void performAction() {
		try {
			// Should wait before performing the action? the event could trigger but the
			// file is in intermediate state, an example of this issue is the Jackson exception:
			// MismatchedInputException: No content to map due to end-of-input
			if (await > 0) {
				sleep(await);
			}
			onChange.run();
		} catch (InterruptedException e) {
			log.info("FileWatcherTask onChange - Received Interrupted Exception: {}.", e.getMessage());
			interrupt();
		} catch (Exception e) {
			log.info("FileWatcherTask onChange - Error detected: {}.", e.getMessage());
		}
	}
}
