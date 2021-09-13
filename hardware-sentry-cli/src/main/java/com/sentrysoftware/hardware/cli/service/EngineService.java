package com.sentrysoftware.hardware.cli.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.cli.component.cli.HardwareSentryCli;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EngineService {

	@Autowired
	private JobResultFormatterService jobResultFormatterService;

	public String call(final HardwareSentryCli data) {

		log.info("EngineService called with data {}", data);
		EngineConfiguration engineConf = new EngineConfiguration();

		engineConf.setTarget(new HardwareTarget(data.getHostname(), data.getHostname(), data.getDeviceType()));
		engineConf.setProtocolConfigurations(getProtocols(data));

		Map<String, Connector> allConnectors = ConnectorStore.getInstance().getConnectors();
		if (allConnectors != null) {
			final Set<String> allConnectorNames = allConnectors.keySet();
			engineConf.setSelectedConnectors(getConnectors(allConnectorNames, data.getConnectors()));
			engineConf.setExcludedConnectors(getConnectors(allConnectorNames, data.getExcludedConnectors()));
		}

		// run jobs
		IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(data.getHostname(),
			engineConf);
		EngineResult engineResult = hostMonitoring.run(new DetectionOperation(), new DiscoveryOperation(),
			new CollectOperation());
		log.info("Jobs status: {}", engineResult.getOperationStatus());

		// Call the formatter with the HostMonitoring object
		return jobResultFormatterService.format(hostMonitoring);
	}

	/**
	 * @param hardwareSentryCli	The {@link HardwareSentryCli} instance calling this service.
	 *
	 * @return A {@link Map} associating the input protocol type to its input credentials.
	 */
	private Map<Class< ? extends IProtocolConfiguration>, IProtocolConfiguration> getProtocols(
		HardwareSentryCli hardwareSentryCli) {

		Map<Class< ? extends IProtocolConfiguration>, IProtocolConfiguration> protocols = new HashMap<>();

		if (hardwareSentryCli.getSnmpConfig() != null) {
			protocols.put(SNMPProtocol.class, hardwareSentryCli.getSnmpConfig().toProtocol(hardwareSentryCli.getUsername(), hardwareSentryCli.getPassword()));
		}

		if (hardwareSentryCli.getWmiConfig() != null) {

			protocols.put(WMIProtocol.class, hardwareSentryCli.getWmiConfig().toProtocol(hardwareSentryCli.getUsername(), hardwareSentryCli.getPassword()));
		}

		if (hardwareSentryCli.getWbemConfig() != null) {

			protocols.put(WBEMProtocol.class, hardwareSentryCli.getWbemConfig().toProtocol(hardwareSentryCli.getUsername(), hardwareSentryCli.getPassword()));
		}

		if (hardwareSentryCli.getHttpConfig() != null) {

			protocols.put(HTTPProtocol.class, hardwareSentryCli.getHttpConfig().toProtocol(hardwareSentryCli.getUsername(), hardwareSentryCli.getPassword()));
		}

		if (hardwareSentryCli.getIpmiConfig() != null) {

			protocols.put(IPMIOverLanProtocol.class, hardwareSentryCli.getIpmiConfig().toProtocol(hardwareSentryCli.getUsername(), hardwareSentryCli.getPassword()));
		}

		return protocols;
	}

	/**
	 * Return configured connector names with the .connector extension. This method excludes badly configured connectors.
	 *
	 * @param allConnectors      All connectors from the {@link ConnectorStore}
	 * @param configConnectors   User's selected or excluded connectors
	 *
	 * @return {@link Set} containing the selected connector names
	 */
	static Set<String> getConnectors(final Set<String> allConnectors, final Set<String> configConnectors) {

		final Set<String> result = new HashSet<>();

		// In connector Store, the filename extension = .connector
		final List<String> connectorStore = getConnectorsWithoutExtension(allConnectors);
		List<String> connectors = null;

		// In the configuration, the filename extension = .hdfs
		if (configConnectors != null && !configConnectors.isEmpty()) {
			connectors = getConnectorsWithoutExtension(configConnectors);
		}

		// add the correct extension (.connector)
		if (connectors != null) {
			// Send only known connectors
			connectors = connectors.stream().filter(connectorStore::contains).collect(Collectors.toList());
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
}
