package com.sentrysoftware.hardware.agent.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sentrysoftware.hardware.agent.dto.HardwareHostDto;
import com.sentrysoftware.hardware.agent.dto.HostConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.protocol.SnmpProtocolDto;
import com.sentrysoftware.hardware.agent.exception.BusinessException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WmiSource;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol.SnmpVersion;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@SpringBootTest
class ConfigHelperTest {

	private static final String DELL_OPEN_MANAGE_CONNECTOR = "DellOpenManage";
	private static final String SUN_F15K = "SunF15K";

	@Autowired
	private File configFile;

	@Test
	void testValidateAndGetConnectors() throws BusinessException {
		final Set<String> connectors = Set.of(DELL_OPEN_MANAGE_CONNECTOR, SUN_F15K);

		{
			assertTrue(ConfigHelper.validateAndGetConnectors(connectors, null, "hostname",false).isEmpty());
			assertTrue(ConfigHelper.validateAndGetConnectors(connectors, Collections.emptySet(), "hostname",false).isEmpty());
		}

		{
			assertEquals(
					Set.of(DELL_OPEN_MANAGE_CONNECTOR, SUN_F15K),
					ConfigHelper.validateAndGetConnectors(
							connectors,
							Set.of(DELL_OPEN_MANAGE_CONNECTOR, SUN_F15K),
							"hostname",
							false
							)
					);

			assertEquals(Set.of(SUN_F15K),
					ConfigHelper.validateAndGetConnectors(connectors, Set.of(SUN_F15K), "hostname", false));
		}

		{
			HashSet<String> configConnectors = new HashSet<>(Set.of("BadConnector", "WrongConnector"));

			assertThrows(BusinessException.class, () -> 
			ConfigHelper.validateAndGetConnectors(
					connectors,
					configConnectors,
					"hostname",
					false
					)
					);
			assertThrows(Exception.class,
					() -> ConfigHelper.validateAndGetConnectors(null, Collections.emptySet(), "hostname", false));
		}

		{
			HashSet<String> configConnectors = new HashSet<>(Set.of("BadConnector", DELL_OPEN_MANAGE_CONNECTOR));

			assertEquals(Set.of(DELL_OPEN_MANAGE_CONNECTOR),
					ConfigHelper.validateAndGetConnectors(
					connectors,
					configConnectors,
					"hostname",
					false
					)
					);
		}

		{
			HashSet<String> configConnectors = new HashSet<>(Set.of("BadConnector", DELL_OPEN_MANAGE_CONNECTOR));

			assertDoesNotThrow(() -> ConfigHelper.validateAndGetConnectors(
					connectors,
					configConnectors,
					"hostname",
					true
					)
					);

			assertEquals(Collections.singleton(DELL_OPEN_MANAGE_CONNECTOR), configConnectors);
		}
	}

	@Test
	void testBuildEngineConfiguration() throws BusinessException {

		final MultiHostsConfigurationDto hostsConfigurations = ConfigHelper
				.readConfigurationSafe(configFile);

		final Set<String> selectedConnectors = Collections.singleton(DELL_OPEN_MANAGE_CONNECTOR);

		for (HostConfigurationDto hostConfigurationDto : hostsConfigurations.getHosts()) {

			EngineConfiguration actual = ConfigHelper.buildEngineConfiguration(hostConfigurationDto, selectedConnectors,
					Collections.emptySet());

			Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations = Map
					.of(SnmpProtocol.class, hostConfigurationDto.getSnmp().toProtocol());

			HardwareHost host = hostConfigurationDto.getHost().toHardwareHost();
			if ("357306c9-07e9-431b-bc71-b7712daabbbf-1".equals(host.getId())) {
				host.setId("357306c9-07e9-431b-bc71-b7712daabbbf-1");
			} else {
				host.setId(host.getHostname());
			}

			EngineConfiguration expected = EngineConfiguration
					.builder()
					.operationTimeout(hostConfigurationDto.getOperationTimeout())
					.protocolConfigurations(protocolConfigurations).selectedConnectors(selectedConnectors)
					.host(host)
					.build();

			assertEquals(expected, actual);
		}
	}

	@Test
	void testReadConfigurationSafeUnknownFile() {
		assertTrue(ConfigHelper.readConfigurationSafe(new File("unknownFile.yml")).isEmpty());
	}

	@Test
	void testBuildHostMonitoringMap() throws IOException {
		final MultiHostsConfigurationDto multiHostsConfigurationDto = ConfigHelper.deserializeYamlFile(configFile,
				MultiHostsConfigurationDto.class);
		final Map<String, IHostMonitoring> hostMonitoringMap = ConfigHelper
				.buildHostMonitoringMap(multiHostsConfigurationDto, Collections.singleton(DELL_OPEN_MANAGE_CONNECTOR));

		assertFalse(hostMonitoringMap.isEmpty());
	}

	@Test
	void testBuildHostMonitoringMapBadConfig() throws IOException {
		final MultiHostsConfigurationDto multiHostsConfigurationDto = ConfigHelper.deserializeYamlFile(configFile,
				MultiHostsConfigurationDto.class);
		final Map<String, IHostMonitoring> hostMonitoringMap = ConfigHelper.buildHostMonitoringMap(
				multiHostsConfigurationDto, Collections.singleton("StoreAcceptsOnlyThisConnector"));

		assertTrue(hostMonitoringMap.isEmpty());
	}

	@Test
	void testDeserializeYamlFile() throws IOException {
		assertNotNull(
				ConfigHelper.deserializeYamlFile(configFile, MultiHostsConfigurationDto.class));
	}

	@Test
	void testValidateHost() {
		assertThrows(BusinessException.class, () -> ConfigHelper.validateHost(null, "hostname"));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateHost(HostType.LINUX, ""));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateHost(HostType.LINUX, null));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateHost(HostType.LINUX, " 	"));
		assertDoesNotThrow(() -> ConfigHelper.validateHost(HostType.LINUX, "hostname"));
	}

	@Test
	void testValidateEngineConfiguration() {



		EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost
						.builder()
						.hostname("localhost")
						.id("localhost")
						.type(HostType.LINUX)
						.build())
				.protocolConfigurations(Map.of(SnmpProtocol.class, SnmpProtocol.builder().build()))
				.build();
		Set<String> selectedConnectors = Set.of(SUN_F15K);
		engineConfiguration.setSelectedConnectors(selectedConnectors);

		Connector connector = new Connector();
		connector.setCompiledFilename(SUN_F15K);

		connector.setSourceTypes(Collections.singleton(WmiSource.class));

		assertThrows(BusinessException.class, () -> ConfigHelper.validateEngineConfiguration(engineConfiguration, Collections.singletonList(connector)));

		connector.setSourceTypes(Collections.singleton(SnmpGetTableSource.class));

		assertDoesNotThrow(() -> ConfigHelper.validateEngineConfiguration(engineConfiguration, Collections.singletonList(connector)));
	}

	@Test
	void testValidateSnmpInfo() {
		final char[] community = new char[] { 'p', 'u', 'b', 'l', 'i', 'c' };
		final char[] emptyCommunity = new char[] {};

		{ 
			SnmpProtocolDto snmpDto = SnmpProtocolDto
					.builder()
					.community(emptyCommunity)
					.port(1234)
					.timeout(60L)
					.version(SnmpVersion.V1)
					.username(null)
					.build();

			assertThrows(BusinessException.class, () -> ConfigHelper.validateSnmpInfo("hostname", snmpDto));
		}

		{
			SnmpProtocolDto snmpDto = SnmpProtocolDto
					.builder()
					.community(null)
					.port(1234)
					.timeout(60L)
					.version(SnmpVersion.V1)
					.username(null)
					.build();

			assertThrows(BusinessException.class, () -> ConfigHelper.validateSnmpInfo("hostname", snmpDto));
		}

		{
			SnmpProtocolDto snmpDto = SnmpProtocolDto
					.builder()
					.community(community)
					.port(-1)
					.timeout(60L)
					.version(SnmpVersion.V1)
					.username(null)
					.build();

			assertThrows(BusinessException.class, () -> ConfigHelper.validateSnmpInfo("hostname", snmpDto));
		}

		{
			SnmpProtocolDto snmpDto = SnmpProtocolDto
					.builder()
					.community(community)
					.port(66666)
					.timeout(60L)
					.version(SnmpVersion.V1)
					.username(null)
					.build();

			assertThrows(BusinessException.class, () -> ConfigHelper.validateSnmpInfo("hostname", snmpDto));
		}

		{
			SnmpProtocolDto snmpDto = SnmpProtocolDto
					.builder()
					.community(community)
					.port(null)
					.timeout(60L)
					.version(SnmpVersion.V1)
					.username(null)
					.build();

			assertThrows(BusinessException.class, () -> ConfigHelper.validateSnmpInfo("hostname", snmpDto));
		}

		{
			SnmpProtocolDto snmpDto = SnmpProtocolDto
					.builder()
					.community(community)
					.port(1234)
					.timeout(-60L)
					.version(SnmpVersion.V1)
					.username(null)
					.build();

			assertThrows(BusinessException.class, () -> ConfigHelper.validateSnmpInfo("hostname", snmpDto));
		}

		{
			SnmpProtocolDto snmpDto = SnmpProtocolDto
					.builder()
					.community(community)
					.port(1234)
					.timeout(null)
					.version(SnmpVersion.V1)
					.username(null)
					.build();		

			assertThrows(BusinessException.class, () -> ConfigHelper.validateSnmpInfo("hostname", snmpDto));
		}

		{
			SnmpProtocolDto snmpDto = SnmpProtocolDto
					.builder()
					.community(community)
					.port(1234)
					.timeout(60L)
					.version(SnmpVersion.V3_SHA)
					.username(null)
					.build();		


			assertThrows(BusinessException.class, () -> ConfigHelper.validateSnmpInfo("hostname", snmpDto));
		}

		{
			SnmpProtocolDto snmpDto = SnmpProtocolDto
					.builder()
					.community(community)
					.port(1234)
					.timeout(60L)
					.version(SnmpVersion.V3_SHA)
					.username("")
					.build();		

			assertThrows(BusinessException.class, () -> ConfigHelper.validateSnmpInfo("hostname", snmpDto));
		}

		{
			SnmpProtocolDto snmpDto = SnmpProtocolDto
					.builder()
					.community(community)
					.port(1234)
					.timeout(60L)
					.version(SnmpVersion.V3_SHA)
					.username("username")
					.build();		

			assertDoesNotThrow(() -> ConfigHelper.validateSnmpInfo("hostname", snmpDto));
		}

		{
			SnmpProtocolDto snmpDto = SnmpProtocolDto
					.builder()
					.community(community)
					.port(1234)
					.timeout(60L)
					.version(SnmpVersion.V3_NO_AUTH)
					.username(null)
					.build();		

			assertDoesNotThrow(() -> ConfigHelper.validateSnmpInfo("hostname", snmpDto));
		}
	}

	@Test
	void testValidateIpmiInfo() {		
		assertThrows(BusinessException.class, () -> ConfigHelper.validateIpmiInfo("hostname", "", 60L));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateIpmiInfo("hostname", null, 60L));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateIpmiInfo("hostname", "username", -60L));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateIpmiInfo("hostname", "username", null));
		assertDoesNotThrow(() -> ConfigHelper.validateIpmiInfo("hostname", "username", 60L));
	}

	@Test
	void testValidateSshInfo() {		
		assertThrows(BusinessException.class, () -> ConfigHelper.validateSshInfo("hostname", "", 60L));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateSshInfo("hostname", null, 60L));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateSshInfo("hostname", "username", -60L));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateSshInfo("hostname", "username", null));
		assertDoesNotThrow(() -> ConfigHelper.validateSshInfo("hostname", "username", 60L));
	}

	@Test
	void testValidateWbemInfo() {		
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWbemInfo("hostname", null, -60L, 1234, "vcenter"));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWbemInfo("hostname", "", null, 1234, "vcenter"));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWbemInfo("hostname", "username", -60L, 1234, "vcenter"));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWbemInfo("hostname", "username", null, 1234, "vcenter"));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWbemInfo("hostname", "username", 60L, -1, "vcenter"));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWbemInfo("hostname", "username", 60L, null, "vcenter"));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWbemInfo("hostname", "username", 60L, 66666, "vcenter"));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWbemInfo("hostname", "username", 60L, null, ""));
		assertDoesNotThrow(() -> ConfigHelper.validateWbemInfo("hostname", "username", 60L, 1234, "vcenter"));
		assertDoesNotThrow(() -> ConfigHelper.validateWbemInfo("hostname", "username", 60L, 1234, null));
	}

	@Test
	void testValidateWmiInfo() {		
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWmiInfo("hostname", -60L));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWmiInfo("hostname", null));
		assertDoesNotThrow(() -> ConfigHelper.validateWmiInfo("hostname", 60L));
	}

	@Test
	void testValidateHttpInfo() {		
		assertThrows(BusinessException.class, () -> ConfigHelper.validateHttpInfo("hostname", -60L, 1234));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateHttpInfo("hostname", null, 1234));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateHttpInfo("hostname", 60L, -1));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateHttpInfo("hostname", 60L, null));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateHttpInfo("hostname", 60L, 66666));
		assertDoesNotThrow(() -> ConfigHelper.validateHttpInfo("hostname", 60L, 1234));
	}

	@Test
	void testValidateOsCommandInfo() {
		assertThrows(BusinessException.class, () -> ConfigHelper.validateOsCommandInfo("hostname", -60L));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateOsCommandInfo("hostname", null));
		assertDoesNotThrow(() -> ConfigHelper.validateOsCommandInfo("hostname", 60L));
	}

	@Test
	void testValidateWinRm() {
		final String hostname = "hostname";
		final String username = "username";

		assertThrows(BusinessException.class, () -> ConfigHelper.validateWinRmInfo(hostname, 1234, -60L, username));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWinRmInfo(hostname, 1234, null, username));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWinRmInfo(hostname, null, 60L, username));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWinRmInfo(hostname, -1234, 60L, username));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWinRmInfo(hostname, 1234, 60L, null));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateWinRmInfo(hostname, 1234, 60L, ""));
		assertDoesNotThrow(() -> ConfigHelper.validateWinRmInfo(hostname, 1234, 60L, username));
	}

	@Test
	void testHostGroups() {
		MultiHostsConfigurationDto multiHostsConfigurationDto = ConfigHelper.readConfigurationSafe(new File("src/test/resources/data/hws-config-hostgroups.yaml"));
		assertEquals(5, multiHostsConfigurationDto.getHosts().size());

		HostConfigurationDto testHost = HostConfigurationDto
			.builder()
			.host(HardwareHostDto
				.builder()
				.hostname("host1")
				.type(HostType.LINUX)
				.build())
			.snmp(SnmpProtocolDto.builder().build())
			.selectedConnectors(Set.of(DELL_OPEN_MANAGE_CONNECTOR))
			.build();
		
		assertTrue(multiHostsConfigurationDto.getHosts().contains(testHost));
	}
}