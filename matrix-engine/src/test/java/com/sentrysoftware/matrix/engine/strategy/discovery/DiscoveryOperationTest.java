package com.sentrysoftware.matrix.engine.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVERAGE_CPU_TEMPERATURE_WARNING;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENCLOSURE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ID_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IS_CPU_SENSOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TARGET_FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.FAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;

@ExtendWith(MockitoExtension.class)
class DiscoveryOperationTest {

	private static final String ID_COUNT_0 = "0";
	private static final String FAN_NAME = "Fan 1";
	private static final String FAN_ID = "myConnector1_fan_ecs1-01_1.1";
	private static final String SPEED = "speed";
	private static final String SPEED_VALUE = "1000";
	private static final String FAN_1 = "Fan 1";
	private static final String ENCLOSURE_DELL = "Enclosure: Dell";
	private static final String HARD_CODED_ENCLOSURE_ID = "myConnector1_enclosure_ecs1-01_DellEnclosure";
	private static final String ENCLOSURE_NAME = "Computer: PowerEdge 54dsf (Dell 2200)";
	private static final String ENCLOSURE_ID = "myConnector1_enclosure_ecs1-01_1.1";
	private static final String TYPE = "type";
	private static final String DELL_ENCLOSURE = "Dell Enclosure";
	private static final String OUT_OF_RANGE = "OutOfRangeParam";
	private static final String MODEL_VALUE = "2200";
	private static final String POWER_EDGE_54DSF = "PowerEdge 54dsf";
	private static final String ID = "1.1";
	private static final String INSTANCETABLE_COLUMN_5 = "instancetable.column(5)";
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
	private static final String OID_ENCLOSURE = "1.2.3.4.5";
	private static final String OID_FAN = "1.2.3.4.6";
	private static final String MY_CONNECTOR_1_NAME = "myConnector1";
	private static final String ENCLOSURE_SOURCE_KEY = "Enclosure.discovery.Source(1)";
	private static final String FAN_SOURCE_KEY = "Fan.discovery.Source(1)";
	private static final String MY_CONNECTOR_2_NAME = "myConnector2";
	private static final String INFORMATION1 = "test information 1";
	private static final String INFORMATION2 = "test information 2";
	private static final String INFORMATION3 = "test information 3";

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private ConnectorStore store;

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

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
				.protocolConfigurations(Map.of(SNMPProtocol.class, protocol)).build();

		connector = Connector.builder().compiledFilename(MY_CONNECTOR_1_NAME).build();
	}

	@BeforeEach
	void beforeEeach() {
		discoveryOperation.setStrategyTime(strategyTime);
	}

	@Test
	void testCall() throws Exception {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString(), null);
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
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString(), null);
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
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString(), null);
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
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString(), null);
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		discoveryOperation.call();
		assertTrue(hostMonitoring.getMonitors().isEmpty());
	}

	@Test
	void testDiscoverMultiJobs() throws Exception {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString(), null);

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
		final List<List<String>> fanData = Collections.singletonList(Arrays.asList(ID, FAN_1, SPEED_VALUE));

		hostMonitoring.addMonitor(targetMonitor);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		doReturn(enclosureData).when(matsyaClientsExecutor)
				.executeSNMPTable(eq(OID_ENCLOSURE), any(), any(), any(), anyBoolean());

		doReturn(fanData).when(matsyaClientsExecutor)
				.executeSNMPTable(eq(OID_FAN), any(), any(), any(), anyBoolean());

		final Map<String, String> enclosureMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		enclosureMetadata.put(DEVICE_ID, ID);
		enclosureMetadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		enclosureMetadata.put(VENDOR, DELL);
		enclosureMetadata.put(MODEL, MODEL_VALUE);
		enclosureMetadata.put(ID_COUNT, ID_COUNT_0);
		enclosureMetadata.put(TYPE, COMPUTER);
		enclosureMetadata.put(CONNECTOR, MY_CONNECTOR_1_NAME);
		enclosureMetadata.put(TARGET_FQDN, null);
		enclosureMetadata.put(ADDITIONAL_INFORMATION1, INFORMATION1);
		enclosureMetadata.put(IDENTIFYING_INFORMATION, INFORMATION1);

		final Monitor expectedEnclosure = Monitor.builder()
				.id(ENCLOSURE_ID)
				.name(ENCLOSURE_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(enclosureMetadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(COMPUTER)
				.alertRules(MonitorType.ENCLOSURE.getMetaMonitor().getStaticAlertRules())
				.parameters(Map.of("Present", DiscreteParam.present()))
				.discoveryTime(strategyTime)
				.build();

		final Map<String, String> fanMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		fanMetadata.put(DEVICE_ID, ID);
		fanMetadata.put(DISPLAY_ID, FAN_1);
		fanMetadata.put(SPEED, SPEED_VALUE);
		fanMetadata.put(ID_COUNT, ID_COUNT_0);
		fanMetadata.put(CONNECTOR, MY_CONNECTOR_1_NAME);
		fanMetadata.put(TARGET_FQDN, null);
		fanMetadata.put(IDENTIFYING_INFORMATION, EMPTY);

		final Monitor expectedFan = Monitor.builder()
				.id(FAN_ID)
				.name(FAN_NAME)
				.parentId(ENCLOSURE_ID)
				.targetId(ECS1_01)
				.metadata(fanMetadata)
				.monitorType(MonitorType.FAN)
				.extendedType(MonitorType.FAN.getNameInConnector())
				.parameters(Map.of(PRESENT_PARAMETER, DiscreteParam.present()))
				.alertRules(MonitorType.FAN.getMetaMonitor().getStaticAlertRules())
				.discoveryTime(strategyTime)
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
				.oid(OID_FAN)
				.snmpTableSelectColumns(Arrays.asList("1", "2", "3"))
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
	void testDiscover() throws Exception {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString(), null);

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

		hostMonitoring.addMonitor(targetMonitor);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(data).when(matsyaClientsExecutor)
				.executeSNMPTable(eq(OID_ENCLOSURE), any(), any(), any(), anyBoolean());

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(VENDOR, DELL);
		metadata.put(MODEL, MODEL_VALUE);
		metadata.put(ID_COUNT, ID_COUNT_0);
		metadata.put(TYPE, COMPUTER);
		metadata.put(CONNECTOR, MY_CONNECTOR_1_NAME);
		metadata.put(TARGET_FQDN, null);
		metadata.put(ADDITIONAL_INFORMATION1, INFORMATION1);
		metadata.put(IDENTIFYING_INFORMATION, INFORMATION1);

		final Monitor expectedEnclosure = Monitor.builder()
				.id(ENCLOSURE_ID)
				.name(ENCLOSURE_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(COMPUTER)
				.alertRules(MonitorType.ENCLOSURE.getMetaMonitor().getStaticAlertRules())
				.parameters(Map.of("Present", DiscreteParam.present()))
				.discoveryTime(strategyTime)
				.build();

		discoveryOperation.discover(connector, hostMonitoring, ECS1_01, targetMonitor);

		final Map<String, Monitor> enclosures = hostMonitoring.selectFromType(MonitorType.ENCLOSURE);

		assertEquals(expectedEnclosure, enclosures.values().stream().findFirst().get());
	}

	@Test
	void testDiscoverSameTypeMonitors() throws Exception {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString(), null);

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

		hostMonitoring.addMonitor(targetMonitor);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(data).when(matsyaClientsExecutor)
				.executeSNMPTable(eq(OID_ENCLOSURE), any(), any(), any(), anyBoolean());

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(VENDOR, DELL);
		metadata.put(MODEL, MODEL_VALUE);
		metadata.put(ID_COUNT, ID_COUNT_0);
		metadata.put(TYPE, COMPUTER);
		metadata.put(CONNECTOR, MY_CONNECTOR_1_NAME);
		metadata.put(TARGET_FQDN, null);
		metadata.put(ADDITIONAL_INFORMATION1, INFORMATION1);
		metadata.put(IDENTIFYING_INFORMATION, INFORMATION1);

		final Monitor expectedEnclosure = Monitor.builder()
				.id(ENCLOSURE_ID)
				.name(ENCLOSURE_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(COMPUTER)
				.parameters(Map.of("Present", DiscreteParam.present()))
				.alertRules(MonitorType.ENCLOSURE.getMetaMonitor().getStaticAlertRules())
				.discoveryTime(strategyTime)
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
				.oid(OID_ENCLOSURE)
				.snmpTableSelectColumns(Arrays.asList("1", "2", "3", "4", "5"))
				.key(ENCLOSURE_SOURCE_KEY)
				.computes(Collections.singletonList(LeftConcat.builder().column(1).string(EMPTY).build()))
				.build();
		final Map<String, String> parameters = Map.of(
				DEVICE_ID, INSTANCETABLE_COLUMN_1,
				DISPLAY_ID, INSTANCETABLE_COLUMN_2,
				VENDOR, DELL,
				MODEL, INSTANCETABLE_COLUMN_3,
				TYPE, COMPUTER,
				ADDITIONAL_INFORMATION1, INFORMATION1);
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
	void testValidateHardwareMonitorFieldsNoParameters() {

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

		discovery.setParameters(null);

		assertTrue(discoveryOperation.validateHardwareMonitorFields(hardwareMonitor, MY_CONNECTOR_1_NAME, ECS1_01));

		discovery.setParameters(Collections.emptyMap());

		assertTrue(discoveryOperation.validateHardwareMonitorFields(hardwareMonitor, MY_CONNECTOR_1_NAME, ECS1_01));
	}

	@Test
	void testValidateHardwareMonitorFieldsNullInstanceTable() {

		final HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(Discovery.builder().instanceTable(null).build()).build();

		assertTrue(discoveryOperation.validateHardwareMonitorFields(hardwareMonitor, MY_CONNECTOR_1_NAME, ECS1_01));
	}

	@Test
	void testValidateHardwareMonitorFieldsNullDiscovery() {

		final HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(null)
				.build();

		assertFalse(discoveryOperation.validateHardwareMonitorFields(hardwareMonitor, MY_CONNECTOR_1_NAME, ECS1_01));
	}

	@Test
	void testValidateHardwareMonitorFieldsNullType() {

		final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().discovery(null).build();

		assertFalse(discoveryOperation.validateHardwareMonitorFields(hardwareMonitor, MY_CONNECTOR_1_NAME, ECS1_01));
	}

	@Test
	void testCreateSameTypeMonitorsNoSourceKey() {

		final IHostMonitoring hostMonitoring = HostMonitoringFactory
			.getInstance()
			.createHostMonitoring(UUID.randomUUID().toString(), null);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		final InstanceTable instanceTable = SourceInstanceTable.builder().sourceKey(null).build();
		final Map<String, String> parameters = Map.of(
				DEVICE_ID, INSTANCETABLE_COLUMN_1,
				DISPLAY_ID, INSTANCETABLE_COLUMN_2,
				VENDOR, DELL,
				MODEL, INSTANCETABLE_COLUMN_3,
				TYPE, COMPUTER);

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();

		discoveryOperation.createSameTypeMonitors(MY_CONNECTOR_1_NAME, hostMonitoring, instanceTable, parameters,
			targetMonitor , MonitorType.ENCLOSURE, ECS1_01);

		assertNull(hostMonitoring.selectFromType(MonitorType.ENCLOSURE));
	}

	@Test
	void testCreateSameTypeMonitorsNoSources() {

		final IHostMonitoring hostMonitoring = HostMonitoringFactory
			.getInstance()
			.createHostMonitoring(UUID.randomUUID().toString(), null);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		final InstanceTable instanceTable = SourceInstanceTable.builder().sourceKey(ENCLOSURE_SOURCE_KEY).build();
		final Map<String, String> parameters = Map.of(
				DEVICE_ID, INSTANCETABLE_COLUMN_1,
				DISPLAY_ID, INSTANCETABLE_COLUMN_2,
				VENDOR, DELL,
				MODEL, INSTANCETABLE_COLUMN_3,
				TYPE, COMPUTER);

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();

		discoveryOperation.createSameTypeMonitors(MY_CONNECTOR_1_NAME, hostMonitoring, instanceTable, parameters,
			targetMonitor , MonitorType.ENCLOSURE, ECS1_01);

		assertNull(hostMonitoring.selectFromType(MonitorType.ENCLOSURE));
	}

	@Test
	void testCreateSameTypeMonitorsSourceTextTable() {

		final IHostMonitoring hostMonitoring = HostMonitoringFactory
			.getInstance()
			.createHostMonitoring(UUID.randomUUID().toString(), null);

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

		discoveryOperation.createSameTypeMonitors(MY_CONNECTOR_1_NAME, hostMonitoring, instanceTable, parameters,
			targetMonitor , MonitorType.ENCLOSURE, ECS1_01);

		final Map<String, Monitor> enclosures = hostMonitoring.selectFromType(MonitorType.ENCLOSURE);
		assertEquals(1, enclosures.size());

		Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		metadata.put(DEVICE_ID, DELL_ENCLOSURE);
		metadata.put(VENDOR, DELL);
		metadata.put(ID_COUNT, ID_COUNT_0);
		metadata.put(CONNECTOR, MY_CONNECTOR_1_NAME);
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedEnclosure = Monitor.builder()
				.id(HARD_CODED_ENCLOSURE_ID)
				.name(ENCLOSURE_DELL)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(ENCLOSURE)
				.alertRules(MonitorType.ENCLOSURE.getMetaMonitor().getStaticAlertRules())
				.parameters(Map.of("Present", DiscreteParam.present()))
				.discoveryTime(strategyTime)
				.build();

		assertEquals(expectedEnclosure, enclosures.values().stream().findFirst().get());
	}

	@Test
	void testCreateSameTypeMonitorsSourceInstanceTable() {

		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString(), null);

		final Map<String, String> parameters = Map.of(
				DEVICE_ID, INSTANCETABLE_COLUMN_1,
				DISPLAY_ID, INSTANCETABLE_COLUMN_2,
				VENDOR, DELL,
				MODEL, INSTANCETABLE_COLUMN_3,
				TYPE, COMPUTER);

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

		hostMonitoring.getConnectorNamespace(MY_CONNECTOR_1_NAME).addSourceTable(ENCLOSURE_SOURCE_KEY, sourceTable);
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
		metadata.put(ID_COUNT, ID_COUNT_0);
		metadata.put(TYPE, COMPUTER);
		metadata.put(CONNECTOR, MY_CONNECTOR_1_NAME);
		metadata.put(TARGET_FQDN, null);
		metadata.put(IDENTIFYING_INFORMATION, EMPTY);

		final Monitor expectedEnclosure = Monitor.builder()
				.id(ENCLOSURE_ID)
				.name(ENCLOSURE_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(COMPUTER)
				.parameters(Map.of("Present", DiscreteParam.present()))
				.alertRules(MonitorType.ENCLOSURE.getMetaMonitor().getStaticAlertRules())
				.discoveryTime(strategyTime)
				.build();

		assertEquals(expectedEnclosure, enclosures.values().stream().findFirst().get());
	}

	@Test
	void testSetIdentifyingInformation() {

		{
			final Monitor monitor = Monitor.builder().build();
			final Map<String, String> parameters = new HashMap<>(
					Map.of(
							ADDITIONAL_INFORMATION1, INFORMATION1,
							ADDITIONAL_INFORMATION2, INFORMATION2,
							ADDITIONAL_INFORMATION3, INFORMATION3)
					);

			monitor.setMetadata(parameters);

			discoveryOperation.setIdentifyingInformation(monitor);

			final Map<String, String> metadata = monitor.getMetadata();

			assertEquals(String.format("%s - %s - %s", INFORMATION1, INFORMATION2, INFORMATION3), 
					metadata.get(IDENTIFYING_INFORMATION));
		}

		{
			final Monitor monitor = Monitor.builder().build();
			final Map<String, String> parameters = new HashMap<>(
					Map.of(
							ADDITIONAL_INFORMATION1, INFORMATION1,
							ADDITIONAL_INFORMATION3, INFORMATION3)
					);

			monitor.setMetadata(parameters);

			discoveryOperation.setIdentifyingInformation(monitor);

			final Map<String, String> metadata = monitor.getMetadata();

			assertEquals(String.format("%s - %s", INFORMATION1, INFORMATION3), 
					metadata.get(IDENTIFYING_INFORMATION));
		}

		{
			// No additionalInformation metadata 
			final Monitor monitor = Monitor.builder().build();

			discoveryOperation.setIdentifyingInformation(monitor);

			final Map<String, String> metadata = monitor.getMetadata();

			assertEquals("", metadata.get(IDENTIFYING_INFORMATION));
		}
	}

	@Test
	void testProcessTextParameters() {

		final Monitor monitor = Monitor.builder().build();
		final Map<String, String> parameters = Map.of(
				DEVICE_ID, DELL_ENCLOSURE,
				VENDOR, DELL);

		discoveryOperation.processTextParameters(parameters, monitor, MY_CONNECTOR_1_NAME);

		final Map<String, String> metadata = monitor.getMetadata();

		assertEquals(DELL_ENCLOSURE, metadata.get(DEVICE_ID_PASCAL));
		assertEquals(DELL, metadata.get(VENDOR_PASCAL));
		assertEquals(MY_CONNECTOR_1_NAME, metadata.get(CONNECTOR));
		assertEquals(ID_COUNT_0, metadata.get(ID_COUNT));
	}

	@Test
	void testProcessSourceTableParameters() {

		final Map<String, String> parameters = Map.of(
				DEVICE_ID, INSTANCETABLE_COLUMN_1,
				DISPLAY_ID, INSTANCETABLE_COLUMN_2,
				VENDOR, DELL,
				MODEL, INSTANCETABLE_COLUMN_3,
				SERIAL_NUMBER, INSTANCETABLE_COLUMN_4,
				OUT_OF_RANGE, INSTANCETABLE_COLUMN_5);
		final List<String> row = Arrays.asList(ID, POWER_EDGE_54DSF, MODEL_VALUE, null);
		final Monitor monitor = Monitor.builder().build();
		discoveryOperation.processSourceTableMetadata(MY_CONNECTOR_1_NAME, parameters, ENCLOSURE_SOURCE_KEY, row , monitor , 0);

		final Map<String, String> metadata = monitor.getMetadata();

		assertEquals(ID, metadata.get(DEVICE_ID_PASCAL));
		assertEquals(POWER_EDGE_54DSF, metadata.get(DISPLAY_ID_PASCAL));
		assertEquals(DELL, metadata.get(VENDOR_PASCAL));
		assertEquals(MODEL_VALUE, metadata.get(MODEL_PASCAL));
		assertEquals(ID_COUNT_0, metadata.get(ID_COUNT));
		assertFalse(metadata.containsKey(OUT_OF_RANGE));
		assertFalse(metadata.containsKey(SERIAL_NUMBER));
	}


	@Test
	void testProcessSourcesAndComputes() throws Exception{

		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring(UUID.randomUUID().toString(), null);

		discoveryOperation.processSourcesAndComputes(Collections.emptyList(), hostMonitoring, connector, MonitorType.ENCLOSURE, ECS1_01);
		assertTrue(hostMonitoring.getConnectorNamespace(MY_CONNECTOR_1_NAME).getSourceTables().isEmpty());

		discoveryOperation.processSourcesAndComputes(null, hostMonitoring, connector, MonitorType.ENCLOSURE, ECS1_01);
		assertTrue(hostMonitoring.getConnectorNamespace(MY_CONNECTOR_1_NAME).getSourceTables().isEmpty());

		SNMPGetTableSource source = SNMPGetTableSource
				.builder()
				.oid(OID_ENCLOSURE)
				.snmpTableSelectColumns(Arrays.asList("1", "2"))
				.key(ENCLOSURE_SOURCE_KEY)
				.computes(Collections.singletonList(LeftConcat.builder().column(1).string(EMPTY).build()))
				.build();
		final List<List<String>> data = Arrays.asList(Arrays.asList("val1", "val2"), Arrays.asList("val3, val4"));
		final SourceTable expected = SourceTable.builder().table(data).headers(Arrays.asList("1", "2")).build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(data).when(matsyaClientsExecutor)
			.executeSNMPTable(eq(OID_ENCLOSURE), any(), any(), any(), anyBoolean());

		discoveryOperation.processSourcesAndComputes(
				Collections.singletonList(source),
				hostMonitoring,
				connector,
				MonitorType.ENCLOSURE,
				ECS1_01);
		assertEquals(expected, hostMonitoring.getConnectorNamespace(MY_CONNECTOR_1_NAME).getSourceTable(ENCLOSURE_SOURCE_KEY));

		source = SNMPGetTableSource.builder()
				.oid(OID_ENCLOSURE)
				.key(ENCLOSURE_SOURCE_KEY)
				.snmpTableSelectColumns(Arrays.asList("1", "2"))
				.build();
		source.setComputes(null);

		discoveryOperation.processSourcesAndComputes(
				Collections.singletonList(source),
				hostMonitoring,
				connector,
				MonitorType.ENCLOSURE,
				ECS1_01);
		assertEquals(expected, hostMonitoring.getConnectorNamespace(MY_CONNECTOR_1_NAME).getSourceTable(ENCLOSURE_SOURCE_KEY));

	}

	@Test
	void testHandleMissingMonitorDetection() {
		final IHostMonitoring hostMonitoring = buildHostMonitoringScenarioForMissingMonitors();
		discoveryOperation.handleMissingMonitorDetection(hostMonitoring);
		assertExpectedMissingMonitors(hostMonitoring);
	}


	@Test
	void testPost() {
		final IHostMonitoring hostMonitoring = buildHostMonitoringScenarioForMissingMonitors();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		discoveryOperation.post();
		assertExpectedMissingMonitors(hostMonitoring);
	}

	private static void assertExpectedMissingMonitors(final IHostMonitoring hostMonitoring) {
		final Monitor expectedFan1 = Monitor.builder()
				.id(FAN_ID + 1)
				.name(FAN_NAME + 1)
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.discoveryTime(strategyTime)
				.build();
		expectedFan1.setAsPresent();

		final Monitor expectedFan2 = Monitor.builder()
				.id(FAN_ID + 2)
				.name(FAN_NAME + 2)
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.discoveryTime(strategyTime)
				.build();
		expectedFan2.setAsPresent();

		final Monitor expectedFan3 = Monitor.builder()
				.id(FAN_ID + 3)
				.name(FAN_NAME + 3)
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.discoveryTime(strategyTime)
				.build();
		expectedFan3.setAsPresent();

		final Monitor expectedFan4 = Monitor.builder()
				.id(FAN_ID + 4)
				.name(FAN_NAME + 4)
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.discoveryTime(strategyTime - 3600)
				.build();

		for (Monitor expected : List.of(expectedFan1, expectedFan2, expectedFan3, expectedFan4)) {
			expected.getMonitorType().getMetaMonitor().accept(new MonitorAlertRulesVisitor(expected));
		}

		expectedFan4.setAsMissing();

		assertEquals(expectedFan1, hostMonitoring.selectFromType(FAN).get(FAN_ID + 1)); // Present
		assertEquals(expectedFan2, hostMonitoring.selectFromType(FAN).get(FAN_ID + 2)); // Present
		assertEquals(expectedFan3, hostMonitoring.selectFromType(FAN).get(FAN_ID + 3)); // Present
		Monitor actualFan4 = hostMonitoring.selectFromType(FAN).get(FAN_ID + 4);
		// First trigger timestamp is hard to determine
		expectedFan4.getAlertRules().values().stream().findFirst().orElseThrow()
				.forEach(ar -> ar.setFirstTriggerTimestamp(null));
		actualFan4.getAlertRules().values().stream().findFirst().orElseThrow()
				.forEach(ar -> {
					assertNotNull(ar.getFirstTriggerTimestamp());
					ar.setFirstTriggerTimestamp(null);
				});
		assertEquals(expectedFan4, actualFan4); // Missing
	}

	private static IHostMonitoring buildHostMonitoringScenarioForMissingMonitors() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor fan1 = Monitor.builder()
				.id(FAN_ID + 1)
				.name(FAN_NAME + 1)
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.discoveryTime(strategyTime)
				.build();

		final Monitor fan2 = Monitor.builder()
				.id(FAN_ID + 2)
				.name(FAN_NAME + 2)
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.discoveryTime(strategyTime)
				.build();

		final Monitor fan3 = Monitor.builder()
				.id(FAN_ID + 3)
				.name(FAN_NAME + 3)
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.discoveryTime(strategyTime)
				.build();

		final Monitor fan4 = Monitor.builder()
				.id(FAN_ID + 4)
				.name(FAN_NAME + 4)
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.discoveryTime(strategyTime - 3600)
				.build();


		hostMonitoring.addMonitor(fan4);
		hostMonitoring.addMonitor(fan3);
		hostMonitoring.addMonitor(fan2);
		hostMonitoring.addMonitor(fan1);

		return hostMonitoring;
	}

	@Test
	void testIsCpuSensor() {
		assertTrue(DiscoveryOperation.isCpuSensor(15.0, (String) null, "val", "cpu"));
		assertTrue(DiscoveryOperation.isCpuSensor(11.0, (String) null, "val", "proc"));
		assertFalse(DiscoveryOperation.isCpuSensor(11.0, (String) null, "val", "val2"));
		assertFalse(DiscoveryOperation.isCpuSensor(9.0, (String) null, "proc", "cpu"));
		assertFalse(DiscoveryOperation.isCpuSensor(null, (String) null, (String) null, (String) null));
		assertFalse(DiscoveryOperation.isCpuSensor(15.0, (String) null, (String) null, (String) null));
	}

	@Test
	void testHandleCpuTemperatures() {
		HostMonitoring hostMonitoring = new HostMonitoring();
		Monitor temperatureMonitor1 = Monitor.builder()
				.id("temperatureMonitorId1")
				.name("temperatureMonitorName")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(MonitorType.TEMPERATURE)
				.build();
		temperatureMonitor1.addMetadata(ADDITIONAL_INFORMATION1, "cpu");
		temperatureMonitor1.addMetadata(WARNING_THRESHOLD, "80.0");
		hostMonitoring.addMonitor(temperatureMonitor1);

		Monitor temperatureMonitor2 = Monitor.builder()
				.id("temperatureMonitorId2")
				.name("temperatureMonitorNameproc")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(MonitorType.TEMPERATURE)
				.build();
		temperatureMonitor2.addMetadata(WARNING_THRESHOLD, "70.0");
		hostMonitoring.addMonitor(temperatureMonitor2);

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();
		hostMonitoring.addMonitor(targetMonitor);

		discoveryOperation.handleCpuTemperatures(hostMonitoring);

		String cpuSensorMetadata = temperatureMonitor1.getMetadata(IS_CPU_SENSOR);
		assertEquals("true", cpuSensorMetadata);
		cpuSensorMetadata = temperatureMonitor2.getMetadata(IS_CPU_SENSOR);
		assertEquals("true", cpuSensorMetadata);
		String averageCpuTemperatureWarningMetadata = targetMonitor.getMetadata(AVERAGE_CPU_TEMPERATURE_WARNING);
		assertEquals("75.0", averageCpuTemperatureWarningMetadata);
	}
}
