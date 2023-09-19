package com.sentrysoftware.matrix.agent.helper;

import static com.sentrysoftware.matrix.agent.helper.AgentConstants.CONFIG_DIRECTORY_NAME;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.CONFIG_EXAMPLE_FILENAME;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.DEFAULT_CONFIG_FILENAME;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.FILE_PATH_FORMAT;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.LOG_DIRECTORY_NAME;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.PRODUCT_CODE;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sentrysoftware.matrix.agent.config.AgentConfig;
import com.sentrysoftware.matrix.agent.config.AlertingSystemConfig;
import com.sentrysoftware.matrix.agent.config.ResourceConfig;
import com.sentrysoftware.matrix.agent.config.ResourceGroupConfig;
import com.sentrysoftware.matrix.agent.context.AgentContext;
import com.sentrysoftware.matrix.agent.security.PasswordEncrypt;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.matrix.security.SecurityManager;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;

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
	 *               assumed under /opt/metricshub
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
	 * <em>/opt/metricshub/lib/app/../config</em> on linux install
	 *
	 * @param subPath sub path to the directory or the file
	 * @return {@link Path} instance
	 */
	public static Path getSubPath(@NonNull final String subPath) {
		final File sourceDirectory = getSourceDirectory();

		final Path path = sourceDirectory.getAbsoluteFile().toPath();

		Path parentLibPath = path.getParent();

		// No parent? let's work with the current directory
		if (parentLibPath == null) {
			parentLibPath = path;
		}

		return parentLibPath.resolve("../" + subPath);
	}

	/**
	 * Retrieves the directory containing the current source file, whether it's located
	 * within a JAR file or a regular directory.<br>
	 *
	 * This method attempts to locate the source directory associated with the calling class, which can be
	 * helpful for accessing resources and configuration files.
	 *
	 * @return A {@link File} instance representing the source directory.
	 *
	 * @throws IllegalStateException if the source directory cannot be determined.
	 */
	public static File getSourceDirectory() {
		final File sourceDirectory;
		try {
			sourceDirectory = ResourceHelper.findSourceDirectory(ConfigHelper.class);
		} catch (Exception e) {
			throw new IllegalStateException("Error detected when getting local source file: ", e);
		}

		if (sourceDirectory == null) {
			throw new IllegalStateException("Could not get the local source file.");
		}
		return sourceDirectory;
	}

	/**
	 * Get the default configuration file path either in the Windows <em>ProgramData\metricshub</em>
	 * directory or under the install directory <em>/opt/metricshub</em> on Linux systems.
	 *
	 * @param directory      Directory of the configuration file. (e.g. config or otel)
	 * @param configFilename Configuration file name (e.g. metricshub.yaml or otel-config.yaml)
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
	 * @param configFilename Configuration file name (e.g. metricshub.yaml or otel-config.yaml)
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
	 * Find the application's configuration file (metricshub.yaml).<br>
	 * <ol>
	 *   <li>If the user has configured the configFilePath via <em>--config=$filePath</em> then it is the chosen file</li>
	 *   <li>Else if <em>config/metricshub.yaml</em> path exists, the resulting File is the one representing this path</li>
	 *   <li>Else we copy <em>config/metricshub-example.yaml</em> to the host file <em>config/metricshub.yaml</em> then we return the resulting host file</li>
	 * </ol>
	 *
	 * The program fails if
	 * <ul>
	 *   <li>The configured file path doesn't exist</li>
	 *   <li>config/metricshub-example.yaml is not present</li>
	 *   <li>If an I/O error occurs</li>
	 * </ul>
	 *
	 * @param configFilePath The configuration file passed by the user. E.g. --config=/opt/PRODUCT-CODE/config/my-metricshub.yaml
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

		// Get the configuration file config/metricshub.yaml
		return getDefaultConfigFile(CONFIG_DIRECTORY_NAME, DEFAULT_CONFIG_FILENAME, CONFIG_EXAMPLE_FILENAME);
	}

	/**
	 * Get the default configuration file.
	 *
	 * @param directory             Directory of the configuration file. (e.g. config or otel)
	 * @param configFilename        Configuration file name (e.g. metricshub.yaml or otel-config.yaml)
	 * @param configFilenameExample Configuration file name example (e.g. metricshub-example.yaml)
	 * @return {@link File} instance
	 * @throws IOException if the copy fails
	 */
	public static File getDefaultConfigFile(
		final String directory,
		final String configFilename,
		final String configFilenameExample
	) throws IOException {
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

		// Now we will proceed with a copy of the example file (e.g. metricshub-example.yaml to config/metricshub.yaml)
		final Path exampleConfigPath = ConfigHelper.getSubPath(
			String.format(FILE_PATH_FORMAT, directory, configFilenameExample)
		);

		// Bad configuration
		if (!Files.exists(exampleConfigPath)) {
			throw new IllegalStateException(
				String.format(
					"Cannot find '%s' . Please create the configuration file '%s' before starting the MetricsHub Agent.",
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
	 * Set write permissions for metricshub.yaml deployed on a Windows machine running the agent
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
	 * Set write permission for metricshub.yaml
	 *
	 * @param configPath the configuration file absolute path
	 * @param logError   whether we should log the error or not. If logError is false, an info message is logged.
	 */
	private static void setUserPermissions(final Path configPath, boolean logError) {
		try {
			final GroupPrincipal users = configPath
				.getFileSystem()
				.getUserPrincipalLookupService()
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
					AclEntryPermission.DELETE
				)
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

	/**
	 * Creates and configures a new instance of the Jackson ObjectMapper for handling YAML data.
	 *
	 * @return A configured ObjectMapper instance.
	 */
	public static JsonMapper newObjectMapper() {
		return JsonMapper
			.builder(new YAMLFactory())
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
			.build();
	}

	/**
	 * Normalizes the agent configuration and sets global values if no specific
	 * values are specified on the resource groups or resources
	 *
	 * @param agentConfig The whole configuration the MetricsHub agent
	 */
	public static void normalizeAgentConfiguration(final AgentConfig agentConfig) {
		agentConfig
			.getResourceGroups()
			.entrySet()
			.forEach(resourceGroupConfigEntry -> {
				final ResourceGroupConfig resourceGroupConfig = resourceGroupConfigEntry.getValue();
				normalizeResourceGroupConfig(agentConfig, resourceGroupConfig);
				resourceGroupConfig
					.getResources()
					.entrySet()
					.forEach(resourceConfigEntry -> normalizeResourceConfig(resourceGroupConfigEntry, resourceConfigEntry));
			});
	}

	/**
	 * Normalizes the resource configuration and sets resource group configuration values if no specific
	 * values are specified on this resource configuration.<br>
	 * If a new connector is configured then it is automatically added to the connector store.
	 *
	 * @param resourceGroupConfigEntry The resource group configuration entry
	 * @param resourceConfigEntry      The individual resource configuration entry
	 */
	private static void normalizeResourceConfig(
		final Entry<String, ResourceGroupConfig> resourceGroupConfigEntry,
		final Entry<String, ResourceConfig> resourceConfigEntry
	) {
		final ResourceGroupConfig resourceGroupConfig = resourceGroupConfigEntry.getValue();
		final ResourceConfig resourceConfig = resourceConfigEntry.getValue();

		// Set resource group configuration's collect period if there is no specific collect period on the resource configuration
		if (resourceConfig.getCollectPeriod() == null) {
			resourceConfig.setCollectPeriod(resourceGroupConfig.getCollectPeriod());
		}

		// Set resource group configuration's discovery cycle if there is no specific collect period on the resource group
		if (resourceConfig.getDiscoveryCycle() == null) {
			resourceConfig.setDiscoveryCycle(resourceGroupConfig.getDiscoveryCycle());
		}

		// Set resource group configuration's logger level in the resource configuration
		if (resourceConfig.getLoggerLevel() == null) {
			resourceConfig.setLoggerLevel(resourceGroupConfig.getLoggerLevel());
		}

		// Set resource group configuration's output directory in the resource configuration
		if (resourceConfig.getOutputDirectory() == null) {
			resourceConfig.setOutputDirectory(resourceGroupConfig.getOutputDirectory());
		}

		// Set resource group configuration's sequential flag in the resource configuration
		if (resourceConfig.getSequential() == null) {
			resourceConfig.setSequential(resourceGroupConfig.getSequential());
		}

		final AlertingSystemConfig resourceGroupAlertingSystemConfig = resourceGroupConfig.getAlertingSystemConfig();

		final AlertingSystemConfig alertingSystemConfig = resourceConfig.getAlertingSystemConfig();
		// Set resource group configuration's alerting system in the resource configuration
		if (alertingSystemConfig == null) {
			resourceConfig.setAlertingSystemConfig(resourceGroupAlertingSystemConfig);
		} else if (alertingSystemConfig.getProblemTemplate() == null) {
			// Set the problem template of the alerting system
			alertingSystemConfig.setProblemTemplate(resourceGroupAlertingSystemConfig.getProblemTemplate());
		} else if (alertingSystemConfig.getDisable() == null) {
			// Set the disable flag of the altering system
			alertingSystemConfig.setDisable(resourceGroupAlertingSystemConfig.getDisable());
		}

		// Set the resolve host name to FQDN flag
		if (resourceConfig.getResolveHostnameToFqdn() == null) {
			resourceConfig.setResolveHostnameToFqdn(resourceGroupConfig.getResolveHostnameToFqdn());
		}

		// Set the job timeout value
		if (resourceConfig.getJobTimeout() == null) {
			resourceConfig.setJobTimeout(resourceGroupConfig.getJobTimeout());
		}

		// Set agent attributes in the resource group attributes map
		mergeAttributes(resourceGroupConfig.getAttributes(), resourceConfig.getAttributes());

		// Do we have a connector?
		final Connector connector = resourceConfig.getConnector();
		if (connector != null) {
			// Create its identity
			final ConnectorIdentity identity = connector.getOrCreateConnectorIdentity();
			final String compiledFileName = String.format(
				"Custom-%s-%s",
				resourceGroupConfigEntry.getKey(),
				resourceConfigEntry.getKey()
			);
			identity.setCompiledFilename(compiledFileName);

			// Add it to the store
			AgentContext.getInstance().getConnectorStore().addOne(compiledFileName, connector);
		}
	}

	/**
	 * Normalizes the resource group configuration and sets agent configuration's values if no specific
	 * values are specified on this resource group configuration
	 *
	 * @param agentConfig         The whole configuration the MetricsHub agent
	 * @param resourceGroupConfig The individual resource group configuration
	 */
	private static void normalizeResourceGroupConfig(
		final AgentConfig agentConfig,
		final ResourceGroupConfig resourceGroupConfig
	) {
		// Set global collect period if there is no specific collect period on the resource group configuration
		if (resourceGroupConfig.getCollectPeriod() == null) {
			resourceGroupConfig.setCollectPeriod(agentConfig.getCollectPeriod());
		}

		// Set global discovery cycle if there is no specific collect period on the resource group configuration
		if (resourceGroupConfig.getDiscoveryCycle() == null) {
			resourceGroupConfig.setDiscoveryCycle(agentConfig.getDiscoveryCycle());
		}

		// Set the global level in the resource group configuration
		if (resourceGroupConfig.getLoggerLevel() == null) {
			resourceGroupConfig.setLoggerLevel(agentConfig.getLoggerLevel());
		}

		// Set the global output directory in the resource group configuration
		if (resourceGroupConfig.getOutputDirectory() == null) {
			resourceGroupConfig.setOutputDirectory(agentConfig.getOutputDirectory());
		}

		// Set global sequential flag in the resource group configuration
		if (resourceGroupConfig.getSequential() == null) {
			resourceGroupConfig.setSequential(agentConfig.isSequential());
		}

		final AlertingSystemConfig alertingSystemConfig = resourceGroupConfig.getAlertingSystemConfig();
		final AlertingSystemConfig globalAlertingSystemConfig = agentConfig.getAlertingSystemConfig();

		// Set global configuration's alerting system in the resource group configuration
		if (alertingSystemConfig == null) {
			resourceGroupConfig.setAlertingSystemConfig(globalAlertingSystemConfig);
		} else if (alertingSystemConfig.getProblemTemplate() == null) {
			// Set the problem template of the alerting system
			alertingSystemConfig.setProblemTemplate(globalAlertingSystemConfig.getProblemTemplate());
		} else if (alertingSystemConfig.getDisable() == null) {
			// Set the disable flag of the altering system
			alertingSystemConfig.setDisable(globalAlertingSystemConfig.getDisable());
		}

		// Set the resolve host name to FQDN flag
		if (resourceGroupConfig.getResolveHostnameToFqdn() == null) {
			resourceGroupConfig.setResolveHostnameToFqdn(agentConfig.isResolveHostnameToFqdn());
		}

		// Set the job timeout value
		if (resourceGroupConfig.getJobTimeout() == null) {
			resourceGroupConfig.setJobTimeout(agentConfig.getJobTimeout());
		}

		// Set agent attributes in the resource group attributes map
		mergeAttributes(agentConfig.getAttributes(), resourceGroupConfig.getAttributes());
	}

	/**
	 * Merge parent attributes into the child attributes
	 *
	 * @param parentAttributes Map of key-pair values defining the attributes at the parent level
	 * @param childAttributes  Map of key-pair values defining the attributes at the child level
	 */
	private static void mergeAttributes(Map<String, String> parentAttributes, Map<String, String> childAttributes) {
		childAttributes.putAll(parentAttributes);
	}

	/**
	 * Configure the 'com.sentrysoftware' logger based on the user's command.<br>
	 * See src/main/resources/log4j2.xml
	 *
	 * @param loggerLevel     Logger level from the configuration as {@link String}
	 * @param outputDirectory The output directory as String
	 */
	public static void configureGlobalLogger(final String loggerLevelStr, final String outputDirectory) {
		final Level loggerLevel = getLoggerLevel(loggerLevelStr);

		ThreadContext.put("logId", "metricshub-agent-global");
		ThreadContext.put("loggerLevel", loggerLevel.toString());

		if (outputDirectory != null) {
			ThreadContext.put("outputDirectory", outputDirectory);
		}
	}

	/**
	 * Get the Log4j log level from the configured logLevel string
	 *
	 * @param loggerLevel string value from the configuration (e.g. off, debug, info, warn, error, trace, all)
	 * @return log4j {@link Level} instance
	 */
	public static Level getLoggerLevel(final String loggerLevel) {
		final Level level = loggerLevel != null ? Level.getLevel(loggerLevel.toUpperCase()) : null;

		return level != null ? level : Level.OFF;
	}
}
