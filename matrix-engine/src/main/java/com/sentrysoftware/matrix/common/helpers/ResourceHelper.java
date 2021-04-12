package com.sentrysoftware.matrix.common.helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.utils.Assert;

public class ResourceHelper {

	private ResourceHelper() {

	}

	/**
	 * Get resource file content as String for the given class.
	 * 
	 * @param path  path of the file in the resources directory
	 * @param clazz class used to load the resource as stream
	 * @return Resource file as {@link String}
	 */
	public static String getResourceAsString(final String path, final Class<?> clazz) {

		Assert.isTrue(path != null && !path.isEmpty(), "path cannot be null or empty");
		Assert.notNull(clazz, "clazz cannot be null");

		try (
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(clazz.getResourceAsStream(path)))) {

			return reader.lines().collect(Collectors.joining("\n"));

		} catch (Exception e) {
			Assert.state(false, () -> String.format("Cannot load resource file '%s' for class '%s'. Message: %s",
					path, clazz.getName(), e.getMessage()));
		}

		return "";
	}
}
