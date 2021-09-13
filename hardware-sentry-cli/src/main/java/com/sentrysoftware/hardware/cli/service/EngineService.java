package com.sentrysoftware.hardware.cli.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.cli.component.cli.HardwareSentryCli;
import com.sentrysoftware.hardware.cli.component.cli.protocols.HttpConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.IpmiConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.SnmpConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WbemConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WmiConfig;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
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

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTOR;
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

			protocols.put(SNMPProtocol.class, getSnmpProtocol(hardwareSentryCli.getSnmpConfig()));
		}

		if (hardwareSentryCli.getWmiConfig() != null) {

			protocols.put(WMIProtocol.class, getWMIProtocol(hardwareSentryCli.getWmiConfig()));
		}

		if (hardwareSentryCli.getWbemConfig() != null) {

			protocols.put(WBEMProtocol.class, getWbemProtocol(hardwareSentryCli.getWbemConfig()));
		}

		if (hardwareSentryCli.getHttpConfig() != null) {

			protocols.put(HTTPProtocol.class, getHttpProtocol(hardwareSentryCli.getHttpConfig()));
		}

		if (hardwareSentryCli.getIpmiConfig() != null) {

			protocols.put(IPMIOverLanProtocol.class, getIpmiOverLanProtocol(hardwareSentryCli.getIpmiConfig()));
		}

		return protocols;
	}

	/**
	 * Get {@link IPMIOverLanProtocol} based on HardwareSentryCLi.ipmiCredentials
	 *
	 * @param ipmiConfig The CLI IPMI credentials input.
	 * @return new instance of {@link IPMIOverLanProtocol}
	 */
	public IProtocolConfiguration getIpmiOverLanProtocol(IpmiConfig ipmiConfig) {
		return IPMIOverLanProtocol.builder()
				.username(ipmiConfig.getUsername())
				.password(ipmiConfig.getPassword())
				.bmcKey(ipmiConfig.getBmcKey() == null ? null : ipmiConfig.getBmcKey().getBytes())
				.timeout(ipmiConfig.getTimeout())
				.skipAuth(ipmiConfig.isSkipAuth())
				.build();
	}

	/**
	 * @param snmpConfig	The CLI SNMP credentials input.
	 *
	 * @return A new {@link SNMPProtocol} based on the given CLI SNMP credentials input.
	 */
	public SNMPProtocol getSnmpProtocol(SnmpConfig snmpConfig) {

		notNull(snmpConfig, "snmpCredentials cannot be null.");

		SNMPProtocol snmpProtocol = new SNMPProtocol();

		snmpProtocol.setVersion(snmpConfig.getSnmpVersion());
		snmpProtocol.setCommunity(snmpConfig.getCommunity());
		snmpProtocol.setPort(snmpConfig.getPort());
		snmpProtocol.setTimeout(snmpConfig.getTimeout());

		snmpProtocol.setUsername(snmpConfig.getUsername());
		snmpProtocol.setPassword(snmpConfig.getPassword());
		snmpProtocol.setPrivacyPassword(snmpConfig.getPrivacyPassword());
		snmpProtocol.setPrivacy(snmpConfig.getPrivacy());

		return snmpProtocol;
	}

	/**
	 * @param httpConfig	The CLI HTTP credentials input.
	 *
	 * @return A new {@link HTTPProtocol} based on the given CLI HTTP credentials input.
	 */
	public HTTPProtocol getHttpProtocol(HttpConfig httpConfig) {

		notNull(httpConfig, "httpCredentials cannot be null.");

		HTTPProtocol httpProtocol = new HTTPProtocol();

		if (httpConfig.getHttpOrHttps() != null) {
			httpProtocol.setHttps(httpConfig.getHttpOrHttps().isHttps());
		}

		httpProtocol.setPort(httpConfig.getPort());
		httpProtocol.setTimeout(httpConfig.getTimeout());

		httpProtocol.setUsername(httpConfig.getUsername());
		httpProtocol.setPassword(httpConfig.getPassword());

		return httpProtocol;
	}

	/**
	 * Set @WMIProtocol based on HardwareSentryCLi.wmiCredentials
	 *
	 * @param wmiConfig	The CLI WMI credentials input.
	 *
	 * @return {@link WMIProtocol} instance
	 */
	private WMIProtocol getWMIProtocol(final WmiConfig wmiConfig) {
		return WMIProtocol.builder()
			.username(wmiConfig.getUsername())
			.password(wmiConfig.getPassword())
			.timeout(wmiConfig.getTimeout())
			.namespace(wmiConfig.getNamespace())
			.build();
	}

	/**
	 * Set @WBEMProtocol based on HardwareSentryCLi.wbemCredentials
	 *
	 * @param cliWBEMCredentials	The CLI WBEM credentials input.
	 *
	 * @return						A new {@link WBEMProtocol} based on the given CLI WBEM credentials input.
	 */
	public WBEMProtocol getWbemProtocol(WbemConfig cliWbemCredentials) {
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
			connectors.replaceAll(f -> f + "." + CONNECTOR);
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
