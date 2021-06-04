package com.sentrysoftware.matrix.engine.strategy.collect;

import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ENCLOSURE;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.FAN;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.TARGET;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.CollectType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.source.SourceVisitor;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

@ExtendWith(MockitoExtension.class)
class CollectOperationTest {

	private static final String POWER_CONSUMPTION_WATTS = "150";
	private static final String OK_RAW_STATUS = "OK";
	private static final String OPERABLE = "Operable";
	private static final String VALUETABLE_COLUMN_10 = "Valuetable.Column(10)";
	private static final String VALUETABLE_COLUMN_1 = "Valuetable.Column(1)";
	private static final String VALUETABLE_COLUMN_2 = "Valuetable.Column(2)";
	private static final String VALUETABLE_COLUMN_3 = "Valuetable.Column(3)";
	private static final String VALUETABLE_COLUMN_4 = "Valuetable.Column(4)";
	private static final String VALUETABLE_COLUMN_5 = "Valuetable.Column(5)";
	private static final String ENCLOSURE_DEVICE_ID = "1.1";
	private static final String COMMUNITY = "public";
	private static final String ECS1_01 = "ecs1-01";
	private static final String MY_CONNECTOR_NAME = "myConnecctor.connector";
	private static final String POWER_CONSUMPTION = "powerConsumption";
	private static final String CONNECTOR_NAME = "myConnector.connector";
	private static final String ENCLOSURE_NAME = "enclosure";
	private static final String ENCLOSURE_ID = "myConnecctor1.connector_enclosure_ecs1-01_1.1";
	private static final String TARGET_ID = "targetId";
	private static final String VALUE_TABLE = "Enclosure.Collect.Source(1)";
	private static final String DEVICE_ID = "deviceId";
	private static final ParameterState UNKNOWN_STATUS_WARN = ParameterState.WARN;
	private static final String OID1 = "1.2.3.4.5";
	private static final String FAN_ID_1 = "myConnecctor1.connector_fan_ecs1-01_1.1";
	private static final String FAN_ID_2 = "myOtherConnecctor.connector_fan_ecs1-01_1.2";
	private static final String ENCLOSURE_BIS_ID = "myConnecctor1.connector_enclosure_ecs1-01_1.2";
	private static final String MY_OTHER_CONNECTOR_NAME = "myOtherConnecctor.connector";
	private static final String FAN_ID_3 = "myConnecctor1.connector_fan_ecs1-01_1.3";
	private static final String OID_MONO_INSTANCE = OID1 + ".%Enclosure.Collect.DeviceID%";
	private static final String VERSION = "4.2.3";
	private static final String SUCCESS = "Success";
	private static final String BAD_RESULT = "1";
	private static final String FAILED = "Failed";

	@Mock
	private CriterionVisitor criterionVisitor;

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private ConnectorStore store;

	@Mock
	private SourceVisitor sourceVisitor;

	private static Long strategyTime = new Date().getTime();

	@InjectMocks
	private CollectOperation collectOperation;

	private static EngineConfiguration engineConfiguration;

	private static Connector connector;
	private static SNMPGetNext criterion;

	private static Map<String, String> parameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static SourceTable sourceTable;
	private static List<String> row = Arrays.asList(ENCLOSURE_DEVICE_ID,
			OK_RAW_STATUS,
			OPERABLE,
			OK_RAW_STATUS,
			POWER_CONSUMPTION_WATTS);

	@BeforeAll
	public static void setUp() {
		final SNMPProtocol protocol = SNMPProtocol.builder().community(COMMUNITY).version(SNMPVersion.V1).port(161)
				.timeout(120L).build();
		engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget
						.builder()
						.hostname(ECS1_01)
						.id(ECS1_01)
						.type(TargetType.LINUX)
						.build())
				.protocolConfigurations(Map.of(SNMPProtocol.class, protocol))
				.unknownStatus(UNKNOWN_STATUS_WARN)
				.build();

		criterion = SNMPGetNext.builder().oid(OID1).build();

		connector = Connector.builder()
				.compiledFilename(MY_CONNECTOR_NAME)
				.detection(Detection.builder()
						.criteria(Collections.singletonList(criterion))
						.build())
				.build();

		parameters.put(HardwareConstants.DEVICE_ID, VALUETABLE_COLUMN_1);
		parameters.put(HardwareConstants.STATUS_PARAMETER, VALUETABLE_COLUMN_2);
		parameters.put(HardwareConstants.STATUS_INFORMATION_PARAMETER, VALUETABLE_COLUMN_3);
		parameters.put(HardwareConstants.INTRUSION_STATUS_PARAMETER, VALUETABLE_COLUMN_4);
		parameters.put(HardwareConstants.POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_5);

		metadata.put(DEVICE_ID, ENCLOSURE_DEVICE_ID);
		metadata.put(HardwareConstants.CONNECTOR, MY_CONNECTOR_NAME);

		final List<List<String>> table = new ArrayList<>();
		table.add(row);

		sourceTable = SourceTable.builder().table(table).build();

		connector.setHardwareMonitors(Collections.singletonList(buildHardwareEnclosureMonitor(CollectType.MULTI_INSTANCE, OID1)));

	}

	@BeforeEach
	void beforeEeach() {
		collectOperation.setStrategyTime(strategyTime);
	}


	@Test
	void testPrepare() {

		// First collect
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor enclosure = buildEnclosure(metadata);
			hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			collectOperation.prepare();
			assertEquals(enclosure, hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID));
			assertTrue(hostMonitoring.getPreviousMonitors().isEmpty());
			assertTrue(hostMonitoring.getSourceTables().isEmpty());
		}

		// Next collect
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			final Monitor enclosure = buildEnclosure(metadata);

			final IParameterValue parameter = NumberParam.builder()
					.name(POWER_CONSUMPTION)
					.collectTime(strategyTime)
					.value(100.0)
					.rawValue(100.0)
					.build();
			enclosure.addParameter(parameter);

			hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			collectOperation.prepare();

			final Monitor result = hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID);
			assertNotNull(result);

			final NumberParam parameterAfterReset = (NumberParam) result.getParameters().get(POWER_CONSUMPTION);
			
			assertNull(parameterAfterReset.getCollectTime());
			assertEquals(strategyTime, parameterAfterReset.getPreviousCollectTime());
			assertEquals(POWER_CONSUMPTION, parameterAfterReset.getName());
			assertEquals(ParameterState.OK, parameterAfterReset.getState());
			assertNull(parameterAfterReset.getThreshold());
			assertNull(parameterAfterReset.getValue());
			assertEquals(100.0, parameterAfterReset.getPreviousRawValue());
		}
	}

	@Test
	void testCallNoConnectorMonitor() throws Exception {

		{
			IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor enclosure = buildEnclosure(metadata);

			hostMonitoring.addMonitor(enclosure);

			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			collectOperation.call();

			final Monitor actual = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

			assertEquals(enclosure, actual);
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			final Monitor connectorMonitor = buildConnectorMonitor();

			final Monitor enclosure = buildEnclosure(metadata);

			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(connectorMonitor);
			hostMonitoring.removeMonitor(connectorMonitor);

			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

			collectOperation.call();

			final Monitor actual = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

			assertEquals(enclosure, actual);


		}

	}

	@Test
	void testCall() throws Exception {

		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor connectorMonitor = buildConnectorMonitor();

		final Monitor enclosure = buildEnclosure(metadata);

		hostMonitoring.addMonitor(enclosure);
		hostMonitoring.addMonitor(connectorMonitor);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(Collections.singletonMap(MY_CONNECTOR_NAME, connector)).when(store).getConnectors();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(sourceTable).when(sourceVisitor).visit((SNMPGetTableSource) connector
				.getHardwareMonitors()
				.get(0)
				.getCollect()
				.getSources()
				.get(0));
		doReturn(CriterionTestResult.builder()
				.success(true)
				.message(SUCCESS)
				.result(VERSION)
				.build())
		.when(criterionVisitor).visit(criterion);

		collectOperation.call();

		final Monitor expected = buildExpectedEnclosure();
		final Monitor actual = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		assertEquals(expected, actual);

		final Monitor expectedConnector = buildExpectedConnectorMonitor(true, VERSION, SUCCESS);
		final Monitor actualConnector = getCollectedMonitor(hostMonitoring, MonitorType.CONNECTOR, MY_CONNECTOR_NAME);

		assertEquals(expectedConnector, actualConnector);

	}

	@Test
	void testValidateHardwareMonitors() {
		assertFalse(collectOperation.validateHardwareMonitors(Connector.builder().hardwareMonitors(null).build(), 
				ECS1_01,
				CollectOperation.NO_HW_MONITORS_FOUND_MSG));
		assertFalse(collectOperation.validateHardwareMonitors(Connector.builder().build(),
				ECS1_01,
				CollectOperation.NO_HW_MONITORS_FOUND_MSG));
		assertTrue(collectOperation.validateHardwareMonitors(Connector
				.builder()
				.hardwareMonitors(Collections.singletonList(HardwareMonitor.builder().type(ENCLOSURE).build()))
				.build(),
				ECS1_01,
				CollectOperation.NO_HW_MONITORS_FOUND_MSG));

	}

	@Test
	void testCollect() {

		final Monitor connectorMonitor = buildConnectorMonitor();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor enclosure = buildEnclosure(metadata);

		hostMonitoring.addMonitor(enclosure);
		hostMonitoring.addMonitor(connectorMonitor);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(sourceTable).when(sourceVisitor).visit((SNMPGetTableSource) connector
				.getHardwareMonitors()
				.get(0)
				.getCollect()
				.getSources()
				.get(0));
		doReturn(CriterionTestResult.builder()
				.success(true)
				.message(SUCCESS)
				.result(VERSION)
				.build())
		.when(criterionVisitor).visit(criterion);

		collectOperation.collect(connector, connectorMonitor, hostMonitoring, ECS1_01);

		final Monitor expectedEnclosure = buildExpectedEnclosure();
		final Monitor actualEnclosure = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		assertEquals(expectedEnclosure, actualEnclosure);

		final Monitor expectedConnector = buildExpectedConnectorMonitor(true, VERSION, SUCCESS);
		final Monitor actualConnector = getCollectedMonitor(hostMonitoring, MonitorType.CONNECTOR, MY_CONNECTOR_NAME);

		assertEquals(expectedConnector, actualConnector);

	}

	private static Monitor buildConnectorMonitor() {
		return Monitor
				.builder()
				.monitorType(MonitorType.CONNECTOR)
				.name(MY_CONNECTOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.id(MY_CONNECTOR_NAME)
				.build();
	}

	@Test
	void testValidateHardwareMonitorFieldsNoParameters() {

		{
			final HardwareMonitor enclosureHardwareMonitor = buildHardwareEnclosureMonitor(CollectType.MULTI_INSTANCE, OID1);

			enclosureHardwareMonitor.getCollect().setParameters(null);

			assertFalse(collectOperation.validateHardwareMonitorFields(enclosureHardwareMonitor, CONNECTOR_NAME, ECS1_01));
		}

		{
			final HardwareMonitor enclosureHardwareMonitor = buildHardwareEnclosureMonitor(CollectType.MULTI_INSTANCE, OID1);

			enclosureHardwareMonitor.getCollect().setParameters(Collections.emptyMap());

			assertFalse(collectOperation.validateHardwareMonitorFields(enclosureHardwareMonitor, CONNECTOR_NAME, ECS1_01));
		}
	}

	@Test
	void testValidateHardwareMonitorFieldsNullCollectType() {

		final HardwareMonitor enclosureHardwareMonitor = buildHardwareEnclosureMonitor(CollectType.MULTI_INSTANCE, OID1);

		enclosureHardwareMonitor.getCollect().setType(null);

		assertFalse(collectOperation.validateHardwareMonitorFields(enclosureHardwareMonitor, CONNECTOR_NAME, ECS1_01));
	}

	@Test
	void testValidateHardwareMonitorFieldsNullValueTable() {

		final HardwareMonitor enclosureHardwareMonitor = buildHardwareEnclosureMonitor(CollectType.MULTI_INSTANCE, OID1);

		enclosureHardwareMonitor.getCollect().setValueTable(null);

		assertFalse(collectOperation.validateHardwareMonitorFields(enclosureHardwareMonitor, CONNECTOR_NAME, ECS1_01));
	}

	@Test
	void testValidateHardwareMonitorFieldsNullCollect() {

		final HardwareMonitor enclosureHardwareMonitor = buildHardwareEnclosureMonitor(CollectType.MULTI_INSTANCE, OID1);

		enclosureHardwareMonitor.setCollect(null);

		assertFalse(collectOperation.validateHardwareMonitorFields(enclosureHardwareMonitor, CONNECTOR_NAME, ECS1_01));
	}

	@Test
	void testValidateHardwareMonitorFieldsNullMonitorType() {

		final HardwareMonitor enclosureHardwareMonitor = buildHardwareEnclosureMonitor(CollectType.MULTI_INSTANCE, OID1);

		enclosureHardwareMonitor.setType(null);

		assertFalse(collectOperation.validateHardwareMonitorFields(enclosureHardwareMonitor, CONNECTOR_NAME, ECS1_01));
	}

	@Test
	void testCollectSameTypeMonitorsMultiInstance() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor enclosure = buildEnclosure(metadata);

		hostMonitoring.addMonitor(enclosure);

		final HardwareMonitor enclosureHardwareMonitor = buildHardwareEnclosureMonitor(CollectType.MULTI_INSTANCE, OID1);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(sourceTable).when(sourceVisitor).visit((SNMPGetTableSource) enclosureHardwareMonitor.getCollect().getSources().get(0));

		collectOperation.collectSameTypeMonitors(enclosureHardwareMonitor, connector, hostMonitoring, ECS1_01);

		final Monitor expected = buildExpectedEnclosure();
		final Monitor actual = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		assertEquals(expected, actual);
	}

	@Test
	void testCollectSameTypeMonitorsNullSourceTable() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor enclosure = buildEnclosure(metadata);

		hostMonitoring.addMonitor(enclosure);

		final HardwareMonitor enclosureHardwareMonitor = buildHardwareEnclosureMonitor(CollectType.MULTI_INSTANCE, OID1);
		collectOperation.collectSameTypeMonitors(enclosureHardwareMonitor, connector, hostMonitoring, ECS1_01);

		final Monitor actual = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		assertEquals(enclosure, actual);
	}

	@Test
	void testProcessMultiInstanceValueTableMonitorNoFound() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		hostMonitoring.addSourceTable(VALUE_TABLE, sourceTable);

		collectOperation.processMultiInstanceValueTable(VALUE_TABLE, MY_CONNECTOR_NAME, hostMonitoring, parameters, ENCLOSURE, ECS1_01);

		assertTrue(hostMonitoring.getMonitors().isEmpty());
	}

	@Test
	void testProcessMultiInstanceValueTableNullSourceTable() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor expectedEnclosure = buildEnclosure(metadata);
		hostMonitoring.addMonitor(expectedEnclosure);

		collectOperation.processMultiInstanceValueTable(VALUE_TABLE, MY_CONNECTOR_NAME, hostMonitoring, parameters, ENCLOSURE, ECS1_01);

		final Monitor collectedEnclosure = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		assertEquals(expectedEnclosure, collectedEnclosure);
		assertTrue(collectedEnclosure.getParameters().isEmpty());
	}

	@Test
	void testProcessMultiInstanceValueTable() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor enclosure = buildEnclosure(metadata);
		hostMonitoring.addMonitor(enclosure);
		hostMonitoring.addSourceTable(VALUE_TABLE, sourceTable);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		collectOperation.processMultiInstanceValueTable(VALUE_TABLE, MY_CONNECTOR_NAME, hostMonitoring, parameters, ENCLOSURE, ECS1_01);

		final Monitor collectedEnclosure = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		final Monitor expected = buildExpectedEnclosure();

		assertEquals(expected, collectedEnclosure);
	}

	private static Monitor getCollectedMonitor(final IHostMonitoring hostMonitoring, final MonitorType monitorType, final String monitorId) {
		return hostMonitoring.selectFromType(monitorType).get(monitorId);
	}

	@Test
	void testGetMonitorNoMonitors() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Optional<Monitor> result = collectOperation.getMonitor(VALUE_TABLE,
				ENCLOSURE,
				hostMonitoring,
				row, VALUETABLE_COLUMN_1);

		assertEquals(Optional.empty(), result);
	}
	
	@Test
	void testGetMonitorCannotExtractDeviceId() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		metadata.put(DEVICE_ID, ENCLOSURE_DEVICE_ID);

		final Monitor enclosure = buildEnclosure(metadata);

		hostMonitoring.addMonitor(enclosure);

		final Optional<Monitor> result = collectOperation.getMonitor(VALUE_TABLE,
				ENCLOSURE,
				hostMonitoring,
				Arrays.asList("differentDeviceId", "100"), VALUETABLE_COLUMN_1);

		assertEquals(Optional.empty(), result);
	}

	@Test
	void testGetMonitorNoDeviceColumnTable() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		metadata.put(DEVICE_ID, ENCLOSURE_DEVICE_ID);

		final Monitor enclosure = buildEnclosure(metadata);
		hostMonitoring.addMonitor(enclosure);

		final Optional<Monitor> result = collectOperation.getMonitor(VALUE_TABLE,
				ENCLOSURE,
				hostMonitoring,
				row, null);

		assertEquals(Optional.empty(), result);
	}

	@Test
	void testGetMonitorDeviceIdDifferent() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		metadata.put(DEVICE_ID, ENCLOSURE_DEVICE_ID);

		final Monitor enclosure = buildEnclosure(metadata);
		hostMonitoring.addMonitor(enclosure);

		// With value table column out of range
		final Optional<Monitor> result = collectOperation.getMonitor(VALUE_TABLE,
				ENCLOSURE,
				hostMonitoring,
				row, VALUETABLE_COLUMN_10);

		assertEquals(Optional.empty(), result);
	}

	@Test
	void testGetMonitorNoMetadata() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = buildEnclosure(Collections.emptyMap());
		enclosure.setMetadata(null);

		hostMonitoring.addMonitor(enclosure);

		final Optional<Monitor> result = collectOperation.getMonitor(VALUE_TABLE,
				ENCLOSURE,
				hostMonitoring,
				row, VALUETABLE_COLUMN_1);

		assertEquals(Optional.empty(), result);
	}

	@Test
	void testGetMonitor() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		metadata.put(DEVICE_ID, ENCLOSURE_DEVICE_ID);

		final Monitor expectedEnclosure = buildEnclosure(metadata);
		hostMonitoring.addMonitor(expectedEnclosure);

		final Optional<Monitor> result = collectOperation.getMonitor(VALUE_TABLE,
				ENCLOSURE,
				hostMonitoring,
				row, VALUETABLE_COLUMN_1);

		assertEquals(expectedEnclosure, result.get());
	}

	private static HardwareMonitor buildHardwareEnclosureMonitor(final CollectType collectType, final String oid) {

		final SNMPGetTableSource source = SNMPGetTableSource
				.builder()
				.oid(oid)
				.key(VALUE_TABLE)
				.computes(Collections.singletonList(LeftConcat.builder().column(1).string(HardwareConstants.EMPTY).build()))
				.build();
		final Map<String, String> parameters = Map.of(
				DEVICE_ID, VALUETABLE_COLUMN_1,
				HardwareConstants.STATUS_PARAMETER, VALUETABLE_COLUMN_2, 
				HardwareConstants.STATUS_INFORMATION_PARAMETER, VALUETABLE_COLUMN_3,
				HardwareConstants.INTRUSION_STATUS_PARAMETER, VALUETABLE_COLUMN_4,
				HardwareConstants.POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_5);
		final Collect collect = Collect
				.builder()
				.valueTable(VALUE_TABLE)
				.sources(Collections.singletonList(source))
				.parameters(parameters)
				.type(collectType)
				.build();
		final HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.collect(collect)
				.build();
		return hardwareMonitor;
	}

	private static Monitor buildExpectedEnclosure() {
		final Monitor expected = buildEnclosure(metadata);

		final String statusInformation = new StringBuilder()
				.append("status: 0 (Operable)")
				.append("\n")
				.append("intrusionStatus: 0 (No Intrusion Detected)")
				.append("\n")
				.append("powerConsumption: 150.0 Watts")
				.toString();

		final IParameterValue statusParam = StatusParam
				.builder()
				.name(HardwareConstants.STATUS_PARAMETER)
				.collectTime(strategyTime)
				.state(ParameterState.OK)
				.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
				.statusInformation(statusInformation)
				.build();
		expected.addParameter(statusParam);

		final IParameterValue intructionStatusParam = StatusParam
				.builder()
				.name(HardwareConstants.INTRUSION_STATUS_PARAMETER)
				.collectTime(strategyTime)
				.state(ParameterState.OK)
				.unit(HardwareConstants.INTRUSION_STATUS_PARAMETER_UNIT)
				.statusInformation("intrusionStatus: 0 (No Intrusion Detected)")
				.build();
		expected.addParameter(intructionStatusParam);

		final IParameterValue powerConsumption = NumberParam
				.builder()
				.name(HardwareConstants.POWER_CONSUMPTION_PARAMETER)
				.collectTime(strategyTime)
				.unit(HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT)
				.value(150D)
				.rawValue(150D)
				.build();
		expected.addParameter(powerConsumption);
		return expected;
	}

	private static Monitor buildEnclosure(final Map<String, String> metadata) {
		return Monitor.builder()
				.id(ENCLOSURE_ID)
				.name(ENCLOSURE_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(HardwareConstants.COMPUTER)
				.build();
	}

	@Test
	void testProcessMonoInstanceValueTableSourceTableNotFound() {

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor expectedEnclosure = buildEnclosure(metadata);
			hostMonitoring.addMonitor(expectedEnclosure);

			collectOperation.processMonoInstanceValueTable(expectedEnclosure, VALUE_TABLE, MY_CONNECTOR_NAME,
					hostMonitoring, parameters, ENCLOSURE, ECS1_01);

			final Monitor collectedEnclosure = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

			assertEquals(expectedEnclosure, collectedEnclosure);
			assertTrue(collectedEnclosure.getParameters().isEmpty());
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor expectedEnclosure = buildEnclosure(metadata);

			hostMonitoring.addMonitor(expectedEnclosure);
			hostMonitoring.addSourceTable(VALUE_TABLE, SourceTable.empty());

			collectOperation.processMonoInstanceValueTable(expectedEnclosure, VALUE_TABLE, MY_CONNECTOR_NAME, hostMonitoring,
					parameters, ENCLOSURE, ECS1_01);

			final Monitor collectedEnclosure = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

			assertEquals(expectedEnclosure, collectedEnclosure);
			assertTrue(collectedEnclosure.getParameters().isEmpty());
		}
	}

	@Test
	void testProcessMonoInstanceValueTable() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor enclosure = buildEnclosure(metadata);
		hostMonitoring.addMonitor(enclosure);
		hostMonitoring.addSourceTable(VALUE_TABLE, sourceTable);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		collectOperation.processMonoInstanceValueTable(enclosure, VALUE_TABLE, MY_CONNECTOR_NAME, hostMonitoring,
				parameters, ENCLOSURE, ECS1_01);

		final Monitor collectedEnclosure = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		final Monitor expected = buildExpectedEnclosure();

		assertEquals(expected, collectedEnclosure);
	}

	@Test
	void testGetSameTypeSameConnectorMonitors() {

		final Monitor fan1 = Monitor
				.builder()
				.id(FAN_ID_1)
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_BIS_ID)
				.monitorType(MonitorType.FAN)
				.build();
		fan1.setMetadata(null);

		final Monitor fan2 = Monitor
				.builder()
				.id(FAN_ID_2)
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_BIS_ID)
				.monitorType(MonitorType.FAN)
				.metadata(Map.of(HardwareConstants.CONNECTOR, MY_OTHER_CONNECTOR_NAME))
				.build();

		final Monitor fan3 = Monitor
				.builder()
				.id(FAN_ID_3)
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_BIS_ID)
				.monitorType(MonitorType.FAN)
				.metadata(metadata)
				.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(fan1);
		hostMonitoring.addMonitor(fan2);
		hostMonitoring.addMonitor(fan3);

		final List<Monitor> monitors = collectOperation.getSameTypeSameConnectorMonitors(MonitorType.FAN, MY_OTHER_CONNECTOR_NAME, hostMonitoring);
		assertEquals(Collections.singletonList(fan2), monitors);
	}

	@Test
	void testCollectSameTypeMonitorsMonoInstance() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor enclosure = buildEnclosure(metadata);

		hostMonitoring.addMonitor(enclosure);

		final HardwareMonitor enclosureHardwareMonitor = buildHardwareEnclosureMonitor(CollectType.MONO_INSTANCE, OID_MONO_INSTANCE);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(sourceTable).when(sourceVisitor).visit(any(SNMPGetTableSource.class));

		collectOperation.collectSameTypeMonitors(enclosureHardwareMonitor, connector, hostMonitoring, ECS1_01);

		final Monitor expected = buildExpectedEnclosure();
		final Monitor actual = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		assertEquals(expected, actual);
	}

	@Test
	void testCollectConnectorMonitorSuccess() {
		final Monitor actual = buildConnectorMonitor();

		doReturn(CriterionTestResult.builder()
				.success(true)
				.message(SUCCESS)
				.result(VERSION)
				.build())
		.when(criterionVisitor).visit(criterion);

		collectOperation.collectConnectorMonitor(connector, actual, ECS1_01);

		final Monitor expected = buildExpectedConnectorMonitor(true, VERSION, SUCCESS);

		assertEquals(expected, actual);
	}

	@Test
	void testCollectConnectorMonitorFailed() {
		final Monitor actual = buildConnectorMonitor();

		doReturn(CriterionTestResult.builder()
				.success(false)
				.message(FAILED)
				.result(BAD_RESULT)
				.build())
		.when(criterionVisitor).visit(criterion);

		collectOperation.collectConnectorMonitor(connector, actual, ECS1_01);

		final Monitor expected = buildExpectedConnectorMonitor(false, BAD_RESULT, FAILED);

		assertEquals(expected, actual);
	}

	private static Monitor buildExpectedConnectorMonitor(final boolean success, final String result, final String message) {
		final Monitor expected = buildConnectorMonitor();

		final StatusParam status = StatusParam
				.builder()
				.collectTime(strategyTime)
				.name(HardwareConstants.STATUS_PARAMETER)
				.state(success ? ParameterState.OK : ParameterState.ALARM)
				.statusInformation(success ? "Connector test succeeded" : "Connector test failed")
				.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
				.build();

		final TextParam testReport = TextParam
				.builder()
				.collectTime(strategyTime)
				.name(HardwareConstants.TEST_REPORT_PARAMETER)
				.value("Received Result: " + result + ". " + message + "\nConclusion: TEST on ecs1-01 "
						+ (success ? "SUCCEEDED" : "FAILED"))
				.build();

		expected.setParameters(Map.of(
				HardwareConstants.STATUS_PARAMETER, status,
				HardwareConstants.TEST_REPORT_PARAMETER, testReport));

		return expected;
	}

	@Test
	void testPost() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor fan1 = Monitor.builder()
				.id("FAN1")
				.name("FAN1")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.build();

		final Monitor fan2 = Monitor.builder()
				.id("FAN2")
				.name("FAN2")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.build();

		final Monitor enclosure = buildEnclosure(metadata);
		hostMonitoring.addMonitor(fan1);
		hostMonitoring.addMonitor(fan2);
		fan2.setParameters(Collections.emptyMap());
		hostMonitoring.addMonitor(enclosure);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		collectOperation.post();

		final Monitor collectedFan1 = hostMonitoring.selectFromType(MonitorType.FAN).get("FAN1");

		assertEquals(strategyTime, 
				collectedFan1.getParameter(HardwareConstants.PRESENT_PARAMETER, PresentParam.class)
				.getCollectTime());

		final Monitor weirdFan2 = hostMonitoring.selectFromType(MonitorType.FAN).get("FAN2");

		assertNull(weirdFan2.getParameter(HardwareConstants.PRESENT_PARAMETER, PresentParam.class));

	}

	@Test
	void testProcessMonoInstanceValueTableParameterNotPresent() {
		// Enclosure doesn't define present
		assertDoesNotThrow(() -> collectOperation.processMonoInstanceValueTable(buildEnclosure(metadata),
				VALUE_TABLE, MY_CONNECTOR_NAME, new HostMonitoring(), parameters, ENCLOSURE, ECS1_01));

		final Monitor fan = Monitor.builder()
				.id("FAN")
				.name("FAN")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.build();
		fan.setAsMissing();

		// Missing is skipped, Present parameter equals 0
		assertDoesNotThrow(() -> collectOperation.processMonoInstanceValueTable(fan,
				VALUE_TABLE, MY_CONNECTOR_NAME, new HostMonitoring(), parameters, ENCLOSURE, ECS1_01));

		// Weird case no parameters, no present
		fan.setParameters(new HashMap<>());
		assertDoesNotThrow(() -> collectOperation.processMonoInstanceValueTable(fan,
				VALUE_TABLE, MY_CONNECTOR_NAME, new HostMonitoring(), parameters, ENCLOSURE, ECS1_01));

		// Present parameter equals 1
		fan.setAsPresent();
		assertDoesNotThrow(() -> collectOperation.processMonoInstanceValueTable(fan,
				VALUE_TABLE, MY_CONNECTOR_NAME, new HostMonitoring(), parameters, ENCLOSURE, ECS1_01));

		// Weird case, Present parameter but present value is null
		fan.getParameter(HardwareConstants.PRESENT_PARAMETER, PresentParam.class).setPresent(null);
		assertDoesNotThrow(() -> collectOperation.processMonoInstanceValueTable(fan,
				VALUE_TABLE, MY_CONNECTOR_NAME, new HostMonitoring(), parameters, ENCLOSURE, ECS1_01));
	}
}
