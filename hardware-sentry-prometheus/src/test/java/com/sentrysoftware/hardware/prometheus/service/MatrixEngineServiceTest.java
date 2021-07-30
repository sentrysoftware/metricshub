package com.sentrysoftware.hardware.prometheus.service;

import com.sentrysoftware.hardware.prometheus.dto.HostConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.dto.MultiHostsConfigurationDTO;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class MatrixEngineServiceTest {

	private static final String MS_HW_DELL_OPEN_MANAGE_CONNECTOR = "MS_HW_DellOpenManage.connector";

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
	void testGetConnectorsWithoutExtension() {
		assertEquals(Collections.singletonList("MyConnector"),
				MatrixEngineService.getConnectorsWithoutExtension(Collections.singleton("MyConnector.hdfs")));
	}

	@Test
	void testGetSelectedConnectors() {

		final Set<String> allConnectors = new HashSet<>(Arrays.asList("aa.hdfs", "bb.hdfs", "cc.hdfs", "dd.hdfs", "ee.hdfs"));
		final Set<String> selected = new HashSet<>(Arrays.asList("aa.hdfs", "bb.hdfs", "cc.hdfs"));
		final Set<String> excluded = new HashSet<>(Arrays.asList("aa.hdfs", "bb.hdfs"));

		final Set<String> expectedInclusion = new HashSet<>(Arrays.asList("aa.connector", "bb.connector", "cc.connector"));
		Set<String> actual = MatrixEngineService.getSelectedConnectors(allConnectors, selected, excluded);
		assertEquals(expectedInclusion, actual);

		actual = MatrixEngineService.getSelectedConnectors(allConnectors, selected, Collections.emptySet());
		assertEquals(expectedInclusion, actual);

		final Set<String> expectedExclusion = new HashSet<>(Arrays.asList("cc.connector", "dd.connector", "ee.connector"));
		actual = MatrixEngineService.getSelectedConnectors(allConnectors, Collections.emptySet(), excluded);
		assertEquals(expectedExclusion, actual);

		actual = MatrixEngineService.getSelectedConnectors(allConnectors, Collections.emptySet(), Collections.emptySet());
		assertEquals(Collections.emptySet(), actual);

		actual = MatrixEngineService.getSelectedConnectors(Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
		assertEquals(Collections.emptySet(), actual);

		actual = MatrixEngineService.getSelectedConnectors(Collections.emptySet(), null, Collections.emptySet());
		assertEquals(Collections.emptySet(), actual);

		actual = MatrixEngineService.getSelectedConnectors(Collections.emptySet(), Collections.emptySet(), null);
		assertEquals(Collections.emptySet(), actual);
	}

	@Test
	void testBuildEngineConfiguration() throws BusinessException {

		final Map<String, Connector> connectors = Map
			.of(MS_HW_DELL_OPEN_MANAGE_CONNECTOR,
				Connector.builder().compiledFilename(MS_HW_DELL_OPEN_MANAGE_CONNECTOR).build());

		final MultiHostsConfigurationDTO hostsConfigurations = matrixEngineService.readConfiguration(targetConfigFile, connectors);

		final Set<String> selectedConnectors = Collections.singleton(MS_HW_DELL_OPEN_MANAGE_CONNECTOR);

		for (HostConfigurationDTO hostConfigurationDTO : hostsConfigurations.getTargets()) {

			EngineConfiguration actual = MatrixEngineService.buildEngineConfiguration(hostConfigurationDTO, selectedConnectors);

			Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations = Map.of(SNMPProtocol.class, hostConfigurationDTO.getSnmp());

			HardwareTarget target = hostConfigurationDTO.getTarget();
			target.setId(target.getHostname());

			EngineConfiguration expected = EngineConfiguration.builder()
				.operationTimeout(hostConfigurationDTO.getOperationTimeout())
				.protocolConfigurations(protocolConfigurations)
				.selectedConnectors(selectedConnectors)
				.target(target)
				.unknownStatus(hostConfigurationDTO.getUnknownStatus())
				.build();

			assertEquals(expected, actual);
		}
	}

	@Test
	void testReadConfigurationException() {

		assertThrows(BusinessException.class, () ->  matrixEngineService.readConfiguration(new File("unknownFile.yml"), null));
	}

	@Test
	void testReadConfiguration() throws BusinessException {

		final Map<String, Connector> connectors = Map
			.of(MS_HW_DELL_OPEN_MANAGE_CONNECTOR,
				Connector.builder().compiledFilename(MS_HW_DELL_OPEN_MANAGE_CONNECTOR).build());

		assertNotNull(matrixEngineService.readConfiguration(targetConfigFile, connectors));
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
			.of(MS_HW_DELL_OPEN_MANAGE_CONNECTOR,
				Connector.builder().compiledFilename(MS_HW_DELL_OPEN_MANAGE_CONNECTOR).build());

		doReturn(connectors).when(store).getConnectors();

		Set<String> hostnames = Set.of("ecs1-01", "ecs1-02", "ecs1-03");

		for (String hostname: hostnames) {

			HostConfigurationDTO hostConfigurationDTO = HostConfigurationDTO
				.builder()
				.target(HardwareTarget.builder().id(hostname).hostname(hostname).type(TargetType.LINUX).build())
				.snmp(SNMPProtocol.builder().build())
				.build();

			EngineConfiguration engineConfiguration = MatrixEngineService.buildEngineConfiguration(hostConfigurationDTO,
				Set.of(MS_HW_DELL_OPEN_MANAGE_CONNECTOR));

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
			.of(MS_HW_DELL_OPEN_MANAGE_CONNECTOR,
				Connector.builder().compiledFilename(MS_HW_DELL_OPEN_MANAGE_CONNECTOR).build());

		doReturn(connectors).when(store).getConnectors();

		// Invalid targetId
		assertThrows(IllegalArgumentException.class, () -> matrixEngineService.performJobs("FOO"));
		verify(store).getConnectors();
		verify(hostMonitoringMap, times(0)).get(anyString());

		// Valid targetId
		String hostname = "ecs1-01";

		HostConfigurationDTO hostConfigurationDTO = HostConfigurationDTO
			.builder()
			.target(HardwareTarget.builder().id(hostname).hostname(hostname).type(TargetType.LINUX).build())
			.snmp(SNMPProtocol.builder().build())
			.build();

		EngineConfiguration engineConfiguration = MatrixEngineService.buildEngineConfiguration(hostConfigurationDTO,
			Set.of(MS_HW_DELL_OPEN_MANAGE_CONNECTOR));

		IHostMonitoring hostMonitoring = HostMonitoringFactory
			.getInstance()
			.createHostMonitoring(hostname, engineConfiguration);

		doReturn(hostMonitoring).when(hostMonitoringMap).get(anyString());

		assertDoesNotThrow(() -> matrixEngineService.performJobs(hostname));
		verify(store, times(2)).getConnectors();
		verify(hostMonitoringMap).get(anyString());
	}
}
