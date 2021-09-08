package com.sentrysoftware.hardware.cli.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.hardware.cli.component.cli.protocols.HttpCredentials;
import com.sentrysoftware.hardware.cli.component.cli.protocols.IpmiCredentials;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.cli.component.cli.HardwareSentryCli;
import com.sentrysoftware.hardware.cli.component.cli.protocols.SnmpCredentials;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WbemCredentials;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WmiCredentials;
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

import static org.springframework.util.Assert.notNull;

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
			engineConf.setSelectedConnectors(getConnectors(allConnectorNames, data.getHdfs()));
			engineConf.setExcludedConnectors(getConnectors(allConnectorNames, data.getHdfsExclusion()));
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

		if (hardwareSentryCli.getSnmpCredentials() != null) {

			protocols.put(SNMPProtocol.class, getSnmpProtocol(hardwareSentryCli.getSnmpCredentials()));
		}

		if (hardwareSentryCli.getWmiCredentials() != null) {

			protocols.put(WMIProtocol.class, getWMIProtocol(hardwareSentryCli.getWmiCredentials()));
		}

		if (hardwareSentryCli.getWbemCredentials() != null) {

			protocols.put(WBEMProtocol.class, getWbemProtocol(hardwareSentryCli.getWbemCredentials()));
		}

		if (hardwareSentryCli.getHttpCredentials() != null) {

			protocols.put(HTTPProtocol.class, getHttpProtocol(hardwareSentryCli.getHttpCredentials()));
		}

		if (hardwareSentryCli.getIpmiCredentials() != null) {

			protocols.put(IPMIOverLanProtocol.class, getIpmiOverLanProtocol(hardwareSentryCli.getIpmiCredentials()));
		}

		return protocols;
	}

	/**
	 * Get {@link IPMIOverLanProtocol} based on HardwareSentryCLi.ipmiCredentials
	 *
	 * @param ipmiCredentials The CLI IPMI credentials input.
	 * @return new instance of {@link IPMIOverLanProtocol}
	 */
	public IProtocolConfiguration getIpmiOverLanProtocol(IpmiCredentials ipmiCredentials) {
		return IPMIOverLanProtocol.builder()
				.username(ipmiCredentials.getUsername())
				.password(ipmiCredentials.getPassword())
				.bmcKey(ipmiCredentials.getBmcKey() == null ? null : ipmiCredentials.getBmcKey().getBytes())
				.timeout(ipmiCredentials.getTimeout())
				.skipAuth(ipmiCredentials.isSkipAuth())
				.build();
	}

	/**
	 * @param snmpCredentials	The CLI SNMP credentials input.
	 *
	 * @return A new {@link SNMPProtocol} based on the given CLI SNMP credentials input.
	 */
	public SNMPProtocol getSnmpProtocol(SnmpCredentials snmpCredentials) {

		notNull(snmpCredentials, "snmpCredentials cannot be null.");

		SNMPProtocol snmpProtocol = new SNMPProtocol();

		snmpProtocol.setVersion(snmpCredentials.getSnmpVersion());
		snmpProtocol.setCommunity(snmpCredentials.getCommunity());
		snmpProtocol.setPort(snmpCredentials.getPort());
		snmpProtocol.setTimeout(snmpCredentials.getTimeout());

		snmpProtocol.setUsername(snmpCredentials.getUsername());
		snmpProtocol.setPassword(snmpCredentials.getPassword());
		snmpProtocol.setPrivacyPassword(snmpCredentials.getPrivacyPassword());
		snmpProtocol.setPrivacy(snmpCredentials.getPrivacy());

		return snmpProtocol;
	}

	/**
	 * @param httpCredentials	The CLI HTTP credentials input.
	 *
	 * @return A new {@link HTTPProtocol} based on the given CLI HTTP credentials input.
	 */
	public HTTPProtocol getHttpProtocol(HttpCredentials httpCredentials) {

		notNull(httpCredentials, "httpCredentials cannot be null.");

		HTTPProtocol httpProtocol = new HTTPProtocol();

		if (httpCredentials.getHttpOrHttps() != null) {
			httpProtocol.setHttps(httpCredentials.getHttpOrHttps().isHttps());
		}

		httpProtocol.setPort(httpCredentials.getPort());
		httpProtocol.setTimeout(httpCredentials.getTimeout());

		httpProtocol.setUsername(httpCredentials.getUsername());
		httpProtocol.setPassword(httpCredentials.getPassword());

		return httpProtocol;
	}

	/**
	 * Set @WMIProtocol based on HardwareSentryCLi.wmiCredentials
	 *
	 * @param wmiCredentials	The CLI WMI credentials input.
	 *
	 * @return {@link WMIProtocol} instance
	 */
	private WMIProtocol getWMIProtocol(final WmiCredentials wmiCredentials) {
		return WMIProtocol.builder()
			.username(wmiCredentials.getUsername())
			.password(wmiCredentials.getPassword() == null ? null : wmiCredentials.getPassword().toCharArray())
			.timeout(wmiCredentials.getTimeout())
			.namespace(wmiCredentials.getNamespace())
			.build();
	}

	/**
	 * Set @WBEMProtocol based on HardwareSentryCLi.wbemCredentials
	 *
	 * @param cliWBEMCredentials	The CLI WBEM credentials input.
	 *
	 * @return						A new {@link WBEMProtocol} based on the given CLI WBEM credentials input.
	 */
	public WBEMProtocol getWbemProtocol(WbemCredentials cliWbemCredentials) {
		WBEMProtocol wbemInstance = new WBEMProtocol();

		wbemInstance.setProtocol(cliWbemCredentials.getProtocol());
		wbemInstance.setPort(cliWbemCredentials.getPort());
		wbemInstance.setNamespace(cliWbemCredentials.getNamespace());
		wbemInstance.setTimeout(cliWbemCredentials.getTimeout());
		wbemInstance.setUsername(cliWbemCredentials.getUsername());
		wbemInstance.setPassword(cliWbemCredentials.getPassword());

		return wbemInstance;
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
