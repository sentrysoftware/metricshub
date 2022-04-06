package com.sentrysoftware.hardware.agent.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sentrysoftware.hardware.agent.dto.ErrorCode;
import com.sentrysoftware.hardware.agent.dto.HardwareTargetDTO;
import com.sentrysoftware.hardware.agent.dto.HostConfigurationDTO;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.agent.dto.protocol.IProtocolConfigDTO;
import com.sentrysoftware.hardware.agent.exception.BusinessException;
import com.sentrysoftware.hardware.agent.security.PasswordEncrypt;
import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.security.SecurityManager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access =  AccessLevel.PRIVATE)
@Slf4j
public class ConfigHelper {

	public static final Path DEFAULT_OUTPUT_DIRECTORY = getSubDirectory("logs", true);

	/**
	 * Deserialize YAML configuration file.
	 * @param <T>
	 *
	 * @param file YAML file
	 * @param type the value type to return
	 *
	 * @return new instance of type T
	 * 
	 * @throws IOException
	 *
	 */
	static <T> T deserializeYamlFile(final File file, final Class<T> type) throws IOException {

		// Since 2.13 use JsonMapper.builder().enable(...)
		return JsonMapper
				.builder(new YAMLFactory())
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
				.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
				.build()
				.readValue(file, type);

	}


	/**
	 * Validate the given target information (hostname and targetType)
	 *
	 * @param targetType type of the target
	 * @param hostname   hostname of the target
	 * @throws BusinessException
	 */
	static void validateTarget(final TargetType targetType, final String hostname) throws BusinessException {

		if (hostname == null || hostname.isBlank()) {
			String message = String.format("Invalid hostname: %s", hostname);
			log.error(message);
			throw new BusinessException(ErrorCode.INVALID_HOSTNAME, message);
		}

		if (targetType == null) {
			String message = String.format("No target type configured for hostname: %s", hostname);
			log.error(message);
			throw new BusinessException(ErrorCode.NO_TARGET_TYPE, message);
		}
	}

	/**
	 * Build the {@link EngineConfiguration} instance from the given
	 * {@link HostConfigurationDTO}
	 *
	 * @param hostConfigurationDto User's configuration
	 * @param selectedConnectors   The connector names, the matrix engine should run
	 * @param excludedConnectors   The connector names, the matrix engine should skip
	 *
	 * @return The built {@link EngineConfiguration}.
	 */
	static EngineConfiguration buildEngineConfiguration(final HostConfigurationDTO hostConfigurationDto,
			final Set<String> selectedConnectors, final Set<String> excludedConnectors) {

		final HardwareTargetDTO target = hostConfigurationDto.getTarget();

		final Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations = 
				new HashMap<>(
						Stream.of(
								hostConfigurationDto.getSnmp(),
								hostConfigurationDto.getSsh(),
								hostConfigurationDto.getHttp(),
								hostConfigurationDto.getWbem(),
								hostConfigurationDto.getWmi(),
								hostConfigurationDto.getOsCommand(),
								hostConfigurationDto.getIpmi()
						)
						.filter(Objects::nonNull)
						.map(IProtocolConfigDTO::toProtocol)
						.filter(Objects::nonNull)
						.collect(Collectors.toMap(IProtocolConfiguration::getClass, Function.identity())));

		return EngineConfiguration
			.builder()
			.operationTimeout(hostConfigurationDto.getOperationTimeout())
			.protocolConfigurations(protocolConfigurations)
			.selectedConnectors(selectedConnectors)
			.excludedConnectors(excludedConnectors)
			.target(target.toHardwareTarget())
			.sequential(Boolean.TRUE.equals(hostConfigurationDto.getSequential()))
			.build();
	}

	/**
	 * Return configured connector names. This method throws a BusinessException if
	 * we encounter an unknown connector
	 *
	 * @param acceptedConnectorNames Known connector names (connector compiled file names)
	 * @param configConnectors       user's selected or excluded connectors
	 * @param hostname               target hostname
	 *
	 * @return {@link Set} containing the selected connector names
	 * @throws BusinessException
	 */
	static Set<String> validateAndGetConnectors(final @NonNull Set<String> acceptedConnectorNames,
			final Set<String> configConnectors, final String hostname) throws BusinessException {

		if (configConnectors == null || configConnectors.isEmpty()) {
			return Collections.emptySet();
		}

		// Get unknown connectors
		final Set<String> unknownConnectors = configConnectors
				.stream()
				.filter(compiledFileName -> !acceptedConnectorNames.contains(compiledFileName))
				.collect(Collectors.toSet());

		// Check unknown connectors
		if (unknownConnectors.isEmpty()) {
			return configConnectors;
		}

		// Throw the bad configuration exception
		String message = String.format(
				"Configured unknown connector(s): %s. Hostname: %s",
				String.join(", ", unknownConnectors),
				hostname
		);

		log.error(message);

		throw new BusinessException(ErrorCode.BAD_CONNECTOR_CONFIGURATION, message);
	}

	/**
	 * Reads the user's configuration.
	 *
	 * @param configFile the configuration file
	 *
	 * @return A {@link MultiHostsConfigurationDTO} instance.
	 *
	 * @throws BusinessException if a read error occurred.
	 */
	public static MultiHostsConfigurationDTO readConfigurationSafe(final File configFile) {
		try {
			final MultiHostsConfigurationDTO multiHostsConfig = deserializeYamlFile(configFile, MultiHostsConfigurationDTO.class);

			multiHostsConfig.getTargets().forEach(configDto -> {
				HardwareTargetDTO target = configDto.getTarget();
				// Make sure the target id is always set
				if (target.getId() == null) {
					target.setId(target.getHostname());
				}

				// Set global collect period if there is no specific collect period on the target
				if (configDto.getCollectPeriod() == null) {
					configDto.setCollectPeriod(multiHostsConfig.getCollectPeriod());
				}

				// Set global collect period if there is no specific collect period on the target
				if (configDto.getDiscoveryCycle() == null) {
					configDto.setDiscoveryCycle(multiHostsConfig.getDiscoveryCycle());
				}

				// Set the global level in the target log level.
				// Always the global logger settings wins as the matrix logger
				// 'com.sentrysoftware', is created only once and handles the Level globally for
				// all the targets.
				configDto.setLoggerLevel(multiHostsConfig.getLoggerLevel());
				configDto.setOutputDirectory(multiHostsConfig.getOutputDirectory());

				// Set global sequential flag in the target configuration if this target doesn't define the sequential flag
				// It is more practical to set the flag only once when the requirement is that each target must run the network calls in serial mode
				if (configDto.getSequential() == null) {
					configDto.setSequential(multiHostsConfig.isSequential());
				}

				// Set the hardware problem template for alerting
				if (configDto.getHardwareProblemTemplate() == null) {
					configDto.setHardwareProblemTemplate(multiHostsConfig.getHardwareProblemTemplate());
				}

				// Set the disableAlerting flag to enable or disable alerting
				if (configDto.getDisableAlerting() == null) {
					configDto.setDisableAlerting(multiHostsConfig.isDisableAlerting());
				}
			});

			return multiHostsConfig;
		} catch (Exception e) {
			log.info("Cannot read the configuration file {}", configFile.getAbsoluteFile());
			log.debug("Exception: ", e);
			return MultiHostsConfigurationDTO.empty();

		}
	}

	/**
	 * Build the {@link IHostMonitoring} map. Each entry is index by the targetId
	 * 
	 * @param multiHostsConfigurationDto DTO that wraps the agent configuration for all the targets
	 * @param acceptedConnectorNames     set of accepted compiled connector names
	 * @return Map of {@link IHostMonitoring} instances indexed by the target id
	 */
	public static Map<String, IHostMonitoring> buildHostMonitoringMap(final MultiHostsConfigurationDTO multiHostsConfigurationDto,
			final Set<String> acceptedConnectorNames) {

		final Map<String, IHostMonitoring> hostMonitoringMap = new HashMap<>();

		multiHostsConfigurationDto
			.getTargets()
			.forEach(hostConfigurationDto -> 
				fillHostMonitoringMap(hostMonitoringMap, acceptedConnectorNames, hostConfigurationDto));

		return hostMonitoringMap;

	}

	/**
	 * Create a new {@link IHostMonitoring} instance for the given
	 * {@link HostConfigurationDTO} and update the host monitoring map
	 * 
	 * @param hostMonitoringMap      Map of {@link IHostMonitoring} instances indexed by the targetId
	 * @param acceptedConnectorNames set of accepted compiled connector names
	 * @param hostConfigurationDto   the host configuration we wish to process in order to build
	 *                               the {@link IHostMonitoring} instance
	 */
	public static void fillHostMonitoringMap(final Map<String, IHostMonitoring> hostMonitoringMap,
			final Set<String> acceptedConnectorNames, final HostConfigurationDTO hostConfigurationDto) {

		final String hostname = hostConfigurationDto.getTarget().getHostname();

		try {
			validateTarget(hostConfigurationDto.getTarget().getType(), hostname);

			final Set<String> selectedConnectors = validateAndGetConnectors(
					acceptedConnectorNames,
					hostConfigurationDto.getSelectedConnectors(),
					hostname
			);
			final Set<String> excludedConnectors =  validateAndGetConnectors(
					acceptedConnectorNames,
					hostConfigurationDto.getExcludedConnectors(),
					hostname
			);

			final EngineConfiguration engineConfiguration = buildEngineConfiguration(
					hostConfigurationDto,
					selectedConnectors,
					excludedConnectors
			);

			// targetId can never be null here
			final String targetId = hostConfigurationDto.getTarget().getId();

			hostMonitoringMap.putIfAbsent(
					targetId,
					HostMonitoringFactory.getInstance().createHostMonitoring(targetId, engineConfiguration)
			);

		} catch (Exception e) {

			log.warn("The given target has been staged as invalid. Target: {}", hostConfigurationDto);

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
	 * @param dir    the directory assumed under the product directory. E.g. logs
	 *               assumed under /usr/local/bin/hws-otel-collector
	 * @param create indicate if we should create the sub directory or not
	 * @return The absolute path of the sub directory
	 */
	public static Path getSubDirectory(@NonNull final String dir, boolean create) {

		Path subDirectory = getSubPath(dir);
		if (!create) {
			return subDirectory;
		}

		try {
			return Files.createDirectories(subDirectory).toRealPath();
		} catch (IOException e) {
			throw new IllegalStateException("Could not create " + dir + " directory " + subDirectory, e);
		}
	}

	/**
	 * Get the sub path under the home directory. E.g. <em>/usr/local/bin/hws-otel-collector/lib/../config</em>
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
			throw new IllegalStateException("Error detected when getting local source file", e);
		}

		if (me == null) {
			throw new IllegalStateException("Could not get the local source file.");
		}
		return me;
	}

	/**
	 * Decrypt the given crypted password.
	 * 
	 * @param crypted
	 * @return char array
	 */
	public static char[] decrypt(final char[] crypted) {
		try {
			return SecurityManager.decrypt(crypted, PasswordEncrypt.getKeyStoreFile(false));
		} catch(Exception e) {
			// This is a real problem, let's log the error
			log.error("Could not decrypt password: {}", e.getMessage());
			log.debug("Exception", e);
			return crypted;
		}
	}
}
