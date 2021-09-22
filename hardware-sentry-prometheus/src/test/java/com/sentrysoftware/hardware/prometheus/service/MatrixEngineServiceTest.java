package com.sentrysoftware.hardware.prometheus.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.sentrysoftware.hardware.prometheus.dto.HardwareTargetDTO;
import com.sentrysoftware.hardware.prometheus.dto.HostConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.SnmpProtocolDTO;
import com.sentrysoftware.hardware.prometheus.exception.BusinessException;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class MatrixEngineServiceTest {

	private static final String DELL_OPEN_MANAGE_CONNECTOR = "DellOpenManage";
	private static final String SUN_F15K  = "SunF15K";

	@Value("${target.config.file}")
	private File targetConfigFile;

	@MockBean
	private Map<String, IHostMonitoring> hostMonitoringMap;

	@MockBean
	private ConnectorStore store;

	@InjectMocks
	@Autowired
	private MatrixEngineService matrixEngineService;

	@Test
	void testGetConnectors() throws BusinessException {
		final Map<String, Connector> connectors = Map
				.of(DELL_OPEN_MANAGE_CONNECTOR, Connector.builder().compiledFilename(DELL_OPEN_MANAGE_CONNECTOR).build(),
					SUN_F15K, Connector.builder().compiledFilename(SUN_F15K).build());

		{
			assertTrue(MatrixEngineService.getConnectors(connectors, null, "selected", "hostname").isEmpty());
			assertTrue(MatrixEngineService.getConnectors(connectors, Collections.emptySet(), "excluded", "hostname").isEmpty());
		}

		{
			assertEquals(Set.of(DELL_OPEN_MANAGE_CONNECTOR, SUN_F15K),
					MatrixEngineService.getConnectors(connectors, Set.of(DELL_OPEN_MANAGE_CONNECTOR, SUN_F15K), "selected", "hostname"));
			assertEquals(Set.of(SUN_F15K), MatrixEngineService.getConnectors(connectors, Set.of(SUN_F15K), "selected", "hostname"));
		}

		{
			assertThrows(BusinessException.class, () -> MatrixEngineService.getConnectors(connectors,
					Set.of("BadConnector", DELL_OPEN_MANAGE_CONNECTOR), "selected", "hostname"));
		}
	}

	@Test
	void testBuildEngineConfiguration() throws BusinessException {

		final Map<String, Connector> connectors = Map
			.of(DELL_OPEN_MANAGE_CONNECTOR,
				Connector.builder().compiledFilename(DELL_OPEN_MANAGE_CONNECTOR).build());

		final MultiHostsConfigurationDTO hostsConfigurations = matrixEngineService.readConfiguration(targetConfigFile, connectors, "hostname");

		final Set<String> selectedConnectors = Collections.singleton(DELL_OPEN_MANAGE_CONNECTOR);

		for (HostConfigurationDTO hostConfigurationDTO : hostsConfigurations.getTargets()) {

			EngineConfiguration actual = MatrixEngineService.buildEngineConfiguration(hostConfigurationDTO, selectedConnectors, Collections.emptySet());

			Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations = Map.of(SNMPProtocol.class, hostConfigurationDTO.getSnmp().toProtocol());

			HardwareTarget target = hostConfigurationDTO.getTarget().toHardwareTarget();
			target.setId(target.getHostname());

			EngineConfiguration expected = EngineConfiguration.builder()
				.operationTimeout(hostConfigurationDTO.getOperationTimeout())
				.protocolConfigurations(protocolConfigurations)
				.selectedConnectors(selectedConnectors)
				.target(target)
				.unknownStatus(Optional.of(hostConfigurationDTO.getUnknownStatus()))
				.build();

			assertEquals(expected, actual);
		}
	}

	@Test
	void testReadConfigurationException() {

		assertThrows(BusinessException.class, () ->  matrixEngineService.readConfiguration(new File("unknownFile.yml"), Collections.emptyMap(), null));
	}

	@Test
	void testReadConfiguration() throws BusinessException {

		{
			final Map<String, Connector> connectors = Map
					.of(DELL_OPEN_MANAGE_CONNECTOR,
						Connector.builder().compiledFilename(DELL_OPEN_MANAGE_CONNECTOR).build());

				assertNotNull(matrixEngineService.readConfiguration(targetConfigFile, connectors, "hostname"));
		}

		{
			// Unknown connector but /metrics
			final Map<String, Connector> connectors = Map
					.of(SUN_F15K,
						Connector.builder().compiledFilename(SUN_F15K).build());

				assertNotNull(matrixEngineService.readConfiguration(targetConfigFile, connectors, null));
		}

		{
			// Unknown connector but /metrics/$target
			final Map<String, Connector> connectors = Map
					.of(SUN_F15K,
						Connector.builder().compiledFilename(SUN_F15K).build());

				assertThrows(BusinessException.class, () -> matrixEngineService.readConfiguration(targetConfigFile, connectors, "target"));
		}

		{
			// Unknown connector but /metrics/$target
			final Map<String, Connector> connectors = Map
					.of(SUN_F15K,
						Connector.builder().compiledFilename(SUN_F15K).build());

				assertThrows(BusinessException.class, () -> matrixEngineService.readConfiguration(null, connectors, "target"));
		}
	}

	@Test
	void testPerformJobsNoStore() {
		{
			doReturn(Collections.emptyMap()).when(store).getConnectors();
			assertThrows(BusinessException.class, () ->  matrixEngineService.performJobs(null));
		}

		{
			doReturn(null).when(store).getConnectors();
			assertThrows(BusinessException.class, () ->  matrixEngineService.performJobs(null));
		}
	}

	@Test
	void testPerformJobsTargetIsNull() {

		final Map<String, Connector> connectors = Map
			.of(DELL_OPEN_MANAGE_CONNECTOR,
				Connector.builder().compiledFilename(DELL_OPEN_MANAGE_CONNECTOR).build());

		doReturn(connectors).when(store).getConnectors();

		Set<String> hostnames = Set.of("357306c9-07e9-431b-bc71-b7712daabbbf",
				"85f8514e-1b4d-4766-87af-09a84ca2a397",
				"3ba095eb-d5e8-4acd-81f4-dc81e29982e7");

		for (String hostname: hostnames) {

			HostConfigurationDTO hostConfigurationDTO = HostConfigurationDTO
				.builder()
				.target(HardwareTargetDTO.builder().id(hostname).hostname(hostname).type(TargetType.LINUX).build())
				.snmp(SnmpProtocolDTO.builder().build())
				.build();

			EngineConfiguration engineConfiguration = MatrixEngineService.buildEngineConfiguration(hostConfigurationDTO,
				Set.of(DELL_OPEN_MANAGE_CONNECTOR), Collections.emptySet());

			IHostMonitoring hostMonitoring = HostMonitoringFactory
				.getInstance()
				.createHostMonitoring(hostname, engineConfiguration);

			doReturn(hostMonitoring).when(hostMonitoringMap).get(hostname);
		}

		assertDoesNotThrow(() -> matrixEngineService.performJobs(null));

		verify(store).getConnectors();
		verify(hostMonitoringMap, times(3)).get(anyString());
	}

	@Test
	void testPerformJobsTargetIsNotNull() {

		final Map<String, Connector> connectors = Map
			.of(DELL_OPEN_MANAGE_CONNECTOR,
				Connector.builder().compiledFilename(DELL_OPEN_MANAGE_CONNECTOR).build());

		doReturn(connectors).when(store).getConnectors();

		// Invalid targetId
		assertThrows(BusinessException.class, () -> matrixEngineService.performJobs("FOO"));
		verify(store).getConnectors();
		verify(hostMonitoringMap, times(0)).get(anyString());

		// Valid targetId
		String hostname = "357306c9-07e9-431b-bc71-b7712daabbbf";

		HostConfigurationDTO hostConfigurationDTO = HostConfigurationDTO
			.builder()
			.target(HardwareTargetDTO.builder().id(hostname).hostname(hostname).type(TargetType.LINUX).build())
			.snmp(SnmpProtocolDTO.builder().build())
			.build();

		EngineConfiguration engineConfiguration = MatrixEngineService.buildEngineConfiguration(hostConfigurationDTO,
			Set.of(DELL_OPEN_MANAGE_CONNECTOR), Collections.emptySet());

		IHostMonitoring hostMonitoring = HostMonitoringFactory
			.getInstance()
			.createHostMonitoring(hostname, engineConfiguration);

		doReturn(hostMonitoring).when(hostMonitoringMap).get(anyString());

		assertDoesNotThrow(() -> matrixEngineService.performJobs(hostname));
		verify(store, times(2)).getConnectors();
		verify(hostMonitoringMap).get(anyString());
	}

	@Test
	void testValidateTarget() {
		assertThrows(BusinessException.class, () -> MatrixEngineService.validateTarget(null, "hostname"));
		assertThrows(BusinessException.class, () -> MatrixEngineService.validateTarget(TargetType.LINUX, ""));
		assertThrows(BusinessException.class, () -> MatrixEngineService.validateTarget(TargetType.LINUX, null));
		assertThrows(BusinessException.class, () -> MatrixEngineService.validateTarget(TargetType.LINUX, " 	"));
		assertDoesNotThrow(() -> MatrixEngineService.validateTarget(TargetType.LINUX, "hostname"));
	}
}
