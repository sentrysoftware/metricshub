package com.sentrysoftware.matrix.engine.strategy.detection;

import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.DEVICE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.common.exception.LocalhostCheckException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OSType;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

@ExtendWith(MockitoExtension.class)
class DetectionOperationTest {

	private static final String DEVICE_NAME = "device";
	private static final String DEVICE_ID = "deviceId";
	private static final String COMMUNITY = "public";
	private static final String BAD_RESULT = "1";
	private static final String FAILED = "Failed";
	private static final String VERSION = "4.2.3";
	private static final String SUCCESS = "Success";
	private static final String CONNECTOR5_ID = "connector5.connector";
	private static final String CONNECTOR4_ID = "connector4.connector";
	private static final String CONNECTOR3_ID = "connector3.connector";
	private static final String CONNECTOR2_ID = "connector2.connector";
	private static final String CONNECTOR1_ID = "connector1.connector";
	private static final String OID1 = "1.2.3.4.5";
	private static final String OID2 = "1.2.3.4.6";
	private static final String OID3 = "1.2.3.4.7";
	private static final String OID4 = "1.2.3.4.8";
	private static final String OID5 = "1.2.3.4.9";
	private static final String ECS1_01 = "ecs1-01";

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private ConnectorStore store;

	@Mock
	private CriterionVisitor criterionVisitor;

	@InjectMocks
	private DetectionOperation detectionOperation;

	private static EngineConfiguration engineConfigurationAuto;
	private static EngineConfiguration engineConfigurationSelection;
	private static SNMPGetNext criterion1;
	private static Connector connector1;
	private static SNMPGetNext criterion2;
	private static Connector connector2;
	private static SNMPGetNext criterion3;
	private static Connector connector3;
	private static SNMPGetNext criterion4;
	private static Connector connector4;
	private static SNMPGetNext criterion5;
	private static Connector connector5;

	@BeforeAll
	public static void setUp() {
		final SNMPProtocol protocol = SNMPProtocol.builder().community(COMMUNITY).version(SNMPVersion.V1).port(161)
				.timeout(120L).build();
		engineConfigurationAuto = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(ECS1_01).id(ECS1_01).type(TargetType.LINUX).build())
				.protocolConfigurations(Stream.of(protocol).collect(Collectors.toSet())).build();

		criterion1 = SNMPGetNext.builder().oid(OID1).build();
		connector1 = Connector.builder().compiledFilename(CONNECTOR1_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).remoteSupport(true)
				.supersedes(Collections.singleton("connector2.hdf"))
				.detection(Detection.builder().criteria(Collections.singletonList(criterion1)).build()).build();

		criterion2 = SNMPGetNext.builder().oid(OID2).build();
		connector2 = Connector.builder().compiledFilename(CONNECTOR2_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).remoteSupport(true)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion2)).build()).build();

		criterion3 = SNMPGetNext.builder().oid(OID3).build();
		connector3 = Connector.builder().compiledFilename(CONNECTOR3_ID)
				.appliesToOS(Stream.of(OSType.HP, OSType.STORAGE).collect(Collectors.toSet())).remoteSupport(true)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion3)).build()).build();

		criterion4 = SNMPGetNext.builder().oid(OID4).build();
		connector4 = Connector.builder().compiledFilename(CONNECTOR4_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).localSupport(true)
				.remoteSupport(false)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion4)).build()).build();

		criterion5 = SNMPGetNext.builder().oid(OID5).build();
		connector5 = Connector.builder().compiledFilename(CONNECTOR5_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).remoteSupport(true)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion5)).build()).build();

		engineConfigurationSelection = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(ECS1_01).id(ECS1_01).type(TargetType.LINUX).build())
				.protocolConfigurations(Stream.of(protocol).collect(Collectors.toSet())).selectedConnectors(Stream
						.of(connector1, connector2).map(Connector::getCompiledFilename).collect(Collectors.toSet()))
				.build();

	}

	@Test
	void testCallAutoDetection() throws Exception {

		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString());
		doReturn(engineConfigurationAuto).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(Stream.of(connector1, connector2, connector3, connector4, connector5)
				.collect(Collectors.toMap(Connector::getCompiledFilename, Function.identity()))).when(store)
						.getConnectors();

		try (MockedStatic<NetworkHelper> networkHelper = Mockito.mockStatic(NetworkHelper.class)) {
			networkHelper.when(() -> NetworkHelper.isLocalhost(eq(ECS1_01))).thenReturn(false);

			doReturn(CriterionTestResult.builder().success(true).message(SUCCESS).result(VERSION).build())
					.when(criterionVisitor).visit(criterion1);

			doReturn(CriterionTestResult.builder().success(true).message(SUCCESS).result(VERSION).build())
					.when(criterionVisitor).visit(criterion2);

			doReturn(CriterionTestResult.builder().success(false).message(FAILED).result(BAD_RESULT).build())
					.when(criterionVisitor).visit(criterion5);

			detectionOperation.call();

			final Monitor device = hostMonitoring.selectFromType(MonitorType.DEVICE).get(ECS1_01);
			assertEquals(ECS1_01, device.getName());
			assertEquals(ECS1_01, device.getDeviceId());
			assertEquals(ECS1_01, device.getTargetId());

			final Map<String, Monitor> connectors = hostMonitoring.selectFromType(MonitorType.CONNECTOR);
			assertEquals(1, connectors.size());
			Monitor connector = connectors.get(ECS1_01 + "@" + CONNECTOR1_ID);
			assertEquals(ECS1_01, connector.getParentId());
			assertEquals(ECS1_01 + "@" + CONNECTOR1_ID, connector.getDeviceId());
			assertEquals(CONNECTOR1_ID, connector.getName());
			assertEquals(ECS1_01, connector.getTargetId());
		}

	}

	@Test
	void testCallProcessSelectedConnectors() throws Exception {

		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString());
		doReturn(engineConfigurationSelection).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(Stream.of(connector1, connector2, connector3, connector4, connector5)
				.collect(Collectors.toMap(Connector::getCompiledFilename, Function.identity()))).when(store)
						.getConnectors();

		doReturn(CriterionTestResult.builder().success(true).message(SUCCESS).result(VERSION).build())
				.when(criterionVisitor).visit(criterion1);

		doReturn(CriterionTestResult.builder().success(false).message(FAILED).result(BAD_RESULT).build())
				.when(criterionVisitor).visit(criterion2);

		detectionOperation.call();

		final Monitor device = hostMonitoring.selectFromType(MonitorType.DEVICE).get(ECS1_01);
		assertEquals(ECS1_01, device.getName());
		assertEquals(ECS1_01, device.getDeviceId());
		assertEquals(ECS1_01, device.getTargetId());

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.CONNECTOR);
		assertEquals(2, monitors.size());
		Monitor connector1Mo = monitors.get(ECS1_01 + "@" + CONNECTOR1_ID);
		assertEquals(ECS1_01, connector1Mo.getParentId());
		assertEquals(ECS1_01 + "@" + CONNECTOR1_ID, connector1Mo.getDeviceId());
		assertEquals(CONNECTOR1_ID, connector1Mo.getName());
		assertEquals(ECS1_01, connector1Mo.getTargetId());

		assertNotNull(connector1Mo.getParameters().get(HardwareConstants.TEST_REPORT_PARAMETER_NAME));
		assertEquals(ParameterState.OK,
				((StatusParam) connector1Mo.getParameters().get(HardwareConstants.STATUS_PARAMETER_NAME)).getState());

		Monitor connector2Mo = monitors.get(ECS1_01 + "@" + CONNECTOR2_ID);
		assertEquals(ECS1_01, connector2Mo.getParentId());
		assertEquals(ECS1_01 + "@" + CONNECTOR2_ID, connector2Mo.getDeviceId());
		assertEquals(CONNECTOR2_ID, connector2Mo.getName());
		assertEquals(ECS1_01, connector2Mo.getTargetId());

		assertNotNull(connector2Mo.getParameters().get(HardwareConstants.TEST_REPORT_PARAMETER_NAME));
		assertEquals(ParameterState.ALARM,
				((StatusParam) connector2Mo.getParameters().get(HardwareConstants.STATUS_PARAMETER_NAME)).getState());
	}

	@Test
	void testCreateDeviceOnExistingDevice() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString());

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfigurationAuto).when(strategyConfig).getEngineConfiguration();

		final Monitor device = Monitor.builder().deviceId(DEVICE_ID).targetId(DEVICE_ID).name(DEVICE_NAME)
				.monitorType(DEVICE).build();

		hostMonitoring.addMonitor(device);

		detectionOperation.createDevice();

		final Map<String, Monitor> devices = hostMonitoring.selectFromType(MonitorType.DEVICE);
		final Monitor actual = hostMonitoring.selectFromType(MonitorType.DEVICE).get(ECS1_01);
		assertNotNull(devices);
		assertNotEquals(device, actual);
		assertEquals(ECS1_01, actual.getName());
		assertEquals(ECS1_01, actual.getDeviceId());
		assertEquals(ECS1_01, actual.getTargetId());
	}

	@Test
	void testIsSuccessCriterion() {
		{
			final TestedConnector testedConnector = TestedConnector.builder().connector(connector1)
					.criterionTestResults(Collections.emptyList()).build();
			assertFalse((boolean) detectionOperation.isSuccessCriterion(testedConnector, ECS1_01));
		}

		{
			final CriterionTestResult ctr = CriterionTestResult.builder().success(true).build();
			final TestedConnector testedConnector = TestedConnector.builder().connector(connector1)
					.criterionTestResults(Stream.of(ctr, ctr).collect(Collectors.toList())).build();
			assertTrue((boolean) detectionOperation.isSuccessCriterion(testedConnector, ECS1_01));
		}

		{
			final CriterionTestResult ctr1 = CriterionTestResult.builder().success(true).build();
			final CriterionTestResult ctr2 = CriterionTestResult.builder().success(false).build();
			final TestedConnector testedConnector = TestedConnector.builder().connector(connector1)
					.criterionTestResults(Stream.of(ctr1, ctr2).collect(Collectors.toList())).build();
			assertFalse((boolean) detectionOperation.isSuccessCriterion(testedConnector, ECS1_01));
		}
	}

	@Test
	void testUpdateSupersedes() {
		{
			final Set<String> supersedes = new HashSet<>();
			final TestedConnector testedConnector = TestedConnector.builder()
					.connector(Connector.builder().supersedes(null).build()).build();
			detectionOperation.updateSupersedes(supersedes, testedConnector);
			assertTrue(supersedes.isEmpty());

		}

		{
			final Set<String> supersedes = new HashSet<>();
			final TestedConnector testedConnector = TestedConnector.builder()
					.connector(Connector.builder().supersedes(Collections.emptySet()).build()).build();
			detectionOperation.updateSupersedes(supersedes, testedConnector);
			assertTrue(supersedes.isEmpty());

		}

		{
			final Set<String> supersedes = new HashSet<>();
			final Set<String> expected = Stream.of("connector1.hdf", "connector2.hdf").collect(Collectors.toSet());
			final TestedConnector testedConnector = TestedConnector.builder()
					.connector(Connector.builder().supersedes(expected).build()).build();
			detectionOperation.updateSupersedes(supersedes, testedConnector);
			assertEquals(expected.stream().map(s -> s.replace(".hdf", ".connector")).collect(Collectors.toSet()),
					supersedes);

		}
	}

	@Test
	void testProcessDetectionNoDetectionNoCriteria() {
		{
			final Connector connector = Connector.builder().detection(null).build();
			final TestedConnector actual = detectionOperation.processDetection(connector, ECS1_01);
			assertEquals(TestedConnector.builder().connector(connector).build(), actual);
		}

		{
			final Connector connector1 = Connector.builder()
					.detection(Detection.builder().criteria(Collections.emptyList()).build()).build();
			final TestedConnector actual1 = detectionOperation.processDetection(connector1, ECS1_01);
			assertEquals(TestedConnector.builder().connector(connector1).build(), actual1);

			final Connector connector2 = Connector.builder().detection(Detection.builder().criteria(null).build())
					.build();
			final TestedConnector actual2 = detectionOperation.processDetection(connector2, ECS1_01);
			assertEquals(TestedConnector.builder().connector(connector2).build(), actual2);
		}
	}

	@Test
	void testFilterConnectorsByLocalAndRemoteSupport() throws LocalhostCheckException {

		{
			Connector connector = Connector.builder().localSupport(false).build();
			final Stream<Connector> stream = Stream.of(connector1, connector2, connector4, connector);
			try (MockedStatic<NetworkHelper> networkHelper = Mockito.mockStatic(NetworkHelper.class)) {
				networkHelper.when(() -> NetworkHelper.isLocalhost(eq(ECS1_01))).thenReturn(true);
				final Stream<Connector> result = detectionOperation.filterConnectorsByLocalAndRemoteSupport(stream,
						ECS1_01);
				assertEquals(Stream.of(connector1, connector2, connector4).collect(Collectors.toSet()),
						result.collect(Collectors.toSet()));

			}
		}

		{
			final Stream<Connector> stream = Stream.of(connector1, connector2, connector4);
			try (MockedStatic<NetworkHelper> networkHelper = Mockito.mockStatic(NetworkHelper.class)) {
				networkHelper.when(() -> NetworkHelper.isLocalhost(eq(ECS1_01))).thenReturn(false);
				final Stream<Connector> result = detectionOperation.filterConnectorsByLocalAndRemoteSupport(stream,
						ECS1_01);
				assertEquals(Stream.of(connector1, connector2).collect(Collectors.toSet()),
						result.collect(Collectors.toSet()));

			}
		}
	}

	@Test
	void testFilterConnectorsByTargetType() {
		{
			Connector connector = Connector.builder().appliesToOS(null).build();
			final Stream<Connector> stream = Stream.of(connector1, connector2, connector4, connector);

			final Stream<Connector> result = detectionOperation.filterConnectorsByTargetType(stream, TargetType.LINUX);
			assertEquals(Stream.of(connector1, connector2, connector4).collect(Collectors.toSet()),
					result.collect(Collectors.toSet()));
		}

		{
			Connector connector = Connector.builder().appliesToOS(null).build();
			final Stream<Connector> stream = Stream.of(connector1, connector2, connector4, connector);

			final Stream<Connector> result = detectionOperation.filterConnectorsByTargetType(stream,
					TargetType.STORAGE);
			assertEquals(Collections.emptySet(), result.collect(Collectors.toSet()));
		}

	}
}
