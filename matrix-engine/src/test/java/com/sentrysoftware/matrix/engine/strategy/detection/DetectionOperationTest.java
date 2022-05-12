package com.sentrysoftware.matrix.engine.strategy.detection;


import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OSType;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.TextInstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
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
	private static final String TARGET_HOSTNAME = "localhost";
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
	private static EngineConfiguration engineConfigurationAutoSequential;
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
		engineConfigurationAuto = EngineConfiguration
				.builder()
				.target(HardwareTarget
						.builder()
						.hostname(TARGET_HOSTNAME)
						.id(TARGET_HOSTNAME)
						.type(TargetType.LINUX)
						.build())
				.protocolConfigurations(Map.of(SNMPProtocol.class, protocol))
				.build();

		engineConfigurationAutoSequential = EngineConfiguration
				.builder()
				.target(HardwareTarget
						.builder()
						.hostname(TARGET_HOSTNAME)
						.id(TARGET_HOSTNAME)
						.type(TargetType.LINUX)
						.build())
				.protocolConfigurations(Map.of(SNMPProtocol.class, protocol))
				.sequential(true)
				.build();

		criterion1 = SNMPGetNext.builder().oid(OID1).build();
		connector1 = Connector.builder()
				.compiledFilename(CONNECTOR1_ID)
				.displayName(CONNECTOR1_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).remoteSupport(true)
				.supersedes(Collections.singleton("connector2.hdf"))
				.detection(Detection.builder().criteria(Collections.singletonList(criterion1)).build())
				.sourceTypes(Collections.singleton(SNMPGetTableSource.class))
				.build();

		criterion2 = SNMPGetNext.builder().oid(OID2).build();
		connector2 = Connector.builder()
				.compiledFilename(CONNECTOR2_ID)
				.displayName(CONNECTOR2_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).remoteSupport(true)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion2)).build())
				.sourceTypes(Collections.singleton(SNMPGetTableSource.class))
				.build();

		criterion3 = SNMPGetNext.builder().oid(OID3).build();
		connector3 = Connector.builder()
				.compiledFilename(CONNECTOR3_ID)
				.displayName(CONNECTOR3_ID)
				.appliesToOS(Stream.of(OSType.HP, OSType.STORAGE).collect(Collectors.toSet())).remoteSupport(true)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion3)).build())
				.sourceTypes(Collections.singleton(SNMPGetTableSource.class))
				.build();

		criterion4 = SNMPGetNext.builder().oid(OID4).build();
		connector4 = Connector.builder()
				.compiledFilename(CONNECTOR4_ID)
				.displayName(CONNECTOR4_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).localSupport(true)
				.remoteSupport(false)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion4)).build())
				.sourceTypes(Collections.singleton(SNMPGetTableSource.class))
				.build();

		criterion5 = SNMPGetNext.builder().oid(OID5).build();
		connector5 = Connector.builder()
				.compiledFilename(CONNECTOR5_ID)
				.displayName(CONNECTOR5_ID)
				.appliesToOS(Stream.of(OSType.NT, OSType.LINUX).collect(Collectors.toSet())).remoteSupport(true)
				.detection(Detection.builder().criteria(Collections.singletonList(criterion5)).build())
				.sourceTypes(Collections.singleton(SNMPGetTableSource.class))
				.build();

		engineConfigurationSelection = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(TARGET_HOSTNAME).id(TARGET_HOSTNAME).type(TargetType.LINUX).build())
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
			networkHelper.when(() -> NetworkHelper.isLocalhost(eq(TARGET_HOSTNAME))).thenReturn(false);

			doReturn(SUCCESS_SNMP_RESULT1).when(matsyaClientsExecutor)
					.executeSNMPGetNext(eq(OID1), any(), any(), anyBoolean());

			doReturn(SUCCESS_SNMP_RESULT2).when(matsyaClientsExecutor)
					.executeSNMPGetNext(eq(OID2), any(), any(), anyBoolean());

			doReturn("").when(matsyaClientsExecutor)
					.executeSNMPGetNext(eq(OID5), any(), any(), anyBoolean());

			detectionOperation.call();

			final Monitor target = hostMonitoring.selectFromType(TARGET).get(TARGET_HOSTNAME);
			assertEquals(TARGET_HOSTNAME, target.getName());
			assertEquals(TARGET_HOSTNAME, target.getId());
			assertEquals(TARGET_HOSTNAME, target.getTargetId());

			final Map<String, Monitor> connectors = hostMonitoring.selectFromType(MonitorType.CONNECTOR);
			assertEquals(1, connectors.size());
			Monitor connector = connectors.get(TARGET_HOSTNAME + "@" + CONNECTOR1_ID);
			assertEquals(TARGET_HOSTNAME, connector.getParentId());
			assertEquals(TARGET_HOSTNAME + "@" + CONNECTOR1_ID, connector.getId());
			assertEquals(CONNECTOR1_ID, connector.getName());
			assertEquals(TARGET_HOSTNAME, connector.getTargetId());
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

		final Monitor target = hostMonitoring.selectFromType(MonitorType.TARGET).get(TARGET_HOSTNAME);
		assertEquals(TARGET_HOSTNAME, target.getName());
		assertEquals(TARGET_HOSTNAME, target.getId());
		assertEquals(TARGET_HOSTNAME, target.getTargetId());

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.CONNECTOR);
		assertEquals(2, monitors.size());
		Monitor connector1Mo = monitors.get(TARGET_HOSTNAME + "@" + CONNECTOR1_ID);
		assertEquals(TARGET_HOSTNAME, connector1Mo.getParentId());
		assertEquals(TARGET_HOSTNAME + "@" + CONNECTOR1_ID, connector1Mo.getId());
		assertEquals(CONNECTOR1_ID, connector1Mo.getName());
		assertEquals(TARGET_HOSTNAME, connector1Mo.getTargetId());

		assertNotNull(connector1Mo.getParameters().get(TEST_REPORT_PARAMETER));
		assertEquals(Status.OK, connector1Mo.getParameter(STATUS_PARAMETER, DiscreteParam.class).getState());

		Monitor connector2Mo = monitors.get(TARGET_HOSTNAME + "@" + CONNECTOR2_ID);
		assertEquals(TARGET_HOSTNAME, connector2Mo.getParentId());
		assertEquals(TARGET_HOSTNAME + "@" + CONNECTOR2_ID, connector2Mo.getId());
		assertEquals(CONNECTOR2_ID, connector2Mo.getName());
		assertEquals(TARGET_HOSTNAME, connector2Mo.getTargetId());

		assertNotNull(connector2Mo.getParameters().get(TEST_REPORT_PARAMETER));
		assertEquals(Status.FAILED, connector2Mo.getParameter(STATUS_PARAMETER, DiscreteParam.class).getState());
	}

	@Test
	void testCreateTargetOnExistingTarget() throws UnknownHostException {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString(), null);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfigurationAuto).when(strategyConfig).getEngineConfiguration();

		final Monitor target = Monitor.builder().id(TARGET_ID).targetId(TARGET_ID).name(TARGET_NAME)
				.monitorType(MonitorType.TARGET).build();

		hostMonitoring.addMonitor(target);

		detectionOperation.createTarget(false);

		final Map<String, Monitor> targets = hostMonitoring.selectFromType(MonitorType.TARGET);
		final Monitor actual = hostMonitoring.selectFromType(MonitorType.TARGET).get(TARGET_HOSTNAME);
		assertNotNull(targets);
		assertNotEquals(target, actual);
		assertEquals(TARGET_HOSTNAME, actual.getName());
		assertEquals(TARGET_HOSTNAME, actual.getId());
		assertEquals(TARGET_HOSTNAME, actual.getTargetId());
	}

	@Test
	void testIsSuccessCriterion() {
		{
			final TestedConnector testedConnector = TestedConnector.builder().connector(connector1)
					.criterionTestResults(Collections.emptyList()).build();
			assertFalse(detectionOperation.isSuccessCriterion(testedConnector, TARGET_HOSTNAME));
		}

		{
			final CriterionTestResult ctr = CriterionTestResult.builder().success(true).build();
			final TestedConnector testedConnector = TestedConnector.builder().connector(connector1)
					.criterionTestResults(Stream.of(ctr, ctr).collect(Collectors.toList())).build();
			assertTrue(detectionOperation.isSuccessCriterion(testedConnector, TARGET_HOSTNAME));
		}

		{
			final CriterionTestResult ctr1 = CriterionTestResult.builder().success(true).build();
			final CriterionTestResult ctr2 = CriterionTestResult.builder().success(false).build();
			final TestedConnector testedConnector = TestedConnector.builder().connector(connector1)
					.criterionTestResults(Stream.of(ctr1, ctr2).collect(Collectors.toList())).build();
			assertFalse(detectionOperation.isSuccessCriterion(testedConnector, TARGET_HOSTNAME));
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
			final TestedConnector actual = detectionOperation.testConnector(connector, TARGET_HOSTNAME);
			assertEquals(TestedConnector.builder().connector(connector).build(), actual);
		}

		{
			final Connector connector1 = Connector.builder()
					.detection(Detection.builder().criteria(Collections.emptyList()).build()).build();
			final TestedConnector actual1 = detectionOperation.testConnector(connector1, TARGET_HOSTNAME);
			assertEquals(TestedConnector.builder().connector(connector1).build(), actual1);

			final Connector connector2 = Connector.builder().detection(Detection.builder().criteria(null).build())
					.build();
			final TestedConnector actual2 = detectionOperation.testConnector(connector2, TARGET_HOSTNAME);
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
	void testFilterConnectorsByAcceptedProtocols() {
		Stream<Connector> result = detectionOperation.filterConnectorsByAcceptedSources(Stream.of(connector1, connector2), Set.of(SNMPGetTableSource.class));
		assertEquals(Set.of(connector1, connector2), result.collect(Collectors.toSet()));

		result = detectionOperation.filterConnectorsByAcceptedSources(Stream.of(connector1, connector2, Connector.builder().build()), Set.of(SNMPGetTableSource.class));
		assertEquals(Set.of(connector1, connector2), result.collect(Collectors.toSet()));

		result = detectionOperation.filterConnectorsByAcceptedSources(Stream.of(connector1, connector2, Connector.builder().build()), Set.of(OSCommandSource.class));
		assertEquals(Collections.emptySet(), result.collect(Collectors.toSet()));
	}

	@Test
	void testFilterNoAutoDetectionConnectors() {

		Connector connector1 = new Connector(); // connector1.getNoAutoDetection() == null
		Connector connector2 = Connector.builder().noAutoDetection(Boolean.FALSE).build(); // connector2.getNoAutoDetection() == Boolean.FALSE
		Connector connector3 = Connector.builder().noAutoDetection(Boolean.TRUE).build(); // connector2.getNoAutoDetection() == Boolean.TRUE

		Stream<Connector> connectorsStreamResult = DetectionOperation.filterNoAutoDetectionConnectors(Stream.of(connector1, connector2, connector3));
		assertNotNull(connectorsStreamResult);
		Set<Connector> connectorsSetResult = connectorsStreamResult.collect(Collectors.toSet());
		assertEquals(Set.of(connector1, connector2), connectorsSetResult);
	}

	@Test
	void testCallAutoDetectionSequential() throws Exception {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		doReturn(engineConfigurationAutoSequential).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(Stream.of(connector1, connector2, connector3, connector4, connector5)
				.collect(Collectors.toMap(Connector::getCompiledFilename, Function.identity()))).when(store)
						.getConnectors();

		try (MockedStatic<NetworkHelper> networkHelper = Mockito.mockStatic(NetworkHelper.class)) {
			networkHelper.when(() -> NetworkHelper.isLocalhost(eq(TARGET_HOSTNAME))).thenReturn(false);

			doReturn(SUCCESS_SNMP_RESULT1).when(matsyaClientsExecutor)
					.executeSNMPGetNext(eq(OID1), any(), any(), anyBoolean());

			doReturn(SUCCESS_SNMP_RESULT2).when(matsyaClientsExecutor)
					.executeSNMPGetNext(eq(OID2), any(), any(), anyBoolean());

			doReturn(EMPTY).when(matsyaClientsExecutor)
					.executeSNMPGetNext(eq(OID5), any(), any(), anyBoolean());

			detectionOperation.call();

			final Monitor target = hostMonitoring.selectFromType(TARGET).get(TARGET_HOSTNAME);
			assertEquals(TARGET_HOSTNAME, target.getName());
			assertEquals(TARGET_HOSTNAME, target.getId());
			assertEquals(TARGET_HOSTNAME, target.getTargetId());

			final Map<String, Monitor> connectors = hostMonitoring.selectFromType(MonitorType.CONNECTOR);
			assertEquals(1, connectors.size());  // connector 1 supersedes connector 2 that's why we've detected only one connector
			Monitor connector = connectors.get(TARGET_HOSTNAME + "@" + CONNECTOR1_ID);
			assertEquals(TARGET_HOSTNAME, connector.getParentId());
			assertEquals(TARGET_HOSTNAME + "@" + CONNECTOR1_ID, connector.getId());
			assertEquals(CONNECTOR1_ID, connector.getName());
			assertEquals(TARGET_HOSTNAME, connector.getTargetId());
		}

	}

	@Test
	void testFilterLastResortConnectors() {
		{
			// A single "last resort" connector discovering the same hardware monitor as a regular connector
			Connector lastResortConnector = Connector.builder().onLastResort(MonitorType.ENCLOSURE).build();
			TestedConnector testedLastResortConnector = TestedConnector.builder().connector(lastResortConnector).build();

			List<TestedConnector> testedConnectors = new ArrayList<>();
			testedConnectors.add(testedLastResortConnector);
			
			detectionOperation.filterLastResortConnectors(testedConnectors, "localhost");
			
			// The last resort connector should be kept
			assertTrue(testedConnectors.size() == 1);
			assertTrue(testedConnectors.contains(testedLastResortConnector));

			// Test with two connectors: the last resort and a regular one with a matching monitor type
			Connector regularConnector = Connector.builder().hardwareMonitors(List.of(HardwareMonitor.builder().type(MonitorType.ENCLOSURE)
					.discovery(Discovery.builder().instanceTable(TextInstanceTable.builder().text("Test").build()).build())
					.build())).build();
		
			TestedConnector testedRegularConnector = TestedConnector.builder().connector(regularConnector).build();
			testedConnectors.add(testedRegularConnector);

			detectionOperation.filterLastResortConnectors(testedConnectors, "localhost");
			
			// We should only have the regular connector left
			assertEquals(1, testedConnectors.size());
			assertTrue(testedConnectors.contains(testedRegularConnector));
		}
		
		{
			// A single "last resort" connector discovering something else than the regular connector
			// Build a list of two connectors: a regular one and a last resort of Disk Controllers
			Connector lastResortConnector = Connector.builder().onLastResort(MonitorType.DISK_CONTROLLER).build();
			TestedConnector testedLastResortConnector = TestedConnector.builder().connector(lastResortConnector).build();
			
			Connector regularConnector = Connector.builder().hardwareMonitors(List.of(HardwareMonitor.builder().type(MonitorType.ENCLOSURE)
					.discovery(Discovery.builder().instanceTable(TextInstanceTable.builder().text("Test").build()).build())
					.build())).build();	
			TestedConnector testedRegularConnector = TestedConnector.builder().connector(regularConnector).build();
			
			List<TestedConnector> testedConnectors = new ArrayList<>(List.of(testedLastResortConnector, testedRegularConnector));
			
			detectionOperation.filterLastResortConnectors(testedConnectors, "localhost");
			
			// Our two connectors should still be in the list as the regular connector does not discover disk controllers
			assertEquals(2, testedConnectors.size());
			assertTrue(testedConnectors.contains(testedRegularConnector));
			assertTrue(testedConnectors.contains(testedLastResortConnector));
		}
		
		{
			// Two identical "last resort" connectors discovering something else than the regular connector

			// Regular connector with an enclosure instance table
			Connector regularConnector = Connector.builder().hardwareMonitors(List.of(HardwareMonitor.builder().type(MonitorType.ENCLOSURE)
					.discovery(Discovery.builder().instanceTable(TextInstanceTable.builder().text("Test").build()).build())
					.build())).build();	
			TestedConnector testedRegularConnector = TestedConnector.builder().connector(regularConnector).build();

			// Last resort connector 1 with a disk controller instance table
			Connector lastResortConnector1 = new Connector();
			lastResortConnector1.setOnLastResort(MonitorType.DISK_CONTROLLER);
			final HardwareMonitor diskControllerMonitor = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).build();
			diskControllerMonitor.setDiscovery(Discovery.builder().instanceTable(TextInstanceTable.builder().text("Test").build()).build());
			lastResortConnector1.setHardwareMonitors(Collections.singletonList(diskControllerMonitor));
			TestedConnector testedLastResortConnector1 = TestedConnector.builder().connector(lastResortConnector1).build();
			
			// Last resort connector 2 with Disk controller monitor type, no instance table (no need)
			Connector lastResortConnector2 = Connector.builder().onLastResort(MonitorType.DISK_CONTROLLER).build();
			TestedConnector testedLastResortConnector2 = TestedConnector.builder().connector(lastResortConnector2).build();
			
			// Build the list
			List<TestedConnector> testedConnectors = new ArrayList<>(List.of(testedLastResortConnector1, testedLastResortConnector2, testedRegularConnector));
			
			detectionOperation.filterLastResortConnectors(testedConnectors, "localhost");
			
			// The regular connector and the first last resort connector should be in the list. The second last resort connector should 
			// have been removed because we already have a connector that discovers the same monitor type (the first last resort connector) 
			assertEquals(2, testedConnectors.size());
			assertTrue(testedConnectors.contains(testedRegularConnector));
			assertTrue(testedConnectors.contains(testedLastResortConnector1));
		}
		
		{
			// Two different "last resort" connectors discovering something else than the regular connector
			
			// Regular connector with an enclosure instance table
			Connector regularConnector = Connector.builder().hardwareMonitors(List.of(HardwareMonitor.builder().type(MonitorType.ENCLOSURE)
					.discovery(Discovery.builder().instanceTable(TextInstanceTable.builder().text("Test").build()).build())
					.build())).build();	
			TestedConnector testedRegularConnector = TestedConnector.builder().connector(regularConnector).build();

			// Last resort connector 1 with a disk controller instance table
			Connector lastResortConnector1 = new Connector();
			lastResortConnector1.setOnLastResort(MonitorType.DISK_CONTROLLER);
			final HardwareMonitor enclosureMonitor = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).build();
			enclosureMonitor.setDiscovery(Discovery.builder().instanceTable(TextInstanceTable.builder().text("Test").build()).build());
			lastResortConnector1.setHardwareMonitors(Collections.singletonList(enclosureMonitor));
			TestedConnector testedLastResortConnector1 = TestedConnector.builder().connector(lastResortConnector1).build();
			
			// Last resort connector 2 with Disk controller monitor type, no instance table (no need)
			Connector lastResortConnector2 = Connector.builder().onLastResort(MonitorType.GPU).build();
			final HardwareMonitor gpuMonitor = HardwareMonitor.builder().type(MonitorType.GPU).build();
			gpuMonitor.setDiscovery(Discovery.builder().instanceTable(TextInstanceTable.builder().text("Test").build()).build());
			lastResortConnector2.setHardwareMonitors(Collections.singletonList(gpuMonitor));
			TestedConnector testedLastResortConnector2 = TestedConnector.builder().connector(lastResortConnector2).build();
			
			// Build the list
			List<TestedConnector> testedConnectors = new ArrayList<>(List.of(testedLastResortConnector1, testedLastResortConnector2, testedRegularConnector));
			
			detectionOperation.filterLastResortConnectors(testedConnectors, "localhost");
			
			// All connectors should be kept 
			assertEquals(3, testedConnectors.size());
			assertTrue(testedConnectors.contains(testedRegularConnector));
			assertTrue(testedConnectors.contains(testedLastResortConnector1));
			assertTrue(testedConnectors.contains(testedLastResortConnector2));
		}
	}
}
