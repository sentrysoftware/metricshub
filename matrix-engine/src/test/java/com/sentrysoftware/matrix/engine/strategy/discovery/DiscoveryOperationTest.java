package com.sentrysoftware.matrix.engine.strategy.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.InstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.SourceInstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.TextInstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.source.SourceVisitor;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;


@ExtendWith(MockitoExtension.class)
class DiscoveryOperationTest {

	private static final String ID_COUNT_0 = "0";
	private static final String FAN_NAME = "Fan: 1";
	private static final String FAN_ID = "myConnecctor1.connector_fan_ecs1-01_1.1";
	private static final String SPEED = "speed";
	private static final String SPEED_VALUE = "1000";
	private static final String FAN_1 = "Fan 1";
	private static final String ENCLOSURE_DELL = "Enclosure: Dell";
	private static final String HARD_CODED_ENCLOSURE_ID = "myConnecctor1.connector_enclosure_ecs1-01_DellEnclosure";
	private static final String ENCLOSURE_NAME = "Computer: PowerEdge 54dsf (Dell 2200)";
	private static final String ENCLOSURE_ID = "myConnecctor1.connector_enclosure_ecs1-01_1.1";
	private static final String TYPE = "type";
	private static final String DELL_ENCLOSURE = "Dell Enclosure";
	private static final String OUT_OF_RANGE = "OutOfRangeParam";
	private static final String MODEL_VALUE = "2200";
	private static final String POWER_EDGE_54DSF = "PowerEdge 54dsf";
	private static final String ID = "1.1";
	private static final String INSTANCETABLE_COLUMN_4 = "instancetable.column(4)";
	private static final String INSTANCETABLE_COLUMN_3 = " instancetable.column(3) ";
	private static final String INSTANCETABLE_COLUMN_2 = " instancetable.column(2)";
	private static final String INSTANCETABLE_COLUMN_1 = "instancetable.column(1)";
	private static final String DELL = "Dell";
	private static final String MODEL_PASCAL = "Model";
	private static final String VENDOR_PASCAL = "Vendor";
	private static final String DISPLAY_ID_PASCAL = "DisplayID";
	private static final String DEVICE_ID_PASCAL = "DeviceID";
	private static final String MODEL = "model";
	private static final String VENDOR = "vendor";
	private static final String DISPLAY_ID = "displayId";
	private static final String DEVICE_ID = "deviceId";
	private static final String EMPTY = "";
	private static final String COMMUNITY = "public";
	private static final String ECS1_01 = "ecs1-01";
	private static final String OID1 = "1.2.3.4.5";
	private static final String OID2 = "1.2.3.4.6";
	private static final String MY_CONNECTOR_1_NAME = "myConnecctor1.connector";
	private static final String ENCLOSURE_SOURCE_KEY = "Enclosure.discovery.Source(1)";
	private static final String FAN_SOURCE_KEY = "Fan.discovery.Source(1)";
	private static final String MY_CONNECTOR_2_NAME = "myConnecctor2.connector";

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private ConnectorStore store;

	@Mock
	private SourceVisitor sourceVisitor;

	private static Long strategyTime = new Date().getTime();

	@InjectMocks
	private DiscoveryOperation discoveryOperation;

	private static EngineConfiguration engineConfiguration;

	private static Connector connector;

	@BeforeAll
	public static void setUp() {
		final SNMPProtocol protocol = SNMPProtocol.builder().community(COMMUNITY).version(SNMPVersion.V1).port(161)
				.timeout(120L).build();
		engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(ECS1_01).id(ECS1_01).type(TargetType.LINUX).build())
				.protocolConfigurations(Stream.of(protocol).collect(Collectors.toSet())).build();

		connector = Connector.builder().compiledFilename(MY_CONNECTOR_1_NAME).build();
	}

	@BeforeEach
	void beforeEeach() {
		discoveryOperation.setStrategyTime(strategyTime);
	}

	@Test
	void testCall() throws Exception {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		hostMonitoring.addMonitor(targetMonitor);

		final Monitor connectorMo1 = Monitor
				.builder()
				.monitorType(MonitorType.CONNECTOR)
				.name(MY_CONNECTOR_1_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.id(MY_CONNECTOR_1_NAME)
				.build();

		final Monitor connectorMo2 = Monitor
				.builder()
				.monitorType(MonitorType.CONNECTOR)
				.name(MY_CONNECTOR_2_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.id(MY_CONNECTOR_2_NAME)
				.build();

		hostMonitoring.addMonitor(connectorMo1);
		hostMonitoring.addMonitor(connectorMo2);

		final Connector connector1 = Connector.builder()
				.compiledFilename(MY_CONNECTOR_1_NAME)
				.hardwareMonitors(
						Collections
						.singletonList(
								HardwareMonitor
								.builder()
								.type(MonitorType.ENCLOSURE)
								.build()))
				.build();

		final Connector connector2 = Connector.builder()
				.compiledFilename(MY_CONNECTOR_2_NAME)
				.hardwareMonitors(
						Collections
						.singletonList(
								HardwareMonitor
								.builder()
								.type(MonitorType.FAN)
								.build()))
				.build();
		final Map<String, Connector> connectors = Map.of(connector1.getCompiledFilename(), connector1, connector2.getCompiledFilename(), connector2);
		doReturn(connectors).when(store).getConnectors();
		final Boolean result = discoveryOperation.call();
		assertEquals(targetMonitor, hostMonitoring.getMonitors().get(MonitorType.TARGET).values().stream().findFirst().get());
		assertEquals(2, hostMonitoring.getMonitors().get(MonitorType.CONNECTOR).values().size());
		assertTrue(result);
	}

	@Test
	void testCallNoDetectedConnectors() throws Exception {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		hostMonitoring.addMonitor(targetMonitor);

		discoveryOperation.call();
		assertEquals(targetMonitor, hostMonitoring.getMonitors().get(MonitorType.TARGET).values().stream().findFirst().get());
		assertEquals(1, hostMonitoring.getMonitors().size());

		final Monitor connector = Monitor
				.builder()
				.monitorType(MonitorType.CONNECTOR)
				.name(MY_CONNECTOR_1_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.id(MY_CONNECTOR_1_NAME)
				.build();

		hostMonitoring.addMonitor(connector);
		hostMonitoring.removeMonitor(connector);

		discoveryOperation.call();
		assertEquals(targetMonitor, hostMonitoring.getMonitors().get(MonitorType.TARGET).values().stream().findFirst().get());
		assertTrue(hostMonitoring.getMonitors().get(MonitorType.CONNECTOR).isEmpty());
	}

	@Test
	void testCallNoTargetMonitor() throws Exception {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		hostMonitoring.addMonitor(targetMonitor);
		hostMonitoring.removeMonitor(targetMonitor);
		discoveryOperation.call();
		assertTrue(hostMonitoring.getMonitors().get(MonitorType.TARGET).isEmpty());
		assertEquals(1, hostMonitoring.getMonitors().size());
	}

	@Test
	void testCallNoTargets() throws Exception {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		discoveryOperation.call();
		assertTrue(hostMonitoring.getMonitors().isEmpty());
	}

	@Test
	void testDiscoverNoHardwareMonitors() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());
		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		final Connector connector = Connector.builder().compiledFilename(MY_CONNECTOR_1_NAME).hardwareMonitors(null).build();
		discoveryOperation.discover(connector , hostMonitoring, ECS1_01, targetMonitor);

		assertTrue(hostMonitoring.getMonitors().isEmpty());
	}

	@Test
	void testDiscoverMultiJobs() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		final HardwareMonitor enclosureMonitor = buildHardwareEnclosureMonitor();
		final HardwareMonitor fanMonitor = buildHardwareFanMonitor();

		final Connector connector = Connector
				.builder()
				.compiledFilename(MY_CONNECTOR_1_NAME)
				.hardwareMonitors(Arrays.asList(enclosureMonitor, fanMonitor, null))
				.build();

		final List<List<String>> enclosureData = Collections.singletonList(Arrays.asList(ID, POWER_EDGE_54DSF, MODEL_VALUE));
		final SourceTable enclosureSourceTable = SourceTable.builder().table(enclosureData).build();

		final List<List<String>> fanDate = Collections.singletonList(Arrays.asList(ID, FAN_1, SPEED_VALUE));
		final SourceTable fanSourceTable = SourceTable.builder().table(fanDate).build();

		hostMonitoring.addMonitor(targetMonitor);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(enclosureSourceTable).when(sourceVisitor).visit((SNMPGetTableSource) enclosureMonitor.getDiscovery().getSources().get(0));
		doReturn(fanSourceTable).when(sourceVisitor).visit((SNMPGetTableSource) fanMonitor.getDiscovery().getSources().get(0));

		final Map<String, String> enclosureMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		enclosureMetadata.put(DEVICE_ID, ID);
		enclosureMetadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		enclosureMetadata.put(VENDOR, DELL);
		enclosureMetadata.put(MODEL, MODEL_VALUE);
		enclosureMetadata.put(HardwareConstants.ID_COUNT, ID_COUNT_0);
		enclosureMetadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

		final Monitor expectedEnclosure = Monitor.builder()
				.id(ENCLOSURE_ID)
				.name(ENCLOSURE_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(enclosureMetadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(HardwareConstants.COMPUTER)
				.build();

		final Map<String, String> fanMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		fanMetadata.put(DEVICE_ID, ID);
		fanMetadata.put(DISPLAY_ID, FAN_1);
		fanMetadata.put(SPEED, SPEED_VALUE);
		fanMetadata.put(HardwareConstants.ID_COUNT, ID_COUNT_0);
		
		final Monitor expectedFan = Monitor.builder()
				.id(FAN_ID)
				.name(FAN_NAME)
				.parentId(ENCLOSURE_ID)
				.targetId(ECS1_01)
				.metadata(fanMetadata)
				.monitorType(MonitorType.FAN)
				.extendedType(MonitorType.FAN.getName())
				.build();

		discoveryOperation.discover(connector, hostMonitoring, ECS1_01, targetMonitor);

		final Map<String, Monitor> enclosures = hostMonitoring.selectFromType(MonitorType.ENCLOSURE);

		assertEquals(expectedEnclosure, enclosures.values().stream().findFirst().get());

		final Map<String, Monitor> fans = hostMonitoring.selectFromType(MonitorType.FAN);

		assertEquals(expectedFan, fans.values().stream().findFirst().get());
	}

	private HardwareMonitor buildHardwareFanMonitor() {
		final SourceInstanceTable sourceInstanceTable = SourceInstanceTable
				.builder()
				.sourceKey(FAN_SOURCE_KEY)
				.build();
		final SNMPGetTableSource source = SNMPGetTableSource
				.builder()
				.oid(OID2)
				.key(FAN_SOURCE_KEY)
				.computes(Collections.singletonList(LeftConcat.builder().column(1).string(EMPTY).build()))
				.build();
		final Map<String, String> parameters = Map.of(
				DEVICE_ID, INSTANCETABLE_COLUMN_1,
				DISPLAY_ID, INSTANCETABLE_COLUMN_2,
				SPEED, INSTANCETABLE_COLUMN_3);
		final Discovery discovery = Discovery
				.builder()
				.instanceTable(sourceInstanceTable)
				.sources(Collections.singletonList(source))
				.parameters(parameters)
				.build();
		final HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.FAN)
				.discovery(discovery)
				.build();
		return hardwareMonitor;
	}

	@Test
	void testDiscover() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		final HardwareMonitor hardwareMonitor = buildHardwareEnclosureMonitor();

		final Connector connector = Connector
				.builder()
				.compiledFilename(MY_CONNECTOR_1_NAME)
				.hardwareMonitors(Collections.singletonList(hardwareMonitor))
				.build();
	
		final List<List<String>> data = Collections.singletonList(Arrays.asList(ID, POWER_EDGE_54DSF, MODEL_VALUE));
		final SourceTable sourceTable = SourceTable.builder().table(data).build();

		hostMonitoring.addMonitor(targetMonitor);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(sourceTable).when(sourceVisitor).visit(any(SNMPGetTableSource.class));

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(VENDOR, DELL);
		metadata.put(MODEL, MODEL_VALUE);
		metadata.put(HardwareConstants.ID_COUNT, ID_COUNT_0);
		metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);
		
		final Monitor expectedEnclosure = Monitor.builder()
				.id(ENCLOSURE_ID)
				.name(ENCLOSURE_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(HardwareConstants.COMPUTER)
				.build();



		discoveryOperation.discover(connector, hostMonitoring, ECS1_01, targetMonitor);

		final Map<String, Monitor> enclosures = hostMonitoring.selectFromType(MonitorType.ENCLOSURE);

		assertEquals(expectedEnclosure, enclosures.values().stream().findFirst().get());
	}

	@Test
	void testDiscoverSameTypeMonitors() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		final HardwareMonitor hardwareMonitor = buildHardwareEnclosureMonitor();

		final List<List<String>> data = Collections.singletonList(Arrays.asList(ID, POWER_EDGE_54DSF, MODEL_VALUE));
		final SourceTable sourceTable = SourceTable.builder().table(data).build();

		hostMonitoring.addMonitor(targetMonitor);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(sourceTable).when(sourceVisitor).visit(any(SNMPGetTableSource.class));

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(VENDOR, DELL);
		metadata.put(MODEL, MODEL_VALUE);
		metadata.put(HardwareConstants.ID_COUNT, ID_COUNT_0);
		metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

		final Monitor expectedEnclosure = Monitor.builder()
				.id(ENCLOSURE_ID)
				.name(ENCLOSURE_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(HardwareConstants.COMPUTER)
				.build();


		discoveryOperation.discoverSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, targetMonitor, ECS1_01);

		final Map<String, Monitor> enclosures = hostMonitoring.selectFromType(MonitorType.ENCLOSURE);

		assertEquals(expectedEnclosure, enclosures.values().stream().findFirst().get());

	}

	private HardwareMonitor buildHardwareEnclosureMonitor() {
		final SourceInstanceTable sourceInstanceTable = SourceInstanceTable
				.builder()
				.sourceKey(ENCLOSURE_SOURCE_KEY)
				.build();
		final SNMPGetTableSource source = SNMPGetTableSource
				.builder()
				.oid(OID1)
				.key(ENCLOSURE_SOURCE_KEY)
				.computes(Collections.singletonList(LeftConcat.builder().column(1).string(EMPTY).build()))
				.build();
		final Map<String, String> parameters = Map.of(
				DEVICE_ID, INSTANCETABLE_COLUMN_1,
				DISPLAY_ID, INSTANCETABLE_COLUMN_2, 
				VENDOR, DELL,
				MODEL, INSTANCETABLE_COLUMN_3,
				TYPE, HardwareConstants.COMPUTER);
		final Discovery discovery = Discovery
				.builder()
				.instanceTable(sourceInstanceTable)
				.sources(Collections.singletonList(source))
				.parameters(parameters)
				.build();
		final HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();
		return hardwareMonitor;
	}

	@Test
	void testDiscoverSameTypeMonitorsNoParameters() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());
		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		final SourceInstanceTable sourceTable = SourceInstanceTable
				.builder()
				.sourceKey(ENCLOSURE_SOURCE_KEY)
				.build();
		final Discovery discovery = Discovery
				.builder()
				.instanceTable(sourceTable)
				.build();
		final HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();

		discoveryOperation.discoverSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, targetMonitor, ECS1_01);

		assertNull(hostMonitoring.selectFromType(MonitorType.ENCLOSURE));

		discovery.setParameters(null);
		discoveryOperation.discoverSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, targetMonitor, ECS1_01);

		assertNull(hostMonitoring.selectFromType(MonitorType.ENCLOSURE));
	}

	@Test
	void testDiscoverSameTypeMonitorsNullInstanceTable() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());
		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.ENCLOSURE).discovery(Discovery.builder().instanceTable(null).build()).build();
		discoveryOperation.discoverSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, targetMonitor, ECS1_01);

		assertNull(hostMonitoring.selectFromType(MonitorType.ENCLOSURE));
	}

	@Test
	void testDiscoverSameTypeMonitorsNullDiscovery() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory
				.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString());
		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		final HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(null)
				.build();
		discoveryOperation.discoverSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, targetMonitor, ECS1_01);

		assertNull(hostMonitoring.selectFromType(MonitorType.ENCLOSURE));
	}

	@Test
	void testDiscoverSameTypeMonitorsNullType() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory
				.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString());
		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().discovery(null).build();
		discoveryOperation.discoverSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, targetMonitor, ECS1_01);

		assertNull(hostMonitoring.selectFromType(MonitorType.ENCLOSURE));
	}

	@Test
	void testCreateSameTypeMonitorsNoSourceKey() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		final InstanceTable instanceTable = SourceInstanceTable.builder().sourceKey(null).build();
		final Map<String, String> parameters = Map.of(
				DEVICE_ID, INSTANCETABLE_COLUMN_1,
				DISPLAY_ID, INSTANCETABLE_COLUMN_2, 
				VENDOR, DELL,
				MODEL, INSTANCETABLE_COLUMN_3,
				TYPE, HardwareConstants.COMPUTER);

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();

		discoveryOperation.createSameTypeMonitors(MY_CONNECTOR_1_NAME, hostMonitoring, instanceTable , parameters, targetMonitor , MonitorType.ENCLOSURE, ECS1_01);

		assertNull(hostMonitoring.selectFromType(MonitorType.ENCLOSURE));
	}

	@Test
	void testCreateSameTypeMonitorsNoSources() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		final InstanceTable instanceTable = SourceInstanceTable.builder().sourceKey(ENCLOSURE_SOURCE_KEY).build();
		final Map<String, String> parameters = Map.of(
				DEVICE_ID, INSTANCETABLE_COLUMN_1,
				DISPLAY_ID, INSTANCETABLE_COLUMN_2, 
				VENDOR, DELL,
				MODEL, INSTANCETABLE_COLUMN_3,
				TYPE, HardwareConstants.COMPUTER);

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();

		discoveryOperation.createSameTypeMonitors(MY_CONNECTOR_1_NAME, hostMonitoring, instanceTable , parameters, targetMonitor , MonitorType.ENCLOSURE, ECS1_01);

		assertNull(hostMonitoring.selectFromType(MonitorType.ENCLOSURE));
	}

	@Test
	void testCreateSameTypeMonitorsSourceTextTable() {

		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());

		final Map<String, String> parameters = Map.of(
				DEVICE_ID, DELL_ENCLOSURE,
				VENDOR, DELL);

		final InstanceTable instanceTable = TextInstanceTable.builder().text(DELL_ENCLOSURE).build();
		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();

		hostMonitoring.addMonitor(targetMonitor);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		discoveryOperation.createSameTypeMonitors(MY_CONNECTOR_1_NAME, hostMonitoring, instanceTable , parameters, targetMonitor , MonitorType.ENCLOSURE, ECS1_01);

		final Map<String, Monitor> enclosures = hostMonitoring.selectFromType(MonitorType.ENCLOSURE);
		assertEquals(1, enclosures.size());

		Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		metadata.put(DEVICE_ID, DELL_ENCLOSURE);
		metadata.put(VENDOR, DELL);
		metadata.put(HardwareConstants.ID_COUNT, ID_COUNT_0);

		final Monitor expectedEnclosure = Monitor.builder()
				.id(HARD_CODED_ENCLOSURE_ID)
				.name(ENCLOSURE_DELL)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(HardwareConstants.ENCLOSURE)
				.build();

		assertEquals(expectedEnclosure, enclosures.values().stream().findFirst().get());
	}

	@Test
	void testCreateSameTypeMonitorsSourceInstanceTable() {

		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());

		final Map<String, String> parameters = Map.of(
				DEVICE_ID, INSTANCETABLE_COLUMN_1,
				DISPLAY_ID, INSTANCETABLE_COLUMN_2, 
				VENDOR, DELL,
				MODEL, INSTANCETABLE_COLUMN_3,
				TYPE, HardwareConstants.COMPUTER);

		final InstanceTable instanceTable = SourceInstanceTable.builder().sourceKey(ENCLOSURE_SOURCE_KEY).build();
		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();

		final List<List<String>> data = Collections.singletonList(Arrays.asList(ID, POWER_EDGE_54DSF, MODEL_VALUE));
		final SourceTable sourceTable = SourceTable.builder().table(data).build();

		hostMonitoring.addSourceTable(ENCLOSURE_SOURCE_KEY, sourceTable);
		hostMonitoring.addMonitor(targetMonitor);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		discoveryOperation.createSameTypeMonitors(
				MY_CONNECTOR_1_NAME,
				hostMonitoring,
				instanceTable,
				parameters,
				targetMonitor,
				MonitorType.ENCLOSURE, ECS1_01);

		final Map<String, Monitor> enclosures = hostMonitoring.selectFromType(MonitorType.ENCLOSURE);
		assertEquals(1, enclosures.size());

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(VENDOR, DELL);
		metadata.put(MODEL, MODEL_VALUE);
		metadata.put(HardwareConstants.ID_COUNT, ID_COUNT_0);
		metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

		final Monitor expectedEnclosure = Monitor.builder()
				.id(ENCLOSURE_ID)
				.name(ENCLOSURE_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(HardwareConstants.COMPUTER)
				.build();

		assertEquals(expectedEnclosure, enclosures.values().stream().findFirst().get());
	}

	@Test
	void testProcessTextParameters() {

		final Monitor monitor = Monitor.builder().build();
		final Map<String, String> parameters = Map.of(
				DEVICE_ID, DELL_ENCLOSURE,
				VENDOR, DELL);
		
		discoveryOperation.processTextParameters(parameters, monitor);
	
		final Map<String, String> metadata = monitor.getMetadata();

		assertEquals(DELL_ENCLOSURE, metadata.get(DEVICE_ID_PASCAL));
		assertEquals(DELL, metadata.get(VENDOR_PASCAL));
	}

	@Test
	void testProcessSourceTableParameters() {

		final Map<String, String> parameters = Map.of(
				DEVICE_ID, INSTANCETABLE_COLUMN_1,
				DISPLAY_ID, INSTANCETABLE_COLUMN_2, 
				VENDOR, DELL,
				MODEL, INSTANCETABLE_COLUMN_3,
				OUT_OF_RANGE, INSTANCETABLE_COLUMN_4);
		final List<String> row = Arrays.asList(ID, POWER_EDGE_54DSF, MODEL_VALUE);
		final Monitor monitor = Monitor.builder().build();
		discoveryOperation.processSourceTableParameters(MY_CONNECTOR_1_NAME, parameters, ENCLOSURE_SOURCE_KEY, row , monitor , 0);

		final Map<String, String> metadata = monitor.getMetadata();
		
		assertEquals(ID, metadata.get(DEVICE_ID_PASCAL));
		assertEquals(POWER_EDGE_54DSF, metadata.get(DISPLAY_ID_PASCAL));
		assertEquals(DELL, metadata.get(VENDOR_PASCAL));
		assertEquals(MODEL_VALUE, metadata.get(MODEL_PASCAL));
		assertEquals(ID_COUNT_0, metadata.get(HardwareConstants.ID_COUNT));
		assertNull(metadata.get(OUT_OF_RANGE));
	}

	
	@Test
	void testProcessSourcesAndComputes() {

		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString());

		discoveryOperation.processSourcesAndComputes(Collections.emptyList(), hostMonitoring, connector, MonitorType.ENCLOSURE, ECS1_01);
		assertTrue(hostMonitoring.getSourceTables().isEmpty());

		discoveryOperation.processSourcesAndComputes(null, hostMonitoring, connector, MonitorType.ENCLOSURE, ECS1_01);
		assertTrue(hostMonitoring.getSourceTables().isEmpty());

		SNMPGetTableSource source = SNMPGetTableSource
				.builder()
				.oid(OID1)
				.key(ENCLOSURE_SOURCE_KEY)
				.computes(Collections.singletonList(LeftConcat.builder().column(1).string(EMPTY).build()))
				.build();
		final List<List<String>> data = Arrays.asList(Arrays.asList("val1", "val2"), Arrays.asList("val3, val4"));
		final SourceTable expected = SourceTable.builder().table(data).build();
		doReturn(expected).when(sourceVisitor).visit(source);
		discoveryOperation.processSourcesAndComputes(
				Collections.singletonList(source),
				hostMonitoring,
				connector,
				MonitorType.ENCLOSURE,
				ECS1_01);
		assertEquals(expected, hostMonitoring.getSourceTableByKey(ENCLOSURE_SOURCE_KEY));

		source = SNMPGetTableSource.builder().oid(OID1).key(ENCLOSURE_SOURCE_KEY).build();
		source.setComputes(null);
		doReturn(expected).when(sourceVisitor).visit(source);
		discoveryOperation.processSourcesAndComputes(
				Collections.singletonList(source),
				hostMonitoring,
				connector,
				MonitorType.ENCLOSURE,
				ECS1_01);
		assertEquals(expected, hostMonitoring.getSourceTableByKey(ENCLOSURE_SOURCE_KEY));

	}

}
