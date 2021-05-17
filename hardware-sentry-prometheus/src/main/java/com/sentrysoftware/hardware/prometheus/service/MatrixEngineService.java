package com.sentrysoftware.hardware.prometheus.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sentrysoftware.hardware.prometheus.dto.ErrorCode;
import com.sentrysoftware.hardware.prometheus.dto.HostConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.exception.BusinessException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.engine.Engine;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MatrixEngineService {

	@Value("${target.config.file}")
	private File targetConfigFile;

	@Autowired
	private IHostMonitoring hostMonitoring;

	@Autowired
	private ConnectorStore store;

	@Autowired
	private Engine engine;

	/**
	 * Call the matrix engine to perform detection, discovery and collect strategies
	 * 
	 * @throws BusinessException
	 */
	public void performJobs() throws BusinessException {

		// Read the configuration
		final HostConfigurationDTO hostConfigurationDTO = readConfiguration(targetConfigFile);

		log.info("MatrixEngineService called for system {}", hostConfigurationDTO.getTarget().getHostname());

		final Map<String, Connector> connectors = store.getConnectors();
		if (connectors == null || connectors.isEmpty()) {
			throw new BusinessException(ErrorCode.BAD_CONNECTOR_STORE, "Could not get the connector lookup for the store.");
		}

		final Set<String> selectedConnectors = getSelectedConnectors(connectors.keySet(),
				hostConfigurationDTO.getSelectedConnectors(), hostConfigurationDTO.getExcludedConnectors());

		final EngineConfiguration engineConfiguration = buildEngineConfiguration(hostConfigurationDTO, selectedConnectors);

		// Detection
		final EngineResult detectionResult = engine.run(engineConfiguration, hostMonitoring, new DetectionOperation());
		log.info("Detection Status {}", detectionResult.getOperationStatus());

		// Discovery
		final EngineResult discoveryResult = engine.run(engineConfiguration, hostMonitoring, new DiscoveryOperation());
		log.info("Discovery Status {}", discoveryResult.getOperationStatus());

		// Collect
		final EngineResult collectResult = engine.run(engineConfiguration, hostMonitoring, new CollectOperation());
		log.info("Collect Status {}", collectResult.getOperationStatus());

	}

	/**
	 * Read the user's configuration
	 * 
	 * @return {@link HostConfigurationDTO} instance
	 * @throws BusinessException
	 */
	HostConfigurationDTO readConfiguration(final File targetConfigFile) throws BusinessException {

		final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

		try {
			return mapper.readValue(targetConfigFile, HostConfigurationDTO.class);
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.CANNOT_READ_CONFIGURATION,
					"IOException when reading the configuration file: hardware-sentry-config.yml");
		}
	}


	/**
	 * Build the {@link EngineConfiguration} instance from the given {@link HostConfigurationDTO}
	 * 
	 * @param exporterConfig     User's configuration
	 * @param selectedConnectors The connector names, the matrix engine will run
	 * @return
	 */
	static EngineConfiguration buildEngineConfiguration(final HostConfigurationDTO exporterConfig, final Set<String> selectedConnectors) {

		final HardwareTarget target = exporterConfig.getTarget();

		// The id is the hostname itself
		target.setId(target.getHostname());

		final Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations = new HashMap<>();
		protocolConfigurations.putAll(Stream
				.of(exporterConfig.getSnmp(),
						exporterConfig.getCiscoUcs(),
						exporterConfig.getSsh(),
						exporterConfig.getHttp(),
						exporterConfig.getWbem(),
						exporterConfig.getWmi(),
						exporterConfig.getHttp())
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(IProtocolConfiguration::getClass, Function.identity())));

		return EngineConfiguration.builder()
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
	 * @param allConnectors      All conncetors from the {@link ConnectorStore}
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
				.map(f -> f.split(HardwareConstants.DOT_ESCAPED)[0])
				.collect(Collectors.toList());
	}

}
