package com.sentrysoftware.matrix.engine.strategy.collect;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AMBIENT_TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CPU_TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CPU_THERMAL_DISSIPATION_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HEATING_MARGIN_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IS_CPU_SENSOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.CPU;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ENCLOSURE;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.FAN;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.NETWORK_CARD;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.TARGET;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.TEMPERATURE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring.PowerMeter;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

@ExtendWith(MockitoExtension.class)
class CollectOperationTest {

	private static final String SNMP_TEST_FAILED = "SNMP Test Failed - SNMP GetNext of 1.2.3.4.5.6 on ecs1-01 was unsuccessful due to an empty result.";
	private static final String SUCCESSFUL_SNMP_GET_NEXT_MESSAGE = "Successful SNMP GetNext of 1.2.3.4.5.6 on ecs1-01. Returned Result: 1.2.3.4.5.6 ASN_OCT 4.2.3.";
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
	private static final String POWER_CONSUMPTION = "powerConsumption";
	private static final String CONNECTOR_NAME = "myConnector.connector";
	private static final String ENCLOSURE_NAME = "enclosure";
	private static final String ENCLOSURE_ID = "myConnecctor1.connector_enclosure_ecs1-01_1.1";
	private static final String TARGET_ID = "targetId";
	private static final String VALUE_TABLE = "Enclosure.Collect.Source(1)";
	private static final String DEVICE_ID = "deviceId";
	private static final ParameterState UNKNOWN_STATUS_WARN = ParameterState.WARN;
	private static final String OID1 = "1.2.3.4.5";
	private static final String CRITERION_OID = "1.2.3.4.5.6";
	private static final String FAN_ID_1 = "myConnecctor1.connector_fan_ecs1-01_1.1";
	private static final String FAN_ID_2 = "myOtherConnecctor.connector_fan_ecs1-01_1.2";
	private static final String ENCLOSURE_BIS_ID = "myConnecctor1.connector_enclosure_ecs1-01_1.2";
	private static final String MY_OTHER_CONNECTOR_NAME = "myOtherConnecctor.connector";
	private static final String FAN_ID_3 = "myConnecctor1.connector_fan_ecs1-01_1.3";
	private static final String OID_MONO_INSTANCE = OID1 + ".%Enclosure.Collect.DeviceID%";
	private static final String VERSION = "4.2.3";

	@Mock
	private StrategyConfig strategyConfig;
	@Mock
	private ConnectorStore store;
	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	private static Long strategyTime = new Date().getTime();

	@InjectMocks
	private CollectOperation collectOperation;

	private static EngineConfiguration engineConfiguration;

	private static Connector connector;
	private static SNMPGetNext criterion;

	private static Map<String, String> parameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static SourceTable sourceTable;
	private static List<List<String>> table = new ArrayList<>();
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

		criterion = SNMPGetNext.builder().oid(CRITERION_OID).build();

		connector = Connector.builder()
				.compiledFilename(CONNECTOR_NAME)
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
		metadata.put(HardwareConstants.CONNECTOR, CONNECTOR_NAME);

		table.add(row);

		sourceTable = SourceTable.builder().table(table).build();

		connector.setHardwareMonitors(Collections.singletonList(buildHardwareEnclosureMonitor(CollectType.MULTI_INSTANCE, OID1)));

	}

	@BeforeEach
	void beforeEeach() {
		collectOperation.setStrategyTime(strategyTime);
		lenient().doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
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
			assertTrue(hostMonitoring.getConnectorNamespace(CONNECTOR_NAME).getSourceTables().isEmpty());
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
			enclosure.collectParameter(parameter);

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

			//doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
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

			//doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
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

		//doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(Collections.singletonMap(CONNECTOR_NAME, connector)).when(store).getConnectors();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		final String snmpResult = CRITERION_OID + " ASN_OCT " + VERSION;

		doReturn(snmpResult).when(matsyaClientsExecutor)
			.executeSNMPGetNext(eq(CRITERION_OID), any(), any(), anyBoolean());

		doReturn(table).when(matsyaClientsExecutor)
			.executeSNMPTable(eq(OID1), any(), any(), any(), anyBoolean());

		collectOperation.call();

		final Monitor expected = buildExpectedEnclosure();
		final Monitor actual = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		assertEquals(expected, actual);

		final Monitor expectedConnector = buildExpectedConnectorMonitor(true, snmpResult,
				SUCCESSFUL_SNMP_GET_NEXT_MESSAGE);
		final Monitor actualConnector = getCollectedMonitor(hostMonitoring, MonitorType.CONNECTOR, CONNECTOR_NAME);

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
	void testCollect() throws Exception {

		final Monitor connectorMonitor = buildConnectorMonitor();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor enclosure = buildEnclosure(metadata);

		hostMonitoring.addMonitor(enclosure);
		hostMonitoring.addMonitor(connectorMonitor);

		//doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final String snmpResult = CRITERION_OID + " ASN_OCT " + VERSION;

		doReturn(snmpResult).when(matsyaClientsExecutor)
			.executeSNMPGetNext(eq(CRITERION_OID), any(), any(), anyBoolean());

		doReturn(table).when(matsyaClientsExecutor)
			.executeSNMPTable(eq(OID1), any(), any(), any(), anyBoolean());

		collectOperation.collect(connector, connectorMonitor, hostMonitoring, ECS1_01);

		final Monitor expectedEnclosure = buildExpectedEnclosure();
		final Monitor actualEnclosure = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		assertEquals(expectedEnclosure, actualEnclosure);

		final Monitor expectedConnector = buildExpectedConnectorMonitor(true, snmpResult,
				SUCCESSFUL_SNMP_GET_NEXT_MESSAGE);
		final Monitor actualConnector = getCollectedMonitor(hostMonitoring, MonitorType.CONNECTOR, CONNECTOR_NAME);

		assertEquals(expectedConnector, actualConnector);

	}

	private static Monitor buildConnectorMonitor() {
		return Monitor
				.builder()
				.monitorType(MonitorType.CONNECTOR)
				.name(CONNECTOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.id(CONNECTOR_NAME)
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
	void testCollectSameTypeMonitorsMultiInstance() throws Exception {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor enclosure = buildEnclosure(metadata);

		hostMonitoring.addMonitor(enclosure);

		final HardwareMonitor enclosureHardwareMonitor = buildHardwareEnclosureMonitor(CollectType.MULTI_INSTANCE, OID1);

		doReturn(table).when(matsyaClientsExecutor)
			.executeSNMPTable(eq(OID1), any(), any(), any(), anyBoolean());

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

		hostMonitoring.getConnectorNamespace(CONNECTOR_NAME).addSourceTable(VALUE_TABLE, sourceTable);

		collectOperation.processMultiInstanceValueTable(VALUE_TABLE, CONNECTOR_NAME, hostMonitoring, parameters, ENCLOSURE, ECS1_01);

		assertTrue(hostMonitoring.getMonitors().isEmpty());
	}

	@Test
	void testProcessMultiInstanceValueTableNullSourceTable() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor expectedEnclosure = buildEnclosure(metadata);
		hostMonitoring.addMonitor(expectedEnclosure);

		collectOperation.processMultiInstanceValueTable(VALUE_TABLE, CONNECTOR_NAME, hostMonitoring, parameters, ENCLOSURE, ECS1_01);

		final Monitor collectedEnclosure = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		assertEquals(expectedEnclosure, collectedEnclosure);
		assertTrue(collectedEnclosure.getParameters().isEmpty());
	}

	@Test
	void testProcessMultiInstanceValueTable() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor enclosure = buildEnclosure(metadata);
		hostMonitoring.addMonitor(enclosure);
		hostMonitoring.getConnectorNamespace(CONNECTOR_NAME).addSourceTable(VALUE_TABLE, sourceTable);

		collectOperation.processMultiInstanceValueTable(VALUE_TABLE, CONNECTOR_NAME, hostMonitoring, parameters, ENCLOSURE, ECS1_01);

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
				.snmpTableSelectColumns(Arrays.asList("1", "2", "3", "4", "5"))
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
		expected.collectParameter(statusParam);

		final IParameterValue intructionStatusParam = StatusParam
				.builder()
				.name(HardwareConstants.INTRUSION_STATUS_PARAMETER)
				.collectTime(strategyTime)
				.state(ParameterState.OK)
				.unit(HardwareConstants.INTRUSION_STATUS_PARAMETER_UNIT)
				.statusInformation("intrusionStatus: 0 (No Intrusion Detected)")
				.build();
		expected.collectParameter(intructionStatusParam);

		final IParameterValue powerConsumption = NumberParam
				.builder()
				.name(HardwareConstants.POWER_CONSUMPTION_PARAMETER)
				.collectTime(strategyTime)
				.unit(HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT)
				.value(150D)
				.rawValue(150D)
				.build();
		expected.collectParameter(powerConsumption);
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

	private static Monitor buildMonitor(final MonitorType monitorType, final String id, final String name,
										final Map<String, String> metadata) {

		return Monitor.builder()
			.id(id)
			.name(name)
			.parentId(ECS1_01)
			.targetId(ECS1_01)
			.metadata(metadata)
			.monitorType(monitorType)
			.extendedType(HardwareConstants.COMPUTER)
			.build();
	}

	@Test
	void testProcessMonoInstanceValueTableSourceTableNotFound() {

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor expectedEnclosure = buildEnclosure(metadata);
			hostMonitoring.addMonitor(expectedEnclosure);

			collectOperation.processMonoInstanceValueTable(expectedEnclosure, VALUE_TABLE, CONNECTOR_NAME,
					hostMonitoring, parameters, ENCLOSURE, ECS1_01);

			final Monitor collectedEnclosure = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

			assertEquals(expectedEnclosure, collectedEnclosure);
			assertTrue(collectedEnclosure.getParameters().isEmpty());
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor expectedEnclosure = buildEnclosure(metadata);

			hostMonitoring.addMonitor(expectedEnclosure);
			hostMonitoring.getConnectorNamespace(CONNECTOR_NAME).addSourceTable(VALUE_TABLE, SourceTable.empty());

			collectOperation.processMonoInstanceValueTable(expectedEnclosure, VALUE_TABLE, CONNECTOR_NAME, hostMonitoring,
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
		hostMonitoring.getConnectorNamespace(CONNECTOR_NAME).addSourceTable(VALUE_TABLE, sourceTable);

		collectOperation.processMonoInstanceValueTable(enclosure, VALUE_TABLE, CONNECTOR_NAME, hostMonitoring,
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
	void testCollectSameTypeMonitorsMonoInstance() throws Exception {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor enclosure = buildEnclosure(metadata);

		hostMonitoring.addMonitor(enclosure);

		final HardwareMonitor enclosureHardwareMonitor = buildHardwareEnclosureMonitor(CollectType.MONO_INSTANCE, OID_MONO_INSTANCE);

		doReturn(table).when(matsyaClientsExecutor)
			.executeSNMPTable(eq(OID1 + ".1.1"), any(), any(), any(), anyBoolean());

		collectOperation.collectSameTypeMonitors(enclosureHardwareMonitor, connector, hostMonitoring, ECS1_01);

		final Monitor expected = buildExpectedEnclosure();
		final Monitor actual = getCollectedMonitor(hostMonitoring, MonitorType.ENCLOSURE, ENCLOSURE_ID);

		assertEquals(expected, actual);
	}

	@Test
	void testCollectConnectorMonitorSuccess() throws Exception {
		final Monitor actual = buildConnectorMonitor();

		final String snmpResult = CRITERION_OID + " ASN_OCT " + VERSION;

		doReturn(snmpResult).when(matsyaClientsExecutor).executeSNMPGetNext(
				any(), any(), any(), anyBoolean());
		collectOperation.collectConnectorMonitor(connector, actual, ECS1_01);

		final Monitor expected = buildExpectedConnectorMonitor(true, snmpResult,
				SUCCESSFUL_SNMP_GET_NEXT_MESSAGE);

		assertEquals(expected, actual);
	}

	@Test
	void testCollectConnectorMonitorFailed() throws Exception {
		final Monitor actual = buildConnectorMonitor();

		doReturn("").when(matsyaClientsExecutor).executeSNMPGetNext(
				any(), any(), any(), anyBoolean());

		collectOperation.collectConnectorMonitor(connector, actual, ECS1_01);

		final Monitor expected = buildExpectedConnectorMonitor(false, "", SNMP_TEST_FAILED);

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
				HardwareConstants.TEST_REPORT_PARAMETER, testReport,
				HardwareConstants.STATUS_PARAMETER, status));

		return expected;
	}

	@Test
	void testPost() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor target = Monitor.builder()
			.id("TARGET")
			.name("TARGET")
			.targetId(ECS1_01)
			.monitorType(TARGET)
			.build();

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

		final Monitor temperature = Monitor.builder()
			.id("TEMPERATURE")
			.name("TEMPERATURE")
			.targetId(ECS1_01)
			.parentId(ENCLOSURE_ID)
			.monitorType(TEMPERATURE)
			.build();
		
		final Monitor networkCard = Monitor.builder()
				.id("NETWORK_CARD")
				.name("NETWORK_CARD")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(NETWORK_CARD)
				.build();

		final Monitor enclosure = buildEnclosure(metadata);
		hostMonitoring.addMonitor(target);
		hostMonitoring.addMonitor(fan1);
		hostMonitoring.addMonitor(fan2);
		hostMonitoring.addMonitor(temperature);
		hostMonitoring.addMonitor(networkCard);
		hostMonitoring.addMonitor(enclosure);

		fan2.setParameters(Collections.emptyMap());
		temperature.collectParameter(NumberParam.builder().name(TEMPERATURE_PARAMETER).build());

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
	void testGetTargetMonitorViaPost() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		// targetMonitors is null
		assertThrows(IllegalStateException.class, () -> collectOperation.post());
		verify(strategyConfig, times(2)).getHostMonitoring();

		// targetMonitors is empty
		Map<MonitorType, Map<String, Monitor>> monitors = hostMonitoring.getMonitors();
		monitors.put(TARGET, new LinkedHashMap<>());
		assertThrows(IllegalStateException.class, () -> collectOperation.post());
		verify(strategyConfig, times(4)).getHostMonitoring();
	}

	@Test
	void testSumArrayValuesViaPost() {

		final Monitor target = Monitor.builder()
			.id("TARGET")
			.name("TARGET")
			.targetId(ECS1_01)
			.monitorType(TARGET)
			.build();

		// Null sum

		final Monitor enclosure1 = buildEnclosure(metadata);
		enclosure1.collectParameter(NumberParam.builder().name(ENERGY_PARAMETER).value(null).rawValue(null).build());

		final Monitor enclosure2 = buildMonitor(ENCLOSURE, "myConnector1.connector_enclosure_ecs1-01_1.2",
			ENCLOSURE_NAME, metadata);
		enclosure2.collectParameter(NumberParam.builder().name(ENERGY_PARAMETER).value(7200000.0).rawValue(2.0).build());

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);
		hostMonitoring.addMonitor(enclosure1);
		hostMonitoring.addMonitor(enclosure2);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));

		collectOperation.post();
		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));

		// Non-null sum

		target.setParameters(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));

		enclosure1.setParameters(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
		enclosure1.collectParameter(NumberParam.builder().name(ENERGY_PARAMETER).value(3600000.0).rawValue(1.0).build());

		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));

		collectOperation.post();
		NumberParam energyParameter = target.getParameter(ENERGY_PARAMETER, NumberParam.class);
		assertNotNull(energyParameter);
		assertEquals(10800000.0, energyParameter.getValue());
		assertEquals(3.0, energyParameter.getRawValue());
	}

	@Test
	void testAggregateTargetEnergyViaPost() {

		final Monitor target = Monitor.builder()
			.id("TARGET")
			.name("TARGET")
			.targetId(ECS1_01)
			.monitorType(TARGET)
			.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));

		// enclosureMonitors is null

		collectOperation.post();
		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));

		// enclosureMonitors is empty

		Map<MonitorType, Map<String, Monitor>> monitors = hostMonitoring.getMonitors();
		monitors.put(ENCLOSURE, new LinkedHashMap<>());

		collectOperation.post();
		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));

		// totalEnergyValues is null

		final Monitor enclosure1 = buildEnclosure(metadata);

		final Monitor enclosure2 = buildMonitor(ENCLOSURE, "myConnector1.connector_enclosure_ecs1-01_1.2",
			ENCLOSURE_NAME, metadata);

		hostMonitoring.addMonitor(enclosure1);
		hostMonitoring.addMonitor(enclosure2);

		collectOperation.post();
		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));

		// totalEnergyValues[0] is null

		enclosure1.collectParameter(NumberParam.builder().name(ENERGY_PARAMETER).value(null).rawValue(null).build());
		enclosure2.collectParameter(NumberParam.builder().name(ENERGY_PARAMETER).value(null).rawValue(null).build());

		collectOperation.post();
		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));

		// totalEnergyValues[0] is not null && totalEnergyValues[1] is null (should never happen...)

		enclosure1.collectParameter(NumberParam.builder().name(ENERGY_PARAMETER).value(1.0).rawValue(null).build());
		enclosure2.collectParameter(NumberParam.builder().name(ENERGY_PARAMETER).value(2.0).rawValue(null).build());

		collectOperation.post();
		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));
	}

	@Test
	void testComputeTargetHeatingMarginViaPost() {

		final Monitor target = Monitor.builder()
			.id("TARGET")
			.name("TARGET")
			.targetId(ECS1_01)
			.monitorType(TARGET)
			.build();

		IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		// No temperature parameter

		Map<String, String> localMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		localMetadata.clear();
		localMetadata.put(WARNING_THRESHOLD, "10.0");

		Monitor temperature1 = buildMonitor(TEMPERATURE, "myConnector1.connector_temperature_ecs1-01_1.1",
			"temperature1", localMetadata);

		hostMonitoring.addMonitor(temperature1);

		assertNull(target.getParameter(HEATING_MARGIN_PARAMETER, NumberParam.class));

		collectOperation.post();
		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));

		// OK

		Monitor temperature2 = buildMonitor(TEMPERATURE, "myConnector1.connector_temperature_ecs1-01_1.1",
			"temperature2", localMetadata);

		hostMonitoring.addMonitor(temperature2);

		temperature1.collectParameter(NumberParam.builder().name(TEMPERATURE_PARAMETER).value(1.0).rawValue(1.0).build());
		temperature2.collectParameter(NumberParam.builder().name(TEMPERATURE_PARAMETER).value(2.0).rawValue(2.0).build());

		assertNull(target.getParameter(HEATING_MARGIN_PARAMETER, NumberParam.class));

		collectOperation.post();
		NumberParam heatingMarginParameter = target.getParameter(HEATING_MARGIN_PARAMETER, NumberParam.class);
		assertNotNull(heatingMarginParameter);
		assertEquals(8.0, heatingMarginParameter.getValue());
		assertEquals(8.0, heatingMarginParameter.getRawValue());
	}

	@Test
	void testComputeTemperatureHeatingMarginViaPost() {

		final Monitor target = Monitor.builder()
			.id("TARGET")
			.name("TARGET")
			.targetId(ECS1_01)
			.monitorType(TARGET)
			.build();

		IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		// temperatureMonitors is null

		collectOperation.post();
		assertNull(target.getParameter(TEMPERATURE_PARAMETER, NumberParam.class));

		// temperatureMonitors is empty

		Map<MonitorType, Map<String, Monitor>> monitors = hostMonitoring.getMonitors();
		monitors.put(TEMPERATURE, new LinkedHashMap<>());

		collectOperation.post();
		assertNull(target.getParameter(TEMPERATURE_PARAMETER, NumberParam.class));

		// Invalid threshold

		Map<String, String> localMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		localMetadata.put(WARNING_THRESHOLD, " ");

		Monitor temperature = buildMonitor(TEMPERATURE, "myConnector1.connector_temperature_ecs1-01_1.1",
			"temperature", localMetadata);
		temperature.collectParameter(NumberParam.builder().name(TEMPERATURE_PARAMETER).value(1.0).rawValue(1.0).build());

		hostMonitoring.addMonitor(temperature);

		assertNull(target.getParameter(HEATING_MARGIN_PARAMETER, NumberParam.class));

		collectOperation.post();
		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));

		// Temperature value is null

		target.setParameters(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));

		localMetadata.clear();
		localMetadata.put(WARNING_THRESHOLD, "10.0");

		temperature = buildMonitor(TEMPERATURE, "myConnector1.connector_temperature_ecs1-01_1.1",
			"temperature1", localMetadata);
		temperature.collectParameter(NumberParam.builder().name(TEMPERATURE_PARAMETER).value(null).build());

		hostMonitoring.setMonitors(new LinkedHashMap<>());
		hostMonitoring.addMonitor(target);
		hostMonitoring.addMonitor(temperature);

		assertNull(target.getParameter(HEATING_MARGIN_PARAMETER, NumberParam.class));

		collectOperation.post();
		assertNull(target.getParameter(ENERGY_PARAMETER, NumberParam.class));
	}

	@Test
	void testProcessMonoInstanceValueTableParameterNotPresent() {
		// Enclosure doesn't define present
		assertDoesNotThrow(() -> collectOperation.processMonoInstanceValueTable(buildEnclosure(metadata),
				VALUE_TABLE, CONNECTOR_NAME, new HostMonitoring(), parameters, ENCLOSURE, ECS1_01));

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
				VALUE_TABLE, CONNECTOR_NAME, new HostMonitoring(), parameters, ENCLOSURE, ECS1_01));

		// Weird case no parameters, no present
		fan.setParameters(new HashMap<>());
		assertDoesNotThrow(() -> collectOperation.processMonoInstanceValueTable(fan,
				VALUE_TABLE, CONNECTOR_NAME, new HostMonitoring(), parameters, ENCLOSURE, ECS1_01));

		// Present parameter equals 1
		fan.setAsPresent();
		assertDoesNotThrow(() -> collectOperation.processMonoInstanceValueTable(fan,
				VALUE_TABLE, CONNECTOR_NAME, new HostMonitoring(), parameters, ENCLOSURE, ECS1_01));

		// Weird case, Present parameter but present value is null
		fan.getParameter(HardwareConstants.PRESENT_PARAMETER, PresentParam.class).setPresent(null);
		assertDoesNotThrow(() -> collectOperation.processMonoInstanceValueTable(fan,
				VALUE_TABLE, CONNECTOR_NAME, new HostMonitoring(), parameters, ENCLOSURE, ECS1_01));
	}

	@Test
	void testComputeTemperatureParameters() {
		final Monitor target = Monitor.builder()
				.id("TARGET")
				.name("TARGET")
				.targetId(ECS1_01)
				.monitorType(TARGET)
				.build();

		IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		// temperatureMonitors is null

		collectOperation.computeTargetTemperatureParameters();
		assertNull(target.getParameter(TEMPERATURE_PARAMETER, NumberParam.class));

		// temperatureMonitors is empty
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		Map<MonitorType, Map<String, Monitor>> monitors = hostMonitoring.getMonitors();
		monitors.put(TEMPERATURE, new LinkedHashMap<>());

		collectOperation.computeTargetTemperatureParameters();
		assertNull(target.getParameter(TEMPERATURE_PARAMETER, NumberParam.class));

		// No CPU sensor

		Map<String, String> localMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		localMetadata.put(IS_CPU_SENSOR, "false");

		Monitor temperature = buildMonitor(TEMPERATURE, "myConnector1.connector_temperature_ecs1-01_1.1",
				"temperature", localMetadata);
		temperature.collectParameter(NumberParam.builder().name(TEMPERATURE_PARAMETER).value(10.0).rawValue(10.0).build());

		hostMonitoring.addMonitor(target);
		hostMonitoring.addMonitor(temperature);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		assertNull(target.getParameter(AMBIENT_TEMPERATURE_PARAMETER, NumberParam.class));
		assertNull(target.getParameter(CPU_TEMPERATURE_PARAMETER, NumberParam.class));

		collectOperation.computeTargetTemperatureParameters();
		assertEquals(10.0, target.getParameter(AMBIENT_TEMPERATURE_PARAMETER, NumberParam.class).getValue());
		assertNull(target.getParameter(CPU_TEMPERATURE_PARAMETER, NumberParam.class));

		// Present CPU sensor

		target.setParameters(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));

		localMetadata.clear();
		localMetadata.put(IS_CPU_SENSOR, "true");

		temperature = buildMonitor(TEMPERATURE, "myConnector1.connector_temperature_ecs1-01_1.1",
				"temperature1", localMetadata);
		temperature.collectParameter(NumberParam.builder().name(TEMPERATURE_PARAMETER).value(10.0).rawValue(10.0).build());

		hostMonitoring.setMonitors(new LinkedHashMap<>());
		hostMonitoring.addMonitor(target);
		hostMonitoring.addMonitor(temperature);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		assertNull(target.getParameter(AMBIENT_TEMPERATURE_PARAMETER, NumberParam.class));
		assertNull(target.getParameter(CPU_TEMPERATURE_PARAMETER, NumberParam.class));

		collectOperation.computeTargetTemperatureParameters();
		assertEquals(10.0, target.getParameter(AMBIENT_TEMPERATURE_PARAMETER, NumberParam.class).getValue());
		assertEquals(10.0, target.getParameter(CPU_TEMPERATURE_PARAMETER, NumberParam.class).getValue());
	}

	@Test
	void testEstimateCpuPowerConsumption() {
		{
			// Max Power Consumption already discovered on the CPU - thermal dissipation rate is set on the target
			final Monitor target = Monitor.builder()
					.id("TARGET")
					.name("TARGET")
					.targetId(ECS1_01)
					.monitorType(TARGET)
					.build();

			CollectHelper.updateNumberParameter(
				target,
				CPU_THERMAL_DISSIPATION_RATE_PARAMETER,
				"",
				strategyTime,
				0.30,
				0.30
			);

			final Monitor cpu = Monitor.builder()
					.id("CPU")
					.name("CPU")
					.targetId(ECS1_01)
					.parentId(ENCLOSURE_ID)
					.monitorType(CPU)
					.build();

			cpu.addMetadata(POWER_CONSUMPTION, "120");

			collectOperation.estimateCpuPowerConsumption(cpu, target, strategyTime, ECS1_01);

			assertEquals(36.0, CollectHelper.getNumberParamValue(cpu, POWER_CONSUMPTION_PARAMETER));
			assertNull(CollectHelper.getNumberParamValue(cpu, ENERGY_PARAMETER));
			assertNull(CollectHelper.getNumberParamValue(cpu, ENERGY_USAGE_PARAMETER));
		}

		{
			// Max Power Consumption not discovered on the CPU - maxSpeed is discovered - thermal dissipation rate is set on the target
			final Monitor target = Monitor.builder()
					.id("TARGET")
					.name("TARGET")
					.targetId(ECS1_01)
					.monitorType(TARGET)
					.build();

			CollectHelper.updateNumberParameter(
				target,
				CPU_THERMAL_DISSIPATION_RATE_PARAMETER,
				"",
				strategyTime,
				0.30,
				0.30
			);

			final Monitor cpu = Monitor.builder()
					.id("CPU")
					.name("CPU")
					.targetId(ECS1_01)
					.parentId(ENCLOSURE_ID)
					.monitorType(CPU)
					.build();

			cpu.addMetadata(MAXIMUM_SPEED, "2200");

			collectOperation.estimateCpuPowerConsumption(cpu, target, strategyTime, ECS1_01);

			assertEquals(12.54, CollectHelper.getNumberParamValue(cpu, POWER_CONSUMPTION_PARAMETER));
			assertNull(CollectHelper.getNumberParamValue(cpu, ENERGY_PARAMETER));
			assertNull(CollectHelper.getNumberParamValue(cpu, ENERGY_USAGE_PARAMETER));
		}

		{
			// Max Power Consumption not discovered on the CPU - maxSpeed is not discovered - thermal dissipation rate is set on the target
			final Monitor target = Monitor.builder()
					.id("TARGET")
					.name("TARGET")
					.targetId(ECS1_01)
					.monitorType(TARGET)
					.build();

			CollectHelper.updateNumberParameter(
				target,
				CPU_THERMAL_DISSIPATION_RATE_PARAMETER,
				"",
				strategyTime,
				0.30,
				0.30
			);

			final Monitor cpu = Monitor.builder()
					.id("CPU")
					.name("CPU")
					.targetId(ECS1_01)
					.parentId(ENCLOSURE_ID)
					.monitorType(CPU)
					.build();

			collectOperation.estimateCpuPowerConsumption(cpu, target, strategyTime, ECS1_01);

			assertEquals(14.25, CollectHelper.getNumberParamValue(cpu, POWER_CONSUMPTION_PARAMETER));
			assertNull(CollectHelper.getNumberParamValue(cpu, ENERGY_PARAMETER));
			assertNull(CollectHelper.getNumberParamValue(cpu, ENERGY_USAGE_PARAMETER));
		}

		{
			// Max Power Consumption not discovered on the CPU - maxSpeed is not discovered - thermal dissipation rate is not set
			final Monitor target = Monitor.builder()
					.id("TARGET")
					.name("TARGET")
					.targetId(ECS1_01)
					.monitorType(TARGET)
					.build();

			final Monitor cpu = Monitor.builder()
					.id("CPU")
					.name("CPU")
					.targetId(ECS1_01)
					.parentId(ENCLOSURE_ID)
					.monitorType(CPU)
					.build();

			collectOperation.estimateCpuPowerConsumption(cpu, target, strategyTime, ECS1_01);

			assertEquals(11.88, CollectHelper.getNumberParamValue(cpu, POWER_CONSUMPTION_PARAMETER));
			assertNull(CollectHelper.getNumberParamValue(cpu, ENERGY_PARAMETER));
			assertNull(CollectHelper.getNumberParamValue(cpu, ENERGY_USAGE_PARAMETER));
		}
	}

	@Test
	void testEstimateCpuPowerConsumptionManyCollects() {
		// Max Power Consumption not discovered on the CPU - maxSpeed is discovered - thermal dissipation rate is set on the target
		final Monitor target = Monitor.builder()
				.id("TARGET")
				.name("TARGET")
				.targetId(ECS1_01)
				.monitorType(TARGET)
				.build();

		CollectHelper.updateNumberParameter(
			target,
			CPU_THERMAL_DISSIPATION_RATE_PARAMETER,
			"",
			strategyTime,
			0.30,
			0.30
		);

		final Monitor cpu = Monitor.builder()
				.id("CPU")
				.name("CPU")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(CPU)
				.build();

		cpu.addMetadata(MAXIMUM_SPEED, "2200");

		// Collect 1
		collectOperation.estimateCpuPowerConsumption(cpu, target, strategyTime, ECS1_01);

		assertEquals(12.54, CollectHelper.getNumberParamValue(cpu, POWER_CONSUMPTION_PARAMETER));
		assertNull(CollectHelper.getNumberParamValue(cpu, ENERGY_PARAMETER));
		assertNull(CollectHelper.getNumberParamValue(cpu, ENERGY_USAGE_PARAMETER));

		// Collect 2
		cpu.getParameters().values().forEach(param -> param.reset());

		collectOperation.estimateCpuPowerConsumption(cpu, target, strategyTime + 2 * 60 * 1000, ECS1_01);

		assertEquals(12.54, CollectHelper.getNumberParamValue(cpu, POWER_CONSUMPTION_PARAMETER));
		assertEquals(1504.8, CollectHelper.getNumberParamValue(cpu, ENERGY_PARAMETER));
		assertEquals(1504.8, CollectHelper.getNumberParamValue(cpu, ENERGY_USAGE_PARAMETER));

		// Collect 3
		cpu.getParameters().values().forEach(param -> param.reset());

		collectOperation.estimateCpuPowerConsumption(cpu, target, strategyTime + 4 * 60 * 1000, ECS1_01);

		assertEquals(12.54, CollectHelper.getNumberParamValue(cpu, POWER_CONSUMPTION_PARAMETER));
		assertEquals(3009.6, CollectHelper.getNumberParamValue(cpu, ENERGY_PARAMETER)); // The energy is increased correctly
		assertEquals(1504.8, CollectHelper.getNumberParamValue(cpu, ENERGY_USAGE_PARAMETER));
	}

	@Test
	void testEstimateCpusPowerConsumption() {
		final Monitor target = Monitor.builder()
				.id("TARGET")
				.name("TARGET")
				.targetId(ECS1_01)
				.monitorType(TARGET)
				.build();

		CollectHelper.updateNumberParameter(
			target,
			CPU_THERMAL_DISSIPATION_RATE_PARAMETER,
			"",
			strategyTime,
			0.30,
			0.30
		);

		final Monitor cpu1 = Monitor.builder()
				.id("CPU1")
				.name("CPU 1")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(CPU)
				.build();

		cpu1.addMetadata(MAXIMUM_SPEED, "2200");

		final Monitor cpu2 = Monitor.builder()
				.id("CPU 2")
				.name("CPU 2")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(CPU)
				.build();

		cpu2.addMetadata(MAXIMUM_SPEED, "2200");

		final Monitor cpu3 = Monitor.builder()
				.id("CPU 3")
				.name("CPU 3")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(CPU)
				.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);
		hostMonitoring.addMonitor(cpu1);
		hostMonitoring.addMissingMonitor(cpu3);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		collectOperation.estimateCpusPowerConsumption();

		hostMonitoring.selectFromType(MonitorType.CPU)
			.values()
			.stream()
			.filter(monitor -> !monitor.isMissing())
			.forEach(cpu ->
				assertEquals(12.54, CollectHelper.getNumberParamValue(cpu, POWER_CONSUMPTION_PARAMETER)));

		final Monitor cpuMissing = hostMonitoring.selectFromType(MonitorType.CPU)
				.values()
				.stream()
				.filter(Monitor::isMissing)
				.findFirst().orElse(null);
		assertNotNull(cpuMissing);
		assertNull(CollectHelper.getNumberParamValue(cpuMissing, POWER_CONSUMPTION_PARAMETER));
	}

	@Test
	void testEstimateCpusPowerConsumptionNoCpus() {
		final Monitor target = Monitor.builder()
				.id("TARGET")
				.name("TARGET")
				.targetId(ECS1_01)
				.monitorType(TARGET)
				.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		assertDoesNotThrow(() -> collectOperation.estimateCpusPowerConsumption());

	}

	@Test
	void testEstimateTargetPowerConsumptionNoData() {
		final Monitor target = Monitor.builder()
				.id("TARGET")
				.name("TARGET")
				.targetId(ECS1_01)
				.monitorType(TARGET)
				.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		collectOperation.estimateTargetPowerConsumption();

		assertNull(CollectHelper.getNumberParamValue(target, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
	}

	@Test
	void testEstimateTargetPowerConsumptionEnclosureHasPower() {
		final Monitor target = Monitor.builder()
				.id("TARGET")
				.name("TARGET")
				.targetId(ECS1_01)
				.monitorType(TARGET)
				.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);

		final Monitor enclosure = buildEnclosure(Collections.emptyMap());
		CollectHelper.updateNumberParameter(
			enclosure,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT,
			strategyTime,
			120.0,
			120.0
		);

		hostMonitoring.addMonitor(enclosure);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		collectOperation.estimateTargetPowerConsumption();

		// First collect
		assertNull(CollectHelper.getNumberParamValue(target, HardwareConstants.ENERGY_PARAMETER));
	}

	@Test
	void testEstimateTargetPowerConsumptionTargetHasEnergy() {
		final Monitor target = Monitor.builder()
				.id("TARGET")
				.name("TARGET")
				.targetId(ECS1_01)
				.monitorType(TARGET)
				.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);

		final Monitor enclosure = buildEnclosure(Collections.emptyMap());
		CollectHelper.updateNumberParameter(
			target,
			ENERGY_PARAMETER,
			ENERGY_PARAMETER_UNIT,
			strategyTime,
			3520255.0,
			3520255.0
		);

		hostMonitoring.addMonitor(enclosure);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		collectOperation.estimateTargetPowerConsumption();

		assertEquals(3520255.0, CollectHelper.getNumberParamValue(target, HardwareConstants.ENERGY_PARAMETER));
	}

	@Test
	void testEstimateTargetPowerConsumptionTargetOnlyPower() {
		final Monitor target = Monitor.builder()
				.id("TARGET")
				.name("TARGET")
				.targetId(ECS1_01)
				.monitorType(TARGET)
				.build();

		final Monitor cpu = Monitor.builder()
				.id("CPU1")
				.name("CPU 1")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(CPU)
				.build();

		CollectHelper.updateNumberParameter(
			cpu,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT,
			strategyTime,
			60.0,
			60.0
		);

		final Monitor memory = Monitor.builder()
				.id("memory1")
				.name("memory 1")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(MonitorType.MEMORY)
				.build();

		CollectHelper.updateNumberParameter(
			memory,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT,
			strategyTime,
			4.0,
			4.0
		);

		final Monitor disk = Monitor.builder()
				.id("disk_nvm_1")
				.name("nvm 1")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(MonitorType.PHYSICAL_DISK)
				.build();

		CollectHelper.updateNumberParameter(
			disk,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT,
			strategyTime,
			6.0,
			6.0
		);

		final Monitor missingDisk = Monitor.builder()
				.id("disk_nvm_2")
				.name("nvm 2")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(MonitorType.PHYSICAL_DISK)
				.build();

		final Monitor diskNoPower = Monitor.builder()
				.id("disk_noPower")
				.name("disk 3")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(MonitorType.PHYSICAL_DISK)
				.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);
		hostMonitoring.addMonitor(cpu);
		hostMonitoring.addMonitor(disk);
		hostMonitoring.addMissingMonitor(missingDisk);
		hostMonitoring.addMonitor(memory);
		hostMonitoring.addMonitor(diskNoPower);
		hostMonitoring.addMonitor(buildEnclosure(Collections.emptyMap()));

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		collectOperation.estimateTargetPowerConsumption();

		assertEquals(77.78, CollectHelper.getNumberParamValue(target, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
	}

	@Test
	void testEstimateTargetPowerConsumptionTargetFull() {
		final Monitor target = Monitor.builder()
				.id("TARGET")
				.name("TARGET")
				.targetId(ECS1_01)
				.monitorType(TARGET)
				.build();

		final Monitor cpu = Monitor.builder()
				.id("CPU1")
				.name("CPU 1")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(CPU)
				.build();

		CollectHelper.updateNumberParameter(
			cpu,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT,
			strategyTime,
			60.0,
			60.0
		);

		CollectHelper.updateNumberParameter(
			cpu,
			ENERGY_USAGE_PARAMETER,
			ENERGY_USAGE_PARAMETER_UNIT,
			strategyTime,
			7200.0,
			7200.0
		);

		CollectHelper.updateNumberParameter(
			cpu,
			ENERGY_PARAMETER,
			ENERGY_PARAMETER_UNIT,
			strategyTime,
			7250.0,
			7250.0
		);

		final Monitor memory = Monitor.builder()
				.id("memory1")
				.name("memory 1")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(MonitorType.MEMORY)
				.build();

		CollectHelper.updateNumberParameter(
			memory,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT,
			strategyTime,
			4.0,
			4.0
		);

		CollectHelper.updateNumberParameter(
			memory,
			ENERGY_USAGE_PARAMETER,
			ENERGY_USAGE_PARAMETER_UNIT,
			strategyTime,
			480.0,
			480.0
		);

		CollectHelper.updateNumberParameter(
			memory,
			ENERGY_PARAMETER,
			ENERGY_PARAMETER_UNIT,
			strategyTime,
			500.0,
			500.0
		);

		final Monitor disk = Monitor.builder()
				.id("disk_nvm_1")
				.name("nvm 1")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(MonitorType.PHYSICAL_DISK)
				.build();

		CollectHelper.updateNumberParameter(
			disk,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT,
			strategyTime,
			6.0,
			6.0
		);

		CollectHelper.updateNumberParameter(
			disk,
			ENERGY_USAGE_PARAMETER,
			ENERGY_USAGE_PARAMETER_UNIT,
			strategyTime,
			720.0,
			720.0
		);

		CollectHelper.updateNumberParameter(
			disk,
			ENERGY_PARAMETER,
			ENERGY_PARAMETER_UNIT,
			strategyTime,
			750.0,
			750.0
		);

		final Monitor missingDisk = Monitor.builder()
				.id("disk_nvm_2")
				.name("nvm 2")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(MonitorType.PHYSICAL_DISK)
				.build();

		final Monitor diskNoPower = Monitor.builder()
				.id("disk_noPower")
				.name("disk 3")
				.targetId(ECS1_01)
				.parentId(ENCLOSURE_ID)
				.monitorType(MonitorType.PHYSICAL_DISK)
				.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);
		hostMonitoring.addMonitor(cpu);
		hostMonitoring.addMonitor(disk);
		hostMonitoring.addMissingMonitor(missingDisk);
		hostMonitoring.addMonitor(memory);
		hostMonitoring.addMonitor(diskNoPower);
		hostMonitoring.addMonitor(buildEnclosure(Collections.emptyMap()));

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		collectOperation.estimateTargetPowerConsumption();

		assertEquals(77.78, CollectHelper.getNumberParamValue(target, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		assertEquals(9333.33, CollectHelper.getNumberParamValue(target, HardwareConstants.ENERGY_USAGE_PARAMETER));
		assertEquals(9444.44, CollectHelper.getNumberParamValue(target, HardwareConstants.ENERGY_PARAMETER));
		assertEquals(PowerMeter.ESTIMATED, hostMonitoring.getPowerMeter());
	}

	@Test
	void testComputeNetworkCardParameters() {
		final Monitor target = Monitor.builder()
				.id("TARGET")
				.name("TARGET")
				.targetId(ECS1_01)
				.monitorType(TARGET)
				.build();

		IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(target);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		// No network cards
		collectOperation.computeNetworkCardParameters();
		assertNull(target.getParameter(HardwareConstants.CONNECTED_PORTS_COUNT_PARAMETER, NumberParam.class));
		assertNull(target.getParameter(HardwareConstants.TOTAL_BANDWIDTH_PARAMETER, NumberParam.class));

		// Network card is empty
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		Map<MonitorType, Map<String, Monitor>> monitors = hostMonitoring.getMonitors();
		monitors.put(NETWORK_CARD, new LinkedHashMap<>());

		collectOperation.computeNetworkCardParameters();
		assertNull(target.getParameter(HardwareConstants.CONNECTED_PORTS_COUNT_PARAMETER, NumberParam.class));
		assertNull(target.getParameter(HardwareConstants.TOTAL_BANDWIDTH_PARAMETER, NumberParam.class));

		Monitor networkCard = buildMonitor(NETWORK_CARD, "myConnector1.connector_temperature_ecs1-01_1.1", "network card", null);
		networkCard.collectParameter(StatusParam
				.builder()
				.name(HardwareConstants.LINK_STATUS_PARAMETER)
				.collectTime(strategyTime)
				.state(ParameterState.OK)
				.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
				.statusInformation("status: 0 (Operable)")
				.build());
		networkCard.collectParameter(NumberParam.builder().name(HardwareConstants.LINK_SPEED_PARAMETER).value(100.0).rawValue(100.0).build());

		hostMonitoring.addMonitor(target);
		hostMonitoring.addMonitor(networkCard);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		assertNull(target.getParameter(HardwareConstants.CONNECTED_PORTS_COUNT_PARAMETER, NumberParam.class));
		assertNull(target.getParameter(HardwareConstants.TOTAL_BANDWIDTH_PARAMETER, NumberParam.class));

		collectOperation.computeNetworkCardParameters();
		assertEquals(1.0, target.getParameter(HardwareConstants.CONNECTED_PORTS_COUNT_PARAMETER, NumberParam.class).getValue());
		assertEquals(100.0, target.getParameter(HardwareConstants.TOTAL_BANDWIDTH_PARAMETER, NumberParam.class).getValue());
	}
}
