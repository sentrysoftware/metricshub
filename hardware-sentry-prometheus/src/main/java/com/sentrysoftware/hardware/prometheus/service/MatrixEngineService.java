package com.sentrysoftware.hardware.prometheus.service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sentrysoftware.hardware.prometheus.dto.ErrorCode;
import com.sentrysoftware.hardware.prometheus.dto.HardwareTargetDTO;
import com.sentrysoftware.hardware.prometheus.dto.HostConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.exception.BusinessException;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MatrixEngineService {

	@Value("${target.config.file}")
	private File targetConfigFile;

	@Value("${server.port:8080}")
	private int serverPort;

	@Value("${http.port:8080}")
	private int httpPort;

	@Value("${debugMode:false}")
	private boolean debugMode;

	@Value("${outputDirectory}")
	private String outputDirectory;

	@Value("${server.ssl.enabled:false}")
	private boolean sslEnabled;

	@Autowired
	private ConnectorStore store;

	@Autowired
	private Map<String, IHostMonitoring> hostMonitoringMap;

	/**
	 * Calls the matrix engine to perform detection, discovery and collect strategies.
	 *
	 * @param targetId				The ID of the target.<br>
	 *                              When null, indicates that the strategies
	 *                              should be performed on all configured targets
	 *
	 * @throws BusinessException	If no connectors lookup were found in the store.
	 */
	public void performJobs(final String targetId) throws BusinessException {

		final Map<String, Connector> connectors = store.getConnectors();
		if (connectors == null || connectors.isEmpty()) {
			throw new BusinessException(ErrorCode.BAD_CONNECTOR_STORE, "Could not get the connector lookup for the store.");
		}

		// Read the configuration
		final MultiHostsConfigurationDTO multiHostsConfigurationDTO = readConfiguration(targetConfigFile, connectors, targetId);

		if (targetId == null) {

			final ExecutorService pool = Executors.newFixedThreadPool(multiHostsConfigurationDTO.getMaxHostThreadsPerExporter());

			// Loop over each host and run a new task
			for (HostConfigurationDTO hostConfigurationDTO : multiHostsConfigurationDTO.getTargets()) {

				// run a task for each host
				pool.execute(() -> performJobs(hostConfigurationDTO));
			}

			// Order the shutdown
			pool.shutdown();

			try {
				// Blocks until all tasks have completed execution after a shutdown request
				pool.awaitTermination(multiHostsConfigurationDTO.getMaxHostThreadsTimeout(), TimeUnit.SECONDS);
			} catch (Exception e) {
				if (e instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}

				log.error("Waiting for threads termination aborted with an error", e);
			}

		} else {

			HostConfigurationDTO hostConfigurationDTO = multiHostsConfigurationDTO
				.getTargets()
				.stream()
				.filter(hostConfiguration -> hostConfiguration.getTarget().getHostname().equals(targetId))
				.findFirst()
				.orElseThrow(() -> new BusinessException(ErrorCode.TARGET_NOT_FOUND, String.format("Invalid target ID: %s", targetId)));

			performJobs(hostConfigurationDTO);
		}
	}

	/**
	 * Calls the matrix engine to perform detection, discovery and collect strategies.
	 *
	 * @param hostConfigurationDTO	The configuration for the target currently being processed.
	 */
	private void performJobs(HostConfigurationDTO hostConfigurationDTO) {

		// Set the context for the logger
		configureLoggerContext(hostConfigurationDTO);

		log.info("MatrixEngineService called for system {}", hostConfigurationDTO.getTarget().getHostname());
		log.info("Server Port: {}", serverPort);

		final IHostMonitoring hostMonitoring = hostMonitoringMap.get(hostConfigurationDTO.getTarget().getHostname());

		// Detection, Discovery and Collect
		EngineResult lastEngineResult = hostMonitoring.run(new DetectionOperation(), new DiscoveryOperation(),
			new CollectOperation());
		log.info("Last job status: {}", lastEngineResult.getOperationStatus());
	}

	/**
	 * Reads the user's configuration.
	 * 
	 * @param configFile the configuration file
	 * @param connectors Map of connectors from the matrix store
	 * @param targetId   the target id
	 * 
	 * @return A {@link MultiHostsConfigurationDTO} instance.
	 *
	 * @throws BusinessException If a read error occurred.
	 */
	MultiHostsConfigurationDTO readConfiguration(final File configFile, @NonNull Map<String, Connector> connectors, String targetId)
		throws BusinessException {

		final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

		try {

			MultiHostsConfigurationDTO multiHostsConfigurationDTO = mapper.readValue(configFile, MultiHostsConfigurationDTO.class);

			final Set<HostConfigurationDTO> invalidTargets = new HashSet<>();

			multiHostsConfigurationDTO
				.getTargets()
				.forEach(hostConfigurationDTO -> {

					final String hostname = hostConfigurationDTO.getTarget().getHostname();

					try {
						validateTarget(hostConfigurationDTO.getTarget().getType(), hostname);

						final Set<String> selectedConnectors = getConnectors(
								connectors, hostConfigurationDTO.getSelectedConnectors(), "selected", hostname);
						final Set<String> excludedConnectors =  getConnectors(
								connectors, hostConfigurationDTO.getExcludedConnectors(), "excluded", hostname);

						final EngineConfiguration engineConfiguration = buildEngineConfiguration(hostConfigurationDTO,
								selectedConnectors, excludedConnectors);

						hostMonitoringMap.putIfAbsent(
								hostname,
								HostMonitoringFactory.getInstance().createHostMonitoring(hostname, engineConfiguration)
						);

					} catch (BusinessException e) {

						// Means we query a specific target, in multiple hosts mode (/metrics) we can only log an error
						if (targetId != null) {
							throw new RuntimeException(e); // NOSONAR
						} else {
							log.warn("The given target has been staged as invalid. Target: {}", hostConfigurationDTO);
							invalidTargets.add(hostConfigurationDTO);
						}

					}

				});

			// Remove invalid targets
			invalidTargets.forEach(invalid -> multiHostsConfigurationDTO.getTargets().remove(invalid));

			return multiHostsConfigurationDTO;

		} catch (IOException e) {

			throw new BusinessException(ErrorCode.CANNOT_READ_CONFIGURATION,
				"IOException when reading the configuration file: " + configFile.getAbsolutePath());

		} catch(Exception e) {
			if (e.getCause() instanceof BusinessException) {
				throw (BusinessException) e.getCause();
			}

			throw new BusinessException(ErrorCode.GENERAL_ERROR, "Error detected when reading configuration", e);
		}
	}

	/**
	 * validate the given target information (hostname and targetType)
	 * 
	 * @param targetType type of the target
	 * @param hostname   hostname of the target
	 * @throws BusinessException
	 */
	static void validateTarget(final TargetType targetType, final String hostname) throws BusinessException {

		if (hostname == null || hostname.isBlank()) {
			String message = String.format("Invalid hostname: %s.", hostname);
			log.error(message);
			throw new BusinessException(ErrorCode.INVALID_HOSTNAME, message);
		}

		if (targetType == null) {
			String message = String.format("No target type configured for hostname: %s.", hostname);
			log.error(message);
			throw new BusinessException(ErrorCode.NO_TARGET_TYPE, message);
		}
	}

	/**
	 * Build the {@link EngineConfiguration} instance from the given {@link HostConfigurationDTO}
	 * 
	 * @param exporterConfig		User's configuration
	 * @param selectedConnectors	The connector names, the matrix engine will run
	 * @param excludedConnectors    The connector names, the matrix engine will skip
	 *
	 * @return						The built {@link EngineConfiguration}.
	 */
	static EngineConfiguration buildEngineConfiguration(final HostConfigurationDTO exporterConfig,
														final Set<String> selectedConnectors, Set<String> excludedConnectors) {

		final HardwareTargetDTO target = exporterConfig.getTarget();

		// The id is the hostname itself
		target.setId(target.getHostname());

		final Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations =
			new HashMap<>(Stream
			.of(exporterConfig.getSnmp() != null ? exporterConfig.getSnmp().toProtocol() : null,
				exporterConfig.getSsh(),
				exporterConfig.getHttp(),
				exporterConfig.getWbem(),
				exporterConfig.getWmi(),
				exporterConfig.getOsCommand(),
				exporterConfig.getIpmi())
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(IProtocolConfiguration::getClass, Function.identity())));

		return EngineConfiguration
			.builder()
			.operationTimeout(exporterConfig.getOperationTimeout())
			.protocolConfigurations(protocolConfigurations)
			.selectedConnectors(selectedConnectors)
			.excludedConnectors(excludedConnectors)
			.target(target.toHardwareTarget())
			.unknownStatus(exporterConfig.getUnknownStatus())
			.build();
	}

	/**
	 * Return configured connector names. This method throws a BusinessException if we encounter an unknown connector
	 * 
	 * @param connectors         all connectors from the {@link ConnectorStore}
	 * @param configConnectors   user's selected or excluded connectors
	 * @param mode               selected or excluded
	 * @param hostname           the hostname we currently read its configuration
	 * 
	 * @return {@link Set} containing the selected connector names
	 * @throws BusinessException 
	 */
	static Set<String> getConnectors(final @NonNull Map<String, Connector> connectors, final Set<String> configConnectors,
			final String mode, String hostname) throws BusinessException {

		if (configConnectors == null || configConnectors.isEmpty()) {
			return Collections.emptySet();
		}

		// Get the unknown connectors
		final Set<String> unknownConnectors = configConnectors
				.stream()
				.filter(compiledFileName -> !connectors.containsKey(compiledFileName))
				.collect(Collectors.toSet());

		// Check the unknown connectors
		if (unknownConnectors.isEmpty()) {
			return configConnectors;
		}

		// Throw the bad configuration exception
		String message = String.format("Configured unknown %s connector(s): %s. Hostname: %s", 
				mode, unknownConnectors.stream().collect(Collectors.joining(", ")), hostname);

		log.error(message);

		throw new BusinessException(ErrorCode.BAD_CONNECTOR_CONFIGURATION, message);
	}

	/**
	 * Configure the logger context with the targetId, port, debugMode and outputDirectory.
	 *
	 * @param hostConfigurationDTO	The host configuration from which the hostname should be extracted.
	 */
	private void configureLoggerContext(HostConfigurationDTO hostConfigurationDTO) {

		ThreadContext.put("targetId", hostConfigurationDTO.getTarget().getHostname());
		ThreadContext.put("debugMode", String.valueOf(debugMode));
		ThreadContext.put("port", String.valueOf(sslEnabled ? httpPort : serverPort));

		if (outputDirectory != null) {
			ThreadContext.put("outputDirectory", outputDirectory);
		}
	}
}
