package com.sentrysoftware.metricshub.agent.helper;

import static com.sentrysoftware.metricshub.agent.helper.AgentConstants.CONFIG_DIRECTORY_NAME;
import static com.sentrysoftware.metricshub.agent.helper.AgentConstants.CONFIG_EXAMPLE_FILENAME;
import static com.sentrysoftware.metricshub.agent.helper.AgentConstants.DEFAULT_CONFIG_FILENAME;
import static com.sentrysoftware.metricshub.agent.helper.AgentConstants.FILE_PATH_FORMAT;
import static com.sentrysoftware.metricshub.agent.helper.AgentConstants.LOG_DIRECTORY_NAME;
import static com.sentrysoftware.metricshub.agent.helper.AgentConstants.PRODUCT_CODE;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sentrysoftware.metricshub.agent.config.AgentConfig;
import com.sentrysoftware.metricshub.agent.config.AlertingSystemConfig;
import com.sentrysoftware.metricshub.agent.config.ConnectorVariables;
import com.sentrysoftware.metricshub.agent.config.ResourceConfig;
import com.sentrysoftware.metricshub.agent.config.ResourceGroupConfig;
import com.sentrysoftware.metricshub.agent.config.protocols.AbstractProtocolConfig;
import com.sentrysoftware.metricshub.agent.config.protocols.HttpProtocolConfig;
import com.sentrysoftware.metricshub.agent.config.protocols.IpmiProtocolConfig;
import com.sentrysoftware.metricshub.agent.config.protocols.OsCommandProtocolConfig;
import com.sentrysoftware.metricshub.agent.config.protocols.ProtocolsConfig;
import com.sentrysoftware.metricshub.agent.config.protocols.SnmpProtocolConfig;
import com.sentrysoftware.metricshub.agent.config.protocols.SshProtocolConfig;
import com.sentrysoftware.metricshub.agent.config.protocols.WbemProtocolConfig;
import com.sentrysoftware.metricshub.agent.config.protocols.WinRmProtocolConfig;
import com.sentrysoftware.metricshub.agent.config.protocols.WmiProtocolConfig;
import com.sentrysoftware.metricshub.agent.context.MetricDefinitions;
import com.sentrysoftware.metricshub.agent.security.PasswordEncrypt;
import com.sentrysoftware.metricshub.engine.common.helpers.JsonHelper;
import com.sentrysoftware.metricshub.engine.common.helpers.LocalOsHandler;
import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import com.sentrysoftware.metricshub.engine.common.helpers.ResourceHelper;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration.SnmpVersion;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import com.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import com.sentrysoftware.metricshub.engine.security.SecurityManager;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.GroupPrincipal;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.io.ClassPathResource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ConfigHelper {

	private static final String OS_COMMAND = "OSCommand";
	private static final String HTTP_PROTOCOL = "HTTP";
	private static final String WMI_PROTOCOL = "WMI";
	private static final String WBEM_PROTOCOL = "WBEM";
	private static final String SSH_PROTOCOL = "SSH";
	private static final String IPMI_PROTOCOL = "IPMI";
	private static final String WIN_RM_PROTOCOL = "WinRM";
	private static final String TIMEOUT_ERROR =
		"Resource %s - Timeout value is invalid for protocol %s." +
		" Timeout value returned: %s. This resource will not be monitored. Please verify the configured timeout value.";
	private static final String PORT_ERROR =
		"Resource %s - Invalid port configured for protocol %s. Port value returned: %s." +
		" This resource will not be monitored. Please verify the configured port value.";
	private static final String USERNAME_ERROR =
		"Resource %s - No username configured for protocol %s." +
		" This resource will not be monitored. Please verify the configured username.";
	private static final Predicate<String> INVALID_STRING_CHECKER = attr -> attr == null || attr.isBlank();
	private static final Predicate<Integer> INVALID_PORT_CHECKER = attr -> attr == null || attr < 1 || attr > 65535;
	private static final Predicate<Long> INVALID_TIMEOUT_CHECKER = attr -> attr == null || attr < 0L;
	private static final Predicate<String> EMPTY_STRING_CHECKER = attr -> attr != null && attr.isBlank();

	/**
	 * Get the default output directory for logging.<br>
	 * On Windows, if the LOCALAPPDATA path is not valid then the output directory will be located
	 * under the install directory.<br>
	 * On Linux, the output directory is located under the installation directory.
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
		if (configFilePath != null && !configFilePath.isBlank()) {
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
		// Get the configuration file absolute path
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

		final File configFile = Files.copy(exampleConfigPath, configPath, StandardCopyOption.REPLACE_EXISTING).toFile();

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
	 * @param agentConfig    The whole configuration of the MetricsHub agent
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

		// Create an identity for the configured connector
		normalizeConfiguredConnector(
			resourceGroupConfigEntry.getKey(),
			resourceConfigEntry.getKey(),
			resourceConfig.getConnector()
		);
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
	 * Merge the given attributes into the destination attributes
	 *
	 * @param attributes            Map of key-pair values defining the attributes at a certain level
	 * @param destinationAttributes Map of key-pair values defining the destination
	 */
	public static void mergeAttributes(
		final Map<String, String> attributes,
		final Map<String, String> destinationAttributes
	) {
		destinationAttributes.putAll(attributes);
	}

	/**
	 * Configure the 'com.sentrysoftware' logger based on the user's command.<br>
	 * See src/main/resources/log4j2.xml
	 *
	 * @param loggerLevelStr     Logger level from the configuration as {@link String}
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

	/**
	 * Build the {@link TelemetryManager} map.
	 *
	 * @param agentConfig    Wraps the agent configuration for all the resources
	 * @param connectorStore Wraps all the connectors
	 * @return Map of {@link TelemetryManager} instances indexed by group id then by resource id
	 */
	public static Map<String, Map<String, TelemetryManager>> buildTelemetryManagers(
		@NonNull final AgentConfig agentConfig,
		@NonNull final ConnectorStore connectorStore
	) {
		final Map<String, Map<String, TelemetryManager>> telemetryManagers = new HashMap<>();

		agentConfig
			.getResourceGroups()
			.forEach((resourceGroupKey, resourceKeyGroupConfig) -> {
				final Map<String, TelemetryManager> resourceGroupTelemetryManagers = new HashMap<>();
				telemetryManagers.put(resourceGroupKey, resourceGroupTelemetryManagers);
				resourceKeyGroupConfig
					.getResources()
					.forEach((resourceKey, resourceConfig) ->
						updateResourceGroupTelemetryManagers(
							resourceGroupTelemetryManagers,
							resourceGroupKey,
							resourceKey,
							resourceConfig,
							connectorStore
						)
					);
			});
		return telemetryManagers;
	}

	/**
	 * Update the given resource group {@link TelemetryManager} map if the configuration is valid
	 *
	 * @param resourceGroupTelemetryManagers {@link Map} of {@link TelemetryManager} per resource group configuration
	 * @param resourceGroupKey               The unique identifier of the resource group
	 * @param resourceKey                    The unique identifier of the resource
	 * @param resourceConfig                 The resource configuration
	 * @param connectorStore                 Wraps all the connectors
	 */
	private static void updateResourceGroupTelemetryManagers(
		@NonNull final Map<String, TelemetryManager> resourceGroupTelemetryManagers,
		@NonNull final String resourceGroupKey,
		@NonNull final String resourceKey,
		final ResourceConfig resourceConfig,
		@NonNull final ConnectorStore connectorStore
	) {
		if (resourceConfig == null) {
			return;
		}

		try {
			// Validate protocols
			validateProtocols(resourceKey, resourceConfig);

			final Map<String, Connector> connectors = connectorStore.getStore();
			final Set<String> connectorIds = connectors.keySet();

			final Set<String> selectedConnectors = validateAndGetConnectors(
				connectorIds,
				resourceConfig.getSelectConnectors(),
				resourceKey,
				false
			);

			final Set<String> excludedConnectors = validateAndGetConnectors(
				connectorIds,
				resourceConfig.getExcludeConnectors(),
				resourceKey,
				true
			);

			final HostConfiguration hostConfiguration = buildHostConfiguration(
				resourceConfig,
				selectedConnectors,
				excludedConnectors,
				resourceKey
			);

			ConnectorStore telemetryManagerConnectorStore = createCustomConnectorStoreIfConfigured(
				resourceConfig.getConnector(),
				connectorStore
			);

			// Retrieve connectors variables map from the resource configuration
			final Map<String, ConnectorVariables> connectorVariablesMap = resourceConfig.getVariables();

			// If connectors variables exist then merge the existing connector store with a new one containing custom connectors
			if (connectorVariablesMap != null && !connectorVariablesMap.isEmpty()) {
				// Call ConnectorTemplateLibraryParser and parse the custom connectors
				final ConnectorTemplateLibraryParser connectorTemplateLibraryParser = new ConnectorTemplateLibraryParser();

				final Map<String, Connector> customConnectors = connectorTemplateLibraryParser.parse(
					ConfigHelper.getSubDirectory("connectors", false),
					connectorVariablesMap
				);

				// Overwrite telemetryManagerConnectorStore
				telemetryManagerConnectorStore = buildNewConnectorStore(customConnectors, telemetryManagerConnectorStore);
			}

			resourceGroupTelemetryManagers.putIfAbsent(
				resourceKey,
				TelemetryManager
					.builder()
					.connectorStore(telemetryManagerConnectorStore)
					.hostConfiguration(hostConfiguration)
					.build()
			);
		} catch (Exception e) {
			log.warn(
				"Resource {} - Under the resource group configuration {}, the resource configuration {}" +
				" has been staged as invalid. Reason: {}",
				resourceKey,
				resourceGroupKey,
				resourceKey,
				e.getMessage()
			);
		}
	}

	/**
	 * Builds a new connector store by merging the existing (standard) connectors with the custom connectors (connectors that contain template variables)
	 * @param customConnectors Map<String, Connector> Connectors containing template variables
	 * @param telemetryManagerConnectorStore the connector store before the merge with custom connectors
	 * @return {@link ConnectorStore} instance
	 */
	protected static ConnectorStore buildNewConnectorStore(
		final Map<String, Connector> customConnectors,
		final ConnectorStore telemetryManagerConnectorStore
	) {
		// Initialize a new connector store that will contain both standard and custom connectors
		final ConnectorStore finalConnectorStore = new ConnectorStore();

		// Initialize a new connectors map that will contain both standard and custom connectors
		final Map<String, Connector> newConnectors = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		// Add the original connector store connectors
		newConnectors.putAll(telemetryManagerConnectorStore.getStore());

		// Add custom connectors
		newConnectors.putAll(customConnectors);

		// Populate the connector store with the existing connectors
		finalConnectorStore.setStore(newConnectors);

		// Return the custom ConnectorStore
		return finalConnectorStore;
	}

	/**
	 * Normalizes the configuration of a configured connector by creating a
	 * unique identifier for it.
	 *
	 * @param resourceGroupKey    The resource group key.
	 * @param resourceKey         The resource key.
	 * @param configuredConnector The configured connector to be normalized.
	 */
	static void normalizeConfiguredConnector(
		final String resourceGroupKey,
		final String resourceKey,
		final Connector configuredConnector
	) {
		// Check if a configured connector exists
		if (configuredConnector != null) {
			// Create a unique connector identifier based on resource keys
			final ConnectorIdentity identity = configuredConnector.getOrCreateConnectorIdentity();
			final String connectorId = String.format("MetricsHub-Configured-Connector-%s-%s", resourceGroupKey, resourceKey);

			// Set the compiled filename of the connector to the unique identifier
			identity.setCompiledFilename(connectorId);
		}
	}

	/**
	 * Create a custom ConnectorStore if a configured connector exists.
	 * @param configuredConnector Configured connector
	 * @param connectorStore      The original ConnectorStore
	 * @return A custom ConnectorStore with the configured connector if it exists, or the original ConnectorStore.
	 */
	static ConnectorStore createCustomConnectorStoreIfConfigured(
		final Connector configuredConnector,
		final ConnectorStore connectorStore
	) {
		// Check if a configured connector is available
		if (configuredConnector != null) {
			// Create a custom ConnectorStore and populate it with the
			// configured connector
			final ConnectorStore customConnectorStore = new ConnectorStore();

			final Map<String, Connector> originalConnectors = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			originalConnectors.putAll(connectorStore.getStore());

			// Populate the connector store with the existing connectors
			customConnectorStore.setStore(originalConnectors);

			// Add the configured connector
			customConnectorStore.addOne(
				configuredConnector.getConnectorIdentity().getCompiledFilename(),
				configuredConnector
			);

			// Return the custom ConnectorStore
			return customConnectorStore;
		}

		// If no configured connector exists, return the original ConnectorStore
		return connectorStore;
	}

	/**
	 * Validate the protocols configured under the given {@link ResourceConfig} instance
	 *
	 * @param resourceKey    Resource unique identifier
	 * @param resourceConfig {@link ResourceConfig} instance configured by the user
	 */
	private static void validateProtocols(@NonNull final String resourceKey, final ResourceConfig resourceConfig) {
		final ProtocolsConfig protocolsConfig = resourceConfig.getProtocols();
		if (protocolsConfig == null) {
			return;
		}

		final WinRmProtocolConfig winRmConfig = protocolsConfig.getWinRm();
		if (winRmConfig != null) {
			validateWinRmInfo(resourceKey, winRmConfig.getPort(), winRmConfig.getTimeout(), winRmConfig.getUsername());
		}

		final SnmpProtocolConfig snmpConfig = protocolsConfig.getSnmp();
		if (snmpConfig != null) {
			validateSnmpInfo(resourceKey, snmpConfig);
		}

		final IpmiProtocolConfig ipmiConfig = protocolsConfig.getIpmi();
		if (ipmiConfig != null) {
			validateIpmiInfo(resourceKey, ipmiConfig.getUsername(), ipmiConfig.getTimeout());
		}

		final SshProtocolConfig sshConfig = protocolsConfig.getSsh();
		if (sshConfig != null) {
			validateSshInfo(resourceKey, sshConfig.getUsername(), sshConfig.getTimeout());
		}

		final WbemProtocolConfig wbemConfig = protocolsConfig.getWbem();
		if (wbemConfig != null) {
			validateWbemInfo(
				resourceKey,
				wbemConfig.getUsername(),
				wbemConfig.getTimeout(),
				wbemConfig.getPort(),
				wbemConfig.getVCenter()
			);
		}

		final WmiProtocolConfig wmiConfig = protocolsConfig.getWmi();
		if (wmiConfig != null) {
			validateWmiInfo(resourceKey, wmiConfig.getTimeout());
		}

		final HttpProtocolConfig httpConfig = protocolsConfig.getHttp();
		if (httpConfig != null) {
			validateHttpInfo(resourceKey, httpConfig.getTimeout(), httpConfig.getPort());
		}

		final OsCommandProtocolConfig osCommandConfig = protocolsConfig.getOsCommand();
		if (osCommandConfig != null) {
			validateOsCommandInfo(resourceKey, osCommandConfig.getTimeout());
		}
	}

	/**
	 * Return configured connector names. This method throws an {@link IllegalStateException} if
	 * we encounter an unknown connector
	 *
	 * @param acceptedConnectorNames   Known connector names (connector compiled file names)
	 * @param resourceConfigConnectors User's selected or excluded connectors
	 * @param resourceKey              Resource unique identifier
	 * @param isExcluded               Specifies if we are validating excluded or selected connectors
	 *
	 * @return {@link Set} containing the validated connector names
	 * @throws IllegalStateException
	 */
	static Set<String> validateAndGetConnectors(
		final @NonNull Set<String> acceptedConnectorNames,
		final Set<String> resourceConfigConnectors,
		final String resourceKey,
		final boolean isExcluded
	) {
		if (resourceConfigConnectors == null || resourceConfigConnectors.isEmpty()) {
			return new HashSet<>();
		}

		// Copy the set of configured connectors as we won't perform operations on the original configuration
		final Set<String> configConnectors = resourceConfigConnectors.stream().collect(Collectors.toSet());

		// Get unknown connectors
		final Set<String> unknownConnectors = configConnectors
			.stream()
			.filter(compiledFileName -> !acceptedConnectorNames.contains(compiledFileName))
			.collect(Collectors.toSet());

		// Check unknown connectors
		if (unknownConnectors.isEmpty()) {
			return configConnectors;
		}

		final String message;
		configConnectors.removeAll(unknownConnectors);

		if (isExcluded) {
			message =
				String.format(
					"Resource %s - Configured unknown excluded connector(s): %s. This resource will be monitored, but the unknown connectors will be ignored.",
					String.join(", ", unknownConnectors),
					resourceKey
				);

			log.error(message);

			return configConnectors;
		} else if (!configConnectors.isEmpty()) {
			message =
				String.format(
					"Resource %s - Configured unknown selected connector(s): %s. This resource will be monitored, but the unknown connectors will be ignored.",
					resourceKey,
					String.join(", ", unknownConnectors)
				);

			log.error(message);

			return configConnectors;
		} else {
			message =
				String.format(
					"Resource %s - Selected connectors are not valid. This resource will not be monitored.",
					resourceKey
				);

			// Throw the bad configuration exception
			throw new IllegalStateException(message);
		}
	}

	/**
	 * Validate the attribute against a predetermined test
	 *
	 * @param attribute       Value getting compared
	 * @param errorChecker    Logic test comparing our value
	 * @param messageSupplier error message supplier
	 * @throws IllegalStateException in case the validation fail
	 */
	private static <T> void validateAttribute(
		final T attribute,
		final Predicate<T> errorChecker,
		final Supplier<String> messageSupplier
	) {
		if (errorChecker.test(attribute)) {
			log.error(messageSupplier.get());
			throw new IllegalStateException(messageSupplier.get());
		}
	}

	/**
	 * Validate the given WinRM information (port, timeout, username and command)
	 *
	 * @param resourceKey  Resource unique identifier
	 * @param port         The port number used to perform WQL queries and commands
	 * @param timeout      How long until the WinRM request times out
	 * @param username	   Name used to establish the connection with the host via the WinRM protocol
	 */
	static void validateWinRmInfo(
		final String resourceKey,
		final Integer port,
		final Long timeout,
		final String username
	) {
		validateAttribute(port, INVALID_PORT_CHECKER, () -> String.format(PORT_ERROR, resourceKey, WIN_RM_PROTOCOL, port));

		validateAttribute(
			timeout,
			INVALID_TIMEOUT_CHECKER,
			() -> String.format(TIMEOUT_ERROR, resourceKey, WIN_RM_PROTOCOL, timeout)
		);

		validateAttribute(
			username,
			INVALID_STRING_CHECKER,
			() -> String.format(USERNAME_ERROR, resourceKey, WIN_RM_PROTOCOL)
		);
	}

	/**
	 * Validate the given SNMP information (resourceKey, snmpConfig)
	 *
	 * @param resourceKey Resource unique identifier
	 * @param snmpConfig  {@link SnmpProtocolConfig} object of the {@link ResourceConfig} instance
	 */
	static void validateSnmpInfo(final String resourceKey, SnmpProtocolConfig snmpConfig) {
		final SnmpVersion snmpVersion = snmpConfig.getVersion();

		final String displayName = snmpVersion.getDisplayName();
		final int intVersion = snmpVersion.getIntVersion();

		if (intVersion != 3) {
			validateAttribute(
				snmpConfig.getCommunity(),
				attr -> attr == null || attr.length == 0,
				() ->
					String.format(
						"Resource %s - No community string configured for %s. This resource will not be monitored.",
						resourceKey,
						displayName
					)
			);
		}

		validateAttribute(
			snmpConfig.getPort(),
			INVALID_PORT_CHECKER,
			() -> String.format(PORT_ERROR, resourceKey, displayName, snmpConfig.getPort())
		);

		validateAttribute(
			snmpConfig.getTimeout(),
			INVALID_TIMEOUT_CHECKER,
			() -> String.format(TIMEOUT_ERROR, resourceKey, displayName, snmpConfig.getTimeout())
		);

		if (intVersion == 3 && snmpVersion.getAuthType() != null) {
			validateAttribute(
				snmpConfig.getUsername(),
				INVALID_STRING_CHECKER,
				() -> String.format(USERNAME_ERROR, resourceKey, displayName)
			);
		}
	}

	/**
	 * Validate the given IPMI information (username and timeout)
	 *
	 * @param resourceKey Resource unique identifier
	 * @param username    Name used to establish the connection with the host via the IPMI protocol
	 * @param timeout     How long until the IPMI request times out
	 */
	static void validateIpmiInfo(final String resourceKey, final String username, final Long timeout) {
		validateAttribute(
			username,
			INVALID_STRING_CHECKER,
			() -> String.format(USERNAME_ERROR, resourceKey, IPMI_PROTOCOL)
		);

		validateAttribute(
			timeout,
			INVALID_TIMEOUT_CHECKER,
			() -> String.format(TIMEOUT_ERROR, resourceKey, IPMI_PROTOCOL, timeout)
		);
	}

	/**
	 * Validate the given SSH information (username, timeout)
	 *
	 * @param resourceKey Resource unique identifier
	 * @param username    Name to use for performing the SSH query
	 * @param timeout     How long until the command times out
	 */
	static void validateSshInfo(final String resourceKey, final String username, final Long timeout) {
		validateAttribute(username, INVALID_STRING_CHECKER, () -> String.format(USERNAME_ERROR, resourceKey, SSH_PROTOCOL));

		validateAttribute(
			timeout,
			INVALID_TIMEOUT_CHECKER,
			() -> String.format(TIMEOUT_ERROR, resourceKey, SSH_PROTOCOL, timeout)
		);
	}

	/**
	 * Validate the given WBEM information (username, timeout, port and vCenter)
	 *
	 * @param resourceKey Resource unique identifier
	 * @param username    Name used to establish the connection with the host via the WBEM protocol
	 * @param timeout     How long until the WBEM request times out
	 * @param port        The HTTP/HTTPS port number used to perform WBEM queries
	 * @param vCenter     vCenter hostname providing the authentication ticket, if applicable
	 */
	static void validateWbemInfo(
		final String resourceKey,
		final String username,
		final Long timeout,
		final Integer port,
		final String vCenter
	) {
		validateAttribute(
			timeout,
			INVALID_TIMEOUT_CHECKER,
			() -> String.format(TIMEOUT_ERROR, resourceKey, WBEM_PROTOCOL, timeout)
		);

		validateAttribute(port, INVALID_PORT_CHECKER, () -> String.format(PORT_ERROR, resourceKey, WBEM_PROTOCOL, port));

		validateAttribute(
			username,
			INVALID_STRING_CHECKER,
			() -> String.format(USERNAME_ERROR, resourceKey, WBEM_PROTOCOL)
		);

		validateAttribute(
			vCenter,
			EMPTY_STRING_CHECKER,
			() ->
				String.format(
					"Resource %s - Empty vCenter hostname configured for protocol %s." +
					" This resource will not be monitored. Please verify the configured vCenter hostname.",
					resourceKey,
					WBEM_PROTOCOL
				)
		);
	}

	/**
	 * Validate the given WMI information: timeout
	 *
	 * @param resourceKey Resource unique identifier
	 * @param timeout     How long until the WMI request times out
	 */
	static void validateWmiInfo(final String resourceKey, final Long timeout) {
		validateAttribute(
			timeout,
			INVALID_TIMEOUT_CHECKER,
			() -> String.format(TIMEOUT_ERROR, resourceKey, WMI_PROTOCOL, timeout)
		);
	}

	/**
	 * Validate the given HTTP information (timeout and port)
	 *
	 * @param resourceKey Resource unique identifier
	 * @param timeout     How long until the HTTP request times out
	 * @param port        The HTTP port number used to perform REST queries
	 */
	static void validateHttpInfo(final String resourceKey, final Long timeout, final Integer port) {
		validateAttribute(
			timeout,
			INVALID_TIMEOUT_CHECKER,
			() -> String.format(TIMEOUT_ERROR, resourceKey, HTTP_PROTOCOL, timeout)
		);

		validateAttribute(port, INVALID_PORT_CHECKER, () -> String.format(PORT_ERROR, resourceKey, HTTP_PROTOCOL, port));
	}

	/**
	 * Validate the given OS Command information: timeout
	 *
	 * @param resourceKey Resource unique identifier
	 * @param timeout     How long until the command times out
	 */
	static void validateOsCommandInfo(final String resourceKey, final Long timeout) {
		validateAttribute(
			timeout,
			INVALID_TIMEOUT_CHECKER,
			() -> String.format(TIMEOUT_ERROR, resourceKey, OS_COMMAND, timeout)
		);
	}

	/**
	 * Build the {@link HostConfiguration} expected by the internal engine
	 *
	 * @param resourceConfig     User's resource configuration
	 * @param selectedConnectors User's selected connectors
	 * @param excludedConnectors User's excluded connectors
	 * @param resourceKey        Resource unique identifier
	 * @return new {@link HostConfiguration} instance
	 */
	static HostConfiguration buildHostConfiguration(
		final ResourceConfig resourceConfig,
		final Set<String> selectedConnectors,
		final Set<String> excludedConnectors,
		final String resourceKey
	) {
		final ProtocolsConfig protocols = resourceConfig.getProtocols();

		final Map<Class<? extends IConfiguration>, IConfiguration> protocolConfigurations = protocols == null
			? new HashMap<>()
			: new HashMap<>(
				Stream
					.of(
						protocols.getSnmp(),
						protocols.getSsh(),
						protocols.getHttp(),
						protocols.getWbem(),
						protocols.getWmi(),
						protocols.getOsCommand(),
						protocols.getIpmi(),
						protocols.getWinRm()
					)
					.filter(Objects::nonNull)
					.map(AbstractProtocolConfig::toConfiguration)
					.filter(Objects::nonNull)
					.collect(Collectors.toMap(IConfiguration::getClass, Function.identity()))
			);

		final Map<String, String> attributes = resourceConfig.getAttributes();

		// Get the host name and make sure it is always set because the engine needs a hostname
		String hostname = attributes.get(MetricsHubConstants.HOST_NAME);
		if (hostname == null) {
			hostname = resourceKey;
		}

		// If we haven't a host.id then it will be set to the resource key
		String hostId = attributes.get("host.id");
		if (hostId == null) {
			hostId = resourceKey;
		}

		// Manage the device kind
		final DeviceKind hostType;
		String hostTypeAttribute = attributes.get("host.type");
		if (hostTypeAttribute == null) {
			hostType = DeviceKind.OTHER;
		} else {
			hostType = detectHostTypeFromAttribute(hostTypeAttribute);
		}

		String configuredConnectorId = null;
		// Retrieve the connector specified by the user in the metricshub.yaml configuration
		final Connector configuredConnector = resourceConfig.getConnector();
		// Check if a custom connector is defined
		if (configuredConnector != null) {
			// The custom connector is considered a selected connector from the engine's perspective
			configuredConnectorId = configuredConnector.getCompiledFilename();
		}

		return HostConfiguration
			.builder()
			.strategyTimeout(resourceConfig.getJobTimeout())
			.configurations(protocolConfigurations)
			.selectedConnectors(selectedConnectors)
			.excludedConnectors(excludedConnectors)
			.includeConnectorTags(resourceConfig.getIncludeConnectorTags())
			.hostname(hostname)
			.hostId(hostId)
			.hostType(hostType)
			.sequential(Boolean.TRUE.equals(resourceConfig.getSequential()))
			.configuredConnectorId(configuredConnectorId)
			.build();
	}

	/**
	 * Try to detect the {@link DeviceKind} from the user input
	 *
	 * @param hostTypeAttribute
	 * @return {@link DeviceKind} enumeration value
	 */
	private static DeviceKind detectHostTypeFromAttribute(String hostTypeAttribute) {
		try {
			return DeviceKind.detect(hostTypeAttribute);
		} catch (Exception e) {
			return DeviceKind.OTHER;
		}
	}

	/**
	 * Get the directory path of the given file
	 *
	 * @param file
	 * @return {@link Path} instance
	 */
	public static Path getDirectoryPath(final File file) {
		return file.getAbsoluteFile().toPath().getParent();
	}

	/**
	 * Read {@link MetricDefinitions} for the root monitor instance (Endpoint)
	 * which is automatically created by the MetricsHub engine
	 *
	 * @return new {@link MetricDefinitions} instance
	 * @throws IOException
	 */
	public static MetricDefinitions readHostMetricDefinitions() throws IOException {
		return JsonHelper.deserialize(
			newObjectMapper(),
			new ClassPathResource("metricshub-host-metrics.yaml").getInputStream(),
			MetricDefinitions.class
		);
	}

	/**
	 * Retrieves the metric definition map associated with the specified connector identifier
	 * within the provided {@link ConnectorStore}.
	 *
	 * @param connectorStore Wrapper for all connectors
	 * @param connectorId    The unique identifier of the connector
	 * @return An Optional containing a Map of metric names to their definitions, or empty if metric definitions are not found
	 */
	public static Optional<Map<String, MetricDefinition>> fetchMetricDefinitions(
		final ConnectorStore connectorStore,
		final String connectorId
	) {
		if (connectorStore != null && connectorId != null) {
			final Connector connector = connectorStore.getStore().get(connectorId);
			if (connector != null) {
				return Optional.ofNullable(connector.getMetrics());
			}
		}
		return Optional.empty();
	}

	/**
	 * Calculates the MD5 checksum of the specified file.
	 *
	 * @param file The file for which the MD5 checksum is to be calculated.
	 * @return The MD5 checksum as a hexadecimal string or <code>null</code> if the calculation has failed.
	 */
	public static String calculateMD5Checksum(final File file) {
		try {
			byte[] data = Files.readAllBytes(file.toPath());
			byte[] hash = MessageDigest.getInstance("MD5").digest(data);
			return new BigInteger(1, hash).toString(16);
		} catch (Exception e) {
			return null;
		}
	}
}
