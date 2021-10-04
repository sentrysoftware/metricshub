package com.sentrysoftware.hardware.prometheus.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sentrysoftware.hardware.prometheus.dto.ErrorCode;
import com.sentrysoftware.hardware.prometheus.dto.HardwareTargetDTO;
import com.sentrysoftware.hardware.prometheus.dto.HostConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.IProtocolConfigDTO;
import com.sentrysoftware.hardware.prometheus.exception.BusinessException;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access =  AccessLevel.PRIVATE)
@Slf4j
public class ConfigHelper {

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

		final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

		return mapper.readValue(file, type);

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
			.unknownStatus(hostConfigurationDto.getUnknownStatus())
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
	 * @param configFile             the target configuration file
	 * @param acceptedConnectorNames set of accepted compiled connector names
	 * @return Map of {@link IHostMonitoring} instances indexed by the target id
	 */
	public static Map<String, IHostMonitoring> buildHostMonitoringMap(final File configFile, Set<String> acceptedConnectorNames) {

		final MultiHostsConfigurationDTO multiHostsConfigurationDto = readConfigurationSafe(configFile);

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

}
