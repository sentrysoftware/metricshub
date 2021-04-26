package com.sentrysoftware.matrix.engine.strategy.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
public class SourceVisitorTest {
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
	public void visitHTTPSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new HTTPSource()));
	}

	@Test
	public void visitIPMISourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new IPMI()));
	}

	@Test
	public void visitOSCommandSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new OSCommandSource()));
	}

	@Test
	public void visitReferenceSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new ReferenceSource()));
	}

	@Test
	public void visitSNMPGetSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new SNMPGetSource()));
	}

	@Test
	void testVisitSNMPGetTableNullArgs() throws Exception {
		assertEquals(SourceTable.empty(), sourceVisitor.visit( SNMPGetTableSource.builder().oid(null).snmpTableSelectColumns(null).build()));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SNMPGetTableSource.builder().snmpTableSelectColumns(SNMP_SELECTED_COLUMNS).build()));
		assertEquals(SourceTable.empty(), sourceVisitor.visit(SNMPGetTableSource.builder().oid(OID).build()));
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
	public void visitTableJoinSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new TableJoinSource()));
	}

	@Test
	public void visitTableUnionSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new TableUnionSource()));
	}

	@Test
	public void visitTelnetInteractiveSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new TelnetInteractiveSource()));
	}

	@Test
	public void visitUCSSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new UCSSource()));
	}

	@Test
	public void visitWBEMSourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new WBEMSource()));
	}

	@Test
	public void visitWMISourceTest () {
		assertEquals(SourceTable.empty(), new SourceVisitor().visit(new WMISource()));
	}
}
