package com.sentrysoftware.matrix.engine.strategy.detection;

import com.sentrysoftware.javax.wbem.WBEMException;
import com.sentrysoftware.matrix.connector.model.common.OSType;
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
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionVisitor.NamespaceResult;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionVisitor.PossibleNamespacesResult;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matsya.exceptions.WqlQuerySyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SEMICOLON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

	private static final String PUREM_SAN = "purem-san";
	private static final String FOO = "FOO";
	private static final String BAR = "BAR";
	private static final String BAZ = "BAZ";
	private static final String QUX = "QUX";
	private static final String QUUX = "QUUX";
	private static final String ROOT = "root";
	private static final String PC14 = "pc14";
	private static final String DEV_HV_01 = "dev-hv-01";

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@InjectMocks
	private CriterionVisitor criterionVisitor;

	private static EngineConfiguration engineConfiguration;
	private static IHostMonitoring hostMonitoring;

	private void initHTTP() {

		if (engineConfiguration != null
			&& engineConfiguration.getProtocolConfigurations().get(HTTPProtocol.class) != null) {

			return;
		}

		HTTPProtocol protocol = HTTPProtocol
			.builder()
			.port(443)
			.timeout(120L)
			.build();

		engineConfiguration = EngineConfiguration
			.builder()
			.target(HardwareTarget.builder().hostname(PUREM_SAN).id(PUREM_SAN).type(TargetType.LINUX).build())
			.protocolConfigurations(Map.of(HTTPProtocol.class, protocol))
			.build();
	}

	@Test
	void testVisitHTTPFailure() {

		// null HTTP
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit((HTTP) null));

		// HTTP is not null, protocol is null
		engineConfiguration = EngineConfiguration.builder().build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		HTTP http = new HTTP();
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(http));
		verify(strategyConfig).getEngineConfiguration();

		// HTTP is not null, protocol is not null, expectedResult is null, result is null
		initHTTP();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(null).when(matsyaClientsExecutor).executeHttp(any(), eq(false));
		CriterionTestResult criterionTestResult = criterionVisitor.visit(http);
		verify(strategyConfig, times(2)).getEngineConfiguration();
		verify(matsyaClientsExecutor).executeHttp(any(), eq(false));
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());

		// HTTP is not null, protocol is not null, expectedResult is null, result is empty
		doReturn(EMPTY).when(matsyaClientsExecutor).executeHttp(any(), eq(false));
		criterionTestResult = criterionVisitor.visit(http);
		verify(strategyConfig, times(3)).getEngineConfiguration();
		verify(matsyaClientsExecutor, times(2)).executeHttp(any(), eq(false));
		assertNotNull(criterionTestResult);
		assertEquals(EMPTY, criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());

		// HTTP is not null, protocol is not null, expectedResult is not null, result is null
		doReturn(null).when(matsyaClientsExecutor).executeHttp(any(), eq(false));
		http.setExpectedResult(FOO);
		criterionTestResult = criterionVisitor.visit(http);
		verify(strategyConfig, times(4)).getEngineConfiguration();
		verify(matsyaClientsExecutor, times(3)).executeHttp(any(), eq(false));
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());

		// HTTP is not null, protocol is not null, expectedResult is not null, result is not null and does not match
		doReturn(BAR).when(matsyaClientsExecutor).executeHttp(any(), eq(false));
		criterionTestResult = criterionVisitor.visit(http);
		verify(strategyConfig, times(5)).getEngineConfiguration();
		verify(matsyaClientsExecutor, times(4)).executeHttp(any(), eq(false));
		assertNotNull(criterionTestResult);
		assertEquals(BAR, criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());
	}

	@Test
	void testVisitHTTPSuccess() {

		initHTTP();
		HTTP http = new HTTP();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		// HTTP is not null, protocol is not null, expectedResult is null, result is neither null nor empty
		doReturn(FOO).when(matsyaClientsExecutor).executeHttp(any(), eq(false));
		CriterionTestResult criterionTestResult = criterionVisitor.visit(http);
		verify(strategyConfig).getEngineConfiguration();
		verify(matsyaClientsExecutor).executeHttp(any(), eq(false));
		assertNotNull(criterionTestResult);
		assertEquals(FOO, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());

		// HTTP is not null, protocol is not null, expectedResult is not null, result is not null and matches
		http.setExpectedResult(FOO);
		criterionTestResult = criterionVisitor.visit(http);
		verify(strategyConfig, times(2)).getEngineConfiguration();
		verify(matsyaClientsExecutor, times(2)).executeHttp(any(), eq(false));
		assertNotNull(criterionTestResult);
		assertEquals(FOO, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());
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
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		CriterionTestResult successfulTestResult = CriterionTestResult
				.builder()
				.message("Successful OS detection operation")
				.result("Configured OS Type : NT")
				.success(true)
				.build();

		CriterionTestResult failedTestResult = CriterionTestResult
				.builder()
				.message("Failed OS detection operation")
				.result("Configured OS Type : NT")
				.success(false)
				.build();

		OS os = OS.builder().exclude(Collections.EMPTY_SET).keepOnly(Collections.EMPTY_SET).build();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OSType.NT));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Collections.EMPTY_SET);
		os.setExclude(Set.of(OSType.NT));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));

		os.setExclude(Set.of(OSType.LINUX));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OSType.LINUX));
		os.setExclude(Collections.EMPTY_SET);
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));
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

	private void initSNMP() {

		if (engineConfiguration != null
			&& engineConfiguration.getProtocolConfigurations().get(SNMPProtocol.class) != null) {

			return;
		}

		SNMPProtocol protocol = SNMPProtocol
			.builder()
			.community("public")
			.version(SNMPVersion.V1)
			.port(161)
			.timeout(120L)
			.build();

		engineConfiguration = EngineConfiguration
			.builder()
			.target(HardwareTarget.builder().hostname(ECS1_01).id(ECS1_01).type(TargetType.LINUX).build())
			.protocolConfigurations(Map.of(SNMPProtocol.class, protocol))
			.build();
	}

	@Test
	void testVisitSNMPGetException() throws Exception {

		initSNMP();

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

		initSNMP();

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

		initSNMP();

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

		initSNMP();

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

		initSNMP();

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

		initSNMP();

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

		initSNMP();

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

	private void initWBEM() {

		if (engineConfiguration != null
			&& engineConfiguration.getProtocolConfigurations().get(WBEMProtocol.class) != null) {

			return;
		}

		WBEMProtocol protocol = WBEMProtocol
			.builder()
			.protocol(WBEMProtocol.WBEMProtocols.HTTPS)
			.port(5989)
			.timeout(120L)
			.namespace(QUX)
			.build();

		engineConfiguration = EngineConfiguration
			.builder()
			.target(HardwareTarget.builder().hostname(DEV_HV_01).id(DEV_HV_01).type(TargetType.MS_WINDOWS).build())
			.protocolConfigurations(Map.of(WBEMProtocol.class, protocol))
			.build();

		hostMonitoring = new HostMonitoring();
	}

	@Test
	void testVisitWBEMBadConfiguration() {

		// null WBEM
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit((WBEM) null));

		// WBEM is not null, query is null
		WBEM wbem = new WBEM();
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(wbem));

		// WBEM is not null, query is not null, protocol is null
		wbem.setWbemQuery(FOO);
		engineConfiguration = EngineConfiguration.builder().build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(wbem));
		verify(strategyConfig).getEngineConfiguration();
	}

	@Test
	void testVisitWBEM() throws WqlQuerySyntaxException, WBEMException, TimeoutException, InterruptedException,
		MalformedURLException {

		// No namespace found
		initWBEM();
		WBEM wbem = WBEM.builder().wbemQuery(FOO).wbemNamespace(AUTOMATIC).build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doThrow(WBEMException.class).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), eq(ROOT));
		CriterionTestResult criterionTestResult = criterionVisitor.visit(wbem);
		verify(strategyConfig).getEngineConfiguration();
		verify(strategyConfig, times(2)).getHostMonitoring();
		verify(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), eq(ROOT));
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());

		// Namespace found, query execution not necessary
		List<List<String>> queryResult = Collections.singletonList(Collections.singletonList(BAZ));
		doReturn(queryResult).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		criterionTestResult = criterionVisitor.visit(wbem);
		verify(strategyConfig, times(2)).getEngineConfiguration();
		verify(strategyConfig, times(5)).getHostMonitoring();
		verify(matsyaClientsExecutor, times(8)).executeWbem(any(), any(), any(), anyInt(), any(), any());
		assertNotNull(criterionTestResult);
		assertEquals(BAZ + SEMICOLON, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());

		// Namespace found, query execution throws exception
		wbem.setWbemNamespace(BAR);
		doThrow(WBEMException.class).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), eq(BAR));
		criterionTestResult = criterionVisitor.visit(wbem);
		verify(strategyConfig, times(3)).getEngineConfiguration();
		verify(strategyConfig, times(5)).getHostMonitoring();
		verify(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), eq(BAR));
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());

		// Namespace found, query execution completes
		doReturn(queryResult).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), eq(BAR));
		criterionTestResult = criterionVisitor.visit(wbem);
		verify(strategyConfig, times(4)).getEngineConfiguration();
		verify(strategyConfig, times(5)).getHostMonitoring();
		verify(matsyaClientsExecutor, times(2)).executeWbem(any(), any(), any(), anyInt(), any(), eq(BAR));
		assertNotNull(criterionTestResult);
		assertEquals(BAZ + SEMICOLON, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());
	}

	@Test
	void testVisitWBEMFindNamespace() throws WqlQuerySyntaxException, WBEMException, TimeoutException,
		InterruptedException, MalformedURLException {

		// WBEM's namespace is automatic, namespace has already detected
		initWBEM();
		WBEM wbem = WBEM.builder().wbemQuery(FOO).wbemNamespace(AUTOMATIC).build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		hostMonitoring.setAutomaticWbemNamespace(BAR);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doThrow(WBEMException.class).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		CriterionTestResult criterionTestResult = criterionVisitor.visit(wbem);
		verify(strategyConfig).getEngineConfiguration();
		verify(strategyConfig).getHostMonitoring();
		verify(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());

		// WBEM's namespace is null
		initWBEM();
		wbem.setWbemNamespace(null);
		doThrow(WBEMException.class).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		criterionTestResult = criterionVisitor.visit(wbem);
		verify(strategyConfig, times(2)).getEngineConfiguration();
		verify(strategyConfig).getHostMonitoring();
		verify(matsyaClientsExecutor, times(2)).executeWbem(any(), any(), any(), anyInt(), any(), any());
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());
	}

	@Test
	void testVisitWBEMDetectWbemNamespace() throws WqlQuerySyntaxException, WBEMException, TimeoutException,
		InterruptedException, MalformedURLException {

		// namespaces is empty
		initWBEM();
		WBEM wbem = WBEM.builder().wbemQuery(FOO).wbemNamespace(AUTOMATIC).build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		hostMonitoring.setAutomaticWbemNamespace(null);
		hostMonitoring.getPossibleWbemNamespaces().add(BAR);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		List<List<String>> queryResult = Collections.emptyList();
		doReturn(queryResult).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		CriterionTestResult criterionTestResult = criterionVisitor.visit(wbem);
		verify(strategyConfig).getEngineConfiguration();
		verify(strategyConfig, times(2)).getHostMonitoring();
		verify(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());

		// namespaces.size() > 1
		hostMonitoring.getPossibleWbemNamespaces().add(QUX);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		queryResult = Collections.singletonList(Collections.singletonList(QUUX));
		doReturn(queryResult).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		criterionTestResult = criterionVisitor.visit(wbem);
		verify(strategyConfig, times(2)).getEngineConfiguration();
		verify(strategyConfig, times(5)).getHostMonitoring();
		verify(matsyaClientsExecutor, times(3)).executeWbem(any(), any(), any(), anyInt(), any(), any());
		assertNotNull(criterionTestResult);
		assertEquals(QUUX + SEMICOLON, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());
	}

	@Test
	void testVisitWBEMDetectPossibleWbemNamespaces() throws WqlQuerySyntaxException, WBEMException, TimeoutException,
		InterruptedException, MalformedURLException {

		// "SELECT Name FROM __NAMESPACE" returned nothing
		initWBEM();
		WBEM wbem = WBEM.builder().wbemQuery(FOO).wbemNamespace(AUTOMATIC).build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		hostMonitoring.setAutomaticWbemNamespace(null);
		hostMonitoring.getPossibleWbemNamespaces().clear();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		List<List<String>> queryResult = Collections.emptyList();
		doReturn(queryResult).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		CriterionTestResult criterionTestResult = criterionVisitor.visit(wbem);
		verify(strategyConfig).getEngineConfiguration();
		verify(strategyConfig, times(2)).getHostMonitoring();
		verify(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());

		// "SELECT Name FROM __NAMESPACE" failed with WBEMException.CIM_ERR_INVALID_NAMESPACE
		WBEMException wbemException = new WBEMException(WBEMException.CIM_ERR_INVALID_NAMESPACE);
		doThrow(wbemException).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		criterionTestResult = criterionVisitor.visit(wbem);
		verify(matsyaClientsExecutor, times(7)).executeWbem(any(), any(), any(), anyInt(), any(), any());
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());

		// "SELECT Name FROM __NAMESPACE" failed with WBEMException.CIM_ERR_INVALID_CLASS
		wbemException = new WBEMException(WBEMException.CIM_ERR_INVALID_CLASS);
		doThrow(wbemException).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		criterionTestResult = criterionVisitor.visit(wbem);
		verify(matsyaClientsExecutor, times(13)).executeWbem(any(), any(), any(), anyInt(), any(), any());
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());

		// "SELECT Name FROM __NAMESPACE" failed with WBEMException.CIM_ERR_NOT_FOUND
		wbemException = new WBEMException(WBEMException.CIM_ERR_NOT_FOUND);
		doThrow(wbemException).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		criterionTestResult = criterionVisitor.visit(wbem);
		verify(matsyaClientsExecutor, times(19)).executeWbem(any(), any(), any(), anyInt(), any(), any());
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());

		// "SELECT Name FROM __NAMESPACE" threw a WqlQuerySyntaxException
		doThrow(WqlQuerySyntaxException.class).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		criterionTestResult = criterionVisitor.visit(wbem);
		verify(matsyaClientsExecutor, times(25)).executeWbem(any(), any(), any(), anyInt(), any(), any());
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());
	}

	@Test
	void testVisitWBEMExecuteWbemAndFilterNamespaces() throws WqlQuerySyntaxException, WBEMException, TimeoutException,
		InterruptedException, MalformedURLException {

		// Exception when running WBEM's query.
		initWBEM();
		WBEM wbem = WBEM.builder().wbemQuery(FOO).wbemNamespace(AUTOMATIC).build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		hostMonitoring.setAutomaticWbemNamespace(null);
		hostMonitoring.getPossibleWbemNamespaces().add(BAR);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doThrow(WBEMException.class).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		CriterionTestResult criterionTestResult = criterionVisitor.visit(wbem);
		verify(strategyConfig).getEngineConfiguration();
		verify(strategyConfig, times(2)).getHostMonitoring();
		verify(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		assertNotNull(criterionTestResult);
		assertNull(criterionTestResult.getResult());
		assertFalse(criterionTestResult.isSuccess());
	}

	@Test
	void testVisitSNMPGetNextException() throws Exception {

		initSNMP();

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

		initSNMP();

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

		initSNMP();

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

		initSNMP();

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

		initSNMP();

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

		initSNMP();

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

		initSNMP();

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

		initSNMP();

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

		initSNMP();

		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit((SNMPGetNext) null));
		assertEquals(CriterionTestResult.empty(),
				new CriterionVisitor().visit(SNMPGetNext.builder().oid(null).build()));
		doReturn(new EngineConfiguration()).when(strategyConfig).getEngineConfiguration();
		assertEquals(CriterionTestResult.empty(),
				criterionVisitor.visit(SNMPGetNext.builder().oid(OID).build()));
	}

	@Test
	void testExtractPossibleNamespaces() {
		final Set<String> result = CriterionVisitor
			.extractPossibleNamespaces(
				Arrays.asList(
					Collections.emptyList(),
					Collections.singletonList("hpq"),
					Collections.singletonList("interop"),
					Collections.singletonList("SECURITY")),
				Collections.singleton("SECURITY"),
				"root\\"
			);

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
