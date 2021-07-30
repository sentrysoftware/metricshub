package com.sentrysoftware.hardware.prometheus.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sentrysoftware.hardware.prometheus.dto.ErrorCode;
import com.sentrysoftware.hardware.prometheus.dto.HostConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.exception.BusinessException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		final MultiHostsConfigurationDTO multiHostsConfigurationDTO = readConfiguration(targetConfigFile, connectors);

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
				log.error("Waiting for threads termination aborted with an error", e);
			}

		} else {

			HostConfigurationDTO hostConfigurationDTO = multiHostsConfigurationDTO
				.getTargets()
				.stream()
				.filter(hostConfiguration -> hostConfiguration.getTarget().getHostname().equals(targetId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(String.format("Invalid target ID: %s", targetId)));

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

		// Detection
		EngineResult lastEngineResult = hostMonitoring.run(new DetectionOperation(), new DiscoveryOperation(),
			new CollectOperation());
		log.info("Last job status: {}", lastEngineResult.getOperationStatus());
	}

	/**
	 * Reads the user's configuration.
	 * 
	 * @return						A {@link MultiHostsConfigurationDTO} instance.
	 *
	 * @throws BusinessException	If a read error occurred.
	 */
	MultiHostsConfigurationDTO readConfiguration(final File targetConfigFile, Map<String, Connector> connectors)
		throws BusinessException {

		final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

		try {

			MultiHostsConfigurationDTO multiHostsConfigurationDTO = mapper.readValue(targetConfigFile,
				MultiHostsConfigurationDTO.class);

			multiHostsConfigurationDTO
				.getTargets()
				.forEach(hostConfigurationDTO -> {

					Set<String> selectedConnectors = getSelectedConnectors(connectors.keySet(),
						hostConfigurationDTO.getSelectedConnectors(), hostConfigurationDTO.getExcludedConnectors());

					EngineConfiguration engineConfiguration = buildEngineConfiguration(hostConfigurationDTO, selectedConnectors);

					hostMonitoringMap.putIfAbsent(hostConfigurationDTO.getTarget().getHostname(),
						HostMonitoringFactory.getInstance().createHostMonitoring(
							hostConfigurationDTO.getTarget().getHostname(), engineConfiguration));
				});

			return multiHostsConfigurationDTO;

		} catch (IOException e) {

			throw new BusinessException(ErrorCode.CANNOT_READ_CONFIGURATION,
				"IOException when reading the configuration file: " + targetConfigFile.getAbsolutePath());
		}
	}

	/**
	 * Build the {@link EngineConfiguration} instance from the given {@link HostConfigurationDTO}
	 * 
	 * @param exporterConfig		User's configuration
	 * @param selectedConnectors	The connector names, the matrix engine will run
	 *
	 * @return						The built {@link EngineConfiguration}.
	 */
	static EngineConfiguration buildEngineConfiguration(final HostConfigurationDTO exporterConfig,
														final Set<String> selectedConnectors) {

		final HardwareTarget target = exporterConfig.getTarget();

		// The id is the hostname itself
		target.setId(target.getHostname());

		final Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations =
			new HashMap<>(Stream
			.of(exporterConfig.getSnmp(),
				exporterConfig.getCiscoUcs(),
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
			.target(target)
			.unknownStatus(exporterConfig.getUnknownStatus())
			.build();
	}

	/**
	 * Return selected connectors, this can be:
	 * <ol>
	 *   <li><em>automatic</em>: this method will return an empty set, the engine will then proceed to the automatic detection</li>
	 *   <li><em>userSelection</em>: replace .hdfs by .connector
	 *   <li><em>userExclusion</em>: based on the ConnectorStore, filter the connectors
	 * </ol>
	 * 
	 * @param allConnectors      All connectors from the {@link ConnectorStore}
	 * @param selectedConnectors User's selected connectors
	 * @param excludedConnectors User's excluded connectors
	 * 
	 * @return {@link Set} containing the selected connector names
	 */
	static Set<String> getSelectedConnectors(final Set<String> allConnectors, final Set<String> selectedConnectors,
			final Set<String> excludedConnectors) {

		final Set<String> result = new HashSet<>();

		// In connector Store, the filename extension = .connector
		final List<String> connectorStore = getConnectorsWithoutExtension(allConnectors);
		List<String> connectors = null;

		// In the configuration, the filename extension = .hdfs
		if (selectedConnectors != null && !selectedConnectors.isEmpty()) {
			connectors = getConnectorsWithoutExtension(selectedConnectors);

		} else if (excludedConnectors != null && !excludedConnectors.isEmpty()) {

			final List<String> connectorExclusion = getConnectorsWithoutExtension(excludedConnectors);
			connectors = connectorStore.stream().filter(f -> !connectorExclusion.contains(f)).collect(Collectors.toList());
		}

		// add the correct extension (.connector)
		if (connectors != null) {
			connectors.replaceAll(f -> f + HardwareConstants.DOT + HardwareConstants.CONNECTOR);
			result.addAll(connectors);
		}

		return result;
	}

	/**
	 * Remove the extension from the given set of connector names and return a new list
	 * 
	 * @param connectors The connector names we wish to process
	 * @return {@link List} of String elements
	 */
	static List<String> getConnectorsWithoutExtension(final Set<String> connectors) {
		return connectors
				.stream()
				.map(f -> f.substring(0, f.lastIndexOf('.')))
				.collect(Collectors.toList());
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
