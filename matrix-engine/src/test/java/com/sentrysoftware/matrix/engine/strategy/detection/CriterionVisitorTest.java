package com.sentrysoftware.matrix.engine.strategy.detection;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SEMICOLON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.javax.wbem.WBEMException;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler;
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
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.OSCommandConfig;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionVisitor.NamespaceResult;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionVisitor.PossibleNamespacesResult;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matsya.exceptions.WqlQuerySyntaxException;
import com.sentrysoftware.matsya.wmi.exceptions.WmiComException;


@ExtendWith(MockitoExtension.class)
class CriterionVisitorTest {

	private static final String MANAGEMENT_CARD_HOST = "management-card-host";
	private static final String HOST_LINUX = "host-linux";
	private static final String HOST_WIN = "host-win";
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

	private static final String USERNAME = "username";
	private static final char[] PASSWORD = "password".toCharArray();
	private static final Long TIME_OUT = 120L;
	private static final String DEV_HV_01 = "dev-hv-01";

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@InjectMocks
	private CriterionVisitor criterionVisitor;

	@InjectMocks
	@Spy
	private CriterionVisitor criterionVisitorSpy;

	private static EngineConfiguration engineConfiguration;
	private static IHostMonitoring hostMonitoring;

	private void initHTTP() {

		if (engineConfiguration != null
				&& engineConfiguration.getProtocolConfigurations().get(HTTPProtocol.class) != null) {

			return;
		}

		final HTTPProtocol protocol = HTTPProtocol
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
		final HTTP http = new HTTP();
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
		final HTTP http = new HTTP();
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
	void testVisitIPMIWindowsFailure() throws Exception {
		// No WMI protocol
		final Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations = new HashMap<>();
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(HOST_WIN)
						.id(HOST_WIN)
						.type(TargetType.MS_WINDOWS)
						.build())
				.protocolConfigurations(protocolConfigurations)
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final IPMI ipmi = IPMI.builder().forceSerialization(true).build();
		CriterionTestResult criterionTestResult = criterionVisitor.visit(ipmi);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals("No WMI credentials provided.", criterionTestResult.getMessage());

		// matsyaClientsExecutor gives null result to WMI request
		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.namespace(HOST_WIN)
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();
		protocolConfigurations.put(wmiProtocol.getClass(), wmiProtocol);

		doReturn(null).when(matsyaClientsExecutor).executeWmi(HOST_WIN,
				USERNAME,
				PASSWORD,
				TIME_OUT,
				"SELECT Description FROM ComputerSystem",
				"root/hardware");

		criterionTestResult = criterionVisitor.visit(ipmi);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals("The Microsoft IPMI WMI provider did not report the presence of any BMC controller.", criterionTestResult.getMessage());

		// Exception thrown by matsyaClientsExecutor when executing the WMI request
		doThrow(new WmiComException("Unit test error")).when(matsyaClientsExecutor).executeWmi(HOST_WIN,
				USERNAME,
				PASSWORD,
				TIME_OUT,
				"SELECT Description FROM ComputerSystem",
				"root/hardware");

		criterionTestResult = criterionVisitor.visit(ipmi);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals("Ipmi Test Failed - WMI request was unsuccessful due to an exception. Message: Unit test error.", criterionTestResult.getMessage());

	}

	@Test
	void testVisitIPMIWindowsSuccess() throws Exception {
		final Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations = new HashMap<>();
		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.namespace(HOST_WIN)
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();
		protocolConfigurations.put(wmiProtocol.getClass(), wmiProtocol);
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(HOST_WIN)
						.id(HOST_WIN)
						.type(TargetType.MS_WINDOWS)
						.build())
				.protocolConfigurations(protocolConfigurations)
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final String resultWmi = "System description";
		final String resultFinal = "System description;";

		doReturn(Collections.singletonList(Collections.singletonList(resultWmi))).when(matsyaClientsExecutor).executeWmi(HOST_WIN,
				USERNAME,
				PASSWORD,
				TIME_OUT,
				"SELECT Description FROM ComputerSystem",
				"root/hardware");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(IPMI.builder().forceSerialization(true).build());

		assertNotNull(criterionTestResult);
		assertEquals(resultFinal, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());
	}


	@Test
	@EnabledOnOs(WINDOWS)
	void testRunOsCommandWindows() throws InterruptedException, IOException {
		HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);
		String version = OsCommandHelper.runLocalCommand("ver");
		assertTrue(version.startsWith("Microsoft Windows"));
	}

	@Test
	@EnabledOnOs(LINUX)
	void testRunOsCommandLinux() throws InterruptedException, IOException {
		HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);
		String version = OsCommandHelper.runLocalCommand("uname -a");
		assertTrue(version.startsWith("Linux"));
	}

	@Test
	void testVisitIPMILinux() throws IOException, InterruptedException {
		// osConfig null
		{
			final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
					.target(HardwareTarget.builder().hostname(HOST_LINUX).id(HOST_LINUX).type(TargetType.LINUX).build())

					.build();

			final HostMonitoring hostMonitoring = new HostMonitoring();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
			assertEquals(CriterionTestResult.builder().result("").success(false)
					.message("No OS Command Configuration for " + HOST_LINUX + ". Retrun empty result.").build(),
					criterionVisitor.visit(new IPMI()));
		}
		final SSHProtocol ssh = SSHProtocol.sshProtocolBuilder().username("root").password("nationale".toCharArray()).build();
		{
			// wrong IPMIToolCommand
			final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
					.target(HardwareTarget.builder().hostname(HOST_LINUX).id(HOST_LINUX).type(TargetType.LINUX).build())
					.protocolConfigurations(Map.of(HTTPProtocol.class, OSCommandConfig.builder().build(),
							OSCommandConfig.class, OSCommandConfig.builder().build(),
							SSHProtocol.class, ssh))
					.build();

			final HostMonitoring hostMonitoring = new HostMonitoring();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
			doReturn("blabla").when(criterionVisitorSpy).buildIpmiCommand(eq(TargetType.LINUX), any(), any(), any(),
					eq(120));
			assertFalse(criterionVisitorSpy.visit(new IPMI()).isSuccess());

		}
		{
			// wrong result when running IPMIToolCommand
			final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
					.target(HardwareTarget.builder().hostname(HOST_LINUX).id(HOST_LINUX).type(TargetType.LINUX).build())
					.protocolConfigurations(Map.of(HTTPProtocol.class, OSCommandConfig.builder().build(),
							OSCommandConfig.class, OSCommandConfig.builder().build(),
							SSHProtocol.class, ssh))
					.build();

			final HostMonitoring hostMonitoring = new HostMonitoring();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

			doReturn("PATH=blabla").when(criterionVisitorSpy).buildIpmiCommand(eq(TargetType.LINUX), any(), any(),
					any(), eq(120));

			doReturn("wrong result").when(criterionVisitorSpy).runOsCommand("PATH=blabla", HOST_LINUX, ssh, 120);
			assertFalse(criterionVisitorSpy.visit(new IPMI()).isSuccess());

		}

		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(HOST_LINUX).id(HOST_LINUX).type(TargetType.LINUX).build())
				.protocolConfigurations(Map.of(HTTPProtocol.class, OSCommandConfig.builder().build(),
						OSCommandConfig.class, OSCommandConfig.builder().build(),
						SSHProtocol.class, ssh))
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		// set ssh
		final String ipmiResultExample = "Device ID                 : 3\r\n" + "Device Revision           : 3\r\n"
				+ "Firmware Revision         : 4.10\r\n" + "IPMI Version              : 2.0\r\n"
				+ "Manufacturer ID           : 10368\r\n" + "Manufacturer Name         : Fujitsu Siemens\r\n"
				+ "Product ID                : 790 (0x0316)\r\n" + "Product Name              : Unknown (0x316)";
		
//		try (MockedStatic<MatsyaClientsExecutor> oscmd = mockStatic(MatsyaClientsExecutor.class)) {
//			oscmd.when(() -> MatsyaClientsExecutor.runRemoteSshCommand(any(), any(), any(), any(), any(),
//					anyInt())).thenReturn(ipmiResultExample);
//
//			assertEquals(CriterionTestResult.builder().result(ipmiResultExample).success(true)
//					.message("Successfully connected to the IPMI BMC chip with the in-band driver interface.").build(),
//					criterionVisitor.visit(new IPMI()));
//		}
		doReturn(ipmiResultExample).when(matsyaClientsExecutor).runRemoteSshCommand(any(), any(), any(), any(), any(),
				 				anyInt());
		assertEquals(CriterionTestResult.builder().result(ipmiResultExample).success(true)
				 				.message("Successfully connected to the IPMI BMC chip with the in-band driver interface.").build(),
				 				criterionVisitor.visit(new IPMI()));

		// run localhost command
		final EngineConfiguration engineConfigurationLocal = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname("localhost").id("localhost").type(TargetType.SUN_SOLARIS)
						.build())
				.protocolConfigurations(Map.of(HTTPProtocol.class, OSCommandConfig.builder().build(),
						OSCommandConfig.class, OSCommandConfig.builder().build(),
						SSHProtocol.class, ssh))
				.build();
		hostMonitoring.setLocalhost(true);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfigurationLocal).when(strategyConfig).getEngineConfiguration();
		// here the try is important because it only will mock the static reference for
		// the following context.
		// Otherwise even for other contexts/methods it will always return the same
		// result (it is static..)
		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd.when(() -> OsCommandHelper.runLocalCommand(any())).thenReturn(ipmiResultExample);
			assertEquals(CriterionTestResult.builder().result(ipmiResultExample).success(true)
					.message("Successfully connected to the IPMI BMC chip with the in-band driver interface.").build(),
					criterionVisitor.visit(new IPMI()));
		}

	}

	@Test
	void testRunOsCommand() throws InterruptedException, IOException {
		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(false);
		final SSHProtocol ssh = SSHProtocol.sshProtocolBuilder().username("root").password("nationale".toCharArray()).build();

		String cmdResult = OsCommandHelper.runLocalCommand(null);
		assertNull(cmdResult);
		
		cmdResult = OsCommandHelper.runSshCommand(null, "localhost", ssh, 120, matsyaClientsExecutor);
		assertNull(cmdResult);
		
		cmdResult = OsCommandHelper.runSshCommand("cmd", "localhost", null, 120, matsyaClientsExecutor);
		assertNull(cmdResult);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn("something").when(matsyaClientsExecutor).runRemoteSshCommand(any(), any(), any(), any(), any(),
				anyInt());
		cmdResult = criterionVisitor.runOsCommand("cmd", "localhost", ssh, 120);
		assertEquals("something", cmdResult);

	}

	@Test
	void testBuildIpmiCommand() {
		final SSHProtocol ssh = SSHProtocol.sshProtocolBuilder().username("root").password("nationale".toCharArray()).build();
		final OSCommandConfig osCommandConfig = OSCommandConfig.builder().build();
		{
			// test Solaris
			final HostMonitoring hostMonitoring = new HostMonitoring();
			hostMonitoring.setLocalhost(true);
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
				oscmd.when(() -> OsCommandHelper.runLocalCommand(any())).thenReturn("5.10");
				final String cmdResult = criterionVisitor.buildIpmiCommand(TargetType.SUN_SOLARIS, "toto", ssh,
						osCommandConfig, 120);
				assertNotNull(cmdResult);
				assertTrue(cmdResult.startsWith("PATH")); // Successful command
			}

			try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
				oscmd.when(() -> OsCommandHelper.runLocalCommand(any())).thenReturn("blabla");
				final String cmdResult = criterionVisitor.buildIpmiCommand(TargetType.SUN_SOLARIS, "toto", ssh,
						osCommandConfig, 120);
				assertNotNull(cmdResult);
				assertTrue(cmdResult.startsWith("Couldn't")); // Not Successful command the response starts with
				// Couldn't identify
			}

			try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
				osCommandConfig.setUseSudo(true);
				oscmd.when(() -> OsCommandHelper.runLocalCommand(any())).thenReturn("5.10");
				final String cmdResult = criterionVisitor.buildIpmiCommand(TargetType.SUN_SOLARIS, "toto", ssh,
						osCommandConfig, 120);
				assertNotNull(cmdResult);
				assertTrue(cmdResult.contains("sudo")); // Successful sudo command
			}
		}

		{
			// test Linux
			osCommandConfig.setUseSudo(false);
			final String cmdResult = criterionVisitor.buildIpmiCommand(TargetType.LINUX, "toto", ssh, osCommandConfig, 120);
			assertEquals("PATH=$PATH:/usr/local/bin:/usr/sfw/bin;export PATH;ipmitool -I open bmc info", cmdResult);
		}

	}

	@Test
	void testGetIpmiCommandForSolaris() throws Exception {
		final String ipmitoolCommand = "ipmitoolCommand ";
		{ // Solaris Version 10 => bmc
			final String cmdResult = criterionVisitor.getIpmiCommandForSolaris(ipmitoolCommand, "toto", "5.10");
			assertEquals("ipmitoolCommand bmc", cmdResult);
		}
		{ // Solaris version 9 => lipmi
			final String cmdResult = criterionVisitor.getIpmiCommandForSolaris(ipmitoolCommand, "toto", "5.9");
			assertEquals("ipmitoolCommand lipmi", cmdResult);
		}


		{// wrong String OS version
			final Exception exception = assertThrows(Exception.class, () -> {
				criterionVisitor.getIpmiCommandForSolaris(ipmitoolCommand, "toto", "blabla");
			});

			final String expectedMessage = "Unkown Solaris version";
			final String actualMessage = exception.getMessage();

			assertTrue(actualMessage.contains(expectedMessage));
		}
		{// old OS version
			final Exception exception = assertThrows(Exception.class, () -> {
				criterionVisitor.getIpmiCommandForSolaris(ipmitoolCommand, "toto", "4.1.1B");
			});

			final String expectedMessage = "Solaris version (4.1.1B) is too old";
			final String actualMessage = exception.getMessage();

			assertTrue(actualMessage.contains(expectedMessage));
		}
	}

	@Test
	void testVisitIPMIOutOfBandConfigurationNotFound() {
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(MANAGEMENT_CARD_HOST)
						.id(MANAGEMENT_CARD_HOST)
						.type(TargetType.MGMT_CARD_BLADE_ESXI)
						.build())
				.protocolConfigurations(Collections.emptyMap())
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(new IPMI()));
	}

	@Test
	void testVisitIPMIOutOfBand() throws InterruptedException, ExecutionException, TimeoutException {
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(MANAGEMENT_CARD_HOST)
						.id(MANAGEMENT_CARD_HOST)
						.type(TargetType.MGMT_CARD_BLADE_ESXI)
						.build())
				.protocolConfigurations(Map.of(IPMIOverLanProtocol.class, IPMIOverLanProtocol
						.builder()
						.username("username")
						.password("password".toCharArray()).build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn("System power state is up").when(matsyaClientsExecutor)
		.executeIpmiDetection(eq(MANAGEMENT_CARD_HOST), any(IPMIOverLanProtocol.class));
		assertEquals(CriterionTestResult
				.builder()
				.result("System power state is up")
				.message("Successfully connected to the IPMI BMC chip with the IPMI-over-LAN interface.")
				.success(true)
				.build(), criterionVisitor.visit(new IPMI()));
	}

	@Test
	void testVisitIPMIOutOfBandNullResult() throws InterruptedException, ExecutionException, TimeoutException {
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(MANAGEMENT_CARD_HOST)
						.id(MANAGEMENT_CARD_HOST)
						.type(TargetType.MGMT_CARD_BLADE_ESXI)
						.build())
				.protocolConfigurations(Map.of(IPMIOverLanProtocol.class, IPMIOverLanProtocol
						.builder()
						.username("username")
						.password("password".toCharArray()).build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(null).when(matsyaClientsExecutor)
		.executeIpmiDetection(eq(MANAGEMENT_CARD_HOST), any(IPMIOverLanProtocol.class));
		assertEquals(CriterionTestResult
				.builder()
				.message("Received <null> result after connecting to the IPMI BMC chip with the IPMI-over-LAN interface.")
				.build(), criterionVisitor.visit(new IPMI()));
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

		final CriterionTestResult successfulTestResult = CriterionTestResult
				.builder()
				.message("Successful OS detection operation")
				.result("Configured OS Type : NT")
				.success(true)
				.build();

		final CriterionTestResult failedTestResult = CriterionTestResult
				.builder()
				.message("Failed OS detection operation")
				.result("Configured OS Type : NT")
				.success(false)
				.build();

		final OS os = OS.builder().build();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OSType.NT));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OSType.NT));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));

		os.setExclude(Set.of(OSType.LINUX));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OSType.LINUX));
		os.setExclude(Collections.emptySet());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		criterionVisitor.visit(os);
		assertEquals(failedTestResult, criterionVisitor.visit(os));

		successfulTestResult.setResult("Configured OS Type : SOLARIS");
		failedTestResult.setResult("Configured OS Type : SOLARIS");
		engineConfiguration.setTarget(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.SUN_SOLARIS).build());

		os.setKeepOnly(Set.of(OSType.LINUX));
		os.setExclude(Collections.emptySet());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OSType.SOLARIS));
		os.setExclude(Collections.emptySet());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OSType.SUNOS));
		os.setExclude(Collections.emptySet());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OSType.SUNOS, OSType.SOLARIS));
		os.setExclude(Collections.emptySet());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OSType.LINUX));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OSType.SOLARIS));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OSType.SUNOS));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OSType.SUNOS, OSType.SOLARIS));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));
	}

	@Test
	void testIsOsTypeIncluded() {
		final OS os = OS.builder().build();
		final List<OSType> osTypeList = Arrays.asList(OSType.STORAGE, OSType.NETWORK, OSType.LINUX);
		assertTrue(criterionVisitor.isOsTypeIncluded(osTypeList, os));

		os.setKeepOnly(Set.of(OSType.NT));
		assertFalse(criterionVisitor.isOsTypeIncluded(osTypeList, os));

		os.setKeepOnly(Set.of(OSType.LINUX));
		assertTrue(criterionVisitor.isOsTypeIncluded(osTypeList, os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OSType.NT));
		assertTrue(criterionVisitor.isOsTypeIncluded(osTypeList, os));

		os.setExclude(Set.of(OSType.LINUX));
		assertFalse(criterionVisitor.isOsTypeIncluded(osTypeList, os));
	}

	@Test
	void testVisitOSCommand() {
		assertEquals(CriterionTestResult.empty(), new CriterionVisitor().visit(new OSCommand()));
	}

	@Test
	void testVisitProcessProcessNull() {
		final Process process = null;
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(process));
	}

	@Test
	void testVisitProcessCommandLineNull() {
		final Process process = new Process();
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(process));
	}

	@Test
	void testVisitProcessCommandLineEmpty() {
		final Process process = new Process();
		process.setProcessCommandLine("");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: actually no test were performed.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitProcessNotLocalHost() {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: no test will be performed remotely.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitProcessUnknownOS() {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getOS).thenReturn(Optional.empty());

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals("Process presence check: OS unknown, no test will be performed.", criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitProcessWindowsFail() throws Exception {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");

		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getOS).thenReturn(Optional.of(LocalOSHandler.WINDOWS));
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSVersion).thenReturn(Optional.of("5.1"));

			doThrow(new TimeoutException("over")).when(matsyaClientsExecutor).executeWmi(
					"localhost",
					null,
					null,
					120L,
					"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process",
					"root\\cimv2");

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"Unable to perform WMI query \"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process\". TimeoutException: over",
					criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitProcessWindowsEmptyResult() throws Exception {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");

		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getOS).thenReturn(Optional.of(LocalOSHandler.WINDOWS));
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSVersion).thenReturn(Optional.of("5.1"));

			doReturn(Collections.emptyList()).when(matsyaClientsExecutor).executeWmi(
					"localhost",
					null,
					null,
					120L,
					"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process",
					"root\\cimv2");

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"WMI query \"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process\" returned empty value.",
					criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitProcessWindowsNoProcess() throws Exception {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");

		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getOS).thenReturn(Optional.of(LocalOSHandler.WINDOWS));
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSVersion).thenReturn(Optional.of("5.1"));

			doReturn(
					List.of(
							List.of("0","System Idle Process", "0", ""),
							List.of("10564","eclipse.exe", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")))
			.when(matsyaClientsExecutor).executeWmi(
					"localhost",
					null,
					null,
					120L,
					"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process",
					"root\\cimv2");

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"No currently running processes matches the following regular expression:\n" +
							"- Regexp (should match with the command-line): MBM[5-9]\\.exe\n" +
							"- Currently running process list:\n" +
							"0;System Idle Process;0;\n" +
							"10564;eclipse.exe;11068;\"C:\\Users\\huan\\eclipse\\eclipse.exe\"",
							criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitProcessWindowsOK() throws Exception {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");

		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getOS).thenReturn(Optional.of(LocalOSHandler.WINDOWS));
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSVersion).thenReturn(Optional.of("5.1"));

			doReturn(
					List.of(
							List.of("0","System Idle Process", "0", ""),
							List.of("2", "MBM6.exe", "0", "MBM6.exe arg1 arg2"),
							List.of("10564","eclipse.exe", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")))
			.when(matsyaClientsExecutor).executeWmi(
					"localhost",
					null,
					null,
					120L,
					"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process",
					"root\\cimv2");

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"One or more currently running processes match the following regular expression:\n- Regexp (should match with the command-line): MBM[5-9]\\.exe",
					criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitProcessLinuxNoProcess() {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class);
				final MockedStatic<CriterionProcessVisitor> mockedCriterionProcessVisitorImpl = mockStatic(CriterionProcessVisitor.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getOS).thenReturn(Optional.of(LocalOSHandler.LINUX));
			mockedCriterionProcessVisitorImpl.when(CriterionProcessVisitor::listAllLinuxProcesses).thenReturn(
					List.of(
							List.of("1", "ps", "root", "0", "ps -A -o pid,comm,ruser,ppid,args"),
							List.of("10564","eclipse.exe", "user", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")));

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"No currently running processes matches the following regular expression:\n" +
							"- Regexp (should match with the command-line): MBM[5-9]\\.exe\n" +
							"- Currently running process list:\n" +
							"1;ps;root;0;ps -A -o pid,comm,ruser,ppid,args\n" +
							"10564;eclipse.exe;user;11068;\"C:\\Users\\huan\\eclipse\\eclipse.exe\"",
							criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitProcessLinuxOK() {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class);
				final MockedStatic<CriterionProcessVisitor> mockedCriterionProcessVisitorImpl = mockStatic(CriterionProcessVisitor.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getOS).thenReturn(Optional.of(LocalOSHandler.LINUX));
			mockedCriterionProcessVisitorImpl.when(CriterionProcessVisitor::listAllLinuxProcesses).thenReturn(
					List.of(
							List.of("1", "ps", "root", "0", "ps -A -o pid,comm,ruser,ppid,args"),
							List.of("2", "MBM6.exe", "user", "0", "MBM6.exe arg1 arg2"),
							List.of("10564","eclipse.exe", "user", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")));

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"One or more currently running processes match the following regular expression:\n- Regexp (should match with the command-line): MBM[5-9]\\.exe",
					criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitProcessNotImplementedAixOK() {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getOS).thenReturn(Optional.of(LocalOSHandler.AIX));

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals("Process presence check: no test will be performed for OS: aix.", criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitServiceCheckServiceNull() {
		final Service service = null;
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(service));
	}

	@Test
	void testVisitServiceCheckServiceNameNull() {
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(new Service()));
	}

	@Test
	void testVisitServiceCheckProtocolNull() {
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(HOST_WIN)
						.build())
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final Service service = new Service();
		service.setServiceName("TWGIPC");

		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(service));
	}

	@Test
	void testVisitServiceCheckOsNull() {
		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(HOST_WIN)
						.build())
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final Service service = new Service();
		service.setServiceName("TWGIPC");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(service);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals("Windows Service check: we are not running under Windows.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitServiceCheckOsNotWindows() {
		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(HOST_WIN)
						.type(TargetType.LINUX)
						.build())
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final Service service = new Service();
		service.setServiceName("TWGIPC");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(service);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals("Windows Service check: we are not running under Windows.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitServiceCheckServiceNameEmpty() {
		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(HOST_WIN)
						.id(HOST_WIN)
						.type(TargetType.MS_WINDOWS)
						.build())
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final Service service = new Service();
		service.setServiceName("");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(service);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Windows Service check: actually no test were performed.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitServiceWmiException() throws Exception {
		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(HOST_WIN)
						.id(HOST_WIN)
						.type(TargetType.MS_WINDOWS)
						.build())
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		doThrow(new WmiComException("Error")).when(matsyaClientsExecutor).executeWmi(
				HOST_WIN,
				USERNAME,
				PASSWORD,
				TIME_OUT,
				"select name, state from win32_service where name = 'TWGIPC'",
				"root\\cimv2");

		final Service service = new Service();
		service.setServiceName("TWGIPC");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(service);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals("Service Criterion, WMI query select name, state from win32_service where name = 'TWGIPC' on host-win was unsuccessful due to an exception. Message: Error.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitServiceNotFound() throws Exception {
		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(HOST_WIN)
						.id(HOST_WIN)
						.type(TargetType.MS_WINDOWS)
						.build())
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		doReturn(Collections.emptyList()).when(matsyaClientsExecutor).executeWmi(
				HOST_WIN,
				USERNAME,
				PASSWORD,
				TIME_OUT,
				"select name, state from win32_service where name = 'TWGIPC'",
				"root\\cimv2");

		final Service service = new Service();
		service.setServiceName("TWGIPC");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(service);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals("Windows Service check: the TWGIPC Windows service is not found.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitServiceStopped() throws Exception {
		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(HOST_WIN)
						.id(HOST_WIN)
						.type(TargetType.MS_WINDOWS)
						.build())
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		doReturn(List.of(List.of("TWGIPC", "Stopped"))).when(matsyaClientsExecutor).executeWmi(
				HOST_WIN,
				USERNAME,
				PASSWORD,
				TIME_OUT,
				"select name, state from win32_service where name = 'TWGIPC'",
				"root\\cimv2");

		final Service service = new Service();
		service.setServiceName("TWGIPC");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(service);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals("Windows Service check: the TWGIPC Windows service is not reported as running.\n Stopped", criterionTestResult.getMessage());
		assertEquals("TWGIPC;Stopped;", criterionTestResult.getResult());
	}

	@Test
	void testVisitServiceOK() throws Exception {
		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget.builder()
						.hostname(HOST_WIN)
						.id(HOST_WIN)
						.type(TargetType.MS_WINDOWS)
						.build())
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		doReturn(List.of(List.of("TWGIPC", "Running"))).when(matsyaClientsExecutor).executeWmi(
				HOST_WIN,
				USERNAME,
				PASSWORD,
				TIME_OUT,
				"select name, state from win32_service where name = 'TWGIPC'",
				"root\\cimv2");

		final Service service = new Service();
		service.setServiceName("TWGIPC");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(service);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Windows Service check: the TWGIPC Windows service is currently running.", criterionTestResult.getMessage());
		assertEquals("TWGIPC;Running;", criterionTestResult.getResult());
	}

	private void initSNMP() {

		if (engineConfiguration != null
				&& engineConfiguration.getProtocolConfigurations().get(SNMPProtocol.class) != null) {

			return;
		}

		final SNMPProtocol protocol = SNMPProtocol
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

		final WBEMProtocol protocol = WBEMProtocol
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
		final WBEM wbem = new WBEM();
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
		final WBEM wbem = WBEM.builder().wbemQuery(FOO).wbemNamespace(AUTOMATIC).build();
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
		final List<List<String>> queryResult = Collections.singletonList(Collections.singletonList(BAZ));
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
		final WBEM wbem = WBEM.builder().wbemQuery(FOO).wbemNamespace(AUTOMATIC).build();
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
		final WBEM wbem = WBEM.builder().wbemQuery(FOO).wbemNamespace(AUTOMATIC).build();
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
		final WBEM wbem = WBEM.builder().wbemQuery(FOO).wbemNamespace(AUTOMATIC).build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		hostMonitoring.setAutomaticWbemNamespace(null);
		hostMonitoring.getPossibleWbemNamespaces().clear();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		final List<List<String>> queryResult = Collections.emptyList();
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
		final WBEM wbem = WBEM.builder().wbemQuery(FOO).wbemNamespace(AUTOMATIC).build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		hostMonitoring.setAutomaticWbemNamespace(null);
		hostMonitoring.getPossibleWbemNamespaces().add(BAR);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doThrow(WBEMException.class).when(matsyaClientsExecutor).executeWbem(any(), any(), any(), anyInt(), any(), any());
		final CriterionTestResult criterionTestResult = criterionVisitor.visit(wbem);
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