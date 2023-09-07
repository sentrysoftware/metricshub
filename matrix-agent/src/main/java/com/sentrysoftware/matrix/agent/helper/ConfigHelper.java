package com.sentrysoftware.matrix.agent.helper;

import static com.sentrysoftware.matrix.agent.helper.AgentConstants.CONFIG_DIRECTORY_NAME;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.CONFIG_EXAMPLE_FILENAME;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.DEFAULT_CONFIG_FILENAME;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.FILE_PATH_FORMAT;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.LOG_DIRECTORY_NAME;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.PRODUCT_CODE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.GroupPrincipal;
import java.util.List;
import java.util.Optional;

import com.sentrysoftware.matrix.agent.security.PasswordEncrypt;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.security.SecurityManager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ConfigHelper {

	/**
	 * Get the default output directory for logging.<br>
	 * On Windows, if the LOCALAPPDATA path is not valid then the output directory will be located
	 * under the install directory.<br>
	 * On Linux, the output directory is located under the install directory.
	 * 
	 * @return {@link Path} instance
	 */
	public static Path getDefaultOutputDirectory() {
		if (LocalOsHandler.isWindows()) {
			final String localAppDataPath = System.getenv("LOCALAPPDATA");

			// Make sure the LOCALAPPDATA path is valid
			if (localAppDataPath != null && !localAppDataPath.isBlank()) {
				return createDirectories(Paths.get(localAppDataPath, PRODUCT_CODE, "logs"));
			}

		}

		return getSubDirectory(LOG_DIRECTORY_NAME, true);
	}

	/**
	 * Get a sub directory under the install directory
	 * 
	 * @param dir    the directory assumed under the product directory. E.g. logs
	 *               assumed under /opt/PRODUCT_CODE
	 * @param create indicate if we should create the sub directory or not
	 * @return The absolute path of the sub directory
	 */
	public static Path getSubDirectory(@NonNull final String dir, boolean create) {

		Path subDirectory = getSubPath(dir);
		if (!create) {
			return subDirectory;
		}

		return createDirectories(subDirectory);
	}


	/**
	 * Create directories of the given path
	 * 
	 * @param path Directories path
	 * @return {@link Path} instance
	 */
	public static Path createDirectories(final Path path) {
		try {
			return Files.createDirectories(path).toRealPath();
		} catch (IOException e) {
			throw new IllegalStateException("Could not create directory '" + path + "'.", e);
		}
	}

	/**
	 * Get the sub path under the home directory. E.g.
	 * <em>/opt/PRODUCT_CODE/lib/app/../config</em> on linux install
	 *
	 * @param subPath sub path to the directory or the file
	 * @return {@link Path} instance
	 */
	public static Path getSubPath(@NonNull final String subPath) {
		File me = getExecutableDir();

		final Path path = me.getAbsoluteFile().toPath();

		Path parentLibPath = path.getParent();

		// No parent? let's work with the current directory
		if (parentLibPath == null) {
			parentLibPath = path;
		}

		return parentLibPath.resolve("../" + subPath);
	}

	/**
	 * Get the directory of the current executable jar.
	 *
	 * @return {@link File} instance
	 */
	public static File getExecutableDir() {
		final File me;
		try {
			me = ResourceHelper.findSource(ConfigHelper.class);
		} catch (Exception e) {
			throw new IllegalStateException("Error detected when getting local source file: ", e);
		}

		if (me == null) {
			throw new IllegalStateException("Could not get the local source file.");
		}
		return me;
	}

	/**
	 * Get the default configuration file path either in the Windows <em>ProgramData\PRODUCT_CODE</em>
	 * directory or under the install directory <em>/opt/PRODUCT_CODE</em> on Linux systems.
	 * 
	 * @param directory      Directory of the configuration file. (e.g. config or otel)
	 * @param configFilename Configuration file name (e.g. PRODUCT-CODE-config.yaml or otel-config.yaml)
	 * @return new {@link Path} instance
	 */
	public static Path getDefaultConfigFilePath(final String directory, final String configFilename) {
		if (LocalOsHandler.isWindows()) {
			return getProgramDataConfigFile(directory, configFilename);
		}
		return ConfigHelper.getSubPath(String.format(FILE_PATH_FORMAT, directory, configFilename));
	}

	/**
	 * Get the configuration file under the ProgramData windows directory.<br>
	 * If the ProgramData path is not valid then the configuration file will be located
	 * under the install directory.
	 * 
	 * @param directory      Directory of the configuration file. (e.g. config or otel)
	 * @param configFilename Configuration file name (e.g. PRODUCT-CODE-config.yaml or otel-config.yaml)
	 * @return new {@link Path} instance
	 */
	static Path getProgramDataConfigFile(final String directory, final String configFilename) {
		return getProgramDataPath()
			.stream()
			.map(path -> 
				Paths.get(
					createDirectories(Paths.get(path, PRODUCT_CODE, directory)).toAbsolutePath().toString(),
					configFilename
				)
			)
			.findFirst()
			.orElseGet(() -> ConfigHelper.getSubPath(String.format(FILE_PATH_FORMAT, directory, configFilename)));
	}

	/**
	 * Get the <em>%PROGRAMDATA%</em> path. If the ProgramData path is not valid
	 * then <code>Optional.empty()</code> is returned.
	 * 
	 * @return {@link Optional} containing a string value (path)
	 */
	public static Optional<String> getProgramDataPath() {
		final String programDataPath = System.getenv("ProgramData");
		if (programDataPath != null && !programDataPath.isBlank()) {
			return Optional.of(programDataPath);
		}

		return Optional.empty();
	}

	/**
	 * Decrypt the given encrypted password.
	 *
	 * @param encrypted 
	 * @return char array
	 */
	public static char[] decrypt(final char[] encrypted) {
		try {
			return SecurityManager.decrypt(encrypted, PasswordEncrypt.getKeyStoreFile(false));
		} catch (Exception e) {
			// This is a real problem, let's log the error
			log.error("Could not decrypt password: {}", e.getMessage());
			log.debug("Exception", e);
			return encrypted;
		}
	}


	/**
	 * Find the application's configuration file (PRODUCT-CODE-config.yaml).<br>
	 * <ol>
	 *   <li>If the user has configured the configFilePath via <em>--config=$filePath</em> then it is the chosen file</li>
	 *   <li>Else if <em>config/PRODUCT-CODE-config.yaml</em> path exists, the resulting File is the one representing this path</li>
	 *   <li>Else we copy <em>config/PRODUCT-CODE-config-example.yaml</em> to the host file <em>config/PRODUCT-CODE-config.yaml</em> then we return the resulting host file</li>
	 * </ol>
	 * 
	 * The program fails if
	 * <ul>
	 *   <li>The configured file path doesn't exist</li>
	 *   <li>config/PRODUCT-CODE-config-example.yaml is not present</li>
	 *   <li>If an I/O error occurs</li>
	 * </ul>
	 * 
	 * @param configFilePath The configuration file passed by the user. E.g. --config=/opt/PRODUCT-CODE/config/my-PRODUCT-CODE-config.yaml
	 * @return {@link File} instance
	 * @throws IOException
	 */
	public static File findConfigFile(final String configFilePath) throws IOException {
		// The user has configured a configuration file path
		if (!configFilePath.isBlank()) {
			final File configFile = new File(configFilePath);
			if (configFile.exists()) {
				return configFile;
			}
			throw new IllegalStateException(
				String.format("Cannot find %s. Please make sure the file exists on your system", configFilePath)
			);
		}

		// Get the configuration file config/PRODUCT-CODE-config.yaml
		return getDefaultConfigFile(CONFIG_DIRECTORY_NAME , DEFAULT_CONFIG_FILENAME, CONFIG_EXAMPLE_FILENAME);

	}

	/**
	 * Get the default configuration file.
	 * 
	 * @param directory             Directory of the configuration file. (e.g. config or otel)
	 * @param configFilename        Configuration file name (e.g. PRODUCT-CODE-config.yaml or otel-config.yaml)
	 * @param configFilenameExample Configuration file name example (e.g. PRODUCT-CODE-config-example.yaml)
	 * @return {@link File} instance
	 * @throws IOException if the copy fails
	 */
	public static File getDefaultConfigFile(final String directory, final String configFilename, final String configFilenameExample) throws IOException {

		// Get the the configuration file absolute path
		final Path configPath = getDefaultConfigFilePath(directory, configFilename);

		// If it exists then we are good we can just return the resulting File
		if (Files.exists(configPath)) {
			// At this time, we don't know who created the configuration file and what permissions are applied.
			// So let's skip the error logging to avoid unnecessary noise. That's why we call the method with
			// logError = false
			setUserPermissionsOnWindows(configPath, false);

			return configPath.toFile();
		}

		// Now we will proceed with a copy of the example file (e.g. PRODUCT-CODE-config-example.yaml to config/PRODUCT-CODE-config.yaml)
		final Path exampleConfigPath = ConfigHelper.getSubPath(String.format(FILE_PATH_FORMAT, directory, configFilenameExample));

		// Bad configuration
		if (!Files.exists(exampleConfigPath)) {
			throw new IllegalStateException(
				String.format(
					"Cannot find '%s' . Please create the configuration file '%s' before starting the Hardware Sentry Agent.",
					exampleConfigPath.toAbsolutePath(),
					configPath.toAbsolutePath()
				)
			);
		}

		File configFile = Files.copy(exampleConfigPath, configPath, StandardCopyOption.REPLACE_EXISTING).toFile();

		setUserPermissionsOnWindows(configPath, true);

		return configFile;
	}

	/**
	 * Set write permissions for PRODUCT-CODE-config.yaml deployed on a Windows machine running the agent
	 * 
	 * @param configPath  the configuration file absolute path
	 * @param logError    whether we should log the error or not. If logError is false, an info message is logged.
	 */
	private static void setUserPermissionsOnWindows(final Path configPath, boolean logError) {
		if (LocalOsHandler.isWindows()) {
			setUserPermissions(configPath, logError);
		}
	}

	/**
	 * Set write permission for PRODUCT-CODE-config.yaml
	 * 
	 * @param configPath the configuration file absolute path
	 * @param logError   whether we should log the error or not. If logError is false, an info message is logged.
	 */
	private static void setUserPermissions(final Path configPath, boolean logError) {
		try {
			final GroupPrincipal users = configPath.getFileSystem().getUserPrincipalLookupService()
					.lookupPrincipalByGroupName("Users");
	
			// get view
			final AclFileAttributeView view = Files.getFileAttributeView(configPath, AclFileAttributeView.class);
	
			// create ACE to give "Users" access
			final AclEntry entry = AclEntry
				.newBuilder()
				.setType(AclEntryType.ALLOW)
				.setPrincipal(users)
				.setPermissions(
					AclEntryPermission.WRITE_DATA,
					AclEntryPermission.WRITE_ATTRIBUTES,
					AclEntryPermission.WRITE_ACL,
					AclEntryPermission.WRITE_OWNER,
					AclEntryPermission.WRITE_NAMED_ATTRS,
					AclEntryPermission.READ_DATA,
					AclEntryPermission.READ_ACL,
					AclEntryPermission.READ_ATTRIBUTES,
					AclEntryPermission.READ_NAMED_ATTRS,
					AclEntryPermission.DELETE,
					AclEntryPermission.APPEND_DATA,
					AclEntryPermission.DELETE)
				.build();
	
			// read ACL, insert ACE, re-write ACL
			final List<AclEntry> acl = view.getAcl();
	
			// insert before any DENY entries
			acl.add(0, entry);
			view.setAcl(acl);
		} catch (Exception e) {
			if (logError) {
				log.error("Could not set write permissions to file: {}. Error: {}", configPath.toString(), e.getMessage());
				log.error("Exception: ", e);
			} else {
				log.info("Could not set write permissions to file: {}. Message: {}", configPath.toString(), e.getMessage());
			}

		}
	}
}
