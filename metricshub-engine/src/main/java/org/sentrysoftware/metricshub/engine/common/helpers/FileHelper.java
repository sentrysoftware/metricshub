package org.sentrysoftware.metricshub.engine.common.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Utility class for common file-related operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileHelper {

	/**
	 * Returns the time of last modification of the specified Path in milliseconds since EPOCH.
	 *
	 * @param path The path to the file.
	 * @return Milliseconds since EPOCH, or 0 (zero) if the file does not exist.
	 * @throws IllegalArgumentException If the specified path is null.
	 */
	public static long getLastModifiedTime(@NonNull Path path) {
		try {
			return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
		} catch (IOException e) {
			return 0;
		}
	}
}
