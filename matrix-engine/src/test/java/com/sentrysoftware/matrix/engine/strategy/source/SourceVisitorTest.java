package com.sentrysoftware.matrix.engine.strategy.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.telnet.TelnetInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs.UCSSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;

@ExtendWith(MockitoExtension.class)
class SourceVisitorTest {
	private static final List<List<String>> EXPECTED_SNMP_TABLE_DATA = Arrays.asList(Arrays.asList("1", "PowerEdge R630", "FSJR3N2", "34377965102"));
	private static final String ECS1_01 = "ecs1-01";
	private static final List<String> SNMP_SELECTED_COLUMNS = Arrays.asList("ID","9","11","49");
	private static final List<String> SNMP_WRONG_COLUMNS = Arrays.asList("ID","ID9","ID11","ID49");
	private static final String OID = "1.3.6.1.4.1.674.10892.1.300.10.1";

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@InjectMocks
	private SourceVisitor sourceVisitor;
	
	@Mock
	private HostMonitoring hostMonitoring;

	private static EngineConfiguration engineConfiguration;

	@BeforeAll
	public static void setUp() {
		SNMPProtocol protocol = SNMPProtocol.builder().community("public").version(SNMPVersion.V1).port(161).timeout(120L).build();
		engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(ECS1_01).id(ECS1_01).type(TargetType.LINUX).build())
				.protocolConfigurations(Stream.of(
						protocol)
						.collect(Collectors.toSet())).build();
		
	}

	@Test
	void visitHTTPSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new HTTPSource()));
	}

	@Test
	void visitIPMISourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new IPMI()));
	}

	@Test
	void visitOSCommandSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new OSCommandSource()));
	}

	@Test
	void visitReferenceSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new ReferenceSource()));
	}

	@Test
	void visitSNMPGetSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new SNMPGetSource()));
	}

	@Test
	void testVisitSNMPGetTableNullArgs() throws Exception {
		assertEquals(SourceTable.empty(), sourceVisitor.visit( SNMPGetTableSource.builder().oid(null).snmpTableSelectColumns(null).build()));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SNMPGetTableSource.builder().snmpTableSelectColumns(SNMP_SELECTED_COLUMNS).build()));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SNMPGetTableSource.builder().oid(OID).build()));
		// no snmp protocol
		EngineConfiguration engineConfigurationNoProtocol = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(ECS1_01).id(ECS1_01).type(TargetType.LINUX).build())
				.build();
		doReturn(engineConfigurationNoProtocol).when(strategyConfig).getEngineConfiguration();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SNMPGetTableSource.builder().oid(OID).snmpTableSelectColumns(SNMP_SELECTED_COLUMNS).build()));
		
		// test when Matsya throws an exception
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		when(matsyaClientsExecutor.executeSNMPTable(any(), any(), any(), any(), eq(true))).thenThrow(TimeoutException.class);
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SNMPGetTableSource.builder().oid(OID).snmpTableSelectColumns(SNMP_SELECTED_COLUMNS).build()));
	}

	@Test
	void testVisitSNMPGetTableExpectedResultNotMatches() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(new ArrayList<>()).when(matsyaClientsExecutor).executeSNMPTable(any(), any(), any(), any(), eq(true));
		final SourceTable actual = sourceVisitor.visit(SNMPGetTableSource.builder().oid(OID).snmpTableSelectColumns(SNMP_WRONG_COLUMNS).build());
		final SourceTable expected = SourceTable.builder().table(new ArrayList<>()
				).headers(SNMP_WRONG_COLUMNS).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetTableExpectedResultMatches() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(EXPECTED_SNMP_TABLE_DATA).when(matsyaClientsExecutor).executeSNMPTable(any(), any(), any(), any(), eq(true));
		final SourceTable actual = sourceVisitor
				.visit(SNMPGetTableSource.builder().oid(OID).snmpTableSelectColumns(SNMP_SELECTED_COLUMNS).build());
		final SourceTable expected = SourceTable.builder().table(EXPECTED_SNMP_TABLE_DATA)
				.headers(SNMP_SELECTED_COLUMNS).build();
		assertEquals(expected, actual);
	}

	@Test
	void visitTableJoinSourceTest () {
		final Map<String, SourceTable> mapSources = new HashMap<>();
		SourceTable tabl1 = SourceTable.builder().table(Arrays.asList(Arrays.asList("a1","b1", "c1"), Arrays.asList("val1","val2", "val3"))).build();
		SourceTable tabl2 = SourceTable.builder().table(Arrays.asList(Arrays.asList("a1","b2", "c2"), Arrays.asList("v1","v2", "v3"))).build();
		mapSources.put("tab1", tabl1 );
		mapSources.put("tab2", tabl2 );

		// standard
		List<List<String>> expectedJoin = Arrays.asList(Arrays.asList("a1", "b1", "c1", "a1", "b2", "c2"), Arrays.asList("val1", "val2", "val3", "a1", "b1", "c1"));
		SourceTable expectedResult = SourceTable.builder().table(expectedJoin).build();
		
		List<List<String>> matsyaReturn = Arrays.asList(Arrays.asList("a1", "b1", "c1", "a1", "b2", "c2"), Arrays.asList("val1", "val2", "val3", "a1", "b1", "c1"));
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(mapSources).when(hostMonitoring).getSourceTables();
		doReturn(matsyaReturn).when(matsyaClientsExecutor).executeTableJoin(tabl1.getTable(), tabl2.getTable(), 1, 1, Arrays.asList("a1","b1", "c1"), false, false);
		
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

		// no default right line
		expectedJoin = Arrays.asList(Arrays.asList("a1", "b1", "c1", "a1", "b2", "c2"));
		expectedResult = SourceTable.builder().table(expectedJoin).build();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(mapSources).when(hostMonitoring).getSourceTables();
		doReturn(expectedJoin).when(matsyaClientsExecutor).executeTableJoin(tabl1.getTable(), tabl2.getTable(), 1, 1, null, false, false);
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

		// no matches
		SourceTable tabl3 = SourceTable.builder().table(Arrays.asList(Arrays.asList("a","b", "c"), Arrays.asList("v10","v20", "v30"))).build();
		mapSources.put("tab3", tabl3 );
		expectedJoin = Arrays.asList(Arrays.asList("a", "b", "c"));
		expectedResult = SourceTable.builder().table(expectedJoin).build();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(mapSources).when(hostMonitoring).getSourceTables();
		doReturn(expectedJoin).when(matsyaClientsExecutor).executeTableJoin(tabl1.getTable(), tabl3.getTable(), 1, 1, null, false, false);
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
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(mapSources).when(hostMonitoring).getSourceTables();
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

		doReturn(null).when(hostMonitoring).getSourceTables();
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
		tableJoinExample = TableJoinSource.builder()
											.keyType("notWbem")
											.leftTable("tab1")
											.rightTable("tab2")
											.leftKeyColumn(1)
											.rightKeyColumn(1)
											.defaultRightLine(null).build();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(tableJoinExample));
	}

	@Test
	void visitTableUnionSourceTest() {
		SourceTable tabl1 = SourceTable.builder()
				.table(Arrays.asList(
						Arrays.asList("a1", "b1", "c1"), 
						Arrays.asList("val1", "val2", "val3")))
				.build();
		SourceTable tabl2 = SourceTable.builder()
				.table(Arrays.asList(
						Arrays.asList("a1", "b2", "c2"), 
						Arrays.asList("v1", "v2", "v3")))
				.build();

		// standard
		List<List<String>> expectedUnion = Arrays.asList(
				Arrays.asList("a1", "b1", "c1"),
				Arrays.asList("val1", "val2", "val3"), 
				Arrays.asList("a1", "b2", "c2"),
				Arrays.asList("v1", "v2", "v3"));

		TableUnionSource tableUnionExample = TableUnionSource.builder().build();
		assertEquals(SourceTable.empty(), sourceVisitor.visit(tableUnionExample));

		tableUnionExample = TableUnionSource.builder().tables(Arrays.asList()).build();
		assertEquals(new ArrayList<>(), sourceVisitor.visit(tableUnionExample).getTable());

		tableUnionExample = TableUnionSource.builder()
				.tables(Arrays.asList("tab1", "tab2", "tab3"))
				.build();

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(tabl1).when(hostMonitoring).getSourceTableByKey("tab1");
		doReturn(tabl2).when(hostMonitoring).getSourceTableByKey("tab2");
		assertEquals(expectedUnion, sourceVisitor.visit(tableUnionExample).getTable());

	}

	@Test
	void visitTelnetInteractiveSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new TelnetInteractiveSource()));
	}

	@Test
	void visitUCSSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new UCSSource()));
	}

	@Test
	void visitWBEMSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new WBEMSource()));
	}

	@Test
	void visitWMISourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new WMISource()));
	}

}