package com.sentrysoftware.hardware.prometheus.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.sentrysoftware.hardware.prometheus.dto.HostConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.exception.BusinessException;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@SpringBootTest
class ConfigHelperTest {

	private static final String DELL_OPEN_MANAGE_CONNECTOR = "DellOpenManage";
	private static final String SUN_F15K = "SunF15K";

	@Value("${target.config.file}")
	private File targetConfigPath;

	@Test
	void testValidateAndGetConnectors() throws BusinessException {
		final Set<String> connectors = Set.of(DELL_OPEN_MANAGE_CONNECTOR, SUN_F15K);

		{
			assertTrue(ConfigHelper.validateAndGetConnectors(connectors, null, "selected", "hostname").isEmpty());
			assertTrue(ConfigHelper.validateAndGetConnectors(connectors, Collections.emptySet(), "excluded", "hostname")
					.isEmpty());
		}

		{
			assertEquals(Set.of(DELL_OPEN_MANAGE_CONNECTOR, SUN_F15K), ConfigHelper.validateAndGetConnectors(connectors,
					Set.of(DELL_OPEN_MANAGE_CONNECTOR, SUN_F15K), "selected", "hostname"));
			assertEquals(Set.of(SUN_F15K),
					ConfigHelper.validateAndGetConnectors(connectors, Set.of(SUN_F15K), "selected", "hostname"));
		}

		{
			assertThrows(BusinessException.class, () -> ConfigHelper.validateAndGetConnectors(connectors,
					Set.of("BadConnector", DELL_OPEN_MANAGE_CONNECTOR), "selected", "hostname"));
			assertThrows(Exception.class,
					() -> ConfigHelper.validateAndGetConnectors(null, Collections.emptySet(), "selected", "hostname"));
		}

	}

	@Test
	void testBuildEngineConfiguration() throws BusinessException {

		final MultiHostsConfigurationDTO hostsConfigurations = ConfigHelper
				.readConfigurationSafe(targetConfigPath);

		final Set<String> selectedConnectors = Collections.singleton(DELL_OPEN_MANAGE_CONNECTOR);

		for (HostConfigurationDTO hostConfigurationDTO : hostsConfigurations.getTargets()) {

			EngineConfiguration actual = ConfigHelper.buildEngineConfiguration(hostConfigurationDTO, selectedConnectors,
					Collections.emptySet());

			Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations = Map
					.of(SNMPProtocol.class, hostConfigurationDTO.getSnmp().toProtocol());

			HardwareTarget target = hostConfigurationDTO.getTarget().toHardwareTarget();
			if ("357306c9-07e9-431b-bc71-b7712daabbbf-1".equals(target.getId())) {
				target.setId("357306c9-07e9-431b-bc71-b7712daabbbf-1");
			} else {
				target.setId(target.getHostname());
			}

			EngineConfiguration expected = EngineConfiguration.builder()
					.operationTimeout(hostConfigurationDTO.getOperationTimeout())
					.protocolConfigurations(protocolConfigurations).selectedConnectors(selectedConnectors)
					.target(target).unknownStatus(hostConfigurationDTO.getUnknownStatus()).build();

			assertEquals(expected, actual);
		}
	}

	@Test
	void testReadConfigurationSafeUnknownFile() {

		assertEquals(MultiHostsConfigurationDTO.empty(), ConfigHelper.readConfigurationSafe(new File("unknownFile.yml")));
	}

	@Test
	void testBuildHostMonitoringMap() {
		final Map<String, IHostMonitoring> hostMonitoringMap = ConfigHelper.buildHostMonitoringMap(
				targetConfigPath, Collections.singleton(DELL_OPEN_MANAGE_CONNECTOR));

		assertFalse(hostMonitoringMap.isEmpty());
	}

	@Test
	void testBuildHostMonitoringMapBadConfig() {
		final Map<String, IHostMonitoring> hostMonitoringMap = ConfigHelper.buildHostMonitoringMap(
				targetConfigPath, Collections.singleton("StoreAcceptsOnlyThisConnector"));

		assertTrue(hostMonitoringMap.isEmpty());
	}

	@Test
	void testDeserializeYamlFile() throws IOException {
		assertNotNull(
				ConfigHelper.deserializeYamlFile(targetConfigPath, MultiHostsConfigurationDTO.class));
	}

	@Test
	void testValidateTarget() {
		assertThrows(BusinessException.class, () -> ConfigHelper.validateTarget(null, "hostname"));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateTarget(TargetType.LINUX, ""));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateTarget(TargetType.LINUX, null));
		assertThrows(BusinessException.class, () -> ConfigHelper.validateTarget(TargetType.LINUX, " 	"));
		assertDoesNotThrow(() -> ConfigHelper.validateTarget(TargetType.LINUX, "hostname"));
	}
}
