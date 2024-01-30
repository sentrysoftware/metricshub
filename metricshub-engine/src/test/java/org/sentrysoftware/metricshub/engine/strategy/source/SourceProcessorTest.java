package org.sentrysoftware.metricshub.engine.strategy.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.sentrysoftware.metricshub.engine.constants.Constants.AUTOMATIC;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ECS1_01;
import static org.sentrysoftware.metricshub.engine.constants.Constants.EMPTY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.EXPECTED_SNMP_TABLE_DATA;
import static org.sentrysoftware.metricshub.engine.constants.Constants.LOCALHOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.MY_CONNECTOR_1_NAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.OID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.PASSWORD;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_SELECTED_COLUMNS;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_SELECTED_COLUMNS_LIST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_WRONG_COLUMNS;
import static org.sentrysoftware.metricshub.engine.constants.Constants.SNMP_WRONG_COLUMNS_LIST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.TAB1_REF;
import static org.sentrysoftware.metricshub.engine.constants.Constants.URL;
import static org.sentrysoftware.metricshub.engine.constants.Constants.USERNAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.VALUE_VAL1;
import static org.sentrysoftware.metricshub.engine.constants.Constants.VALUE_VAL2;
import static org.sentrysoftware.metricshub.engine.constants.Constants.VALUE_VAL3;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WBEM_QUERY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.WMI_NAMESPACE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.exception.ClientException;
import org.sentrysoftware.metricshub.engine.common.exception.NoCredentialProvidedException;
import org.sentrysoftware.metricshub.engine.common.helpers.ResourceHelper;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IWinConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IpmiConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.OsCommandConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SshConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WbemConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.common.HttpMethod;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.CopySource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.HttpSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.OsCommandSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.StaticSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.TableJoinSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.TableUnionSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WmiSource;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandHelper;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandResult;
import org.sentrysoftware.metricshub.engine.telemetry.ConnectorNamespace;
import org.sentrysoftware.metricshub.engine.telemetry.HostProperties;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class SourceProcessorTest {

	@Mock
	private ClientsExecutor clientsExecutorMock;

	private static final String LOWERCASE_A = "a";
	private static final String LOWERCASE_B = "b";
	private static final String LOWERCASE_C = "c";
	private static final String LOWERCASE_A1 = "a1";
	private static final String LOWERCASE_B1 = "b1";
	private static final String LOWERCASE_C1 = "c1";
	private static final String LOWERCASE_B2 = "b2";
	private static final String LOWERCASE_C2 = "c2";
	private static final String UPPERCASE_B2 = "B2";
	private static final String UPPERCASE_C2 = "C2";
	private static final String PC14 = "pc14";
	private static final String LOWERCASE_T = "t";
	private static final String LOWERCASE_U = "u";
	private static final String LOWERCASE_V = "v";
	private static final String CAMELCASE_VAL1 = "VaL1";
	private static final String LOWERCASE_V1 = "v1";
	private static final String LOWERCASE_V2 = "v2";
	private static final String LOWERCASE_V3 = "v3";
	private static final String UPPERCASE_V1 = "V1";
	private static final String UPPERCASE_V2 = "V2";
	private static final String UPPERCASE_V3 = "V3";
	private static final String LOWERCASE_V10 = "v10";
	private static final String LOWERCASE_V20 = "v20";
	private static final String LOWERCASE_V30 = "v30";
	private static final String LOWERCASE_X = "x";
	private static final String LOWERCASE_Y = "y";
	private static final String LOWERCASE_Z = "z";
	private static final String TAB2_REF = "${source::monitors.cpu.discovery.sources.tab2}";
	private static final String TAB3_REF = "${source::monitors.cpu.discovery.sources.tab3}";
	private static final String VALUE_LIST = "a1;b1;c1";

	private static final String CAMELCASE_NOT_WBEM = "notWbem";
	private static final String CONNECTOR_ID = "myConnector";

	@Test
	void testProcessHttpSourceOK() {
		final HttpConfiguration httpConfiguration = HttpConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(HttpConfiguration.class, httpConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();

		doReturn(ECS1_01).when(clientsExecutorMock).executeHttp(any(), eq(true));
		final SourceTable actual = sourceProcessor.process(HttpSource.builder().url(URL).method(HttpMethod.GET).build());

		final SourceTable expected = SourceTable.builder().rawData(ECS1_01).build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessHttpSourceNoHttpConfiguration() {
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder().telemetryManager(telemetryManager).build();

		assertEquals(
			SourceTable.empty(),
			sourceProcessor.process(HttpSource.builder().url("my/url").method(HttpMethod.GET).build())
		);
	}

	@Test
	void testProcessSnmpGetSource() throws Exception {
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(HostConfiguration.builder().build())
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();

		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));
		assertEquals(SourceTable.empty(), sourceProcessor.process(new SnmpGetSource()));

		// no snmp protocol
		HostConfiguration hostConfigurationNoProtocol = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.build();
		telemetryManager.setHostConfiguration(hostConfigurationNoProtocol);
		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));

		// classic case
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		telemetryManager.setHostConfiguration(hostConfiguration);
		doReturn(ECS1_01).when(clientsExecutorMock).executeSNMPGet(any(), any(), any(), eq(true));
		final SourceTable actual = sourceProcessor.process(SnmpGetSource.builder().oid(OID).build());
		final SourceTable expected = SourceTable.builder().table(Arrays.asList(Arrays.asList(ECS1_01))).build();
		assertEquals(expected, actual);

		// test that the exception is correctly caught and still returns a result
		when(clientsExecutorMock.executeSNMPGet(any(), any(), any(), eq(true))).thenThrow(TimeoutException.class);
		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));
	}

	@Test
	void testProcessSnmpGetTableExpectedResultNotMatches() throws Exception {
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(HostConfiguration.builder().build())
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		assertEquals(
			SourceTable.empty(),
			sourceProcessor.process(SnmpTableSource.builder().oid(OID).selectColumns(SNMP_WRONG_COLUMNS).build())
		);

		// no snmp protocol
		HostConfiguration hostConfigurationNoProtocol = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.build();
		telemetryManager.setHostConfiguration(hostConfigurationNoProtocol);
		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));

		// no matches
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		telemetryManager.setHostConfiguration(hostConfiguration);
		doReturn(new ArrayList<>()).when(clientsExecutorMock).executeSNMPTable(any(), any(), any(), any(), eq(true));
		final SourceTable actual = sourceProcessor.process(
			SnmpTableSource.builder().oid(OID).selectColumns(SNMP_WRONG_COLUMNS).build()
		);
		final SourceTable expected = SourceTable
			.builder()
			.table(new ArrayList<>())
			.headers(SNMP_WRONG_COLUMNS_LIST)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSbmpGetTableExpectedResultMatches() throws Exception {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		doReturn(EXPECTED_SNMP_TABLE_DATA).when(clientsExecutorMock).executeSNMPTable(any(), any(), any(), any(), eq(true));
		final SourceTable actual = sourceProcessor.process(
			SnmpTableSource.builder().oid(OID).selectColumns(SNMP_SELECTED_COLUMNS).build()
		);
		final SourceTable expected = SourceTable
			.builder()
			.table(EXPECTED_SNMP_TABLE_DATA)
			.headers(SNMP_SELECTED_COLUMNS_LIST)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessTableJoinSource() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();

		final Map<String, SourceTable> mapSources = new HashMap<>();
		SourceTable tabl1 = SourceTable
			.builder()
			.table(
				Arrays.asList(
					Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1),
					Arrays.asList(VALUE_VAL1, VALUE_VAL2, VALUE_VAL3),
					Arrays.asList(UPPERCASE_V1, UPPERCASE_V2, UPPERCASE_V3),
					Arrays.asList(LOWERCASE_X, LOWERCASE_Y, LOWERCASE_Z)
				)
			)
			.build();
		SourceTable tabl2 = SourceTable
			.builder()
			.table(
				Arrays.asList(
					Arrays.asList(LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2),
					Arrays.asList(LOWERCASE_V1, LOWERCASE_V2, LOWERCASE_V3),
					Arrays.asList(CAMELCASE_VAL1, UPPERCASE_B2, UPPERCASE_C2),
					Arrays.asList(LOWERCASE_T, LOWERCASE_U, LOWERCASE_V)
				)
			)
			.build();
		mapSources.put(TAB1_REF, tabl1);
		mapSources.put(TAB2_REF, tabl2);
		ConnectorNamespace connectorNamespace = ConnectorNamespace.builder().sourceTables(mapSources).build();

		Map<String, ConnectorNamespace> connectorNamespaces = new HashMap<>();
		connectorNamespaces.put(MY_CONNECTOR_1_NAME, connectorNamespace);
		HostProperties hostProperties = HostProperties.builder().connectorNamespaces(connectorNamespaces).build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(hostProperties)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.connectorId(MY_CONNECTOR_1_NAME)
			.build();

		// standard
		List<List<String>> expectedJoin = Arrays.asList(
			Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1, LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2),
			Arrays.asList(VALUE_VAL1, VALUE_VAL2, VALUE_VAL3, CAMELCASE_VAL1, UPPERCASE_B2, UPPERCASE_C2),
			Arrays.asList(UPPERCASE_V1, UPPERCASE_V2, UPPERCASE_V3, CAMELCASE_VAL1, UPPERCASE_B2, UPPERCASE_C2),
			Arrays.asList(LOWERCASE_X, LOWERCASE_Y, LOWERCASE_Z, LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1)
		);
		SourceTable expectedResult = SourceTable.builder().table(expectedJoin).build();

		List<List<String>> clientReturn = Arrays.asList(
			Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1, LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2),
			Arrays.asList(VALUE_VAL1, VALUE_VAL2, VALUE_VAL3, CAMELCASE_VAL1, UPPERCASE_B2, UPPERCASE_C2),
			Arrays.asList(UPPERCASE_V1, UPPERCASE_V2, UPPERCASE_V3, CAMELCASE_VAL1, UPPERCASE_B2, UPPERCASE_C2),
			Arrays.asList(LOWERCASE_X, LOWERCASE_Y, LOWERCASE_Z, LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1)
		);
		doReturn(clientReturn)
			.when(clientsExecutorMock)
			.executeTableJoin(
				tabl1.getTable(),
				tabl2.getTable(),
				1,
				1,
				Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1),
				false,
				true
			);

		TableJoinSource tableJoinExample = TableJoinSource
			.builder()
			.keyType(CAMELCASE_NOT_WBEM)
			.leftTable(TAB1_REF)
			.rightTable(TAB2_REF)
			.leftKeyColumn(1)
			.rightKeyColumn(1)
			.defaultRightLine(VALUE_LIST)
			.build();
		assertEquals(expectedJoin, sourceProcessor.process(tableJoinExample).getTable());
		assertTrue(
			expectedJoin.size() == sourceProcessor.process(tableJoinExample).getTable().size() &&
			expectedJoin.containsAll(sourceProcessor.process(tableJoinExample).getTable()) &&
			sourceProcessor.process(tableJoinExample).getTable().containsAll(expectedJoin)
		);
		assertEquals(new ArrayList<>(), sourceProcessor.process(tableJoinExample).getHeaders());
		assertEquals(expectedResult.getHeaders(), sourceProcessor.process(tableJoinExample).getHeaders());
		assertEquals(expectedResult.getTable(), sourceProcessor.process(tableJoinExample).getTable());

		// no default right line
		expectedJoin =
			Arrays.asList(Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1, LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2));
		expectedResult = SourceTable.builder().table(expectedJoin).build();
		doReturn(expectedJoin)
			.when(clientsExecutorMock)
			.executeTableJoin(tabl1.getTable(), tabl2.getTable(), 1, 1, null, false, true);
		tableJoinExample =
			TableJoinSource
				.builder()
				.keyType(CAMELCASE_NOT_WBEM)
				.leftTable(TAB1_REF)
				.rightTable(TAB2_REF)
				.leftKeyColumn(1)
				.rightKeyColumn(1)
				.defaultRightLine(null)
				.build();
		assertEquals(expectedResult.getTable(), sourceProcessor.process(tableJoinExample).getTable());
		assertEquals(new ArrayList<>(), sourceProcessor.process(tableJoinExample).getHeaders());
		assertEquals(expectedResult.getHeaders(), sourceProcessor.process(tableJoinExample).getHeaders());
		assertEquals(expectedResult.getTable(), sourceProcessor.process(tableJoinExample).getTable());

		// no matches
		SourceTable tabl3 = SourceTable
			.builder()
			.table(
				Arrays.asList(
					Arrays.asList(LOWERCASE_A, LOWERCASE_B, LOWERCASE_C),
					Arrays.asList(LOWERCASE_V10, LOWERCASE_V20, LOWERCASE_V30)
				)
			)
			.build();
		mapSources.put(TAB3_REF, tabl3);
		connectorNamespace = ConnectorNamespace.builder().sourceTables(mapSources).build();
		expectedJoin = Arrays.asList(Arrays.asList(LOWERCASE_A, LOWERCASE_B, LOWERCASE_C));
		expectedResult = SourceTable.builder().table(expectedJoin).build();
		doReturn(expectedJoin)
			.when(clientsExecutorMock)
			.executeTableJoin(tabl1.getTable(), tabl3.getTable(), 1, 1, null, false, true);
		tableJoinExample =
			TableJoinSource
				.builder()
				.keyType(CAMELCASE_NOT_WBEM)
				.leftTable(TAB1_REF)
				.rightTable(TAB3_REF)
				.leftKeyColumn(1)
				.rightKeyColumn(1)
				.defaultRightLine(null)
				.build();
		assertEquals(expectedResult.getTable(), sourceProcessor.process(tableJoinExample).getTable());

		// wrong column key
		tabl3 =
			SourceTable
				.builder()
				.table(
					Arrays.asList(
						Arrays.asList(LOWERCASE_A, LOWERCASE_B, LOWERCASE_C),
						Arrays.asList(LOWERCASE_V10, LOWERCASE_V20, LOWERCASE_V30)
					)
				)
				.build();
		mapSources.put(TAB3_REF, tabl3);
		connectorNamespace = ConnectorNamespace.builder().sourceTables(mapSources).build();
		tableJoinExample =
			TableJoinSource
				.builder()
				.keyType(CAMELCASE_NOT_WBEM)
				.leftTable(TAB1_REF)
				.rightTable(TAB3_REF)
				.leftKeyColumn(0)
				.rightKeyColumn(1)
				.defaultRightLine(null)
				.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process(tableJoinExample));

		// null args
		tableJoinExample =
			TableJoinSource
				.builder()
				.keyType(CAMELCASE_NOT_WBEM)
				.leftTable(null)
				.rightTable(TAB3_REF)
				.leftKeyColumn(1)
				.rightKeyColumn(1)
				.defaultRightLine(null)
				.build();
		assertEquals(new ArrayList<>(), sourceProcessor.process(tableJoinExample).getTable());
		tableJoinExample =
			TableJoinSource
				.builder()
				.keyType(CAMELCASE_NOT_WBEM)
				.leftTable(TAB1_REF)
				.rightTable(null)
				.leftKeyColumn(1)
				.rightKeyColumn(1)
				.defaultRightLine(null)
				.build();
		// table not in sources
		assertEquals(new ArrayList<>(), sourceProcessor.process(tableJoinExample).getTable());
		tableJoinExample =
			TableJoinSource
				.builder()
				.keyType(CAMELCASE_NOT_WBEM)
				.leftTable(TAB1_REF)
				.rightTable("blabla")
				.leftKeyColumn(1)
				.rightKeyColumn(1)
				.defaultRightLine(null)
				.build();
		assertEquals(new ArrayList<>(), sourceProcessor.process(tableJoinExample).getTable());

		tableJoinExample =
			TableJoinSource
				.builder()
				.keyType(CAMELCASE_NOT_WBEM)
				.leftTable(TAB1_REF)
				.rightTable(TAB2_REF)
				.leftKeyColumn(1)
				.rightKeyColumn(1)
				.defaultRightLine(null)
				.build();
		assertEquals(
			Arrays.asList(Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1, LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2)),
			sourceProcessor.process(tableJoinExample).getTable()
		);
	}

	@Test
	void testProcessTableUnionSourceTest() {
		SourceTable tabl1 = SourceTable
			.builder()
			.table(
				Arrays.asList(
					Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1),
					Arrays.asList(VALUE_VAL1, VALUE_VAL2, VALUE_VAL3)
				)
			)
			.rawData(LOWERCASE_A1)
			.build();
		SourceTable tabl2 = SourceTable
			.builder()
			.table(
				Arrays.asList(
					Arrays.asList(LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2),
					Arrays.asList(LOWERCASE_V1, LOWERCASE_V2, LOWERCASE_V3)
				)
			)
			.rawData(TAB1_REF)
			.build();

		final Map<String, SourceTable> mapSources = new HashMap<>();
		mapSources.put(TAB1_REF, tabl1);
		mapSources.put(TAB2_REF, tabl2);

		// standard
		List<List<String>> expectedUnion = Arrays.asList(
			Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1),
			Arrays.asList(VALUE_VAL1, VALUE_VAL2, VALUE_VAL3),
			Arrays.asList(LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2),
			Arrays.asList(LOWERCASE_V1, LOWERCASE_V2, LOWERCASE_V3)
		);

		final SnmpConfiguration snmpConfiguration = SnmpConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		ConnectorNamespace connectorNamespace = ConnectorNamespace.builder().sourceTables(mapSources).build();

		Map<String, ConnectorNamespace> connectorNamespaces = new HashMap<>();
		connectorNamespaces.put(MY_CONNECTOR_1_NAME, connectorNamespace);
		HostProperties hostProperties = HostProperties.builder().connectorNamespaces(connectorNamespaces).build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(hostProperties)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.connectorId(MY_CONNECTOR_1_NAME)
			.build();

		TableUnionSource tableUnionExample = TableUnionSource.builder().tables(Arrays.asList()).build();
		assertEquals(new ArrayList<>(), sourceProcessor.process(tableUnionExample).getTable());

		tableUnionExample = TableUnionSource.builder().tables(Arrays.asList(TAB1_REF, TAB2_REF, TAB3_REF)).build();

		assertEquals(expectedUnion, sourceProcessor.process(tableUnionExample).getTable());
		assertEquals(LOWERCASE_A1 + "\n" + TAB1_REF, sourceProcessor.process(tableUnionExample).getRawData());
	}

	@Test
	void testProcessCopySource() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();

		final List<List<String>> expectedTable = new ArrayList<>(
			Arrays.asList(
				new ArrayList<>(Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1)),
				new ArrayList<>(Arrays.asList(VALUE_VAL1, VALUE_VAL2, VALUE_VAL3))
			)
		);

		SourceTable table = SourceTable.builder().table(expectedTable).rawData("rawData").build();

		ConnectorNamespace namespace = ConnectorNamespace.builder().sourceTables(Map.of(TAB1_REF, table)).build();
		Map<String, ConnectorNamespace> connectorNamespaces = new HashMap<>();
		connectorNamespaces.put(MY_CONNECTOR_1_NAME, namespace);
		HostProperties hostProperties = HostProperties.builder().connectorNamespaces(connectorNamespaces).build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(hostProperties)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.connectorId(MY_CONNECTOR_1_NAME)
			.build();

		CopySource copySource = null;
		assertEquals(SourceTable.empty(), sourceProcessor.process(copySource));
		copySource = new CopySource();
		assertEquals(SourceTable.empty(), sourceProcessor.process(copySource));

		copySource = CopySource.builder().from("").build();
		assertEquals(SourceTable.empty(), sourceProcessor.process(copySource));

		copySource = CopySource.builder().from(TAB1_REF).build();

		final List<List<String>> tableResult = sourceProcessor.process(copySource).getTable();

		assertEquals(expectedTable, tableResult);

		tableResult.get(0).add(VALUE_VAL3);

		assertEquals(
			Arrays.asList(
				Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1),
				Arrays.asList(VALUE_VAL1, VALUE_VAL2, VALUE_VAL3)
			),
			expectedTable
		);
	}

	@Test
	void testProcessStaticSource() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();

		StaticSource staticSource = null;
		assertEquals(SourceTable.empty(), sourceProcessor.process(staticSource));
		staticSource = new StaticSource();
		assertEquals(SourceTable.empty(), sourceProcessor.process(staticSource));

		List<List<String>> expectedTable = Arrays.asList(Arrays.asList(LOWERCASE_A1));

		staticSource = StaticSource.builder().value(LOWERCASE_A1).build();

		assertEquals(expectedTable, sourceProcessor.process(staticSource).getTable());
		assertEquals(SourceTable.empty(), sourceProcessor.process(new StaticSource()));

		expectedTable = Arrays.asList(new ArrayList<>(Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1)));

		staticSource = StaticSource.builder().value(VALUE_LIST).build();

		assertEquals(expectedTable, sourceProcessor.process(staticSource).getTable());
	}

	@Test
	void testProcessWbemSource() throws ClientException {
		final WbemConfiguration wbemConfiguration = WbemConfiguration
			.builder()
			.username(ECS1_01 + "\\" + USERNAME)
			.password(PASSWORD.toCharArray())
			.build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(ECS1_01)
					.hostId(ECS1_01)
					.hostType(DeviceKind.LINUX)
					.configurations(Map.of(WbemConfiguration.class, wbemConfiguration))
					.build()
			)
			.build();

		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.connectorId(CONNECTOR_ID)
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process((WbemSource) null));
		assertEquals(SourceTable.empty(), sourceProcessor.process(WbemSource.builder().query(EMPTY).build()));

		final WbemSource wbemSource = WbemSource.builder().query(WBEM_QUERY).build();
		telemetryManager.setHostConfiguration(HostConfiguration.builder().configurations(Collections.emptyMap()).build());

		// no wbem configuration
		assertEquals(SourceTable.empty(), sourceProcessor.process(wbemSource));

		telemetryManager.setHostConfiguration(
			HostConfiguration
				.builder()
				.configurations(Map.of(WbemConfiguration.class, WbemConfiguration.builder().build()))
				.build()
		);

		// empty configuration
		assertEquals(SourceTable.empty(), sourceProcessor.process(wbemSource));

		telemetryManager.setHostConfiguration(
			HostConfiguration
				.builder()
				.configurations(
					Map.of(
						WbemConfiguration.class,
						WbemConfiguration.builder().username(USERNAME).password(PASSWORD.toCharArray()).build()
					)
				)
				.build()
		);

		// no namespace
		assertEquals(SourceTable.empty(), sourceProcessor.process(wbemSource));

		telemetryManager.setHostConfiguration(
			HostConfiguration
				.builder()
				.configurations(
					Map.of(
						WbemConfiguration.class,
						WbemConfiguration
							.builder()
							.username(USERNAME)
							.password(PASSWORD.toCharArray())
							.namespace(WMI_NAMESPACE)
							.build()
					)
				)
				.build()
		);

		// unable to build URL : no port
		assertEquals(SourceTable.empty(), sourceProcessor.process(wbemSource));

		telemetryManager.setHostConfiguration(
			HostConfiguration
				.builder()
				.hostname(null)
				.configurations(
					Map.of(
						WbemConfiguration.class,
						WbemConfiguration
							.builder()
							.username(USERNAME)
							.password(PASSWORD.toCharArray())
							.namespace(WMI_NAMESPACE)
							.port(5989)
							.build()
					)
				)
				.build()
		);

		// unable to build URL : no hostname
		assertEquals(SourceTable.empty(), sourceProcessor.process(wbemSource));

		telemetryManager.setHostConfiguration(
			HostConfiguration
				.builder()
				.hostname(ECS1_01)
				.hostId(ECS1_01)
				.strategyTimeout(120L)
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(WbemConfiguration.class, wbemConfiguration))
				.build()
		);

		final List<List<String>> listValues = Arrays.asList(
			Arrays.asList("a1", "b2", "c2"),
			Arrays.asList("v1", "v2", "v3")
		);

		doReturn(listValues).when(clientsExecutorMock).executeWbem(any(), any(), any(), any());
		assertEquals(listValues, sourceProcessor.process(wbemSource).getTable());

		// handle exception
		doThrow(new ClientException()).when(clientsExecutorMock).executeWbem(any(), any(), any(), any());
		assertEquals(SourceTable.empty(), sourceProcessor.process(wbemSource));
	}

	@Test
	void testProcessWmiSourceMalformed() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration
			.builder()
			.community("public")
			.version(SnmpConfiguration.SnmpVersion.V1)
			.port(161)
			.timeout(120L)
			.build();
		final HttpConfiguration httpConfiguration = HttpConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(ECS1_01)
					.hostId(ECS1_01)
					.hostType(DeviceKind.LINUX)
					.configurations(
						Map.of(SnmpConfiguration.class, snmpConfiguration, HttpConfiguration.class, httpConfiguration)
					)
					.build()
			)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process((WmiSource) null));
		assertEquals(SourceTable.empty(), sourceProcessor.process(WmiSource.builder().query(WBEM_QUERY).build()));
	}

	@Test
	void testProcessWmiSourceButWmiNotConfigured() {
		final WmiSource wmiSource = WmiSource.builder().query(WBEM_QUERY).build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(ECS1_01)
					.hostId(ECS1_01)
					.hostType(DeviceKind.LINUX)
					.configurations(Collections.emptyMap())
					.build()
			)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process(wmiSource));
	}

	@Test
	void testProcessWmiSourceNoNamespace() {
		final WmiSource wmiSource = WmiSource.builder().query(WBEM_QUERY).namespace(AUTOMATIC).build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(ECS1_01)
					.hostId(ECS1_01)
					.hostType(DeviceKind.LINUX)
					.configurations(
						Map.of(
							WmiConfiguration.class,
							WmiConfiguration.builder().username(ECS1_01 + "\\" + USERNAME).password(PASSWORD.toCharArray()).build()
						)
					)
					.build()
			)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.connectorId(CONNECTOR_ID)
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process(wmiSource));
	}

	@Test
	void testProcessWmiSource() throws Exception {
		final WmiSource wmiSource = WmiSource.builder().query(WBEM_QUERY).namespace(WMI_NAMESPACE).build();
		final WmiConfiguration wmiConfiguration = WmiConfiguration
			.builder()
			.username(ECS1_01 + "\\" + USERNAME)
			.password(PASSWORD.toCharArray())
			.build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(ECS1_01)
					.hostId(ECS1_01)
					.hostType(DeviceKind.LINUX)
					.configurations(Map.of(WmiConfiguration.class, wmiConfiguration))
					.build()
			)
			.build();
		final List<List<String>> expected = Arrays.asList(
			Arrays.asList("1.1", "0|4587"),
			Arrays.asList("1.2", "2|4587"),
			Arrays.asList("1.3", "1|4587")
		);
		doReturn(expected).when(clientsExecutorMock).executeWql(ECS1_01, wmiConfiguration, WBEM_QUERY, WMI_NAMESPACE);
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.connectorId(CONNECTOR_ID)
			.build();
		assertEquals(SourceTable.builder().table(expected).build(), sourceProcessor.process(wmiSource));
	}

	@Test
	void testProcessWmiSourceTimeout() {
		final WmiSource wmiSource = WmiSource.builder().query(WBEM_QUERY).namespace(AUTOMATIC).build();
		final WmiConfiguration wmiConfiguration = WmiConfiguration
			.builder()
			.username(ECS1_01 + "\\" + USERNAME)
			.password(PASSWORD.toCharArray())
			.build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(ECS1_01)
					.hostId(ECS1_01)
					.hostType(DeviceKind.LINUX)
					.configurations(Map.of(WmiConfiguration.class, wmiConfiguration))
					.build()
			)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.connectorId(CONNECTOR_ID)
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process(wmiSource));
	}

	@Test
	void testProcessOsCommandSource() {
		final OsCommandConfiguration osCommandConfiguration = OsCommandConfiguration.builder().timeout(120L).build();

		final HostProperties hostProperties = HostProperties.builder().isLocalhost(true).build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(LOCALHOST)
					.hostId(LOCALHOST)
					.hostType(DeviceKind.LINUX)
					.configurations(Map.of(OsCommandConfiguration.class, osCommandConfiguration))
					.build()
			)
			.hostProperties(hostProperties)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process((OsCommandSource) null));
		assertEquals(SourceTable.empty(), sourceProcessor.process(new OsCommandSource()));
		assertEquals(SourceTable.empty(), sourceProcessor.process(OsCommandSource.builder().commandLine("").build()));

		final String commandLine = "/usr/sbin/ioscan -kFC ext_bus";
		final String keepOnlyRegExp = ":ext_bus:";
		final String separators = ":";
		final String selectColumns = "2-4,5,6";

		final OsCommandSource commandSource = new OsCommandSource();
		commandSource.setCommandLine(commandLine);
		commandSource.setKeep(keepOnlyRegExp);
		commandSource.setSeparators(separators);
		commandSource.setSelectColumns(selectColumns);
		commandSource.setExecuteLocally(true);

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.runOsCommand(
						commandLine,
						telemetryManager,
						commandSource.getTimeout(),
						commandSource.getExecuteLocally(),
						hostProperties.isLocalhost()
					)
				)
				.thenThrow(NoCredentialProvidedException.class);

			assertEquals(SourceTable.empty(), sourceProcessor.process(commandSource));
		}

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.runOsCommand(
						commandLine,
						telemetryManager,
						commandSource.getTimeout(),
						commandSource.getExecuteLocally(),
						hostProperties.isLocalhost()
					)
				)
				.thenThrow(IOException.class);

			assertEquals(SourceTable.empty(), sourceProcessor.process(commandSource));
		}

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			final String result = "xxxxxx\n" + "xxxxxx\n" + "0:1:ext_bus:3:4:5:6:7:8\n" + "xxxxxx\n" + "xxxxxx\n";
			final OsCommandResult commandResult = new OsCommandResult(result, commandLine);

			mockedOsCommandHelper
				.when(() ->
					OsCommandHelper.runOsCommand(
						commandLine,
						telemetryManager,
						commandSource.getTimeout(),
						commandSource.getExecuteLocally(),
						hostProperties.isLocalhost()
					)
				)
				.thenReturn(commandResult);

			final SourceTable expected = SourceTable
				.builder()
				.rawData("1;ext_bus;3;4;5")
				.table(List.of(List.of("1", "ext_bus", "3", "4", "5")))
				.build();
			assertEquals(expected, sourceProcessor.process(commandSource));
		}
	}

	@Test
	void testProcessIpmiSourceStorageHost() {
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.STORAGE)
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process(new IpmiSource()));
	}

	@Test
	void testProcessIpmiSourceOobHostNoIpmiConfig() {
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.OOB)
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process(new IpmiSource()));
	}

	@Test
	void testProcessIpmiSourceOob() throws Exception {
		final IpmiConfiguration ipmiConfiguration = IpmiConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.OOB)
			.configurations(Collections.singletonMap(IpmiConfiguration.class, ipmiConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		String ipmiResult =
			"FRU;IBM;System x3650 M2;KD9098C - 794722G\n" +
			"System Board;1;System Board 1;IBM;System x3650 M2;KD9098C - 794722G;Base board 1=Device Present";
		doReturn(ipmiResult).when(clientsExecutorMock).executeIpmiGetSensors(eq(ECS1_01), any(IpmiConfiguration.class));
		assertEquals(SourceTable.builder().rawData(ipmiResult).build(), sourceProcessor.process(new IpmiSource()));
	}

	@Test
	void testProessIpmiSourceOobNullResult() throws Exception {
		final IpmiConfiguration ipmiConfiguration = IpmiConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.OOB)
			.configurations(Collections.singletonMap(IpmiConfiguration.class, ipmiConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		doReturn(null).when(clientsExecutorMock).executeIpmiGetSensors(eq(ECS1_01), any(IpmiConfiguration.class));
		assertEquals(SourceTable.empty(), sourceProcessor.process(new IpmiSource()));
	}

	@Test
	void testVisitIpmiSourceOobException() throws Exception {
		final IpmiConfiguration ipmiConfiguration = IpmiConfiguration
			.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.OOB)
			.configurations(Collections.singletonMap(IpmiConfiguration.class, ipmiConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();

		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		doThrow(new ExecutionException(new Exception("Exception from tests")))
			.when(clientsExecutorMock)
			.executeIpmiGetSensors(eq(ECS1_01), any(IpmiConfiguration.class));
		assertEquals(SourceTable.empty(), sourceProcessor.process(new IpmiSource()));
	}

	@Test
	void testProcessWindowsIpmiSource() throws Exception {
		final IWinConfiguration wmiConfiguration = WmiConfiguration
			.builder()
			.username(PC14 + "\\" + "Administrator")
			.password("password".toCharArray())
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(PC14)
			.hostId(PC14)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Collections.singletonMap(WmiConfiguration.class, wmiConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		final List<List<String>> wmiResult1 = Arrays.asList(Arrays.asList("IdentifyingNumber", "Name", "Vendor"));
		doReturn(wmiResult1)
			.when(clientsExecutorMock)
			.executeWql(
				PC14,
				wmiConfiguration,
				"SELECT IdentifyingNumber,Name,Vendor FROM Win32_ComputerSystemProduct",
				"root/cimv2"
			);

		final List<List<String>> wmiResult2 = Arrays.asList(
			Arrays.asList("2", "20", "sensorName(sensorId):description for deviceId", "10", "15", "2", "0", "30", "25")
		);
		doReturn(wmiResult2)
			.when(clientsExecutorMock)
			.executeWql(
				PC14,
				wmiConfiguration,
				"SELECT BaseUnits,CurrentReading,Description,LowerThresholdCritical,LowerThresholdNonCritical,SensorType,UnitModifier,UpperThresholdCritical,UpperThresholdNonCritical FROM NumericSensor",
				"root/hardware"
			);

		final List<List<String>> wmiResult3 = Arrays.asList(
			Arrays.asList("state", "sensorName(sensorId):description for deviceType deviceId")
		);
		doReturn(wmiResult3)
			.when(clientsExecutorMock)
			.executeWql(PC14, wmiConfiguration, "SELECT CurrentState,Description FROM Sensor", "root/hardware");

		final List<List<String>> expected = Arrays.asList(
			Arrays.asList("FRU", "Vendor", "Name", "IdentifyingNumber"),
			Arrays.asList("Temperature", "sensorId", "sensorName", "deviceId", "20.0", "25.0", "30.0"),
			Arrays.asList("deviceType", "deviceId", "deviceType deviceId", EMPTY, EMPTY, EMPTY, "sensorName=state")
		);
		SourceTable result = sourceProcessor.process(new IpmiSource());
		assertEquals(SourceTable.builder().table(expected).build(), result);
	}

	@Test
	void testProcessWindowsIpmiSourceWmiException() throws Exception {
		final IWinConfiguration wmiConfiguration = WmiConfiguration
			.builder()
			.username(PC14 + "\\" + "Administrator")
			.password("password".toCharArray())
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(PC14)
			.hostId(PC14)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Collections.singletonMap(WmiConfiguration.class, wmiConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();
		doThrow(ClientException.class)
			.when(clientsExecutorMock)
			.executeWql(
				PC14,
				wmiConfiguration,
				"SELECT IdentifyingNumber,Name,Vendor FROM Win32_ComputerSystemProduct",
				"root/cimv2"
			);
		assertEquals(SourceTable.empty(), sourceProcessor.process(new IpmiSource()));
	}

	@Test
	void testProcessWindowsIpmiSourceWmiProtocolNull() throws Exception {
		final HttpConfiguration httpConfiguration = HttpConfiguration
			.builder()
			.username("username")
			.password("password".toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.WINDOWS)
			.configurations(Collections.singletonMap(HttpConfiguration.class, httpConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();

		assertEquals(SourceTable.empty(), sourceProcessor.process(new IpmiSource()));
	}

	@Test
	void testProcessUnixIpmiSource() {
		// classic case
		final SshConfiguration sshConfiguration = SshConfiguration
			.sshConfigurationBuilder()
			.username("root")
			.password("nationale".toCharArray())
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostname("localhost")
			.hostId("localhost")
			.hostType(DeviceKind.LINUX)
			.configurations(
				Map.of(
					HttpConfiguration.class,
					OsCommandConfiguration.builder().build(),
					SshConfiguration.class,
					sshConfiguration
				)
			)
			.build();

		final HostProperties hostProperties = HostProperties
			.builder()
			.isLocalhost(true)
			.ipmitoolCommand("ipmiCommand")
			.build();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(hostProperties)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.telemetryManager(telemetryManager)
			.clientsExecutor(clientsExecutorMock)
			.build();

		// local
		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd
				.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommandfru"), anyLong(), eq(null)))
				.thenReturn("impiResultFru");
			oscmd
				.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommand-v sdr elist all"), anyLong(), eq(null)))
				.thenReturn("impiResultSdr");
			final SourceTable ipmiResult = sourceProcessor.process(new IpmiSource());
			assertEquals(SourceTable.empty(), ipmiResult);
		}

		String fru = "/data/IpmiFruBabbage";
		String sensor = "/data/IpmiSensorBabbage";
		String expected = "/data/ipmiProcessingResult";
		String fruResult = ResourceHelper.getResourceAsString(fru, this.getClass());
		String sensorResult = ResourceHelper.getResourceAsString(sensor, this.getClass());

		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd
				.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommand" + "fru"), anyLong(), any()))
				.thenReturn(fruResult);
			oscmd
				.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommand" + "-v sdr elist all"), anyLong(), any()))
				.thenReturn(sensorResult);
			final SourceTable ipmiResult = sourceProcessor.process(new IpmiSource());
			String expectedResult = ResourceHelper.getResourceAsString(expected, this.getClass());
			List<List<String>> result = new ArrayList<>();
			Stream.of(expectedResult.split("\n")).forEach(line -> result.add(Arrays.asList(line.split(";"))));
			assertEquals(result, ipmiResult.getTable());
		}

		// remote
		hostProperties.setLocalhost(false);

		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd
				.when(() -> OsCommandHelper.runSshCommand(eq("ipmiCommand" + "fru"), any(), any(), anyLong(), any(), any()))
				.thenReturn("impiResultFru");
			oscmd
				.when(() ->
					OsCommandHelper.runSshCommand(eq("ipmiCommand" + "-v sdr elist all"), any(), any(), anyLong(), any(), any())
				)
				.thenReturn("impiResultSdr");
			final SourceTable ipmiResult = sourceProcessor.process(new IpmiSource());
			assertEquals(SourceTable.empty(), ipmiResult);
		}

		// ipmiToolCommand is empty
		hostProperties.setIpmitoolCommand("");
		SourceTable ipmiResultEmpty = sourceProcessor.process(new IpmiSource());
		assertEquals(SourceTable.empty(), ipmiResultEmpty);

		// ipmiToolCommand is null
		hostProperties.setIpmitoolCommand(null);
		ipmiResultEmpty = sourceProcessor.process(new IpmiSource());
		assertEquals(SourceTable.empty(), ipmiResultEmpty);

		// osCommandConfig is null
		hostProperties.setLocalhost(true);
		hostProperties.setIpmitoolCommand("ipmiCommand");
		ipmiResultEmpty = sourceProcessor.process(new IpmiSource());
		assertEquals(SourceTable.empty(), ipmiResultEmpty);
	}
}
