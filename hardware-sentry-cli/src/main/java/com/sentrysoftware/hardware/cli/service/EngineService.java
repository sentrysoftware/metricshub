package com.sentrysoftware.hardware.cli.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.cli.component.cli.HardwareSentryCLI;
import com.sentrysoftware.hardware.cli.component.cli.protocols.SNMPCredentials;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.engine.Engine;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.strategy.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EngineService {

	private static final String DOT_CONNECTOR = ".connector";
	private static final String DOT_SEPARATOR = "\\.";
	@Autowired
	private JobResultFormatterService jobResultFormatterService;

	public String call(final HardwareSentryCLI data) {

		log.info("EngineService called with data {}", data);
		EngineConfiguration engineConf = new EngineConfiguration();

		engineConf.setTarget(new HardwareTarget(data.getHostname(), data.getHostname(), data.getDeviceType()));

		Set<IProtocolConfiguration> protocols = new HashSet<>();

		// for the moment we only manage SNMP protocol, so we will set
		// IProtocolConfiguration in this way
		if (null != data.getSnmpCredentials()) {
			SNMPProtocol snmpInstance = getSNMPCredentials(data.getSnmpCredentials());
			protocols.add(snmpInstance);
		}

		engineConf.setProtocolConfigurations(protocols);
		Map<String, Connector> allConnectors = ConnectorStore.getInstance().getConnectors();
		if (null != allConnectors) {
			Set<String> allConnectorKeySet = allConnectors.keySet();
			engineConf.setSelectedConnectors(
					getSelectedConnectors(allConnectorKeySet, data.getHdfs(), data.getHdfsExclusion()));
		}
		// run detection
		IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(data.getHostname());
		EngineResult detectionResult = new Engine().run(engineConf, hostMonitoring, new DetectionOperation());
		// run discovery
		EngineResult discoveryResult = new Engine().run(engineConf, hostMonitoring, new DiscoveryOperation());
		// run collect
		EngineResult collectResult = new Engine().run(engineConf, hostMonitoring, new CollectOperation());

		// Call the formatter with the HostMonitoring object
		return jobResultFormatterService.format(hostMonitoring);

	}

	/**
	 * Set @SNMPProtocol based on HardwareSentryCLi.snmpCredentials
	 * 
	 * @param cliSNMPCredentials
	 * @return
	 */
	public SNMPProtocol getSNMPCredentials(SNMPCredentials cliSNMPCredentials) {
		SNMPProtocol snmpInstance = new SNMPProtocol();

		snmpInstance.setVersion(cliSNMPCredentials.getSnmpVersion());
		snmpInstance.setCommunity(cliSNMPCredentials.getCommunity());
		snmpInstance.setPort(cliSNMPCredentials.getPort());
		snmpInstance.setTimeout(cliSNMPCredentials.getTimeout());
		snmpInstance.setPort(cliSNMPCredentials.getPort());

		snmpInstance.setUsername(cliSNMPCredentials.getUsername());
		snmpInstance.setPassword(cliSNMPCredentials.getPassword());
		snmpInstance.setPrivacyPassword(cliSNMPCredentials.getPrivacyPassword());
		snmpInstance.setPrivacy(cliSNMPCredentials.getPrivacy());

		return snmpInstance;
	}

	/**
	 * Return selected connectors, this can be : 1- automatic : this method will
	 * return an empty set, the engine will then proceed to the automatic selection based on
	 * the 2- userSelection : replace .hdfs by .connector 3- userExclusion : based
	 * on the ConnectorStore, filter
	 * 
	 * @param cliHdfs
	 * @param cliHdfsExclusion
	 * @return
	 */
	public Set<String> getSelectedConnectors(Set<String> allConnectors, Set<String> cliHdfs,
			Set<String> cliHdfsExclusion) {

		Set<String> selectedConnectors = new HashSet<>();
		if (null == allConnectors) {
			log.info("Cannot get connectors from the Connector Store, we trigger the automatic selection.");
			return selectedConnectors;
		}
		// In connector Store, the filename extension = .connector
		List<String> connectorStore = allConnectors.stream().map(f -> f.split(DOT_SEPARATOR)[0])
				.collect(Collectors.toList());
		List<String> hdfs = null;
		// In CLI, the filename extension = .hdfs
		if (null != cliHdfs) {
			hdfs = cliHdfs.stream().map(f -> f.split(DOT_SEPARATOR)[0]).collect(Collectors.toList());
		} else {
			if (null != cliHdfsExclusion) {
				List<String> hdfsExclusion = cliHdfsExclusion.stream().map(f -> f.split(DOT_SEPARATOR)[0])
						.collect(Collectors.toList());
				hdfs = connectorStore.stream().filter(f -> !hdfsExclusion.contains(f)).collect(Collectors.toList());
			}
		}
		// add the correct extension (.connector)
		if (null != hdfs) {
			hdfs.replaceAll(x -> x + DOT_CONNECTOR);
			selectedConnectors = new HashSet<>(hdfs);
		}

		return selectedConnectors;
	}
}
