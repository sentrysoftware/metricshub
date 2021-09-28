package com.sentrysoftware.matrix.engine.strategy.detection;


import java.net.UnknownHostException;
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

import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OSType;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEST_REPORT_PARAMETER;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.TARGET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class DetectionOperationTest {

	private static final String TARGET_NAME = "target";
	private static final String TARGET_ID = "targetId";
	private static final String COMMUNITY = "public";
	private static final String VERSION = "4.2.3";
	private static final String CONNECTOR5_ID = "connector5";
	private static final String CONNECTOR4_ID = "connector4";
	private static final String CONNECTOR3_ID = "connector3";
	private static final String CONNECTOR2_ID = "connector2";
	private static final String CONNECTOR1_ID = "connector1";
	private static final String OID1 = "1.2.3.4.5";
	private static final String OID2 = "1.2.3.4.6";
	private static final String OID3 = "1.2.3.4.7";
	private static final String OID4 = "1.2.3.4.8";
	private static final String OID5 = "1.2.3.4.9";
	private static final String ECS1_01 = "ecs1-01";
	private static final String SUCCESS_SNMP_RESULT1 = OID1 + " ASN_OCT " + VERSION;
	private static final String SUCCESS_SNMP_RESULT2 = OID2 + " ASN_OCT " + VERSION;

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private ConnectorStore store;

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

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
				.protocolConfigurations(Map.of(SNMPProtocol.class, protocol)).build();

		criterion1 = SNMPGetNext.builder().oid(OID1).build();
		connector1 = Connector.builder()
				.compiledFilename(CONNECTOR1_ID)
				.displayName(CONNECTOR1_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).remoteSupport(true)
				.supersedes(Collections.singleton("connector2.hdf"))
				.detection(Detection.builder().criteria(Collections.singletonList(criterion1)).build())
				.sourceProtocols(Collections.singleton(SNMPGetTableSource.PROTOCOL))
				.build();

		criterion2 = SNMPGetNext.builder().oid(OID2).build();
		connector2 = Connector.builder()
				.compiledFilename(CONNECTOR2_ID)
				.displayName(CONNECTOR2_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).remoteSupport(true)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion2)).build())
				.sourceProtocols(Collections.singleton(SNMPGetTableSource.PROTOCOL))
				.build();

		criterion3 = SNMPGetNext.builder().oid(OID3).build();
		connector3 = Connector.builder()
				.compiledFilename(CONNECTOR3_ID)
				.displayName(CONNECTOR3_ID)
				.appliesToOS(Stream.of(OSType.HP, OSType.STORAGE).collect(Collectors.toSet())).remoteSupport(true)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion3)).build())
				.sourceProtocols(Collections.singleton(SNMPGetTableSource.PROTOCOL))
				.build();

		criterion4 = SNMPGetNext.builder().oid(OID4).build();
		connector4 = Connector.builder()
				.compiledFilename(CONNECTOR4_ID)
				.displayName(CONNECTOR4_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).localSupport(true)
				.remoteSupport(false)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion4)).build())
				.sourceProtocols(Collections.singleton(SNMPGetTableSource.PROTOCOL))
				.build();

		criterion5 = SNMPGetNext.builder().oid(OID5).build();
		connector5 = Connector.builder()
				.compiledFilename(CONNECTOR5_ID)
				.displayName(CONNECTOR5_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).remoteSupport(true)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion5)).build())
				.sourceProtocols(Collections.singleton(SNMPGetTableSource.PROTOCOL))
				.build();

		engineConfigurationSelection = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(ECS1_01).id(ECS1_01).type(TargetType.LINUX).build())
				.protocolConfigurations(Map.of(SNMPProtocol.class, protocol)).selectedConnectors(Stream
						.of(connector1, connector2).map(Connector::getCompiledFilename).collect(Collectors.toSet()))
				.build();

	}

	@Test
	void testCallAutoDetection() throws Exception {

		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString(), null);
		doReturn(engineConfigurationAuto).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(Stream.of(connector1, connector2, connector3, connector4, connector5)
				.collect(Collectors.toMap(Connector::getCompiledFilename, Function.identity()))).when(store)
						.getConnectors();

		try (MockedStatic<NetworkHelper> networkHelper = Mockito.mockStatic(NetworkHelper.class)) {
			networkHelper.when(() -> NetworkHelper.isLocalhost(eq(ECS1_01))).thenReturn(false);

			doReturn(SUCCESS_SNMP_RESULT1).when(matsyaClientsExecutor)
					.executeSNMPGetNext(eq(OID1), any(), any(), anyBoolean());

			doReturn(SUCCESS_SNMP_RESULT2).when(matsyaClientsExecutor)
					.executeSNMPGetNext(eq(OID2), any(), any(), anyBoolean());

			doReturn("").when(matsyaClientsExecutor)
					.executeSNMPGetNext(eq(OID5), any(), any(), anyBoolean());

			detectionOperation.call();

			final Monitor target = hostMonitoring.selectFromType(TARGET).get(ECS1_01);
			assertEquals(ECS1_01, target.getName());
			assertEquals(ECS1_01, target.getId());
			assertEquals(ECS1_01, target.getTargetId());

			final Map<String, Monitor> connectors = hostMonitoring.selectFromType(MonitorType.CONNECTOR);
			assertEquals(1, connectors.size());
			Monitor connector = connectors.get(ECS1_01 + "@" + CONNECTOR1_ID);
			assertEquals(ECS1_01, connector.getParentId());
			assertEquals(ECS1_01 + "@" + CONNECTOR1_ID, connector.getId());
			assertEquals(CONNECTOR1_ID, connector.getName());
			assertEquals(ECS1_01, connector.getTargetId());
		}

	}

	@Test
	void testCallProcessSelectedConnectors() throws Exception {

		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString(), null);
		doReturn(engineConfigurationSelection).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(Stream.of(connector1, connector2, connector3, connector4, connector5)
				.collect(Collectors.toMap(Connector::getCompiledFilename, Function.identity()))).when(store)
						.getConnectors();

		doReturn(SUCCESS_SNMP_RESULT1).when(matsyaClientsExecutor)
				.executeSNMPGetNext(eq(OID1), any(), any(), anyBoolean());

		doReturn("").when(matsyaClientsExecutor)
				.executeSNMPGetNext(eq(OID2), any(), any(), anyBoolean());

		detectionOperation.call();

		final Monitor target = hostMonitoring.selectFromType(MonitorType.TARGET).get(ECS1_01);
		assertEquals(ECS1_01, target.getName());
		assertEquals(ECS1_01, target.getId());
		assertEquals(ECS1_01, target.getTargetId());

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.CONNECTOR);
		assertEquals(2, monitors.size());
		Monitor connector1Mo = monitors.get(ECS1_01 + "@" + CONNECTOR1_ID);
		assertEquals(ECS1_01, connector1Mo.getParentId());
		assertEquals(ECS1_01 + "@" + CONNECTOR1_ID, connector1Mo.getId());
		assertEquals(CONNECTOR1_ID, connector1Mo.getName());
		assertEquals(ECS1_01, connector1Mo.getTargetId());

		assertNotNull(connector1Mo.getParameters().get(TEST_REPORT_PARAMETER));
		assertEquals(ParameterState.OK,
				((StatusParam) connector1Mo.getParameters().get(STATUS_PARAMETER)).getState());

		Monitor connector2Mo = monitors.get(ECS1_01 + "@" + CONNECTOR2_ID);
		assertEquals(ECS1_01, connector2Mo.getParentId());
		assertEquals(ECS1_01 + "@" + CONNECTOR2_ID, connector2Mo.getId());
		assertEquals(CONNECTOR2_ID, connector2Mo.getName());
		assertEquals(ECS1_01, connector2Mo.getTargetId());

		assertNotNull(connector2Mo.getParameters().get(TEST_REPORT_PARAMETER));
		assertEquals(ParameterState.ALARM,
				((StatusParam) connector2Mo.getParameters().get(STATUS_PARAMETER)).getState());
	}

	@Test
	void testCreatetargetOnExistingtarget() throws UnknownHostException {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString(), null);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfigurationAuto).when(strategyConfig).getEngineConfiguration();

		final Monitor target = Monitor.builder().id(TARGET_ID).targetId(TARGET_ID).name(TARGET_NAME)
				.monitorType(MonitorType.TARGET).build();

		hostMonitoring.addMonitor(target);

		detectionOperation.createTarget(false);

		final Map<String, Monitor> targets = hostMonitoring.selectFromType(MonitorType.TARGET);
		final Monitor actual = hostMonitoring.selectFromType(MonitorType.TARGET).get(ECS1_01);
		assertNotNull(targets);
		assertNotEquals(target, actual);
		assertEquals(ECS1_01, actual.getName());
		assertEquals(ECS1_01, actual.getId());
		assertEquals(ECS1_01, actual.getTargetId());
	}

	@Test
	void testIsSuccessCriterion() {
		{
			final TestedConnector testedConnector = TestedConnector.builder().connector(connector1)
					.criterionTestResults(Collections.emptyList()).build();
			assertFalse(detectionOperation.isSuccessCriterion(testedConnector, ECS1_01));
		}

		{
			final CriterionTestResult ctr = CriterionTestResult.builder().success(true).build();
			final TestedConnector testedConnector = TestedConnector.builder().connector(connector1)
					.criterionTestResults(Stream.of(ctr, ctr).collect(Collectors.toList())).build();
			assertTrue(detectionOperation.isSuccessCriterion(testedConnector, ECS1_01));
		}

		{
			final CriterionTestResult ctr1 = CriterionTestResult.builder().success(true).build();
			final CriterionTestResult ctr2 = CriterionTestResult.builder().success(false).build();
			final TestedConnector testedConnector = TestedConnector.builder().connector(connector1)
					.criterionTestResults(Stream.of(ctr1, ctr2).collect(Collectors.toList())).build();
			assertFalse(detectionOperation.isSuccessCriterion(testedConnector, ECS1_01));
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
			assertEquals(expected.stream().map(s -> s.replace(".hdf", "")).collect(Collectors.toSet()),
					supersedes);

		}
	}

	@Test
	void testTestConnectorNoDetectionNoCriteria() {
		{
			final Connector connector = Connector.builder().detection(null).build();
			final TestedConnector actual = detectionOperation.testConnector(connector, ECS1_01);
			assertEquals(TestedConnector.builder().connector(connector).build(), actual);
		}

		{
			final Connector connector1 = Connector.builder()
					.detection(Detection.builder().criteria(Collections.emptyList()).build()).build();
			final TestedConnector actual1 = detectionOperation.testConnector(connector1, ECS1_01);
			assertEquals(TestedConnector.builder().connector(connector1).build(), actual1);

			final Connector connector2 = Connector.builder().detection(Detection.builder().criteria(null).build())
					.build();
			final TestedConnector actual2 = detectionOperation.testConnector(connector2, ECS1_01);
			assertEquals(TestedConnector.builder().connector(connector2).build(), actual2);
		}
	}

	@Test
	void testFilterConnectorsByLocalAndRemoteSupport() {

		{
			Connector connector = Connector.builder().localSupport(false).build();
			final Stream<Connector> stream = Stream.of(connector1, connector2, connector4, connector);

			final Stream<Connector> result = detectionOperation.filterConnectorsByLocalAndRemoteSupport(stream, true);
			assertEquals(Stream.of(connector1, connector2, connector4).collect(Collectors.toSet()),
					result.collect(Collectors.toSet()));
		}

		{
			final Stream<Connector> stream = Stream.of(connector1, connector2, connector4);

			final Stream<Connector> result = detectionOperation.filterConnectorsByLocalAndRemoteSupport(stream, false);
			assertEquals(Stream.of(connector1, connector2).collect(Collectors.toSet()),
						result.collect(Collectors.toSet()));
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

	@Test
	void testFilterExcludedConnectors() {
		Set<Connector> localStore = Stream.of(connector1, connector2, connector3, connector4)
				.collect(Collectors.toSet());

		// exclude set is empty
		assertEquals(localStore,
				DetectionOperation.filterExcludedConnectors(Collections.emptySet(), localStore)
				.collect(Collectors.toSet()));

		// Excludes connector4
		assertEquals(Stream.of(connector1, connector2, connector3).collect(Collectors.toSet()),
				DetectionOperation.filterExcludedConnectors(
				Collections.singleton(connector4.getCompiledFilename()), localStore)
				.collect(Collectors.toSet()));
	}

	@Test
	void testDetermineAcceptedProtocols() {
		{
			final Set<String> actual = detectionOperation.determineAcceptedProtocols(false, TargetType.MS_WINDOWS,
					Collections.singleton(WBEMProtocol.class));
			final Set<String> expected = Collections.singleton(WBEMSource.PROTOCOL);
			assertEquals(expected, actual);
		}

		{
			final Set<String> actual = detectionOperation.determineAcceptedProtocols(false, TargetType.MS_WINDOWS,
					Collections.singleton(WMIProtocol.class));
			final Set<String> expected = Set.of(IPMI.PROTOCOL, WMISource.PROTOCOL);
			assertEquals(expected, actual);
		}

		{
			final Set<String> actual = detectionOperation.determineAcceptedProtocols(false, TargetType.LINUX,
					Collections.singleton(WMIProtocol.class));
			final Set<String> expected = Collections.emptySet();
			assertEquals(expected, actual);
		}

		{
			final Set<String> actual = detectionOperation.determineAcceptedProtocols(false, TargetType.LINUX,
					Collections.singleton(SSHProtocol.class));
			final Set<String> expected = Set.of(OSCommandSource.PROTOCOL, IPMI.PROTOCOL);
			assertEquals(expected, actual);
		}

		{
			final Set<String> actual = detectionOperation.determineAcceptedProtocols(false, TargetType.SUN_SOLARIS,
					Collections.singleton(SSHProtocol.class));
			final Set<String> expected = Set.of(OSCommandSource.PROTOCOL, IPMI.PROTOCOL);
			assertEquals(expected, actual);
		}

		{
			final Set<String> actual = detectionOperation.determineAcceptedProtocols(true, TargetType.MS_WINDOWS,
					Collections.emptySet());
			final Set<String> expected = Collections.singleton(OSCommandSource.PROTOCOL);
			assertEquals(expected, actual);
		}

		{
			final Set<String> actual = detectionOperation.determineAcceptedProtocols(true, TargetType.LINUX,
					Collections.emptySet());
			final Set<String> expected = Set.of(OSCommandSource.PROTOCOL, IPMI.PROTOCOL);
			assertEquals(expected, actual);
		}

		{
			final Set<String> actual = detectionOperation.determineAcceptedProtocols(true, TargetType.SUN_SOLARIS,
					Collections.emptySet());
			final Set<String> expected = Set.of(OSCommandSource.PROTOCOL, IPMI.PROTOCOL);
			assertEquals(expected, actual);
		}

		{
			final Set<String> actual = detectionOperation.determineAcceptedProtocols(true, TargetType.SUN_SOLARIS,
					Collections.singleton(SSHProtocol.class));
			final Set<String> expected = Set.of(OSCommandSource.PROTOCOL, IPMI.PROTOCOL);
			assertEquals(expected, actual);
		}
	}

	@Test
	void testFilterConnectorsByAcceptedProtocols() {
		Stream<Connector> result = detectionOperation.filterConnectorsByAcceptedProtocols(Stream.of(connector1, connector2), Set.of(SNMPGetTableSource.PROTOCOL));
		assertEquals(Set.of(connector1, connector2), result.collect(Collectors.toSet()));

		result = detectionOperation.filterConnectorsByAcceptedProtocols(Stream.of(connector1, connector2, Connector.builder().build()), Set.of(SNMPGetTableSource.PROTOCOL));
		assertEquals(Set.of(connector1, connector2), result.collect(Collectors.toSet()));

		result = detectionOperation.filterConnectorsByAcceptedProtocols(Stream.of(connector1, connector2, Connector.builder().build()), Set.of(OSCommandSource.PROTOCOL));
		assertEquals(Collections.emptySet(), result.collect(Collectors.toSet()));
	}
}
