package com.sentrysoftware.matrix.engine.strategy.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.connector.model.detection.criteria.http.HTTP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.detection.criteria.kmversion.KMVersion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.OS;
import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OSCommand;
import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;
import com.sentrysoftware.matrix.connector.model.detection.criteria.service.Service;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.model.detection.criteria.telnet.TelnetInteractive;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ucs.UCS;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.WMI;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionVisitor.NamespaceResult;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionVisitor.PossibleNamespacesResult;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@ExtendWith(MockitoExtension.class)
class CriterionVisitorTest {

	private static final String AUTOMATIC = "Automatic";
	private static final String ROOT_HPQ_NAMESPACE = "root\\hpq";
	private static final String NAMESPACE_WMI_QUERY = "SELECT Name FROM __NAMESPACE";
	private static final String WMI_WQL = "SELECT Version FROM IBMPSG_UniversalManageabilityServices";
	private static final String UCS_EXPECTED = "UCS";
	private static final String UCS_SYSTEM_CISCO_RESULT = "UCS System Cisco";
	private static final String RESULT_4 = "1.3.6.1.4.1.674.10893.1.20.1 ASN_OCT";
	private static final String RESULT_3 = "1.3.6.1.4.1.674.10893.1.20.1 ASN_OCT 2.4.6";
	private static final String RESULT_2 = "1.3.6.1.4.1.674.10893.1.20.1 ASN_INTEGER 1";
	private static final String RESULT_1 = "1.3.6.1.4.1.674.99999.1.20.1 ASN_INTEGER 1";
	private static final String ECS1_01 = "ecs1-01";
	private static final String VERSION = "2.4.6";
	private static final String EMPTY = "";
	private static final String OID = "1.3.6.1.4.1.674.10893.1.20";

	private static final String PC14 = "pc14";

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@InjectMocks
	private CriterionVisitor criterionVisitor;

	private static EngineConfiguration engineConfiguration;

	@BeforeAll
	public static void setUp() {
		SNMPProtocol protocol = SNMPProtocol.builder().community("public").version(SNMPVersion.V1).port(161).timeout(120L).build();
		engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(ECS1_01).id(ECS1_01).type(TargetType.LINUX).build())
				.protocolConfigurations(Map.of(SNMPProtocol.class, protocol)).build();
		
	}

	@Test
	void testVisitHTTP() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new HTTP()));
	}

	@Test
	void testVisitIPMI() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new IPMI()));
	}

	@Test
	void testVisitKMVersion() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new KMVersion()));
	}

	@Test
	void testVisitOS() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new OS()));
	}

	@Test
	void testVisitOSCommand() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new OSCommand()));
	}

	@Test
	void testVisitProcess() {
		;
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new Process()));
	}

	@Test
	void testVisitService() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new Service()));
	}

	@Test
	void testVisitSNMPGetException() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doThrow(new TimeoutException("SNMPGet timeout")).when(matsyaClientsExecutor).executeSNMPGet(any(),
				any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGet.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"SNMP Test Failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01 was unsuccessful due to an exception. Message: SNMPGet timeout.")
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNullResult() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(null).when(matsyaClientsExecutor).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGet.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"SNMP Test Failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01 was unsuccessful due to a null result.")
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetEmptyResult() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(EMPTY).when(matsyaClientsExecutor).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGet.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"SNMP Test Failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01 was unsuccessful due to an empty result.")
				.result(EMPTY).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetSuccessWithNoExpectedResult() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(UCS_SYSTEM_CISCO_RESULT).when(matsyaClientsExecutor).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGet.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Successful SNMP Get of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01. Returned Result: UCS System Cisco.")
				.result(UCS_SYSTEM_CISCO_RESULT)
				.success(true).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetExpectedResultNotMatches() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(UCS_SYSTEM_CISCO_RESULT).when(matsyaClientsExecutor).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGet.builder().oid(OID).expectedResult(VERSION).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"SNMP Test Failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01 was successful but the value of the returned OID did not match with the expected result. Expected value: 2.4.6 - returned value UCS System Cisco.")
				.result(UCS_SYSTEM_CISCO_RESULT)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetExpectedResultMatches() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(UCS_SYSTEM_CISCO_RESULT).when(matsyaClientsExecutor).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGet.builder().oid(OID).expectedResult(UCS_EXPECTED).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Successful SNMP Get of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01. Returned Result: UCS System Cisco.")
				.result(UCS_SYSTEM_CISCO_RESULT)
				.success(true)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetReturnsEmptyResult() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit((SNMPGet) null));
		assertEquals(CriterionTestResult.empty(),
				new CriterionVisitor().visit(SNMPGet.builder().oid(null).build()));
		doReturn(new EngineConfiguration()).when(strategyConfig).getEngineConfiguration();
		assertEquals(CriterionTestResult.empty(),
				criterionVisitor.visit(SNMPGet.builder().oid(OID).build()));
	}

	@Test
	void testVisitTelnetInteractive() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new TelnetInteractive()));
	}

	@Test
	void testVisitUCS() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new UCS()));
	}

	@Test
	void testVisitWBEM() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new WBEM()));
	}

	@Test
	void testVisitWMI() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new WMI()));
	}

	@Test
	void testVisitSNMPGetNextException() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doThrow(new TimeoutException("SNMPGetNext timeout")).when(matsyaClientsExecutor).executeSNMPGetNext(any(),
				any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGetNext.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"SNMP Test Failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01 was unsuccessful due to an exception. Message: SNMPGetNext timeout.")
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextNullResult() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(null).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGetNext.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"SNMP Test Failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01 was unsuccessful due to a null result.")
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextEmptyResult() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(EMPTY).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGetNext.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"SNMP Test Failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01 was unsuccessful due to an empty result.")
				.result(EMPTY).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextNotSameSubTreeOID() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(RESULT_1).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGetNext.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"SNMP Test Failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01 was successful but the returned OID is not under the same tree. Returned OID: 1.3.6.1.4.1.674.99999.1.20.1.")
				.result(RESULT_1).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextSuccessWithNoExpectedResult() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(RESULT_2).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGetNext.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Successful SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01. Returned Result: 1.3.6.1.4.1.674.10893.1.20.1 ASN_INTEGER 1.")
				.result(RESULT_2)
				.success(true).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextExpectedResultNotMatches() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(RESULT_2).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGetNext.builder().oid(OID).expectedResult(VERSION).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"SNMP Test Failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01 was successful but the value of the returned OID did not match with the expected result. Expected value: 2.4.6 - returned value 1.")
				.result(RESULT_2)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextExpectedResultMatches() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(RESULT_3).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGetNext.builder().oid(OID).expectedResult(VERSION).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Successful SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01. Returned Result: 1.3.6.1.4.1.674.10893.1.20.1 ASN_OCT 2.4.6.")
				.result(RESULT_3)
				.success(true)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextExpectedResultCannotExtract() throws Exception {
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(RESULT_4).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SNMPGetNext.builder().oid(OID).expectedResult(VERSION).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"SNMP Test Failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 on ecs1-01 was successful but the value cannot be extracted. Returned Result: 1.3.6.1.4.1.674.10893.1.20.1 ASN_OCT.")
				.result(RESULT_4)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextReturnsEmptyResult() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit((SNMPGetNext) null));
		assertEquals(CriterionTestResult.empty(),
				new CriterionVisitor().visit(SNMPGetNext.builder().oid(null).build()));
		doReturn(new EngineConfiguration()).when(strategyConfig).getEngineConfiguration();
		assertEquals(CriterionTestResult.empty(),
				criterionVisitor.visit(SNMPGetNext.builder().oid(OID).build()));
	}

	@Test
	void testExtractPossibleNamespaces() {
		final Set<String> result = CriterionVisitor.extractPossibleNamespaces(Arrays.asList(
				Collections.emptyList(),
				Collections.singletonList("hpq"),
				Collections.singletonList("interop"),
				Collections.singletonList("SECURITY")));

		assertEquals(Set.of(ROOT_HPQ_NAMESPACE), result);

	}

	@Test
	void testDetectPossibleWmiNamespacesAlreadyDetected() {
		final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).build();
		final WMIProtocol protocol = WMIProtocol.builder()
				.username(PC14 + "\\" + "Administrator")
				.password("password".toCharArray())
				.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.getPossibleWmiNamespaces().add(ROOT_HPQ_NAMESPACE);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		assertEquals(PossibleNamespacesResult.builder().possibleNamespaces(Set.of(ROOT_HPQ_NAMESPACE)).success(true).build(),
				criterionVisitor.detectPossibleWmiNamespaces(wmi, protocol));

	}

	@Test 
	void testDetectPossibleWmiNamespaces() throws Exception {
		final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).build();
		final WMIProtocol protocol = WMIProtocol.builder()
				.username(PC14 + "\\" + "Administrator")
				.password("password".toCharArray())
				.build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WMIProtocol.class, protocol))
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final List<List<String>> wqlResult = Arrays.asList(List.of("hpq"), List.of("SECURITY"), List.of("Cli"));

		doReturn(wqlResult).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator", "password".toCharArray(),
				protocol.getTimeout(), NAMESPACE_WMI_QUERY, "root");

		final PossibleNamespacesResult actual = criterionVisitor.detectPossibleWmiNamespaces(wmi, protocol);
		final PossibleNamespacesResult expected = PossibleNamespacesResult.builder()
				.possibleNamespaces(Set.of(ROOT_HPQ_NAMESPACE))
				.success(true)
				.build();

		assertEquals(expected, actual);
	}

	@Test 
	void testDetectPossibleWmiNamespacesException() throws Exception {
		final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).build();
		final WMIProtocol protocol = WMIProtocol.builder()
				.username(PC14 + "\\" + "Administrator")
				.password("password".toCharArray())
				.build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WMIProtocol.class, protocol))
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		doThrow(new TimeoutException()).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator", "password".toCharArray(),
				protocol.getTimeout(), NAMESPACE_WMI_QUERY, "root");

		final PossibleNamespacesResult actual = criterionVisitor.detectPossibleWmiNamespaces(wmi, protocol);

		assertFalse(actual.isSuccess());
	}

	@Test 
	void testDetectPossibleWmiNamespacesEmpty() throws Exception {
		final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).build();
		final WMIProtocol protocol = WMIProtocol.builder()
				.username(PC14 + "\\" + "Administrator")
				.password("password".toCharArray())
				.build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WMIProtocol.class, protocol))
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		doReturn(Collections.emptyList()).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator", "password".toCharArray(),
				protocol.getTimeout(), NAMESPACE_WMI_QUERY, "root");

		final PossibleNamespacesResult actual = criterionVisitor.detectPossibleWmiNamespaces(wmi, protocol);

		assertFalse(actual.isSuccess());
	}

	@Test
	void testFindNamespaceAutomaticAlreadyDetected() {
		final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).wbemNamespace(AUTOMATIC).build();
		final WMIProtocol protocol = WMIProtocol.builder()
				.username(PC14 + "\\" + "Administrator")
				.password("password".toCharArray())
				.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setAutomaticWmiNamespace(ROOT_HPQ_NAMESPACE);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		final NamespaceResult result = criterionVisitor.findNamespace(wmi, protocol);
		assertTrue(result.isSuccess());
		assertEquals(ROOT_HPQ_NAMESPACE, result.getNamespace());
	}

	@Test
	void testFindNamespaceNotAutomatic() {
		{
			final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).wbemNamespace(ROOT_HPQ_NAMESPACE).build();
			final WMIProtocol protocol = WMIProtocol.builder()
					.username(PC14 + "\\" + "Administrator")
					.password("password".toCharArray())
					.build();

			final NamespaceResult result = criterionVisitor.findNamespace(wmi, protocol);
			assertTrue(result.isSuccess());
			assertEquals(ROOT_HPQ_NAMESPACE, result.getNamespace());
		}

		{
			final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).build();
			final WMIProtocol protocol = WMIProtocol.builder()
					.username(PC14 + "\\" + "Administrator")
					.password("password".toCharArray())
					.build();

			final NamespaceResult result = criterionVisitor.findNamespace(wmi, protocol);
			assertTrue(result.isSuccess());
			assertEquals("root/cimv2", result.getNamespace());
		}
	}

	@Test
	void testFindNamespaceAutomatic() throws Exception {

		{
			final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).wbemNamespace(AUTOMATIC).expectedResult("^ibm.*$").build();
			final WMIProtocol protocol = WMIProtocol.builder()
					.username(PC14 + "\\" + "Administrator")
					.password("password".toCharArray())
					.build();
			final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
					.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
					.protocolConfigurations(Map.of(WMIProtocol.class, protocol))
					.build();
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

			doReturn(Arrays.asList(List.of("ibmsd"),
					List.of("cimv2"),
					List.of("ibm"),
					List.of("ibm2"))).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator", "password".toCharArray(),
					protocol.getTimeout(), NAMESPACE_WMI_QUERY, "root");

			doReturn(List.of(List.of("ibm system version 1.0.00"))).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator", "password".toCharArray(),
					protocol.getTimeout(), WMI_WQL, "root\\ibmsd");
			doReturn(Collections.emptyList()).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator", "password".toCharArray(),
					protocol.getTimeout(), WMI_WQL, "root\\ibm2");
			doReturn(List.of(List.of("ibm system version 1.0.00"))).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator", "password".toCharArray(),
					protocol.getTimeout(), WMI_WQL, "root\\cimv2");
			doThrow(new TimeoutException("Test timeout exception")).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator", "password".toCharArray(),
					protocol.getTimeout(), WMI_WQL, "root\\ibm");
			final NamespaceResult result = criterionVisitor.findNamespace(wmi, protocol);

			assertTrue(result.isSuccess());
			assertEquals("root\\ibmsd", result.getNamespace());
			assertEquals("root\\ibmsd", hostMonitoring.getAutomaticWmiNamespace());
		}

		{
			final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).wbemNamespace(AUTOMATIC).expectedResult(null).build();
			final WMIProtocol protocol = WMIProtocol.builder()
					.username(PC14 + "\\" + "Administrator")
					.password("password".toCharArray())
					.build();
			final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
					.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
					.protocolConfigurations(Map.of(WMIProtocol.class, protocol))
					.build();
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

			doReturn(List.of(List.of("ibmsd"))).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator", "password".toCharArray(),
					protocol.getTimeout(), NAMESPACE_WMI_QUERY, "root");

			final NamespaceResult result = criterionVisitor.findNamespace(wmi, protocol);

			assertTrue(result.isSuccess());
			assertEquals("root\\ibmsd", result.getNamespace());
			assertEquals("root\\ibmsd", hostMonitoring.getAutomaticWmiNamespace());
		}
	}

	@Test
	void testVisitWmiBadCriterion() {
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(WMI.builder()
				.wbemNamespace(AUTOMATIC)
				.expectedResult(null)
				.build()));
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit((WMI) null));
	}

	@Test
	void testVisitWmiNoProtocol() {
		final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).wbemNamespace(AUTOMATIC).expectedResult(null).build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(SNMPProtocol.class, new SNMPProtocol()))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(wmi));
	}

	@Test
	void testVisitWmiCannotDetectNamespace() throws Exception {
		{
			final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).wbemNamespace(AUTOMATIC).expectedResult("^ibm.*$").build();
			final WMIProtocol protocol = WMIProtocol.builder()
					.username(PC14 + "\\" + "Administrator")
					.password("password".toCharArray())
					.build();
			final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
					.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
					.protocolConfigurations(Map.of(WMIProtocol.class, protocol))
					.build();

			doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
			doReturn(Collections.emptyList()).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator",
					"password".toCharArray(),
					protocol.getTimeout(),
					NAMESPACE_WMI_QUERY,
					"root");

			assertFalse(criterionVisitor.visit(wmi).isSuccess());
		}

		{
			final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).wbemNamespace(AUTOMATIC).expectedResult(null).build();
			final WMIProtocol protocol = WMIProtocol.builder()
					.username(PC14 + "\\" + "Administrator")
					.password("password".toCharArray())
					.build();
			final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
					.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
					.protocolConfigurations(Map.of(WMIProtocol.class, protocol))
					.build();
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

			doReturn(List.of(List.of("ibmsd"))).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator",
					"password".toCharArray(),
					protocol.getTimeout(),
					NAMESPACE_WMI_QUERY,
					"root");

			// Expected doesn't matches
			doReturn(Collections.emptyList()).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator",
					"password".toCharArray(),
					protocol.getTimeout(),
					WMI_WQL,
					"root\\ibmsd");

			assertFalse(criterionVisitor.visit(wmi).isSuccess());
		}
	}

	@Test
	void testVisitWmi() throws Exception {
		WMI wmi = WMI.builder().wbemQuery(WMI_WQL).wbemNamespace(AUTOMATIC).expectedResult("^ibm.*$").build();
		final WMIProtocol protocol = WMIProtocol.builder()
				.username(PC14 + "\\" + "Administrator")
				.password("password".toCharArray())
				.build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WMIProtocol.class, protocol))
				.build();
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		doReturn(Arrays.asList(List.of("ibmsd"),
				List.of("cimv2"),
				List.of("ibm"),
				List.of("ibm2"))).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator",
						"password".toCharArray(),
						protocol.getTimeout(),
						NAMESPACE_WMI_QUERY,
						"root");

		doReturn(List.of(List.of("ibm system version 1.0.00"),
				List.of("controller version 8.8.00"))).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator", "password".toCharArray(),
				protocol.getTimeout(), WMI_WQL, "root\\ibmsd");

		assertTrue(criterionVisitor.visit(wmi).isSuccess());

		wmi = WMI.builder().wbemQuery(WMI_WQL).wbemNamespace(AUTOMATIC).expectedResult(null).build();
		assertTrue(criterionVisitor.visit(wmi).isSuccess());
	}

	@Test
	void testVisitWmiResultNotMatched() throws Exception {
		final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).wbemNamespace(AUTOMATIC).expectedResult("^ibm.*$").build();
		final WMIProtocol protocol = WMIProtocol.builder()
				.username(PC14 + "\\" + "Administrator")
				.password("password".toCharArray())
				.build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(WMIProtocol.class, protocol))
				.build();
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setAutomaticWmiNamespace("root\\ibmsd");
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		doReturn(List.of(List.of("hp system version 1.0.00"),
				List.of("controller version 8.8.00"))).when(matsyaClientsExecutor)
		.executeWmi(PC14, PC14 + "\\" + "Administrator",
				"password".toCharArray(),
				protocol.getTimeout(),
				WMI_WQL,
				"root\\ibmsd");
		assertFalse(criterionVisitor.visit(wmi).isSuccess());

		doReturn(Collections.emptyList()).when(matsyaClientsExecutor)
		.executeWmi(PC14, PC14 + "\\" + "Administrator",
				"password".toCharArray(),
				protocol.getTimeout(),
				WMI_WQL,
				"root\\ibmsd");
		assertFalse(criterionVisitor.visit(wmi).isSuccess());

		// Exception
		doThrow(new RuntimeException("Test exception")).when(matsyaClientsExecutor).executeWmi(PC14, PC14 + "\\" + "Administrator",
				"password".toCharArray(),
				protocol.getTimeout(),
				WMI_WQL,
				"root\\ibmsd");

		assertFalse(criterionVisitor.visit(wmi).isSuccess());
	}


}
