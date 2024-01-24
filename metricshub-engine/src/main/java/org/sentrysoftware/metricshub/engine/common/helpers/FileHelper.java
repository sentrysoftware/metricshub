package org.sentrysoftware.metricshub.engine.common.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileHelper {

	/**
	 * Returns the time of last modification of specified Path in milliseconds since
	 * EPOCH.
	 *
	 * @param path Path to the file
	 * @return Milliseconds since EPOCH, or 0 (zero) if file does not exist
	 * @throws IllegalArgumentException if specified path is null
	 */
	public static long getLastModifiedTime(@NonNull Path path) {
		try {
			return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
		} catch (IOException e) {
			return 0;
		}
	}
}
