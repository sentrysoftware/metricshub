package com.sentrysoftware.matrix.common.helpers;

import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import lombok.extern.slf4j.Slf4j;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IMPLEMENTATION_TITLE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IMPLEMENTATION_VERSION;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MANIFEST_FILE_PATH;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MATRIX_ENGINE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.VERSION_REGEX_DELIMITER;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.VERSION_REGEX_REPLACEMENT;

@Slf4j
public class VersionHelper {

	private VersionHelper() {}

	/**
	 * Returns the JAR implementation version for the specified class.
	 * @param pClass Any {@link Class} in the JAR we want to know the version.
	 * @return The version of the JAR (extracted from the MANIFEST) or <code>null</code> if not available.
	 */
	public static String getClassVersion(Class<?> pClass) {
		if (null == pClass) {
			return null;
		}
		String version = null;

		try {

			String path = pClass.getProtectionDomain().getCodeSource().getLocation().getPath();
			URL vClassUrl = ClassLoader.getSystemResource(path);

			if (null == vClassUrl) {
				return null;
			}

			String vClassUri = vClassUrl.toString();
			if (!vClassUri.startsWith("jar:")) {
				return null;
			}

			int vSeparatorIndex = vClassUri.lastIndexOf('!');
			if (vSeparatorIndex <= 0) {
				return null;
			}

			String vManifestUri = vClassUri.substring(0, vSeparatorIndex + 2) + MANIFEST_FILE_PATH;
			URL vUrl = new URL(vManifestUri);
			Manifest manifest = new Manifest(vUrl.openStream());
			Attributes attribute = manifest.getMainAttributes();
			if (attribute != null && MATRIX_ENGINE.equalsIgnoreCase(attribute.getValue(IMPLEMENTATION_TITLE))) {
				version = attribute.getValue(IMPLEMENTATION_VERSION);
			}

		} catch (Throwable vEx) {
			if (log.isWarnEnabled()) {
				log.warn("getClassVersion", vEx, "Cannot retrieve JAR manifest");
			}
			return null;

		}

		return version;
	}

	/**
	 * Check if the given <code>version</code> is less than the given <code>otherVersion</code>
	 *
	 * @param version
	 * @param otherVersion
	 * @return <code>true</code> if <code>version</code> is less than
	 *         <code>otherVersion</code> otherwise <code>false</code>
	 * 
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
	 *         than <code>version2</code>
	 * 
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
	 * 
	 */
	private static String normalizeVersion(final String version) {

		if (version == null) {
			return "0";
		}

		final String normalized = version.replaceAll(VERSION_REGEX_REPLACEMENT, "");

		return normalized.trim().isEmpty() ? "0" : normalized;
	}
}
