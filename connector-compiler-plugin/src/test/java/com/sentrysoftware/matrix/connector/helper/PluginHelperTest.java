package com.sentrysoftware.matrix.connector.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;

class PluginHelperTest {

	public static final String MS_HW_DELL_OPEN_MANAGE_HDFS_PATH = "/hdf/MS_HW_DellOpenManage.hdfs";
	private static final String MS_HW_DELL_STORAGE_MANAGER_HDFS_PATH = "/hdf/MS_HW_DellStorageManager.hdfs";
	private static final String MS_HW_DELL_STORAGE_MANAGER_HDFS = "MS_HW_DellStorageManager.hdfs";
	public static final String MS_HW_DELL_OPEN_MANAGE_HDFS = "MS_HW_DellOpenManage.hdfs";

	@TempDir
	File testDirectory;

	@Test
	void testValidateOutputDirectory() throws IllegalAccessException, InvocationTargetException {

		assertThrows(IllegalArgumentException.class, () -> PluginHelper.validateOutputDirectory(null));

		try {
			PluginHelper.validateOutputDirectory(testDirectory);
		} catch (Exception e) {
			fail("Unexpected exception");
		}
	}

	@Test
	void testGetFileListMojoExecutionException() {
		try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class)) {
			fileUtils.when(() -> FileUtils.getFiles(eq(testDirectory), eq(""), eq("")))
					.thenThrow(new IOException("test error"));
			assertThrows(MojoExecutionException.class,
					() -> PluginHelper.getFileList(testDirectory, null, Collections.emptyList()));
		}
	}

	@Test
	void testGetFileList() throws Exception {
		final Path tempFile1 = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_OPEN_MANAGE_HDFS));

		Files.writeString(tempFile1,
				ResourceHelper.getResourceAsString(MS_HW_DELL_OPEN_MANAGE_HDFS_PATH, this.getClass()));

		final Path tempFile2 = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_STORAGE_MANAGER_HDFS));

		Files.writeString(tempFile2,
				ResourceHelper.getResourceAsString(MS_HW_DELL_STORAGE_MANAGER_HDFS_PATH, this.getClass()));

		assertEquals(
				Stream.of(MS_HW_DELL_OPEN_MANAGE_HDFS, MS_HW_DELL_STORAGE_MANAGER_HDFS).collect(Collectors.toSet()),
				PluginHelper.getFileList(testDirectory, Arrays.asList("*.hdfs"), null).stream().map(File::getName)
						.collect(Collectors.toSet()));

		assertEquals(Collections.emptySet(), PluginHelper.getFileList(testDirectory, null, Arrays.asList("*.hdfs"))
				.stream().map(File::getName).collect(Collectors.toSet()));

		assertEquals(Collections.emptySet(), PluginHelper.getFileList(testDirectory, null, null).stream()
				.map(File::getName).collect(Collectors.toSet()));

		assertEquals(Collections.emptySet(),
				PluginHelper.getFileList(testDirectory, Arrays.asList("*.hdfs"), Arrays.asList("*.hdfs")).stream()
						.map(File::getName).collect(Collectors.toSet()));

		List<File> files = PluginHelper.getFileList(testDirectory, Arrays.asList("*.hdfs"), null);
		assertEquals(MS_HW_DELL_OPEN_MANAGE_HDFS, files.get(0).getName());
		assertEquals(MS_HW_DELL_STORAGE_MANAGER_HDFS, files.get(1).getName());
	}

	@Test
	void testRelativizeToProject() throws Exception {
		final MavenProject project = new MavenProject();
		project.setFile(testDirectory);

		assertEquals(testDirectory.getName() + "/", PluginHelper.relativizeToProject(testDirectory, project));

		final Path tempFile = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_OPEN_MANAGE_HDFS));
		Files.writeString(tempFile,
				ResourceHelper.getResourceAsString(MS_HW_DELL_OPEN_MANAGE_HDFS_PATH, this.getClass()));
		assertEquals(testDirectory.getName() + "/" + MS_HW_DELL_OPEN_MANAGE_HDFS,
				PluginHelper.relativizeToProject(tempFile.toFile(), project));
	}
}
