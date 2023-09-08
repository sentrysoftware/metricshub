package com.sentrysoftware.matrix.strategy.source;

import static com.sentrysoftware.matrix.constants.Constants.ECS1_01;
import static com.sentrysoftware.matrix.constants.Constants.EXPECTED_SNMP_TABLE_DATA;
import static com.sentrysoftware.matrix.constants.Constants.MY_CONNECTOR_1_NAME;
import static com.sentrysoftware.matrix.constants.Constants.OID;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_SELECTED_COLUMNS;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_SELECTED_COLUMNS_LIST;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_WRONG_COLUMNS;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_WRONG_COLUMNS_LIST;
import static com.sentrysoftware.matrix.constants.Constants.TAB1_REF;
import static com.sentrysoftware.matrix.constants.Constants.URL;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.CopySource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableUnionSource;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.ConnectorNamespace;
import com.sentrysoftware.matrix.telemetry.HostProperties;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class SourceProcessorTest {

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutorMock;

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
	private static final String LOWERCASE_T = "t";
	private static final String LOWERCASE_U = "u";
	private static final String LOWERCASE_V = "v";
	private static final String CAMELCASE_VAL1 = "VaL1";
	private static final String LOWERCASE_VAL1 = "val1";
	private static final String LOWERCASE_VAL2 = "val2";
	private static final String LOWERCASE_VAL3 = "val3";
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

	private static final String CAMELCASE_NOT_WBEM = "notWbem";

	@Test
	void testProcessHttpSourceOK() {
		final HttpConfiguration httpConfiguration = HttpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(HttpConfiguration.class, httpConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.build();

		doReturn(ECS1_01).when(matsyaClientsExecutorMock).executeHttp(any(), eq(true));
		final SourceTable actual = sourceProcessor.process(HttpSource.builder()
			.url(URL)
			.method(HttpMethod.GET)
			.build());

		final SourceTable expected = SourceTable.builder().rawData(ECS1_01).build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessHttpSourceNoHttpConfiguration() {
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder().telemetryManager(telemetryManager).build();

		assertEquals(SourceTable.empty(), sourceProcessor.process(HttpSource.builder()
			.url("my/url")
			.method(HttpMethod.GET)
			.build()));
	}

	@Test
	void testProcessSnmpGetSource() throws Exception {
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(HostConfiguration.builder().build()).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.build();

		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));
		assertEquals(SourceTable.empty(), sourceProcessor.process(new SnmpGetSource()));

		// no snmp protocol
		HostConfiguration hostConfigurationNoProtocol = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.build();
		telemetryManager.setHostConfiguration(hostConfigurationNoProtocol);
		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));

		// classic case
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		telemetryManager.setHostConfiguration(hostConfiguration);
		doReturn(ECS1_01).when(matsyaClientsExecutorMock).executeSNMPGet(any(), any(), any(), eq(true));
		final SourceTable actual = sourceProcessor.process(SnmpGetSource.builder().oid(OID).build());
		final SourceTable expected = SourceTable.builder().table(Arrays.asList(Arrays.asList(ECS1_01))).build();
		assertEquals(expected, actual);

		// test that the exception is correctly caught and still returns a result
		when(matsyaClientsExecutorMock.executeSNMPGet(any(), any(), any(), eq(true))).thenThrow(TimeoutException.class);
		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));
	}

	@Test
	void testProcessSnmpGetTableExpectedResultNotMatches() throws Exception {
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(HostConfiguration.builder().build()).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpTableSource.builder().oid(OID).selectColumns(SNMP_WRONG_COLUMNS).build()));

		// no snmp protocol
		HostConfiguration hostConfigurationNoProtocol = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.build();
		telemetryManager.setHostConfiguration(hostConfigurationNoProtocol);
		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));

		// no matches
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		telemetryManager.setHostConfiguration(hostConfiguration);
		doReturn(new ArrayList<>()).when(matsyaClientsExecutorMock).executeSNMPTable(any(), any(), any(), any(), eq(true));
		final SourceTable actual = sourceProcessor.process(SnmpTableSource.builder().oid(OID).selectColumns(SNMP_WRONG_COLUMNS).build());
		final SourceTable expected = SourceTable.builder().table(new ArrayList<>()).headers(SNMP_WRONG_COLUMNS_LIST).build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSbmpGetTableExpectedResultMatches() throws Exception {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.build();
		doReturn(EXPECTED_SNMP_TABLE_DATA).when(matsyaClientsExecutorMock).executeSNMPTable(any(), any(), any(), any(), eq(true));
		final SourceTable actual = sourceProcessor.process(SnmpTableSource.builder().oid(OID).selectColumns(SNMP_SELECTED_COLUMNS).build());
		final SourceTable expected = SourceTable.builder().table(EXPECTED_SNMP_TABLE_DATA)
			.headers(SNMP_SELECTED_COLUMNS_LIST).build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessTableJoinSource() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();

		final Map<String, SourceTable> mapSources = new HashMap<>();
		SourceTable tabl1 = SourceTable.builder().table(Arrays.asList(
			Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1),
			Arrays.asList(LOWERCASE_VAL1, LOWERCASE_VAL2, LOWERCASE_VAL3),
			Arrays.asList(UPPERCASE_V1, UPPERCASE_V2, UPPERCASE_V3),
			Arrays.asList(LOWERCASE_X, LOWERCASE_Y, LOWERCASE_Z)))
			.build();
		SourceTable tabl2 = SourceTable.builder().table(Arrays.asList(
			Arrays.asList(LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2),
			Arrays.asList(LOWERCASE_V1, LOWERCASE_V2, LOWERCASE_V3),
			Arrays.asList(CAMELCASE_VAL1, UPPERCASE_B2, UPPERCASE_C2),
			Arrays.asList(LOWERCASE_T, LOWERCASE_U, LOWERCASE_V)))
			.build();
		mapSources.put(TAB1_REF, tabl1);
		mapSources.put(TAB2_REF, tabl2);
		ConnectorNamespace connectorNamespace = ConnectorNamespace.builder().sourceTables(mapSources).build();

		Map<String,ConnectorNamespace> connectorNamespaces = new HashMap<>();
		connectorNamespaces.put(MY_CONNECTOR_1_NAME, connectorNamespace);
		HostProperties hostProperties = HostProperties.builder().connectorNamespaces(connectorNamespaces).build();

		final TelemetryManager telemetryManager = TelemetryManager.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(hostProperties)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.connectorName(MY_CONNECTOR_1_NAME)
			.build();

		// standard
		List<List<String>> expectedJoin = Arrays.asList(Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1, LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2),
			Arrays.asList(LOWERCASE_VAL1, LOWERCASE_VAL2, LOWERCASE_VAL3, CAMELCASE_VAL1, UPPERCASE_B2, UPPERCASE_C2),
			Arrays.asList(UPPERCASE_V1, UPPERCASE_V2, UPPERCASE_V3, CAMELCASE_VAL1, UPPERCASE_B2, UPPERCASE_C2),
			Arrays.asList(LOWERCASE_X, LOWERCASE_Y, LOWERCASE_Z, LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1));
		SourceTable expectedResult = SourceTable.builder().table(expectedJoin).build();

		List<List<String>> matsyaReturn = Arrays.asList(
			Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1, LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2),
			Arrays.asList(LOWERCASE_VAL1, LOWERCASE_VAL2,LOWERCASE_VAL3, CAMELCASE_VAL1, UPPERCASE_B2, UPPERCASE_C2),
			Arrays.asList(UPPERCASE_V1, UPPERCASE_V2, UPPERCASE_V3, CAMELCASE_VAL1, UPPERCASE_B2, UPPERCASE_C2),
			Arrays.asList(LOWERCASE_X, LOWERCASE_Y, LOWERCASE_Z, LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1));
		doReturn(matsyaReturn)
		.when(matsyaClientsExecutorMock)
		.executeTableJoin(tabl1.getTable(), tabl2.getTable(), 1, 1, Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1), false, true);

		TableJoinSource tableJoinExample = TableJoinSource.builder()
			.keyType(CAMELCASE_NOT_WBEM)
			.leftTable(TAB1_REF)
			.rightTable(TAB2_REF)
			.leftKeyColumn(1)
			.rightKeyColumn(1)
			.defaultRightLine("a1;b1;c1")
			.build();
		assertEquals(expectedJoin, sourceProcessor.process(tableJoinExample).getTable());
		assertTrue(expectedJoin.size() == sourceProcessor.process(tableJoinExample).getTable().size()
			&& expectedJoin.containsAll(sourceProcessor.process(tableJoinExample).getTable())
			&& sourceProcessor.process(tableJoinExample).getTable().containsAll(expectedJoin));
		assertEquals(new ArrayList<>(), sourceProcessor.process(tableJoinExample).getHeaders());
		assertEquals(expectedResult.getHeaders(), sourceProcessor.process(tableJoinExample).getHeaders());
		assertEquals(expectedResult.getTable(), sourceProcessor.process(tableJoinExample).getTable());

		// no default right line
		expectedJoin = Arrays.asList(Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1, LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2));
		expectedResult = SourceTable.builder().table(expectedJoin).build();
		doReturn(expectedJoin).when(matsyaClientsExecutorMock).executeTableJoin(tabl1.getTable(), tabl2.getTable(), 1, 1, null, false, true);
		tableJoinExample = TableJoinSource.builder()
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
		SourceTable tabl3 = SourceTable.builder()
			.table(Arrays.asList(Arrays.asList(LOWERCASE_A, LOWERCASE_B, LOWERCASE_C), Arrays.asList(LOWERCASE_V10, LOWERCASE_V20, LOWERCASE_V30)))
			.build();
		mapSources.put(TAB3_REF, tabl3);
		connectorNamespace = ConnectorNamespace.builder().sourceTables(mapSources).build();
		expectedJoin = Arrays.asList(Arrays.asList(LOWERCASE_A, LOWERCASE_B, LOWERCASE_C));
		expectedResult = SourceTable.builder().table(expectedJoin).build();
		doReturn(expectedJoin).when(matsyaClientsExecutorMock).executeTableJoin(tabl1.getTable(), tabl3.getTable(), 1, 1, null, false, true);
		tableJoinExample = TableJoinSource.builder()
			.keyType(CAMELCASE_NOT_WBEM)
			.leftTable(TAB1_REF)
			.rightTable(TAB3_REF)
			.leftKeyColumn(1)
			.rightKeyColumn(1)
			.defaultRightLine(null)
			.build();
		assertEquals(expectedResult.getTable(), sourceProcessor.process(tableJoinExample).getTable());

		// wrong column key
		tabl3 = SourceTable.builder()
			.table(Arrays.asList(Arrays.asList(LOWERCASE_A, LOWERCASE_B, LOWERCASE_C), Arrays.asList(LOWERCASE_V10, LOWERCASE_V20, LOWERCASE_V30)))
			.build();
		mapSources.put(TAB3_REF, tabl3 );
		connectorNamespace = ConnectorNamespace.builder().sourceTables(mapSources).build();
		tableJoinExample = TableJoinSource.builder()
			.keyType(CAMELCASE_NOT_WBEM)
			.leftTable(TAB1_REF)
			.rightTable(TAB3_REF)
			.leftKeyColumn(0)
			.rightKeyColumn(1)
			.defaultRightLine(null)
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process(tableJoinExample));

		// null args
		tableJoinExample = TableJoinSource.builder()
			.keyType(CAMELCASE_NOT_WBEM)
			.leftTable(null)
			.rightTable(TAB3_REF)
			.leftKeyColumn(1)
			.rightKeyColumn(1)
			.defaultRightLine(null)
			.build();
		assertEquals(new ArrayList<>(), sourceProcessor.process(tableJoinExample).getTable());
		tableJoinExample = TableJoinSource.builder()
			.keyType(CAMELCASE_NOT_WBEM)
			.leftTable(TAB1_REF)
			.rightTable(null)
			.leftKeyColumn(1)
			.rightKeyColumn(1)
			.defaultRightLine(null)
			.build();
		// table not in sources
		assertEquals(new ArrayList<>(), sourceProcessor.process(tableJoinExample).getTable());
		tableJoinExample = TableJoinSource.builder()
			.keyType(CAMELCASE_NOT_WBEM)
			.leftTable(TAB1_REF)
			.rightTable("blabla")
			.leftKeyColumn(1)
			.rightKeyColumn(1)
			.defaultRightLine(null)
			.build();
		assertEquals(new ArrayList<>(), sourceProcessor.process(tableJoinExample).getTable());

		tableJoinExample = TableJoinSource.builder()
			.keyType(CAMELCASE_NOT_WBEM)
			.leftTable(TAB1_REF)
			.rightTable(TAB2_REF)
			.leftKeyColumn(1)
			.rightKeyColumn(1)
			.defaultRightLine(null)
			.build();
		assertEquals(Arrays.asList(Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1, LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2)),
			sourceProcessor.process(tableJoinExample).getTable());
	}

	@Test
	void testProcessTableUnionSourceTest() {
		SourceTable tabl1 = SourceTable.builder()
			.table(Arrays.asList(
				Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1),
				Arrays.asList(LOWERCASE_VAL1, LOWERCASE_VAL2, LOWERCASE_VAL3)))
			.rawData(LOWERCASE_A1)
			.build();
		SourceTable tabl2 = SourceTable.builder()
			.table(Arrays.asList(
				Arrays.asList(LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2),
				Arrays.asList(LOWERCASE_V1, LOWERCASE_V2, LOWERCASE_V3)))
			.rawData(TAB1_REF)
			.build();

		final Map<String, SourceTable> mapSources = new HashMap<>();
		mapSources.put(TAB1_REF, tabl1);
		mapSources.put(TAB2_REF, tabl2);

		// standard
		List<List<String>> expectedUnion = Arrays.asList(
			Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1),
			Arrays.asList(LOWERCASE_VAL1, LOWERCASE_VAL2, LOWERCASE_VAL3),
			Arrays.asList(LOWERCASE_A1, LOWERCASE_B2, LOWERCASE_C2),
			Arrays.asList(LOWERCASE_V1, LOWERCASE_V2, LOWERCASE_V3));

		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		ConnectorNamespace connectorNamespace = ConnectorNamespace.builder().sourceTables(mapSources).build();

		Map<String,ConnectorNamespace> connectorNamespaces = new HashMap<>();
		connectorNamespaces.put(MY_CONNECTOR_1_NAME, connectorNamespace);
		HostProperties hostProperties = HostProperties.builder().connectorNamespaces(connectorNamespaces).build();

		final TelemetryManager telemetryManager = TelemetryManager.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(hostProperties)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.connectorName(MY_CONNECTOR_1_NAME)
			.build();

		TableUnionSource tableUnionExample = TableUnionSource.builder().tables(Arrays.asList()).build();
		assertEquals(new ArrayList<>(), sourceProcessor.process(tableUnionExample).getTable());

		tableUnionExample = TableUnionSource.builder()
			.tables(Arrays.asList(TAB1_REF, TAB2_REF, TAB3_REF))
			.build();

		assertEquals(expectedUnion, sourceProcessor.process(tableUnionExample).getTable());
		assertEquals(LOWERCASE_A1 +"\n"+ TAB1_REF, sourceProcessor.process(tableUnionExample).getRawData());

	}

	@Test
	void testProcessCopySource() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();

		final List<List<String>> expectedTable = new ArrayList<>(
			Arrays.asList(
				new ArrayList<>(Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1)),
				new ArrayList<>(Arrays.asList(LOWERCASE_VAL1, LOWERCASE_VAL2, LOWERCASE_VAL3))
			)
		);

		SourceTable table = SourceTable.builder()
			.table(expectedTable)
			.rawData("rawData")
			.build();

		ConnectorNamespace namespace = ConnectorNamespace.builder().sourceTables(Map.of(TAB1_REF, table)).build();
		Map<String,ConnectorNamespace> connectorNamespaces = new HashMap<>();
		connectorNamespaces.put(MY_CONNECTOR_1_NAME, namespace);
		HostProperties hostProperties = HostProperties.builder().connectorNamespaces(connectorNamespaces).build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(hostConfiguration)
			.hostProperties(hostProperties)
			.build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.connectorName(MY_CONNECTOR_1_NAME)
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

		tableResult.get(0).add(LOWERCASE_VAL3);

		assertEquals(
			Arrays.asList(
				Arrays.asList(LOWERCASE_A1, LOWERCASE_B1, LOWERCASE_C1),
				Arrays.asList(LOWERCASE_VAL1, LOWERCASE_VAL2, LOWERCASE_VAL3)
			),
			expectedTable
		);
	}
}