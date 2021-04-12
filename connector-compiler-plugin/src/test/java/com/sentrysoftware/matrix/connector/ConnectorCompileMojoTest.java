package com.sentrysoftware.matrix.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.connector.helper.PluginHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.serialize.ConnectorSerializer;
import com.sentrysoftware.matrix.connector.utils.TestHelper;

class ConnectorCompileMojoTest {

	private static final String CHECK_OPTIONAL_CONNECTOR = "checkOptionalConnector";
	private static final String MY_CONNECTOR_CONNECTOR = "MyConnector.connector";
	private static final String EXPECTED_EXCEPTION = "Expected exception";
	private static final String SERIALIZE = "serialize";
	private static final String MY_CONNECTOR_HDFS = "/MyConnector.hdfs";
	private static final String PROJECT_DESCRIPTION = "Hardware Connector Library";
	private static final String PROJECT_VERSION = "1.0";
	private static final String MS_HW_DELL_OPEN_MANAGE_CONNECTOR = "MS_HW_DellOpenManage.connector";
	private static final String MS_HW_DELL_STORAGE_MANAGER_CONNECTOR = "MS_HW_DellStorageManager.connector";
	private static final String MS_HW_DELL_OPEN_MANAGE_HDFS_PATH = "/hdf/MS_HW_DellOpenManage.hdfs";
	private static final String MS_HW_DELL_STORAGE_MANAGER_HDFS_PATH = "/hdf/MS_HW_DellStorageManager.hdfs";
	private static final String MS_HW_DELL_STORAGE_MANAGER_HDFS = "MS_HW_DellStorageManager.hdfs";
	private static final String MS_HW_DELL_OPEN_MANAGE_HDFS = "MS_HW_DellOpenManage.hdfs";
	private static final String COMPILE_HDFS_FILES = "compileHdfsFiles";

	private static Log logger = new SystemStreamLog();

	private static ConnectorCompileMojo connectorCompileMojo = new ConnectorCompileMojo();

	@TempDir
	File testDirectory;

	@Test
	void testCheckOptionalConnector() throws Exception {
		
		try {
			TestHelper.invokeMethod(connectorCompileMojo, CHECK_OPTIONAL_CONNECTOR, Arrays.asList(Log.class, String.class, Optional.class), 
					Arrays.asList(logger, MY_CONNECTOR_CONNECTOR, Optional.empty()));
			fail(EXPECTED_EXCEPTION);
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof MojoExecutionException);
		}
		
		try {
			TestHelper.invokeMethod(connectorCompileMojo, CHECK_OPTIONAL_CONNECTOR, Arrays.asList(Log.class, String.class, Optional.class), 
					Arrays.asList(logger, MY_CONNECTOR_CONNECTOR, Optional.of(Connector.builder().build())));
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		
	}

	@Test
	void testSerialize() throws Exception {

		final String expectedFilename = MY_CONNECTOR_CONNECTOR;

		Connector expected = Connector.builder().compiledFilename(expectedFilename).build();
		TestHelper.invokeMethod(connectorCompileMojo, SERIALIZE,
				Arrays.asList(Log.class, File.class, String.class, Connector.class),
				Arrays.asList(logger, testDirectory, testDirectory.getPath() + MY_CONNECTOR_HDFS, expected));

		final String[] fileNames = testDirectory.list();

		assertEquals(1, fileNames.length);
		assertEquals(expectedFilename, fileNames[0]);

		// Integrity check
		final File[] serializedConnectors = testDirectory.listFiles((file, name) -> name.endsWith(".connector"));

		assertEquals(1, serializedConnectors.length);
		try (final FileInputStream is = new FileInputStream(serializedConnectors[0]);
				final ObjectInputStream in = new ObjectInputStream(is);) {
			assertEquals(expected, (Connector) in.readObject());
		}
	}

	@Test
	void testSerializeMojoExecutionException() throws Exception {

		final String expectedFilename = MY_CONNECTOR_CONNECTOR;

		final Connector connector = Connector.builder().compiledFilename(expectedFilename).build();
		final String connectorPath = testDirectory.getPath() + MY_CONNECTOR_HDFS;
		try (MockedStatic<ConnectorSerializer> connectorSerializer = Mockito.mockStatic(ConnectorSerializer.class)) {
			connectorSerializer.when(() -> ConnectorSerializer.serialize(eq(testDirectory.getAbsolutePath()), eq(connector))).thenThrow(new IOException("exception from test"));
			
			try {
				TestHelper.invokeMethod(connectorCompileMojo, SERIALIZE,
						Arrays.asList(Log.class, File.class, String.class, Connector.class),
						Arrays.asList(logger, testDirectory, connectorPath, connector));
				fail(EXPECTED_EXCEPTION);
			} catch (Exception e) {
				assertTrue(e.getCause() instanceof MojoExecutionException);
			}

		}
	}
	
	@Test
	void testCompileHdfsFilesIllegalStateException() throws Exception {

		final File directory = new File(testDirectory.getAbsolutePath() + "/" + UUID.randomUUID().toString());
		directory.mkdir();

		final Path tempFile = Files.createFile(testDirectory.toPath().resolve("NotDirectory.text"));

		{
			MavenProject project = new MavenProject();
			project.setVersion(PROJECT_VERSION);
			project.setDescription(PROJECT_DESCRIPTION);
			project.setFile(testDirectory);

			try {
				TestHelper.invokeMethod(connectorCompileMojo, COMPILE_HDFS_FILES,
						Arrays.asList(Log.class, File.class, File.class, MavenProject.class),
						Arrays.asList(logger, directory, new File(tempFile.toString()), project));
				fail(EXPECTED_EXCEPTION);
			} catch (InvocationTargetException e) {
				assertTrue(e.getCause() instanceof IllegalStateException);
			}

		}

		{

			final MavenProject project = new MavenProject();
			project.setVersion(PROJECT_VERSION);
			project.setDescription(PROJECT_DESCRIPTION);
			project.setFile(testDirectory);

			try {
				TestHelper.invokeMethod(connectorCompileMojo, COMPILE_HDFS_FILES,
						Arrays.asList(Log.class, File.class, File.class, MavenProject.class),
						Arrays.asList(logger, new File(tempFile.toString()), directory, project));
				fail(EXPECTED_EXCEPTION);
			} catch (InvocationTargetException e) {
				assertTrue(e.getCause() instanceof IllegalStateException);
			}

		}
	}

	@Test
	void testCompileHdfsFilesNoHdfs() throws Exception {
	
		final MavenProject project = new MavenProject();
		project.setVersion(PROJECT_VERSION);
		project.setDescription(PROJECT_DESCRIPTION);
		project.setFile(testDirectory);

		final File outputDirectory = new File(testDirectory.getAbsolutePath() + "/" + UUID.randomUUID().toString());
		outputDirectory.mkdir();

		final Path tempFile = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_OPEN_MANAGE_HDFS));

		Files.writeString(tempFile,
				ResourceHelper.getResourceAsString(MS_HW_DELL_OPEN_MANAGE_HDFS_PATH, this.getClass()));

		try (MockedStatic<PluginHelper> pluginHelper = Mockito.mockStatic(PluginHelper.class)) {
			pluginHelper.when(() -> PluginHelper.getFileList(eq(testDirectory), eq(Arrays.asList("*.hdfs")), eq(null))).thenReturn(null);
			try {
				TestHelper.invokeMethod(connectorCompileMojo, COMPILE_HDFS_FILES,
						Arrays.asList(Log.class, File.class, File.class, MavenProject.class),
						Arrays.asList(logger, testDirectory, outputDirectory, project));
				fail(EXPECTED_EXCEPTION);
			} catch (InvocationTargetException e) {
				assertTrue(e.getCause() instanceof IllegalStateException);
			}
		  }
	}
	@Test
	void testCompileHdfsFiles() throws Exception {

		final MavenProject project = new MavenProject();
		project.setVersion(PROJECT_VERSION);
		project.setDescription(PROJECT_DESCRIPTION);
		project.setFile(testDirectory);

		final File outputDirectory = new File(testDirectory.getAbsolutePath() + "/" + UUID.randomUUID().toString());
		outputDirectory.mkdir();

		final Path tempFile1 = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_OPEN_MANAGE_HDFS));

		Files.writeString(tempFile1,
				ResourceHelper.getResourceAsString(MS_HW_DELL_OPEN_MANAGE_HDFS_PATH, this.getClass()));

		final Path tempFile2 = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_STORAGE_MANAGER_HDFS));

		Files.writeString(tempFile2,
				ResourceHelper.getResourceAsString(MS_HW_DELL_STORAGE_MANAGER_HDFS_PATH, this.getClass()));

		final int count = TestHelper.invokeMethod(connectorCompileMojo, COMPILE_HDFS_FILES,
				Arrays.asList(Log.class, File.class, File.class, MavenProject.class),
				Arrays.asList(logger, testDirectory, outputDirectory, project));

		assertEquals(2, count);

	}

	@Test
	void testExecute() throws Exception {

		final MavenProject project = new MavenProject();
		project.setVersion(PROJECT_VERSION);
		project.setDescription(PROJECT_DESCRIPTION);
		project.setFile(testDirectory);
		
		final File outputDirectory = new File(testDirectory.getAbsolutePath() + "/" + UUID.randomUUID().toString());
		outputDirectory.mkdir();

		final Path tempFile1 = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_OPEN_MANAGE_HDFS));

		Files.writeString(tempFile1,
				ResourceHelper.getResourceAsString(MS_HW_DELL_OPEN_MANAGE_HDFS_PATH, this.getClass()));

		final Path tempFile2 = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_STORAGE_MANAGER_HDFS));

		Files.writeString(tempFile2,
				ResourceHelper.getResourceAsString(MS_HW_DELL_STORAGE_MANAGER_HDFS_PATH, this.getClass()));

		TestHelper.setField(connectorCompileMojo, "logger", logger);
		TestHelper.setField(connectorCompileMojo, "connectorDirectory", testDirectory);
		TestHelper.setField(connectorCompileMojo, "outputDirectory", outputDirectory);
		TestHelper.setField(connectorCompileMojo, "project", project);

		connectorCompileMojo.execute();

		final String[] fileNames = outputDirectory.list();

		assertEquals(2, fileNames.length);
		assertEquals(Stream.of(MS_HW_DELL_OPEN_MANAGE_CONNECTOR, MS_HW_DELL_STORAGE_MANAGER_CONNECTOR)
				.collect(Collectors.toSet()), Arrays.stream(fileNames).collect(Collectors.toSet()));
	}
}
