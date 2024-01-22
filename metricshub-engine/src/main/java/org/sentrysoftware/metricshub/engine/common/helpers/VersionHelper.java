package org.sentrysoftware.metricshub.engine.common.helpers;

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.ENGINE_PROPERTIES_FILE_NAME;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.ENGINE_VERSION_PROPERTY;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

@Slf4j
public class VersionHelper {

	private static final String VERSION_REGEX_DELIMITER = "\\.";

	private VersionHelper() {}

	/**
	 * Returns the project version
	 *
	 * @return The current version of MetricsHub engine module (which is equal to the project version)
	 */
	public static String getClassVersion() {
		final ClassPathResource resource = new ClassPathResource(ENGINE_PROPERTIES_FILE_NAME);
		final Properties properties = new Properties();
		try {
			final InputStream inputStream = resource.getInputStream();
			properties.load(inputStream);
			inputStream.close();
		} catch (IOException ioException) {
			log.error(ioException.getMessage(), ioException);
		}
		return properties.getProperty(ENGINE_VERSION_PROPERTY);
	}

	/**
	 * Check if the given <code>version</code> is less than the given <code>otherVersion</code>
	 *
	 * @param version
	 * @param otherVersion
	 * @return <code>true</code> if <code>version</code> is less than
	 * <code>otherVersion</code> otherwise <code>false</code>
	 */
	public static boolean isVersionLessThanOtherVersion(String version, String otherVersion) {
		return compareVersions(version, otherVersion) < 0;
	}

	/**
	 * Compare the given versions
	 *
	 * @param version1
	 * @param version2
	 * @return 0 if both versions are the equal, -1 if <code>version1</code> is less
	 * than <code>version2</code>
	 */
	public static int compareVersions(String version1, String version2) {
		version1 = normalizeVersion(version1);
		version2 = normalizeVersion(version2);

		final String[] version1Splits = version1.split(VERSION_REGEX_DELIMITER);
		final String[] version2Splits = version2.split(VERSION_REGEX_DELIMITER);
		final int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

		for (int i = 0; i < maxLengthOfVersionSplits; i++) {
			final Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
			final Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
			final int compare = v1.compareTo(v2);

			if (compare != 0) {
				return compare;
			}
		}

		return 0;
	}

	/**
	 * Normalize the given version. Keep only digits and if the version is null or
	 * empty return "0".
	 *
	 * @param version
	 * @return normalized version
	 */
	private static String normalizeVersion(final String version) {
		if (version == null) {
			return "0";
		}

		final String normalized = version.replaceAll("[^\\d\\.]", "");

		return normalized.trim().isEmpty() ? "0" : normalized;
	}
}
