package com.sentrysoftware.matrix.common.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceHelper {

	/**
	 * Get resource file content as String for the given class.
	 * 
	 * @param path  path of the file in the resources directory
	 * @param clazz class used to load the resource as stream
	 * @return Resource file as {@link String}
	 */
	public static String getResourceAsString(final String path, @NonNull final Class<?> clazz) {

		Assert.isTrue(path != null && !path.isEmpty(), "path cannot be null or empty");

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(clazz.getResourceAsStream(path)))) {

			return reader.lines().collect(Collectors.joining("\n"));

		} catch (Exception e) {
			Assert.state(false, () -> String.format("Cannot load resource file '%s' for class '%s'. Message: %s", path,
					clazz.getName(), e.getMessage()));
		}

		return "";
	}

	/**
	 * Find the source file for the given sourceClass
	 * 
	 * @param sourceClass The sourceClass we wish to get its file, this class could
	 *                    be located under a jar or for example under
	 *                    <em>\target\classes</em>
	 * @return File instance
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static File findSource(@NonNull final Class<?> sourceClass) throws IOException, URISyntaxException {
		ProtectionDomain domain = sourceClass.getProtectionDomain();
		CodeSource codeSource = domain != null ? domain.getCodeSource() : null;
		URL location = codeSource != null ? codeSource.getLocation() : null;
		File source = location != null ? findSource(location) : null;
		if (source != null && source.exists()) {
			return source.getAbsoluteFile();
		}

		return null;
	}

	/**
	 * Find source file for the given URL location
	 * 
	 * @param location {@link URL} instance
	 * @return File instance
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	static File findSource(@NonNull final URL location) throws IOException, URISyntaxException {
		URLConnection connection = location.openConnection();
		if (connection instanceof JarURLConnection jarUrlConnection) {
			return getRootJarFile(jarUrlConnection.getJarFile());
		}
		return new File(location.toURI());
	}

	/**
	 * Since jars can be embedded in other jars, this method will get the root jar
	 * file instance
	 * 
	 * @param jarFile
	 * @return File instance
	 */
	static File getRootJarFile(@NonNull final JarFile jarFile) {
		String name = jarFile.getName();
		int separator = name.indexOf("!/");
		if (separator > 0) {
			name = name.substring(0, separator);
		}
		return new File(name);
	}
}
