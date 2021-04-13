package com.sentrysoftware.matrix.connector.helper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.FileUtils;
import org.springframework.util.Assert;

/**
 * Class with various utilities that will help developing this Maven plugin
 */
public class PluginHelper {

	private PluginHelper() {	}

	/**
	 * Make sure the specified directory exists and create it if it doesn't.
	 * <p>
	 * @param dir Directory to be tested
	 * @throws MojoExecutionException if specified directory cannot be created
	 * @throws IllegalArgumentException if specified directory is null
	 */
	public static void validateOutputDirectory(File dir) throws MojoExecutionException {

		// Sanity check
		Assert.notNull(dir, "outputDirectory is not defined");

		// Do we need to create it?
		if (!dir.exists() && !dir.mkdirs()) {
			// Create it!
			throw new MojoExecutionException("Could not create outputDirectory: " + dir.getAbsolutePath());
		}

	}

	/**
	 * Get the list of files in the specified directory and matching the specified
	 * criteria (includes and excludes), sorted lexicographically
	 * <p>
	 *
	 * @param sourceDirectory The directory to search for files
	 * @param includes List of criteria of files to include (e.g. *.hdfs)
	 * @param excludes List of criteria of files to exclude (e.g. *.bak)
	 * @return List of matching {@link File} objects
	 * @throws MojoExecutionException 
	 */
	public static List<File> getFileList(final File sourceDirectory, List<String> includes, List<String> excludes)
			throws MojoExecutionException {

		// Sanity check to avoid NPE
		if (includes == null) {
			includes = Collections.emptyList();
		}
		if (excludes == null) {
			excludes = Collections.emptyList();
		}

		// Get the list
		final List<File> fileList;
		try {
			fileList = FileUtils.getFiles(sourceDirectory, String.join(",", includes), String.join(",", excludes));
		} catch (IOException e) {
			throw new MojoExecutionException("Error while listing the files from " + sourceDirectory, e);
		}

		// Sort it
		Collections.sort(fileList);

		return fileList;

	}

	/**
	 * Returns the path of the specified file, relative to the project basedir.
	 * <p>
	 * @param file The file like d:/dev/hardware-connectors/src/main/hdf/my-connector.hdfs
	 * @return The relative path to basedir, like src/main/hdf/my-connector.hdfs
	 */
	public static String relativizeToProject(File file, MavenProject project) {
		return project.getBasedir().toURI().relativize(file.toURI()).getPath();
	}

}
