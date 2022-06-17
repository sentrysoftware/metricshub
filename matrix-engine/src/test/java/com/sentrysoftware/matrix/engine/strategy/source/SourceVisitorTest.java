package com.sentrysoftware.matrix.engine.strategy.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.common.exception.NoCredentialProvidedException;
import com.sentrysoftware.matrix.common.exception.StepException;
import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendText;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitFor;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.Ipmi;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OsCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.StaticSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs.UcsSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WbemSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WmiSource;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.HttpProtocol;
import com.sentrysoftware.matrix.engine.protocol.IWinProtocol;
import com.sentrysoftware.matrix.engine.protocol.IpmiOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.OsCommandConfig;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol.SnmpVersion;
import com.sentrysoftware.matrix.engine.protocol.SshProtocol;
import com.sentrysoftware.matrix.engine.protocol.TransportProtocols;
import com.sentrysoftware.matrix.engine.protocol.WbemProtocol;
import com.sentrysoftware.matrix.engine.protocol.WmiProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandResult;
import com.sentrysoftware.matrix.engine.strategy.utils.SshInteractiveHelper;
import com.sentrysoftware.matrix.model.monitoring.ConnectorNamespace;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;

import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;

@ExtendWith(MockitoExtension.class)
class SourceVisitorTest {
	private static final String ROOT_IBMSD_WMI_NAMESPACE = "root\\ibmsd";
	private static final String PC14 = "pc14";
	private static final String WQL = "SELECT DeviceID,State FROM IBMPSG_RAIDDiskDrive";
	private static final String EMC_HOSTNAME = "dev-hv-01";
	private static final String WBEM_QUERY = "SELECT __PATH,Name FROM EMC_StorageSystem";
	private static final List<List<String>> EXPECTED_SNMP_TABLE_DATA = Arrays.asList(Arrays.asList("1", "PowerEdge R630", "FSJR3N2", "34377965102"));
	private static final String ECS1_01 = "ecs1-01";
	private static final List<String> SNMP_SELECTED_COLUMNS = Arrays.asList("ID","9","11","49");
	private static final List<String> SNMP_WRONG_COLUMNS = Arrays.asList("ID","ID9","ID11","ID49");
	private static final String OID = "1.3.6.1.4.1.674.10892.1.300.10.1";
	private static final String VALUE_TABLE = "enclosure.collect.source(1)";
	private static final String VALUE_LIST = "a1;b1;c1";
	private static final String VALUE_A1 = "a1";
	private static final String CONNECTOR_NAME = "myConnector";

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@Mock
	private Connector connector;

	@InjectMocks
	private SourceVisitor sourceVisitor;

	@Mock
	private HostMonitoring hostMonitoring;

	private static EngineConfiguration engineConfiguration;

	@BeforeAll
	public static void setUp() {
		SnmpProtocol snmpProtocol = SnmpProtocol.builder().community("public").version(SnmpVersion.V1).port(161).timeout(120L).build();
		HttpProtocol httpProtocol = HttpProtocol.builder().username("username").password("password".toCharArray()).port(161).timeout(120L).build();
		engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(ECS1_01).id(ECS1_01).type(HostType.LINUX).build())
				.protocolConfigurations(Map.of(SnmpProtocol.class, snmpProtocol, HttpProtocol.class, httpProtocol)).build();

	}


	@BeforeEach
	void beforeEeach() {
		lenient().doReturn(CONNECTOR_NAME).when(connector).getCompiledFilename();
	}

	@Test
	void testVisitHttpSource() {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(HttpSource.builder().build()));

		// no http protocol
		EngineConfiguration engineConfigurationNoProtocol = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(ECS1_01).id(ECS1_01).type(HostType.LINUX).build())
				.build();
		doReturn(engineConfigurationNoProtocol).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(HttpSource.builder().build()));

		// classic case
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(ECS1_01).when(matsyaClientsExecutor).executeHttp(any(), eq(true));
		final SourceTable actual = sourceVisitor
				.visit(HttpSource.builder().build());
		final SourceTable expected = SourceTable.builder().rawData(ECS1_01).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitIpmiSourceStorageHost() {
		EngineConfiguration engineConfigurationStorageHost = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(ECS1_01).id(ECS1_01).type(HostType.STORAGE).build())
				.build();
		doReturn(engineConfigurationStorageHost).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(new Ipmi()));
	}

	@Test
	void testVisitIpmiSourceOobHostNoIpmiConfig() {
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder()
						.hostname(ECS1_01)
						.id(ECS1_01)
						.type(HostType.MGMT_CARD_BLADE_ESXI)
						.build())
				.protocolConfigurations(Collections.emptyMap())
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(new Ipmi()));
	}

	@Test
	void testVisitIpmiSourceOob() throws Exception {
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder()
						.hostname(ECS1_01)
						.id(ECS1_01)
						.type(HostType.MGMT_CARD_BLADE_ESXI)
						.build())
				.protocolConfigurations(Collections.emptyMap())
				.protocolConfigurations(Map.of(IpmiOverLanProtocol.class, IpmiOverLanProtocol
						.builder()
						.username("username")
						.password("password".toCharArray()).build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		String ipmiResult = "FRU;IBM;System x3650 M2;KD9098C - 794722G\n"
				+ "System Board;1;System Board 1;IBM;System x3650 M2;KD9098C - 794722G;Base board 1=Device Present";
		doReturn(ipmiResult).when(matsyaClientsExecutor).executeIpmiGetSensors(eq(ECS1_01), any(IpmiOverLanProtocol.class));
		assertEquals(SourceTable.builder().rawData(ipmiResult).build(), sourceVisitor.visit(new Ipmi()));
	}

	@Test
	void testVisitIpmiSourceOobNullResult() throws Exception {
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder()
						.hostname(ECS1_01)
						.id(ECS1_01)
						.type(HostType.MGMT_CARD_BLADE_ESXI)
						.build())
				.protocolConfigurations(Collections.emptyMap())
				.protocolConfigurations(Map.of(IpmiOverLanProtocol.class, IpmiOverLanProtocol
						.builder()
						.username("username")
						.password("password".toCharArray()).build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(null).when(matsyaClientsExecutor).executeIpmiGetSensors(eq(ECS1_01), any(IpmiOverLanProtocol.class));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(new Ipmi()));
	}

	@Test
	void testVisitIpmiSourceOobException() throws Exception {
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder()
						.hostname(ECS1_01)
						.id(ECS1_01)
						.type(HostType.MGMT_CARD_BLADE_ESXI)
						.build())
				.protocolConfigurations(Collections.emptyMap())
				.protocolConfigurations(Map.of(IpmiOverLanProtocol.class, IpmiOverLanProtocol
						.builder()
						.username("username")
						.password("password".toCharArray()).build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doThrow(new ExecutionException(new Exception("Exception from tests")))
				.when(matsyaClientsExecutor).executeIpmiGetSensors(eq(ECS1_01), any(IpmiOverLanProtocol.class));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(new Ipmi()));
	}

	@Test
	void testVisitOsCommandSource() {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit((OsCommandSource) null));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(new OsCommandSource()));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(OsCommandSource.builder().commandLine("").build()));

		final String commandLine = "/usr/sbin/ioscan -kFC ext_bus";
		final String keepOnlyRegExp = ":ext_bus:";
		final String separators = ":";
		final List<String> selectColumns = List.of("2-4", "5", "6");

		final OsCommandSource commandSource = new OsCommandSource();
		commandSource.setCommandLine(commandLine);
		commandSource.setKeepOnlyRegExp(keepOnlyRegExp);
		commandSource.setSeparators(separators);
		commandSource.setSelectColumns(selectColumns);

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(null).when(connector).getEmbeddedFiles();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.runOsCommand(
					commandLine,
					engineConfiguration,
					null,
					null,
					false,
					hostMonitoring.isLocalhost())).thenThrow(NoCredentialProvidedException.class);

			assertEquals(SourceTable.empty(), sourceVisitor.visit(commandSource));
		}

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.runOsCommand(
					commandLine,
					engineConfiguration,
					null,
					null,
					false,
					hostMonitoring.isLocalhost())).thenThrow(IOException.class);

			assertEquals(SourceTable.empty(), sourceVisitor.visit(commandSource));
		}

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {

			final String result =
					"xxxxxx\n"
					+ "xxxxxx\n"
					+ "0:1:ext_bus:3:4:5:6:7:8\n"
					+ "xxxxxx\n"
					+ "xxxxxx\n";
			final OsCommandResult commandResult = new OsCommandResult(result, commandLine);

			mockedOsCommandHelper.when(() -> OsCommandHelper.runOsCommand(
					commandLine,
					engineConfiguration,
					null,
					null,
					false,
					hostMonitoring.isLocalhost())).thenReturn(commandResult);

			final SourceTable expected = SourceTable.builder()
					.rawData("1;ext_bus;3;4;5")
					.table(List.of(List.of("1", "ext_bus", "3", "4", "5")))
					.build();
			assertEquals(expected, sourceVisitor.visit(commandSource));
		}
	}

	@Test
	void testVisitReferenceSource() {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		ReferenceSource referenceSource = null;
		assertEquals(SourceTable.empty(), sourceVisitor.visit(referenceSource));
		referenceSource = new ReferenceSource();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(referenceSource));

		List<List<String>> expectedTable = Arrays.asList(
				Arrays.asList("a1", "b1", "c1"),
				Arrays.asList("val1", "val2", "val3"));

		SourceTable table = SourceTable.builder()
				.table(expectedTable)
				.rawData("rawData")
				.build();

		ConnectorNamespace namespace = ConnectorNamespace.builder().sourceTables(
				Map.of(VALUE_TABLE, table)).build();

		referenceSource = ReferenceSource.builder().build();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(referenceSource));

		referenceSource = ReferenceSource.builder().reference(VALUE_TABLE).build();

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(namespace).when(hostMonitoring).getConnectorNamespace(connector);
		assertEquals(expectedTable, sourceVisitor.visit(referenceSource).getTable());
	}

	@Test
	void testVisitStaticSourceSingleValue() {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		StaticSource staticSource = null;
		assertEquals(SourceTable.empty(), sourceVisitor.visit(staticSource));
		staticSource = new StaticSource();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(staticSource));

		List<List<String>> expectedTable = Arrays.asList(
				Arrays.asList(VALUE_A1));

		staticSource = StaticSource.builder().build();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(staticSource));

		staticSource = StaticSource.builder().staticValue(VALUE_A1).build();

		assertEquals(expectedTable, sourceVisitor.visit(staticSource).getTable());
	}

	@Test
	void testVisitStaticSourceMultipleValues() {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(new StaticSource()));

		List<List<String>> expectedTable = Arrays.asList(
				Arrays.asList("a1", "b1", "c1"));

		StaticSource staticSource = StaticSource.builder().build();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(staticSource));

		staticSource = StaticSource.builder().staticValue(VALUE_LIST).build();

		assertEquals(expectedTable, sourceVisitor.visit(staticSource).getTable());
	}

	@Test
	void testVisitSnmpGetSource() throws InterruptedException, ExecutionException, TimeoutException {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit( SnmpGetSource.builder().oid(null).build()));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SnmpGetSource.builder().oid(OID).build()));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(new SnmpGetSource()));

		// no snmp protocol
		EngineConfiguration engineConfigurationNoProtocol = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(ECS1_01).id(ECS1_01).type(HostType.LINUX).build())
				.build();
		doReturn(engineConfigurationNoProtocol).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor
				.visit(SnmpGetSource.builder().oid(OID).build()));

		// classic case
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(ECS1_01).when(matsyaClientsExecutor).executeSNMPGet(any(), any(), any(), eq(true));
		final SourceTable actual = sourceVisitor
				.visit(SnmpGetSource.builder().oid(OID).build());
		final SourceTable expected = SourceTable.builder().table(Arrays.asList(Arrays.asList(ECS1_01)))
				.build();
		assertEquals(expected, actual);

		// test that the exception is correctly caught and still return a result
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		when(matsyaClientsExecutor.executeSNMPGet(any(), any(), any(), eq(true))).thenThrow(TimeoutException.class);
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SnmpGetSource.builder().oid(OID).build()));

	}


	@Test
	void testVisitSnmpGetTableNullArgs() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit( SnmpGetTableSource.builder().oid(null).snmpTableSelectColumns(null).build()));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SnmpGetTableSource.builder().snmpTableSelectColumns(SNMP_SELECTED_COLUMNS).build()));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SnmpGetTableSource.builder().oid(OID).build()));
		// no snmp protocol
		EngineConfiguration engineConfigurationNoProtocol = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(ECS1_01).id(ECS1_01).type(HostType.LINUX).build())
				.build();
		doReturn(engineConfigurationNoProtocol).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SnmpGetTableSource.builder().oid(OID).snmpTableSelectColumns(SNMP_SELECTED_COLUMNS).build()));

		// test when Matsya throws an exception
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		when(matsyaClientsExecutor.executeSNMPTable(any(), any(), any(), any(), eq(true))).thenThrow(TimeoutException.class);
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SnmpGetTableSource.builder().oid(OID).snmpTableSelectColumns(SNMP_SELECTED_COLUMNS).build()));
	}

	@Test
	void testVisitSnmpGetTableExpectedResultNotMatches() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(new ArrayList<>()).when(matsyaClientsExecutor).executeSNMPTable(any(), any(), any(), any(), eq(true));
		final SourceTable actual = sourceVisitor.visit(SnmpGetTableSource.builder().oid(OID).snmpTableSelectColumns(SNMP_WRONG_COLUMNS).build());
		final SourceTable expected = SourceTable.builder().table(new ArrayList<>()
				).headers(SNMP_WRONG_COLUMNS).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSbmpGetTableExpectedResultMatches() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(EXPECTED_SNMP_TABLE_DATA).when(matsyaClientsExecutor).executeSNMPTable(any(), any(), any(), any(), eq(true));
		final SourceTable actual = sourceVisitor
				.visit(SnmpGetTableSource.builder().oid(OID).snmpTableSelectColumns(SNMP_SELECTED_COLUMNS).build());
		final SourceTable expected = SourceTable.builder().table(EXPECTED_SNMP_TABLE_DATA)
				.headers(SNMP_SELECTED_COLUMNS).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitTableJoinSource() {
		final Map<String, SourceTable> mapSources = new HashMap<>();
		SourceTable tabl1 = SourceTable.builder().table(Arrays.asList(
						Arrays.asList("a1","b1", "c1"),
						Arrays.asList("val1","val2", "val3"),
						Arrays.asList("V1","V2", "V3"),
						Arrays.asList("x","y", "z"))).build();
		SourceTable tabl2 = SourceTable.builder().table(Arrays.asList(
						Arrays.asList("a1","b2", "c2"),
						Arrays.asList("v1","v2", "v3"),
						Arrays.asList("VaL1","B2", "C2"),
						Arrays.asList("t","u", "v"))).build();
		mapSources.put("tab1", tabl1 );
		mapSources.put("tab2", tabl2 );

		ConnectorNamespace namespace = ConnectorNamespace.builder().sourceTables(mapSources).build();

		// standard
		List<List<String>> expectedJoin = Arrays.asList(Arrays.asList("a1", "b1", "c1", "a1", "b2", "c2"),
														Arrays.asList("val1", "val2", "val3", "VaL1","B2", "C2"),
														Arrays.asList("V1","V2", "V3", "VaL1","B2", "C2"),
														Arrays.asList("x","y", "z", "a1","b1", "c1"));
		SourceTable expectedResult = SourceTable.builder().table(expectedJoin).build();

		List<List<String>> matsyaReturn = Arrays.asList(
				Arrays.asList("a1", "b1", "c1", "a1", "b2", "c2"),
				Arrays.asList("val1", "val2", "val3", "VaL1", "B2", "C2"),
				Arrays.asList("V1", "V2", "V3", "VaL1", "B2", "C2"),
				Arrays.asList("x","y", "z", "a1","b1", "c1"));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(namespace).when(hostMonitoring).getConnectorNamespace(connector);
		doReturn(matsyaReturn).when(matsyaClientsExecutor).executeTableJoin(tabl1.getTable(), tabl2.getTable(), 1, 1, Arrays.asList("a1","b1", "c1"), false, true);

		TableJoinSource tableJoinExample = TableJoinSource.builder()
																	.keyType("notWbem")
																	.leftTable("tab1")
																	.rightTable("tab2")
																	.leftKeyColumn(1)
																	.rightKeyColumn(1)
																	.defaultRightLine(Arrays.asList("a1","b1", "c1")).build();
		assertEquals(expectedJoin, sourceVisitor.visit(tableJoinExample).getTable());
		assertTrue(expectedJoin.size() == sourceVisitor.visit(tableJoinExample).getTable().size() && expectedJoin.containsAll(sourceVisitor.visit(tableJoinExample).getTable()) && sourceVisitor.visit(tableJoinExample).getTable().containsAll(expectedJoin));
		assertEquals(new ArrayList<>(), sourceVisitor.visit(tableJoinExample).getHeaders());
		assertEquals(expectedResult.getHeaders(), sourceVisitor.visit(tableJoinExample).getHeaders());
		assertEquals(expectedResult.getTable(), sourceVisitor.visit(tableJoinExample).getTable());

		// no default right line
		expectedJoin = Arrays.asList(Arrays.asList("a1", "b1", "c1", "a1", "b2", "c2"));
		expectedResult = SourceTable.builder().table(expectedJoin).build();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(namespace).when(hostMonitoring).getConnectorNamespace(connector);
		doReturn(expectedJoin).when(matsyaClientsExecutor).executeTableJoin(tabl1.getTable(), tabl2.getTable(), 1, 1, null, false, true);
		tableJoinExample = TableJoinSource.builder()
											.keyType("notWbem")
											.leftTable("tab1")
											.rightTable("tab2")
											.leftKeyColumn(1)
											.rightKeyColumn(1)
											.defaultRightLine(null).build();
		assertEquals(expectedResult.getTable(), sourceVisitor.visit(tableJoinExample).getTable());
		assertEquals(new ArrayList<>(), sourceVisitor.visit(tableJoinExample).getHeaders());
		assertEquals(expectedResult.getHeaders(), sourceVisitor.visit(tableJoinExample).getHeaders());
		assertEquals(expectedResult.getTable(), sourceVisitor.visit(tableJoinExample).getTable());

		// no matches
		SourceTable tabl3 = SourceTable.builder().table(Arrays.asList(Arrays.asList("a","b", "c"), Arrays.asList("v10","v20", "v30"))).build();
		mapSources.put("tab3", tabl3 );
		namespace = ConnectorNamespace.builder().sourceTables(mapSources).build();
		expectedJoin = Arrays.asList(Arrays.asList("a", "b", "c"));
		expectedResult = SourceTable.builder().table(expectedJoin).build();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(namespace).when(hostMonitoring).getConnectorNamespace(connector);
		doReturn(expectedJoin).when(matsyaClientsExecutor).executeTableJoin(tabl1.getTable(), tabl3.getTable(), 1, 1, null, false, true);
		tableJoinExample = TableJoinSource.builder()
											.keyType("notWbem")
											.leftTable("tab1")
											.rightTable("tab3")
											.leftKeyColumn(1)
											.rightKeyColumn(1)
											.defaultRightLine(null).build();
		assertEquals(expectedResult.getTable(), sourceVisitor.visit(tableJoinExample).getTable());

		// wrong column key
		tabl3 = SourceTable.builder().table(Arrays.asList(Arrays.asList("a","b", "c"), Arrays.asList("v10","v20", "v30"))).build();
		mapSources.put("tab3", tabl3 );
		namespace = ConnectorNamespace.builder().sourceTables(mapSources).build();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(namespace).when(hostMonitoring).getConnectorNamespace(connector);
		tableJoinExample = TableJoinSource.builder()
											.keyType("notWbem")
											.leftTable("tab1")
											.rightTable("tab3")
											.leftKeyColumn(0)
											.rightKeyColumn(1)
											.defaultRightLine(null).build();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(tableJoinExample));

		// null args
		tableJoinExample = TableJoinSource.builder()
											.keyType("notWbem")
											.leftTable(null)
											.rightTable("tab3")
											.leftKeyColumn(1)
											.rightKeyColumn(1)
											.defaultRightLine(null).build();
		assertEquals(new ArrayList<>(), sourceVisitor.visit(tableJoinExample).getTable());
		tableJoinExample = TableJoinSource.builder()
											.keyType("notWbem")
											.leftTable("tab1")
											.rightTable(null)
											.leftKeyColumn(1)
											.rightKeyColumn(1)
											.defaultRightLine(null).build();
		// table not in sources
		assertEquals(new ArrayList<>(), sourceVisitor.visit(tableJoinExample).getTable());
		tableJoinExample = TableJoinSource.builder()
											.keyType("notWbem")
											.leftTable("tab1")
											.rightTable("blabla")
											.leftKeyColumn(1)
											.rightKeyColumn(1)
											.defaultRightLine(null).build();
		assertEquals(new ArrayList<>(), sourceVisitor.visit(tableJoinExample).getTable());

		doReturn(ConnectorNamespace.builder().build()).when(hostMonitoring).getConnectorNamespace(connector);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		tableJoinExample = TableJoinSource.builder()
											.keyType("notWbem")
											.leftTable("tab1")
											.rightTable("tab2")
											.leftKeyColumn(1)
											.rightKeyColumn(1)
											.defaultRightLine(null).build();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(tableJoinExample));

		doReturn(null).when(strategyConfig).getHostMonitoring();
		final TableJoinSource tableJoinSource = TableJoinSource.builder()
											.keyType("notWbem")
											.leftTable("tab1")
											.rightTable("tab2")
											.leftKeyColumn(1)
											.rightKeyColumn(1)
											.defaultRightLine(null).build();
		assertThrows(NullPointerException.class, () -> sourceVisitor.visit(tableJoinSource));
	}

	@Test
	void testVisitTableUnionSourceTest() {
		SourceTable tabl1 = SourceTable.builder()
				.table(Arrays.asList(
						Arrays.asList("a1", "b1", "c1"),
						Arrays.asList("val1", "val2", "val3")))
				.rawData(VALUE_A1)
				.build();
		SourceTable tabl2 = SourceTable.builder()
				.table(Arrays.asList(
						Arrays.asList("a1", "b2", "c2"),
						Arrays.asList("v1", "v2", "v3")))
				.rawData(VALUE_TABLE)
				.build();

		final Map<String, SourceTable> mapSources = new HashMap<>();
		mapSources.put("Enclosure.Discovery.Source(1)", tabl1);
		mapSources.put("Enclosure.Discovery.Source(2)", tabl2);

		final ConnectorNamespace namespace = ConnectorNamespace.builder().sourceTables(mapSources).build();

		// standard
		List<List<String>> expectedUnion = Arrays.asList(
				Arrays.asList("a1", "b1", "c1"),
				Arrays.asList("val1", "val2", "val3"),
				Arrays.asList("a1", "b2", "c2"),
				Arrays.asList("v1", "v2", "v3"));

		TableUnionSource tableUnionExample = TableUnionSource.builder().build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(tableUnionExample));

		tableUnionExample = TableUnionSource.builder().tables(Arrays.asList()).build();
		assertEquals(new ArrayList<>(), sourceVisitor.visit(tableUnionExample).getTable());

		tableUnionExample = TableUnionSource.builder()
				.tables(Arrays.asList("Enclosure.Discovery.Source(1)", "Enclosure.Discovery.Source(2)", "Enclosure.Discovery.Source(3)"))
				.build();

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(namespace).when(hostMonitoring).getConnectorNamespace(connector);
		assertEquals(expectedUnion, sourceVisitor.visit(tableUnionExample).getTable());
		assertEquals(VALUE_A1 +"\n"+ VALUE_TABLE, sourceVisitor.visit(tableUnionExample).getRawData());

	}

	@Test
	void testVisitSshInteractiveSource() {

		final SendText sendText = new SendText();
		sendText.setText("show enclosure info\n");

		final WaitFor waitFor = new WaitFor();
		waitFor.setText("show enclosure info");
		waitFor.setCapture(true);

		final List<Step> steps = List.of(sendText, waitFor);

		final SshInteractiveSource sshInteractiveSource = new SshInteractiveSource();
		sshInteractiveSource.setIndex(1);
		sshInteractiveSource.setKey("enclosure.discovery.source(1)");
		sshInteractiveSource.setRemoveHeader(8);
		sshInteractiveSource.setRemoveFooter(6);
		sshInteractiveSource .setKeepOnlyRegExp("Serial Number");
		sshInteractiveSource.setSteps(steps);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()))
				.host(new HardwareHost("id", "host", HostType.LINUX))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		// check NoCredentialProvidedException
		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive " + sshInteractiveSource.getKey()))
			.thenThrow(NoCredentialProvidedException.class);

			assertEquals(SourceTable.empty(), sourceVisitor.visit(sshInteractiveSource));
		}

		// check StepException
		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive " + sshInteractiveSource.getKey()))
			.thenThrow(StepException.class);

			assertEquals(SourceTable.empty(), sourceVisitor.visit(sshInteractiveSource));
		}

		// check ok
		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			final List<String> output = List.of(
					"Output:\n",
					"\n",
					"HP BladeSystem Onboard Administrator\n",
					"(C) Copyright 2006-2015 Hewlett-Packard Development Company, L.P.\n",
					"\n",
					"fermat-oa [SCRIPT MODE]> show enclosure info\n",
					"\n",
					"\n",
					"Enclosure Information:\n",
					"        Enclosure Name: fermat-oa\n",
					"        Enclosure Type: BladeSystem c3000 Enclosure\n",
					"        Part Number: 437502-B21\n",
					"        Serial Number: CZC8171W57\n",
					"        UUID: 09CZC8171W57\n",
					"        Asset Tag:\n",
					"        Midplane Spare Part Number: 441829-001\n",
					"        Solutions ID: 0000000000000000\n",
					"        Power Distribution Unit:\n",
					"                [No PDU is present on c3000 enclosures]\n",
					"        Onboard Administrator Tray Information:\n",
					"                [No Tray Information is present on c3000 enclosures]\n",
					"\n",
					"\n",
					"fermat-oa [SCRIPT MODE]>\n",
					"\n",
					"show enclosure info\n",
					"\n");

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive " + sshInteractiveSource.getKey()))
			.thenReturn(output);

			final SourceTable expected = SourceTable.builder()
					.rawData("        Serial Number: CZC8171W57\n")
					.table(List.of(List.of("        Serial Number: CZC8171W57\n")))
					.build();

			assertEquals(expected, sourceVisitor.visit(sshInteractiveSource));
		}
	}

	@Test
	void testVisitUcsSource() {
		assertEquals(SourceTable.empty(), sourceVisitor.visit(new UcsSource()));
	}

	@Test
	void testVisitWbemSource() throws MatsyaException {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit((WbemSource) null));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(WbemSource.builder().build()));

		WbemSource wbemSource = WbemSource.builder().wbemQuery(WBEM_QUERY).build();
		EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(EMC_HOSTNAME).id(EMC_HOSTNAME).type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Collections.emptyMap()).build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		
		// no wbem protocol
		assertEquals(SourceTable.empty(), sourceVisitor.visit(wbemSource));


		engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(EMC_HOSTNAME).id(EMC_HOSTNAME).type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WbemProtocol.class,
						WbemProtocol.builder()
						.build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		
		// empty protocol
		assertEquals(SourceTable.empty(), sourceVisitor.visit(wbemSource));

		engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().id(EMC_HOSTNAME).type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WbemProtocol.class,
						WbemProtocol.builder()
						.username(EMC_HOSTNAME)
						.password("password".toCharArray())
						.build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		// no namespace
		assertEquals(SourceTable.empty(), sourceVisitor.visit(wbemSource));


		engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().id(EMC_HOSTNAME).type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WbemProtocol.class,
						WbemProtocol.builder()
						.namespace(ROOT_IBMSD_WMI_NAMESPACE)
						.username(EMC_HOSTNAME)
						.password("password".toCharArray())
						.build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		
		// unable to build URL : no port
		assertEquals(SourceTable.empty(), sourceVisitor.visit(wbemSource));

		engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WbemProtocol.class,
						WbemProtocol.builder()
						.port(5989)
						.namespace(ROOT_IBMSD_WMI_NAMESPACE)
						.username(EMC_HOSTNAME)
						.password("password".toCharArray())
						.build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		// unable to build URL : no hostname
		assertEquals(SourceTable.empty(), sourceVisitor.visit(wbemSource));

		engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().id(EMC_HOSTNAME).hostname(EMC_HOSTNAME).type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WbemProtocol.class,
						WbemProtocol.builder()
						.port(5989)
						.protocol(TransportProtocols.HTTPS)
						.namespace(ROOT_IBMSD_WMI_NAMESPACE)
						.username(EMC_HOSTNAME)
						.password("password".toCharArray())
						.timeout(120L)
						.build()))
				.build();

		List<List<String>> listValues = Arrays.asList(
				Arrays.asList("a1", "b2", "c2"),
				Arrays.asList("v1", "v2", "v3"));

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(listValues).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), any());
		assertEquals(listValues, sourceVisitor.visit(wbemSource).getTable());

		 // handle exception
		doThrow(new MatsyaException()).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), any());
		assertEquals(SourceTable.empty(), sourceVisitor.visit(wbemSource));
	}

	@Test
	void testVisitWmiSourceMalformed() {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit((WmiSource) null));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(WmiSource.builder().build()));
	}

	@Test
	void testVisitWmiSourceButWmiNotConfigured() {
		final WmiSource wmiSource = WmiSource.builder().wbemQuery(WQL).build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(PC14).id(PC14).type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Collections.emptyMap()).build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		assertEquals(SourceTable.empty(), sourceVisitor.visit(wmiSource));
	}

	@Test
	void testVisitWmiSourceNoNamespace() {
		final WmiSource wmiSource = WmiSource.builder().wbemQuery(WQL).wbemNamespace("automatic").build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(PC14).id(PC14).type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WmiProtocol.class,
						WmiProtocol.builder()
						.username(PC14 + "\\" + "Administrator")
						.password("password".toCharArray())
						.build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(ConnectorNamespace.builder().automaticWmiNamespace(null).build()).when(hostMonitoring).getConnectorNamespace(connector);
		assertEquals(SourceTable.empty(), sourceVisitor.visit(wmiSource));
	}

	@Test
	void testVisitWmiSource() throws Exception {
		final WmiSource wmiSource = WmiSource.builder().wbemQuery(WQL).wbemNamespace("automatic").build();
		final WmiProtocol wmiProtocol = WmiProtocol.builder()
				.username(PC14 + "\\" + "Administrator")
				.password("password".toCharArray())
				.build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(PC14).id(PC14).type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WmiProtocol.class,
						wmiProtocol))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(ConnectorNamespace
				.builder()
				.automaticWmiNamespace(ROOT_IBMSD_WMI_NAMESPACE)
				.build()).when(hostMonitoring).getConnectorNamespace(connector);
		final List<List<String>> expected = Arrays.asList(
				Arrays.asList("1.1", "0|4587"),
				Arrays.asList("1.2", "2|4587"),
				Arrays.asList("1.3", "1|4587"));
		doReturn(expected).when(matsyaClientsExecutor)
				.executeWql(
						PC14,
						wmiProtocol,
						WQL,
						ROOT_IBMSD_WMI_NAMESPACE);
		assertEquals(SourceTable.builder().table(expected).build(), sourceVisitor.visit(wmiSource));

	}

	@Test
	void testVisitWmiSourceTimeout() throws Exception {

		final WmiSource wmiSource = WmiSource.builder().wbemQuery(WQL).wbemNamespace("automatic").build();
		final WmiProtocol wmiProtocol = WmiProtocol.builder()
				.username(PC14 + "\\" + "Administrator")
				.password("password".toCharArray())
				.build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(PC14).id(PC14).type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WmiProtocol.class, wmiProtocol))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(ConnectorNamespace
				.builder()
				.automaticWmiNamespace(ROOT_IBMSD_WMI_NAMESPACE)
				.build()).when(hostMonitoring).getConnectorNamespace(connector);
		doThrow(new MatsyaException()).when(matsyaClientsExecutor)
				.executeWql(
						PC14,
						wmiProtocol,
						WQL,
						ROOT_IBMSD_WMI_NAMESPACE);
		assertEquals(SourceTable.empty(), sourceVisitor.visit(wmiSource));

	}

	@Test
	void testGetNamespace() {
		{
			final WmiSource wmiSource = WmiSource.builder().wbemQuery(WQL).build();
			assertEquals("root\\cimv2", sourceVisitor.getNamespace(wmiSource));
		}

		{
			final WmiSource wmiSource = WmiSource.builder().wbemQuery(WQL).wbemNamespace("automatic").build();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(ConnectorNamespace
					.builder()
					.automaticWmiNamespace(ROOT_IBMSD_WMI_NAMESPACE)
					.build()).when(hostMonitoring).getConnectorNamespace(connector);
			assertEquals(ROOT_IBMSD_WMI_NAMESPACE, sourceVisitor.getNamespace(wmiSource));
		}

		{
			final WmiSource wmiSource = WmiSource.builder().wbemQuery(WQL).wbemNamespace(ROOT_IBMSD_WMI_NAMESPACE).build();
			assertEquals(ROOT_IBMSD_WMI_NAMESPACE, sourceVisitor.getNamespace(wmiSource));
		}

	}

	@Test
	void testGetSourceTable() {
		{
			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(ConnectorNamespace.builder().build()).when(hostMonitoring).getConnectorNamespace(connector);
			assertNull(sourceVisitor.getSourceTable("Temperature.Collect.Source(1)"));
		}

		{
			final SourceTable expected = SourceTable.builder().table(EXPECTED_SNMP_TABLE_DATA).build();
			final Map<String, SourceTable> sourceTableMap = Map.of("Temperature.Collect.Source(1)", expected);
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(ConnectorNamespace.builder().sourceTables(sourceTableMap).build()).when(hostMonitoring).getConnectorNamespace(connector);
			assertEquals(expected, sourceVisitor.getSourceTable("Temperature.Collect.Source(1)"));
		}

		{
			assertEquals(SourceTable.builder().table(Arrays.asList(Arrays.asList("val1", "val2", "val3"))).build(),
					sourceVisitor.getSourceTable("val1;val2;val3;"));
		}
	}

	@Test
	void testProcessWindowsIpmiSource() throws Exception {
		final IWinProtocol wmiProtocol = WmiProtocol
			.builder()
			.username(PC14 + "\\" + "Administrator")
			.password("password".toCharArray())
			.timeout(120L)
			.build();
		final EngineConfiguration engineConfiguration = EngineConfiguration
			.builder()
			.host(HardwareHost.builder().hostname(PC14).id(PC14).type(HostType.MS_WINDOWS).build())
			.protocolConfigurations(Map.of(WmiProtocol.class, wmiProtocol))
			.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final List<List<String>> wmiResult1 = Arrays.asList(
				Arrays.asList("IdentifyingNumber", "Name", "Vendor"));
		doReturn(wmiResult1).when(matsyaClientsExecutor).executeWql(
			PC14,
			wmiProtocol,
			"SELECT IdentifyingNumber,Name,Vendor FROM Win32_ComputerSystemProduct",
			"root/cimv2"
		);

		final List<List<String>> wmiResult2 = Arrays.asList(
				Arrays.asList("2", "20", "sensorName(sensorId):description for deviceId", "10", "15", "2", "0", "30", "25"));
		doReturn(wmiResult2).when(matsyaClientsExecutor).executeWql(
			PC14,
			wmiProtocol,
			"SELECT BaseUnits,CurrentReading,Description,LowerThresholdCritical,LowerThresholdNonCritical,SensorType,UnitModifier,UpperThresholdCritical,UpperThresholdNonCritical FROM NumericSensor",
			"root/hardware"
		);

		final List<List<String>> wmiResult3 = Arrays.asList(
				Arrays.asList("state", "sensorName(sensorId):description for deviceType deviceId"));
		doReturn(wmiResult3).when(matsyaClientsExecutor).executeWql(
			PC14,
			wmiProtocol,
			"SELECT CurrentState,Description FROM Sensor",
			"root/hardware"
		);

		final List<List<String>> expected = Arrays.asList(
				Arrays.asList(
						"FRU",
						"Vendor",
						"Name",
						"IdentifyingNumber"),
				Arrays.asList(
						"Temperature",
						"sensorId",
						"sensorName",
						"deviceId",
						"20.0",
						"25.0",
						"30.0"),
				Arrays.asList(
						"deviceType",
						"deviceId",
						"deviceType deviceId",
						"",
						"",
						"",
						"sensorName=state"));
		assertEquals(SourceTable.builder().table(expected).build(), sourceVisitor.processWindowsIpmiSource(null));
	}

	@Test
	void testProcessWindowsIpmiSourceWmiException() throws Exception {
		final IWinProtocol wmiProtocol = WmiProtocol
			.builder()
			.username(PC14 + "\\" + "Administrator")
			.password("password".toCharArray())
			.timeout(120L)
			.build();
		final EngineConfiguration engineConfiguration = EngineConfiguration
			.builder()
			.host(HardwareHost.builder().hostname(PC14).id(PC14).type(HostType.MS_WINDOWS).build())
			.protocolConfigurations(Map.of(WmiProtocol.class,wmiProtocol))
			.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		doThrow(MatsyaException.class).when(matsyaClientsExecutor).executeWql(
			PC14,
			wmiProtocol,
			"SELECT IdentifyingNumber,Name,Vendor FROM Win32_ComputerSystemProduct",
			"root/cimv2"
		);
		assertEquals(SourceTable.empty(), sourceVisitor.processWindowsIpmiSource(null));
	}

	@Test
	void testProcessWindowsIpmiSourceWmiProtocolNull() throws Exception {
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(PC14).id(PC14).type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(HttpProtocol.class,
						HttpProtocol.builder().username("username").password("password".toCharArray()).port(161).timeout(120L).build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		assertEquals(SourceTable.empty(), sourceVisitor.processWindowsIpmiSource(null));
	}


	@Test
	void testProcessUnixIpmiSource() {

		// classic case
		final SshProtocol ssh = SshProtocol.builder().username("root").password("nationale".toCharArray()).build();
		EngineConfiguration engineConfigurationLocal = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX)
						.build())
				.protocolConfigurations(Map.of(HttpProtocol.class, OsCommandConfig.builder().build(),
						OsCommandConfig.class, OsCommandConfig.builder().build(),
						SshProtocol.class, ssh))
				.build();
		doReturn(engineConfigurationLocal).when(strategyConfig).getEngineConfiguration();
		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);
		hostMonitoring.setIpmitoolCommand("ipmiCommand");
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		// local
		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommand"+ "fru"), anyInt(), any())).thenReturn("impiResultFru");
			oscmd.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommand"+ "-v sdr elist all"), anyInt(), any())).thenReturn("impiResultSdr");
			final SourceTable ipmiResult = sourceVisitor.processUnixIpmiSource(null);
			assertEquals(SourceTable.empty(), ipmiResult);
		}

		String fru = "/data/IpmiFruBabbage";
		String sensor = "/data/IpmiSensorBabbage";
		String expected = "/data/ipmiProcessingResult";
		String fruResult = ResourceHelper.getResourceAsString(fru, this.getClass());
		String sensorResult = ResourceHelper.getResourceAsString(sensor, this.getClass());

		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommand"+ "fru"), anyInt(), any())).thenReturn(fruResult);
			oscmd.when(() -> OsCommandHelper.runLocalCommand(eq("ipmiCommand"+ "-v sdr elist all"), anyInt(), any())).thenReturn(sensorResult);
			final SourceTable ipmiResult = sourceVisitor.processUnixIpmiSource(null);
			String expectedResult = ResourceHelper.getResourceAsString(expected, this.getClass());
			List<List<String>> result = new ArrayList<>();
			Stream.of(expectedResult.split("\n")).forEach(line -> result.add(Arrays.asList(line.split(";"))));
			assertEquals(result, ipmiResult.getTable());
		}

		// remote
		hostMonitoring.setLocalhost(false);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd.when(() -> OsCommandHelper.runSshCommand(eq("ipmiCommand"+ "fru"), any(), any(), anyInt(), any(), any())).thenReturn("impiResultFru");
			oscmd.when(() -> OsCommandHelper.runSshCommand(eq("ipmiCommand"+ "-v sdr elist all"), any(), any(), anyInt(), any(), any())).thenReturn("impiResultSdr");
			final SourceTable ipmiResult = sourceVisitor.processUnixIpmiSource(null);
			assertEquals(SourceTable.empty(), ipmiResult);
		}

		// ipmiToolCommand is empty
		hostMonitoring.setIpmitoolCommand("");
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		SourceTable ipmiResultEmpty = sourceVisitor.processUnixIpmiSource(null);
		assertEquals(SourceTable.empty(), ipmiResultEmpty);

		// ipmiToolCommand is null
		hostMonitoring.setIpmitoolCommand(null);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		ipmiResultEmpty = sourceVisitor.processUnixIpmiSource(null);
		assertEquals(SourceTable.empty(), ipmiResultEmpty);

		// osCommandConfig is null
		engineConfigurationLocal = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX)
						.build())
				.protocolConfigurations(Map.of(HttpProtocol.class, OsCommandConfig.builder().build(),
						SshProtocol.class, ssh))
				.build();
		doReturn(engineConfigurationLocal).when(strategyConfig).getEngineConfiguration();
		final HostMonitoring hostMonitoring2 = new HostMonitoring();
		hostMonitoring2.setLocalhost(true);
		hostMonitoring2.setIpmitoolCommand("ipmiCommand");
		doReturn(hostMonitoring2).when(strategyConfig).getHostMonitoring();
		ipmiResultEmpty = sourceVisitor.processUnixIpmiSource(null);
		assertEquals(SourceTable.empty(), ipmiResultEmpty);
	}

}