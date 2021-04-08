package com.sentrysoftware.matrix.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.utils.TestHelper;

import junit.framework.Assert;

class ConnectorCompileMojoTest {

	private static final String MS_HW_DELL_OPEN_MANAGE_CONNECTOR = "MS_HW_DellOpenManage.connector";
	private static final String MS_HW_DELL_OPEN_MANAGE_HDFS_PATH = "/hdf/MS_HW_DellOpenManage.hdfs";
	private static final String MS_HW_DELL_STORAGE_MANAGER_HDFS_PATH = "/hdf/MS_HW_DellStorageManager.hdfs";
	private static final String MS_HW_DELL_STORAGE_MANAGER_HDFS = "MS_HW_DellStorageManager.hdfs";
	private static final String MS_HW_DELL_OPEN_MANAGE_HDFS = "MS_HW_DellOpenManage.hdfs";
	private static final String COMPILE_HDFS_FILES = "compileHdfsFiles";

	private static Log logger;
	private static ConnectorCompileMojo connectorCompileMojo;

	@TempDir
	File testDirectory;

	@BeforeAll
	public static void setUp() {
		logger = new SystemStreamLog();
		connectorCompileMojo = new ConnectorCompileMojo();
	}

	@Test
	void testCreateOutputDirectoryIfNeeded()
			throws IllegalAccessException, InvocationTargetException {

		try {
			TestHelper.invokeMethod(ConnectorCompileMojo.class, "createOutputDirectoryIfNeeded",
					Arrays.asList(Log.class, File.class), Arrays.asList(logger, testDirectory), connectorCompileMojo);

		} catch (Exception e) {
			Assert.fail("Unexpected Exception");
		}
	}

	@Test
	void testSerialize() throws IllegalAccessException, InvocationTargetException, IOException, ClassNotFoundException {

		final String expectedFilename = "MyConnector.connector";

		Connector expected = Connector.builder().compiledFilename(expectedFilename).build();
		TestHelper.invokeMethod(ConnectorCompileMojo.class, "serialize",
				Arrays.asList(Log.class, File.class, Connector.class), Arrays.asList(logger, testDirectory, expected),
				connectorCompileMojo);

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
	void testParse() throws Exception {

		// Create a temporary file.
		// This is guaranteed to be deleted after the test finishes.
		final Path tempFile = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_OPEN_MANAGE_HDFS));

		// Write HDFs content to it.
		Files.writeString(tempFile,
				ResourceHelper.getResourceAsString(MS_HW_DELL_OPEN_MANAGE_HDFS_PATH, this.getClass()));

		final Optional<Connector> connectorOptional = TestHelper.invokeMethod(ConnectorCompileMojo.class, "parse",
				Collections.singletonList(File.class), Collections.singletonList(new File(tempFile.toString())),
				connectorCompileMojo);

		assertTrue(connectorOptional.isPresent());
		assertEquals(MS_HW_DELL_OPEN_MANAGE_CONNECTOR, connectorOptional.get().getCompiledFilename());

	}

	@Test
	void testCompileHdfsFilesIllegalStateException() throws Exception {

		final File directory = new File(testDirectory.getAbsolutePath() + "/" + UUID.randomUUID().toString());
		directory.mkdir();

		final Path tempFile = Files.createFile(testDirectory.toPath().resolve("NotDirectory.text"));

		{

			try {
				TestHelper.invokeMethod(ConnectorCompileMojo.class, COMPILE_HDFS_FILES,
						Arrays.asList(Log.class, File.class, File.class),
						Arrays.asList(logger, directory, new File(tempFile.toString())), connectorCompileMojo);
			} catch (InvocationTargetException e) {
				assertTrue(e.getCause() instanceof IllegalStateException);
			}

		}

		{

			try {
				TestHelper.invokeMethod(ConnectorCompileMojo.class, COMPILE_HDFS_FILES,
						Arrays.asList(Log.class, File.class, File.class),
						Arrays.asList(logger, new File(tempFile.toString()), directory), connectorCompileMojo);
			} catch (InvocationTargetException e) {
				assertTrue(e.getCause() instanceof IllegalStateException);
			}

		}
	}

	@Test
	void testCompileHdfsFiles() throws Exception {

		final File outputDirectory = new File(testDirectory.getAbsolutePath() + "/" + UUID.randomUUID().toString());
		outputDirectory.mkdir();

		final Path tempFile1 = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_OPEN_MANAGE_HDFS));

		Files.writeString(tempFile1,
				ResourceHelper.getResourceAsString(MS_HW_DELL_OPEN_MANAGE_HDFS_PATH, this.getClass()));

		final Path tempFile2 = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_STORAGE_MANAGER_HDFS));

		Files.writeString(tempFile2,
				ResourceHelper.getResourceAsString(MS_HW_DELL_STORAGE_MANAGER_HDFS_PATH, this.getClass()));

		final int count = TestHelper.invokeMethod(ConnectorCompileMojo.class, COMPILE_HDFS_FILES,
				Arrays.asList(Log.class, File.class, File.class), Arrays.asList(logger, testDirectory, outputDirectory),
				connectorCompileMojo);

		assertEquals(2, count);

	}

	@Test
	void testExecute() throws Exception {

		final File outputDirectory = new File(testDirectory.getAbsolutePath() + "/" + UUID.randomUUID().toString());
		outputDirectory.mkdir();

		final Path tempFile1 = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_OPEN_MANAGE_HDFS));

		Files.writeString(tempFile1,
				ResourceHelper.getResourceAsString(MS_HW_DELL_OPEN_MANAGE_HDFS_PATH, this.getClass()));

		final Path tempFile2 = Files.createFile(testDirectory.toPath().resolve(MS_HW_DELL_STORAGE_MANAGER_HDFS));

		Files.writeString(tempFile2,
				ResourceHelper.getResourceAsString(MS_HW_DELL_STORAGE_MANAGER_HDFS_PATH, this.getClass()));

		final MavenProject project = new MavenProject();
		project.setVersion("1.0");
		project.setDescription("Hardware Connector Library");

		TestHelper.setField(connectorCompileMojo, "logger", logger);
		TestHelper.setField(connectorCompileMojo, "connectorDirectory", testDirectory);
		TestHelper.setField(connectorCompileMojo, "outputDirectory", outputDirectory);
		TestHelper.setField(connectorCompileMojo, "project", project);

		connectorCompileMojo.execute();

		final String[] fileNames = outputDirectory.list();

		assertEquals(2, fileNames.length);
	}
}
