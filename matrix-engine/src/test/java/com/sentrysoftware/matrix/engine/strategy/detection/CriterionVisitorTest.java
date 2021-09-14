package com.sentrysoftware.matrix.engine.strategy.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.common.helpers.LocalOSHandler;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
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
import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.WMI;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.OSCommandConfig;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;


@ExtendWith(MockitoExtension.class)
class CriterionVisitorTest {

	private static final String MANAGEMENT_CARD_HOST = "management-card-host";
	private static final String HOST_LINUX = "host-linux";
	private static final String HOST_WIN = "host-win";
	private static final String AUTOMATIC = "Automatic";
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
	private static final String PC14 = "pc14";

	private static final String USERNAME = "username";
	private static final char[] PASSWORD = "password".toCharArray();
	private static final Long TIME_OUT = 120L;

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@Mock
	private Connector connector;

	@Mock
	private WqlDetectionHelper wqlDetectionHelper;

	@InjectMocks
	private CriterionVisitor criterionVisitor;

	@InjectMocks
	@Spy
	private CriterionVisitor criterionVisitorSpy;

	private static EngineConfiguration engineConfiguration;

	@Mock
	private IHostMonitoring hostMonitoring;

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
		assertTrue(criterionTestResult.getMessage().contains("No WMI credentials provided."));

		// wqlDetectionHelper gives unsuccessful result
		final WMIProtocol wmiProtocol = WMIProtocol.builder()
				.namespace(HOST_WIN)
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();
		protocolConfigurations.put(wmiProtocol.getClass(), wmiProtocol);

		doReturn(CriterionTestResult.error(ipmi, "No result"))
				.when(wqlDetectionHelper).performDetectionTest(any(), any(), any());

		criterionTestResult = criterionVisitor.visit(ipmi);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().contains("No result"));

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

		final IPMI ipmi = IPMI.builder().forceSerialization(true).build();
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

		final String resultFinal = "System description;";

		doReturn(CriterionTestResult.success(ipmi, resultFinal))
			.when(wqlDetectionHelper).performDetectionTest(any(), any(), any());

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(ipmi);

		assertNotNull(criterionTestResult);
		assertEquals(resultFinal, criterionTestResult.getResult());
		assertTrue(criterionTestResult.isSuccess());
	}


	@Test
	@EnabledOnOs(WINDOWS)
	void testRunOsCommandWindows() throws Exception {
		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);
		final String version = OsCommandHelper.runLocalCommand("ver", 120, null);
		assertTrue(version.contains("Microsoft Windows"));
	}

	@Test
	@EnabledOnOs(LINUX)
	void testRunOsCommandLinux() throws Exception {
		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);
		final String version = OsCommandHelper.runLocalCommand("uname -a", 120, null);
		assertTrue(version.contains("Linux"));
	}

	@Test
	void testVisitIPMILinux() throws Exception {
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

		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd.when(() -> OsCommandHelper.runSshCommand(anyString(), eq(HOST_LINUX), any(SSHProtocol.class), eq(120), isNull(), isNull())).thenReturn(ipmiResultExample);
			assertEquals(CriterionTestResult.builder().result(ipmiResultExample).success(true)
					.message("Successfully connected to the IPMI BMC chip with the in-band driver interface.").build(),
					criterionVisitor.visit(new IPMI()));
		}

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
			oscmd.when(() -> OsCommandHelper.runLocalCommand(any(), eq(120), isNull())).thenReturn(ipmiResultExample);
			assertEquals(CriterionTestResult.builder().result(ipmiResultExample).success(true)
					.message("Successfully connected to the IPMI BMC chip with the in-band driver interface.").build(),
					criterionVisitor.visit(new IPMI()));
		}

	}

	@Test
	void testRunOsCommand() throws Exception {
		final SSHProtocol ssh = SSHProtocol.sshProtocolBuilder().username("root").password("nationale".toCharArray()).build();

		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			final HostMonitoring hostMonitoring = new HostMonitoring();
			hostMonitoring.setLocalhost(true);

			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

			oscmd.when(() -> OsCommandHelper.runLocalCommand("cmd", 120, null)).thenReturn("something");

			assertEquals("something", criterionVisitor.runOsCommand("cmd", "localhost", ssh, 120));
		}

		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();

			oscmd.when(() -> OsCommandHelper.runSshCommand("cmd", "host", ssh, 120, null, null)).thenReturn("something");

			assertEquals("something", criterionVisitor.runOsCommand("cmd", "host", ssh, 120));
		}
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
				oscmd.when(() -> OsCommandHelper.runLocalCommand(any(), eq(120), isNull())).thenReturn("5.10");
				final String cmdResult = criterionVisitor.buildIpmiCommand(TargetType.SUN_SOLARIS, "toto", ssh,
						osCommandConfig, 120);
				assertNotNull(cmdResult);
				assertTrue(cmdResult.startsWith("PATH")); // Successful command
			}

			try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
				oscmd.when(() -> OsCommandHelper.runLocalCommand(any(), eq(120), isNull())).thenReturn("blabla");
				final String cmdResult = criterionVisitor.buildIpmiCommand(TargetType.SUN_SOLARIS, "toto", ssh,
						osCommandConfig, 120);
				assertNotNull(cmdResult);
				assertTrue(cmdResult.startsWith("Couldn't")); // Not Successful command the response starts with
				// Couldn't identify
			}

			try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
				osCommandConfig.setUseSudo(true);
				oscmd.when(() -> OsCommandHelper.runLocalCommand(any(), eq(120), isNull())).thenReturn("5.10");
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
	void testVisitIPMIOutOfBand() throws Exception {
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
	void testVisitIPMIOutOfBandNullResult() throws Exception {
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
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(new KMVersion()));
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
	void testVisitOsCommandNull() {
		final OSCommand osCommand = null;

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Error with a <null> Criterion. Malformed OSCommand criterion.",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandLineNull() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setExpectedResult("Agent Rev:");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Error in OSCommand test:\n" + osCommand.toString() +
						"\n\n" +
						"Malformed OSCommand criterion.",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandExpectedResultNull() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Error in OSCommand test:\n" + osCommand.toString() +
						"\n\n" +
						"Malformed OSCommand criterion.",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandLineEmpty() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("");
		osCommand.setExpectedResult("Agent Rev:");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"OSCommand test succeeded:\n" + osCommand.toString() +
					"\n\n" +
					"Result: CommandLine or ExpectedResult are empty. Skipping this test.", 
				criterionTestResult.getMessage());
		assertEquals("CommandLine or ExpectedResult are empty. Skipping this test.", criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandExpectedResultEmpty() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent");
		osCommand.setExpectedResult("");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"OSCommand test succeeded:\n" + osCommand.toString() +
					"\n\n" +
					"Result: CommandLine or ExpectedResult are empty. Skipping this test.", 
				criterionTestResult.getMessage());
		assertEquals("CommandLine or ExpectedResult are empty. Skipping this test.", criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandRemoteNoUser() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("%{SUDO:naviseccli} naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent");
		osCommand.setExpectedResult("Agent Rev:");
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final SSHProtocol sshProtocol = new SSHProtocol(" ", "pwd".toCharArray(), null);

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "host", TargetType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.target(hardwareTarget)
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Error in OSCommand test:\n" + osCommand.toString() +
						"\n\n" +
						"No credentials provided.",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandRemoteWindowsEmbeddedFilesError() throws Exception {

		final String command = 
				"copy %EmbeddedFile(2)% %EmbeddedFile(2)%.bat > NUL"
				+ " & %EmbeddedFile(1)%"
				+ " & %EmbeddedFile(2)%.bat"
				+ " & del /F /Q %EmbeddedFile(1)%"
				+ " & del /F /Q %EmbeddedFile(2)%.bat";

		final String result = "Windows_NT\nHello World";

		final Map<Integer, EmbeddedFile> embeddedFiles = new HashMap<>();
		embeddedFiles.put(1, new EmbeddedFile("ECHO %OS%", "bat"));
		embeddedFiles.put(2, new EmbeddedFile("echo Hello World", null));

		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine(command);
		osCommand.setExpectedResult(result);
		osCommand.setErrorMessage("Connector only works on a Windows NT Server");
		osCommand.setEmbeddedFiles(embeddedFiles);

		final WMIProtocol wmiProtocol = new WMIProtocol();
		wmiProtocol.setUsername("user");
		wmiProtocol.setPassword("pwd".toCharArray());

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "host", TargetType.MS_WINDOWS);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.target(hardwareTarget)
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(wmiProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(null, null, wmiProtocol, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(wmiProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), isNull())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();

			mockedOsCommandHelper.when(() -> OsCommandHelper.createOsCommandEmbeddedFiles(command, embeddedFiles, null)).thenThrow(new IOException("error in file1"));

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"Error in OSCommand test:\n" + osCommand.toString() +
							"\n\n" +
							"IOException: error in file1",
							criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testVisitOsCommandWindowsError() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("PAUSE");
		osCommand.setExpectedResult(" ");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No date.");

		final OSCommandConfig osCommandConfig = new OSCommandConfig();
		osCommandConfig.setTimeout(1L);

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "localhost", TargetType.MS_WINDOWS);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol, osCommandConfig.getClass(), osCommandConfig))
				.target(hardwareTarget)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Error in OSCommand test:\n" + osCommand.toString() +
						"\n\n" +
						"TimeoutException: Command \"PAUSE\" execution has timed out after 1 s",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(LINUX)
	void testVisitOsCommandLinuxError() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("sleep 5");
		osCommand.setExpectedResult(" ");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No date.");
		osCommand.setTimeout(1L);

		final OSCommandConfig osCommandConfig = new OSCommandConfig();
		osCommandConfig.setTimeout(1L);

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "localhost", TargetType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol, osCommandConfig.getClass(), osCommandConfig))
				.target(hardwareTarget)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Error in OSCommand test:\n" + osCommand.toString() +
						"\n\n" +
						"TimeoutException: Command \"sleep 5\" execution has timed out after 5 s",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testVisitOsCommandLocalWindowsFailedToMatchCriteria() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("date /t");
		osCommand.setExpectedResult("Criteria not found");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No date.");

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "localhost", TargetType.MS_WINDOWS);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.target(hardwareTarget)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		final String result = new SimpleDateFormat("dd-MMM-yy ").format(new Date());

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"OSCommand test ran but failed:\n" + 
						"OSCommand(super=Criterion(forceSerialization=false, index=0), commandLine=date /t, errorMessage=No date., expectedResult=Criteria not found, executeLocally=true, timeout=null)" + 
						"\n\n" +
						"Actual result:\n" + result,
						criterionTestResult.getMessage());
		assertEquals( result, criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(LINUX)
	void testVisitOsCommandLocalLinuxFailedToMatchCriteria() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("date +\"%d%m%y\"");
		osCommand.setExpectedResult("Criteria not found");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No date.");

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "localhost", TargetType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.target(hardwareTarget)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		final String result = new SimpleDateFormat("dd/MM/yy").format(new Date());

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"OSCommand test ran but failed::\n" + osCommand.toString() +
						"\n\n" +
						"Actual result:\n" + result,
						criterionTestResult.getMessage());
		assertEquals( result, criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testVisitOsCommandLocalWindows() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("date /t");
		osCommand.setExpectedResult("\\d{2}-[A-Z][a-z]{2}-\\d{2} ");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No date.");

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "localhost", TargetType.MS_WINDOWS);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.target(hardwareTarget)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		final String result = new SimpleDateFormat("dd-MMM-yy ").format(new Date());

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"OSCommand test succeeded:\n" + 
					"OSCommand(super=Criterion(forceSerialization=false, index=0), commandLine=date /t, errorMessage=No date., expectedResult=\\d{2}-[A-Z][a-z]{2}-\\d{2} , executeLocally=true, timeout=null)" + 
					"\n\n" +
					"Result: " + result,
				criterionTestResult.getMessage());
		assertEquals(result, criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(LINUX)
	void testVisitOsCommandLocalLinux() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("date +\"%d%m%y\"");
		osCommand.setExpectedResult("\\d{2}/\\d{2}/\\d{2}");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No date.");

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "localhost", TargetType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.target(hardwareTarget)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		final String result = new SimpleDateFormat("dd/MM/yy").format(new Date());

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"OSCommand test succeeded:\n" + osCommand.toString() + 
					"\n\n" +
					"Result: " + result,
				criterionTestResult.getMessage());
		assertEquals(result, criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testVisitOsCommandRemoteExecutedLocallyWindows() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("date /t");
		osCommand.setExpectedResult("\\d{2}-[A-Z][a-z]{2}-\\d{2} ");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No date.");

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "host", TargetType.MS_WINDOWS);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.target(hardwareTarget)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		final String result = new SimpleDateFormat("dd-MMM-yy ").format(new Date());

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"OSCommand test succeeded:\n" + 
					"OSCommand(super=Criterion(forceSerialization=false, index=0), commandLine=date /t, errorMessage=No date., expectedResult=\\d{2}-[A-Z][a-z]{2}-\\d{2} , executeLocally=true, timeout=null)" + 
					"\n\n" +
					"Result: " + result,
				criterionTestResult.getMessage());
		assertEquals(result, criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(LINUX)
	void testVisitOsCommandRemoteExecutedLocallyLinux() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("date +\"%d%m%y\"");
		osCommand.setExpectedResult("\\d{2}/\\d{2}/\\d{2}");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No date.");

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "host", TargetType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.target(hardwareTarget)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		final String result = new SimpleDateFormat("dd/MM/yy").format(new Date());

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"OSCommand test succeeded:\n" + osCommand.toString() + 
					"\n\n" +
					"Result: " + result,
				criterionTestResult.getMessage());
		assertEquals(result, criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandRemoteWindows() throws Exception {

		final String command = 
				"copy %EmbeddedFile(2)% %EmbeddedFile(2)%.bat > NUL"
				+ " & %EmbeddedFile(1)%"
				+ " & %EmbeddedFile(2)%.bat"
				+ " & del /F /Q %EmbeddedFile(1)%"
				+ " & del /F /Q %EmbeddedFile(2)%.bat";

		final String result = "Windows_NT\nHello World";

		final Map<Integer, EmbeddedFile> embeddedFiles = new HashMap<>();
		embeddedFiles.put(1, new EmbeddedFile("ECHO %OS%", "bat"));
		embeddedFiles.put(2, new EmbeddedFile("echo Hello World", null));
		
		final File file1 = mock(File.class);
		final File file2 = mock(File.class);
		final Map<String, File> embeddedTempFiles = new HashMap<>();
		embeddedTempFiles.put("%EmbeddedFile(1)%", file1);
		embeddedTempFiles.put("%EmbeddedFile(2)%", file2);

		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine(command);
		osCommand.setExpectedResult(result);
		osCommand.setErrorMessage("Connector only works on a Windows NT Server");
		osCommand.setEmbeddedFiles(embeddedFiles);

		final WMIProtocol wmiProtocol = new WMIProtocol();
		wmiProtocol.setUsername("user");
		wmiProtocol.setPassword("pwd".toCharArray());

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "host", TargetType.MS_WINDOWS);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(wmiProtocol.getClass(), wmiProtocol))
				.target(hardwareTarget)
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(wmiProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(null, null, wmiProtocol, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(wmiProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), isNull())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();

			mockedOsCommandHelper.when(() -> OsCommandHelper.createOsCommandEmbeddedFiles(command, embeddedFiles, null)).thenReturn(embeddedTempFiles);

			final String absolutePath1 = "/tmp/SEN_Embedded_1.bat";
			final String absolutePath2 = "/tmp/SEN_Embedded_2";
			final String updatedCommand = 
					"copy /tmp/SEN_Embedded_2 /tmp/SEN_Embedded_2.bat > NUL"
							+ " & /tmp/SEN_Embedded_1.bat"
							+ " & /tmp/SEN_Embedded_2.bat"
							+ " & del /F /Q /tmp/SEN_Embedded_1.bat"
							+ " & del /F /Q /tmp/SEN_Embedded_2.bat";

			doReturn(absolutePath1).when(file1).getAbsolutePath();
			doReturn(absolutePath2).when(file2).getAbsolutePath();
			
			try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {
				mockedMatsyaClientsExecutor.when(() -> MatsyaClientsExecutor.executeWmiRemoteCommand(
						updatedCommand, 
						"host", 
						"user", 
						"pwd".toCharArray(), 
						120,
						List.of(absolutePath1, absolutePath2))).thenReturn(result);

				final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

				assertNotNull(criterionTestResult);
				assertTrue(criterionTestResult.isSuccess());
				assertEquals(
						"OSCommand test succeeded:\n" + OSCommand.builder().commandLine(updatedCommand).expectedResult(osCommand.getExpectedResult()).errorMessage(osCommand.getErrorMessage()).build().toString() + 
						"\n\n" +
						"Result: " + result,
					criterionTestResult.getMessage());
				assertEquals(result, criterionTestResult.getResult());
			}
		}
	}

	@Test
	void testVisitOsCommandRemoteLinuxOSCommandConfigNull() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("%{SUDO:naviseccli} naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent");
		osCommand.setExpectedResult("Agent Rev:");
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "host", TargetType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.target(hardwareTarget)
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(null, null, sshProtocol, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), isNull())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.createOsCommandEmbeddedFiles(
					osCommand.getCommandLine(), 
					osCommand.getEmbeddedFiles(),
					null)).thenCallRealMethod();

			mockedOsCommandHelper.when(() -> OsCommandHelper.runSshCommand(
					" naviseccli -User user -Password pwd -Address host -Scope 1 getagent",
					"host",
					sshProtocol,
					120,
					Collections.emptyList(),
					" naviseccli -User user -Password ******** -Address host -Scope 1 getagent"))
			.thenReturn("Agent Rev:");

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"OSCommand test succeeded:\n" + OSCommand.builder().commandLine(" naviseccli -User user -Password ******** -Address host -Scope 1 getagent").expectedResult(osCommand.getExpectedResult()).errorMessage(osCommand.getErrorMessage()).build().toString() + 
						"\n\n" +
						"Result: Agent Rev:",
					criterionTestResult.getMessage());
			assertEquals("Agent Rev:", criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitOsCommandRemoteLinuxNoSudo() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("%{SUDO:naviseccli} naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent");
		osCommand.setExpectedResult("Agent Rev:");
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final OSCommandConfig osCommandConfig = new OSCommandConfig();
		osCommandConfig.setUseSudoCommands(Collections.singleton("naviseccli"));

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "host", TargetType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol, osCommandConfig.getClass(), osCommandConfig))
				.target(hardwareTarget)
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(null, osCommandConfig, sshProtocol, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfig))).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.createOsCommandEmbeddedFiles(
					osCommand.getCommandLine(),
					osCommand.getEmbeddedFiles(),
					osCommandConfig)).thenCallRealMethod();

			mockedOsCommandHelper.when(() -> OsCommandHelper.runSshCommand(
					" naviseccli -User user -Password pwd -Address host -Scope 1 getagent",
					"host",
					sshProtocol,
					120,
					Collections.emptyList(),
					" naviseccli -User user -Password ******** -Address host -Scope 1 getagent"))
			.thenReturn("Agent Rev:");

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"OSCommand test succeeded:\n" + OSCommand.builder().commandLine(" naviseccli -User user -Password ******** -Address host -Scope 1 getagent").expectedResult(osCommand.getExpectedResult()).errorMessage(osCommand.getErrorMessage()).build().toString() + 
						"\n\n" +
						"Result: Agent Rev:",
					criterionTestResult.getMessage());
			assertEquals("Agent Rev:", criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitOsCommandRemoteLinuxNotInUseSudoCommands() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("%{SUDO:naviseccli} naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent");
		osCommand.setExpectedResult("Agent Rev:");
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final OSCommandConfig osCommandConfig = new OSCommandConfig();
		osCommandConfig.setUseSudo(true);
		osCommandConfig.setUseSudoCommands(Collections.singleton("other"));

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "host", TargetType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol, osCommandConfig.getClass(), osCommandConfig))
				.target(hardwareTarget)
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(null, osCommandConfig, sshProtocol, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfig))).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.createOsCommandEmbeddedFiles(
					osCommand.getCommandLine(),
					osCommand.getEmbeddedFiles(),
					osCommandConfig)).thenCallRealMethod();

			mockedOsCommandHelper.when(() -> OsCommandHelper.runSshCommand(
					" naviseccli -User user -Password pwd -Address host -Scope 1 getagent",
					"host",
					sshProtocol,
					120,
					Collections.emptyList(),
					" naviseccli -User user -Password ******** -Address host -Scope 1 getagent"))
			.thenReturn("Agent Rev:");

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"OSCommand test succeeded:\n" + OSCommand.builder().commandLine(" naviseccli -User user -Password ******** -Address host -Scope 1 getagent").expectedResult(osCommand.getExpectedResult()).errorMessage(osCommand.getErrorMessage()).build().toString() + 
						"\n\n" +
						"Result: Agent Rev:",
					criterionTestResult.getMessage());
			assertEquals("Agent Rev:", criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitOsCommandRemoteLinuxWithSudoReplaced() {
		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("%{Sudo:naviseccli} naviseccli -User %{UserName} -Password %{password} -Address %{HOSTNAME} -Scope 1 getagent");
		osCommand.setExpectedResult("Agent Rev:");
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final OSCommandConfig osCommandConfig = new OSCommandConfig();
		osCommandConfig.setUseSudo(true);
		osCommandConfig.setUseSudoCommands(Collections.singleton("naviseccli"));

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "host", TargetType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol, osCommandConfig.getClass(), osCommandConfig))
				.target(hardwareTarget)
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(null, osCommandConfig, sshProtocol, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfig))).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.createOsCommandEmbeddedFiles(
					osCommand.getCommandLine(),
					osCommand.getEmbeddedFiles(),
					osCommandConfig)).thenCallRealMethod();

			mockedOsCommandHelper.when(() -> OsCommandHelper.runSshCommand(
					"sudo naviseccli -User user -Password pwd -Address host -Scope 1 getagent",
					"host",
					sshProtocol,
					120,
					Collections.emptyList(),
					"sudo naviseccli -User user -Password ******** -Address host -Scope 1 getagent"))
			.thenReturn("Agent Rev:");

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"OSCommand test succeeded:\n" + OSCommand.builder().commandLine("sudo naviseccli -User user -Password ******** -Address host -Scope 1 getagent").expectedResult(osCommand.getExpectedResult()).errorMessage(osCommand.getErrorMessage()).build().toString() + 
						"\n\n" +
						"Result: Agent Rev:",
					criterionTestResult.getMessage());
			assertEquals("Agent Rev:", criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitOsCommandRemoteLinuxWithEmbeddedFilesReplaced() {

		final String embeddedContent = 
				"# Awk (or nawk)\n" + 
				"if [ -f /usr/xpg4/bin/awk ]; then\n" + 
				"	AWK=\"/usr/xpg4/bin/awk\";\n" + 
				"elif [ -f /usr/bin/nawk ]; then\n" + 
				"	AWK=\"/usr/bin/nawk\";\n" + 
				"else\n" + 
				"	AWK=\"awk\";\n" + 
				"fi\n" + 
				"if [ -f /opt/StorMan/arcconf ]; then\n" + 
				"       STORMAN=\"/opt/StorMan\";\n" + 
				"elif [ -f /usr/StorMan/arcconf ]; then\n" + 
				"       STORMAN=\"/usr/StorMan\";\n" + 
				"else\n" + 
				"	echo No Storman Installed; exit;\n" + 
				"fi\n" + 
				"DEVICES=`%{SUDO:/[opt|usr]/StorMan/arcconf} $STORMAN/arcconf getversion | $AWK '($1 ~ /Controller/ && $2 ~ /#[0-9]/) {controller=$2;gsub(/#/,\"\",controller);print(controller)}'`\n" + 
				"for CTRL in $DEVICES\n" + 
				"                do\n" + 
				"                echo MSHWController $CTRL\n" + 
				"                %{SUDO:/[opt|usr]/StorMan/arcconf} $STORMAN/arcconf getconfig $CTRL PD\n" + 
				"                done";

		final Map<Integer, EmbeddedFile> embeddedFiles = Collections.singletonMap(1, new EmbeddedFile(embeddedContent, null));
		
		final File localFile = mock(File.class);
		final Map<String, File> embeddedTempFiles = new HashMap<>();
		embeddedTempFiles.put("%EmbeddedFile(1)%", localFile);

		final OSCommandConfig osCommandConfig = new OSCommandConfig();
		osCommandConfig.setUseSudo(true);
		osCommandConfig.setUseSudoCommands(Collections.singleton("/[opt|usr]/StorMan/arcconf"));

		final OSCommand osCommand = new OSCommand();
		osCommand.setCommandLine("/bin/sh %EmbeddedFile(1)%");
		osCommand.setExpectedResult("Hard drive");
		osCommand.setErrorMessage("No Adaptec Controller with Physical Disks attached or not enough rights to execute arcconf.");
		osCommand.setEmbeddedFiles(embeddedFiles);

		final SSHProtocol sshProtocol = new SSHProtocol("user", "pwd".toCharArray(), null);

		final HardwareTarget hardwareTarget = new HardwareTarget("id", "host", TargetType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol, osCommandConfig.getClass(), osCommandConfig))
				.target(hardwareTarget)
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<OsCommandHelper> mockedOsCommandHelper = mockStatic(OsCommandHelper.class)) {
			mockedOsCommandHelper.when(() -> OsCommandHelper.getUsername(sshProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getTimeout(null, osCommandConfig, sshProtocol, 300)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getPassword(sshProtocol)).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.toCaseInsensitiveRegex(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.getFileNameFromSudoCommand(anyString())).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.replaceSudo(anyString(), eq(osCommandConfig))).thenCallRealMethod();
			mockedOsCommandHelper.when(() -> OsCommandHelper.createOsCommandEmbeddedFiles(
					osCommand.getCommandLine(), 
					osCommand.getEmbeddedFiles(), 
					osCommandConfig)).thenReturn(embeddedTempFiles);

			doReturn("C:\\Users\\user\\AppData\\Local\\Temp\\SEN_Embedded_0001").when(localFile).getAbsolutePath();

			mockedOsCommandHelper.when(() -> OsCommandHelper.runSshCommand(
					"/bin/sh C:\\Users\\user\\AppData\\Local\\Temp\\SEN_Embedded_0001",
					"host",
					sshProtocol,
					120,
					List.of(localFile), 
					"/bin/sh C:\\Users\\user\\AppData\\Local\\Temp\\SEN_Embedded_0001"))
			.thenReturn("Hard drive");

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"OSCommand test succeeded:\n" + OSCommand.builder().commandLine("/bin/sh C:\\Users\\user\\AppData\\Local\\Temp\\SEN_Embedded_0001").expectedResult(osCommand.getExpectedResult()).errorMessage(osCommand.getErrorMessage()).build().toString() + 
						"\n\n" +
						"Result: Hard drive",
					criterionTestResult.getMessage());
			assertEquals("Hard drive", criterionTestResult.getResult());
		}
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
	void testVisitProcessWindowsEmptyResult() throws Exception {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		try (final MockedStatic<LocalOSHandler> mockedLocalOSHandler = mockStatic(LocalOSHandler.class)) {
			mockedLocalOSHandler.when(LocalOSHandler::getOS).thenReturn(Optional.of(LocalOSHandler.WINDOWS));
			mockedLocalOSHandler.when(LocalOSHandler::getSystemOSVersion).thenReturn(Optional.of("5.1"));

			doReturn(CriterionTestResult.error(process,
					"WMI query \"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process\" returned empty value.")).when(wqlDetectionHelper)
				.performDetectionTest(any(), any(), any());

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertTrue(criterionTestResult.getMessage().contains("WMI query \"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process\" returned empty value."));
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	@Disabled
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
					wmiProtocol,
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
		assertTrue(criterionVisitor.visit(service).getMessage().contains("Malformed Service criterion."));
	}

	@Test
	void testVisitServiceCheckServiceNameNull() {
		assertTrue(criterionVisitor.visit(new Service()).getMessage().contains("Malformed Service criterion."));
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

		assertTrue(criterionVisitor.visit(service).getMessage().contains("WMI Credentials are not configured."));
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
		assertTrue(criterionTestResult.getMessage().contains("Target system is not Windows"));
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
		assertNull(criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(WINDOWS)
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
		assertTrue(criterionTestResult.getMessage().contains("Service name is not specified"));
		assertNotNull(criterionTestResult.getResult());
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

		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit((SNMPGet) null));
		assertEquals(CriterionTestResult.empty(),
				criterionVisitor.visit(SNMPGet.builder().oid(null).build()));
		doReturn(new EngineConfiguration()).when(strategyConfig).getEngineConfiguration();
		assertEquals(CriterionTestResult.empty(),
				criterionVisitor.visit(SNMPGet.builder().oid(OID).build()));
	}

	@Test
	void testVisitTelnetInteractive() {
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(new TelnetInteractive()));
	}

	@Test
	void testVisitUCS() {
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(new UCS()));
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

		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit((SNMPGetNext) null));
		assertEquals(CriterionTestResult.empty(),
				criterionVisitor.visit(SNMPGetNext.builder().oid(null).build()));
		doReturn(new EngineConfiguration()).when(strategyConfig).getEngineConfiguration();
		assertEquals(CriterionTestResult.empty(),
				criterionVisitor.visit(SNMPGetNext.builder().oid(OID).build()));
	}

	@Test
	void testVisitWmiBadCriterion() {
		CriterionTestResult result = criterionVisitor.visit(WMI.builder()
				.wbemNamespace(AUTOMATIC)
				.expectedResult(null)
				.build());
		assertTrue(result.getMessage().contains("Malformed criterion"));
		result = criterionVisitor.visit((WMI) null);
		assertTrue(result.getMessage().contains("Malformed criterion"));
	}

	@Test
	void testVisitWmiNoProtocol() {
		final WMI wmi = WMI.builder().wbemQuery(WMI_WQL).wbemNamespace(AUTOMATIC).expectedResult(null).build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname(PC14).id(PC14).type(TargetType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(SNMPProtocol.class, new SNMPProtocol()))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		assertTrue(criterionVisitor.visit(wmi).getMessage().contains("The WBEM Credentials are not configured"));
	}

}