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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.common.exception.StepException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OsType;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitFor;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.Http;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ipmi.Ipmi;
import com.sentrysoftware.matrix.connector.model.detection.criteria.kmversion.KmVersion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.Os;
import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OsCommand;
import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;
import com.sentrysoftware.matrix.connector.model.detection.criteria.service.Service;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SnmpGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SnmpGetNext;
import com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive.SshInteractive;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ucs.Ucs;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.Wmi;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.HttpProtocol;
import com.sentrysoftware.matrix.engine.protocol.IpmiOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.OsCommandConfig;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol.SnmpVersion;
import com.sentrysoftware.matrix.engine.protocol.SshProtocol;
import com.sentrysoftware.matrix.engine.protocol.WmiProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.SshInteractiveHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;

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

	private static EngineConfiguration engineConfiguration;

	@Mock
	private IHostMonitoring hostMonitoring;

	private void initHTTP() {

		if (engineConfiguration != null
				&& engineConfiguration.getProtocolConfigurations().get(HttpProtocol.class) != null) {

			return;
		}

		final HttpProtocol protocol = HttpProtocol
				.builder()
				.port(443)
				.timeout(120L)
				.build();

		engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder().hostname(PUREM_SAN).id(PUREM_SAN).type(HostType.LINUX).build())
				.protocolConfigurations(Map.of(HttpProtocol.class, protocol))
				.build();
	}

	@Test
	void testVisitHTTPFailure() {

		// null HTTP
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit((Http) null));

		// HTTP is not null, protocol is null
		engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder().hostname(PUREM_SAN).id(PUREM_SAN).type(HostType.LINUX).build())
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		final Http http = new Http();
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
		final Http http = new Http();
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
		
		// HTTP is not null, protocol is not null, expectedResult is not null, result is not null and case does not match
		doReturn("foo").when(matsyaClientsExecutor).executeHttp(any(), eq(false));
		http.setExpectedResult(FOO);
		criterionTestResult = criterionVisitor.visit(http);
		assertTrue(criterionTestResult.isSuccess());
	}

	@Test
	void testVisitIPMIWindowsFailure() throws Exception {
		// No WMI protocol
		final Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations = new HashMap<>();
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder()
						.hostname(HOST_WIN)
						.id(HOST_WIN)
						.type(HostType.MS_WINDOWS)
						.build())
				.protocolConfigurations(protocolConfigurations)
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final Ipmi ipmi = Ipmi.builder().forceSerialization(true).build();
		CriterionTestResult criterionTestResult = criterionVisitor.visit(ipmi);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().contains(CriterionVisitor.NEITHER_WMI_NOR_WINRM_ERROR));

		// wqlDetectionHelper gives unsuccessful result
		final WmiProtocol wmiProtocol = WmiProtocol.builder()
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
		final WmiProtocol wmiProtocol = WmiProtocol.builder()
				.namespace(HOST_WIN)
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();

		final Ipmi ipmi = Ipmi.builder().forceSerialization(true).build();
		protocolConfigurations.put(wmiProtocol.getClass(), wmiProtocol);
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder()
						.hostname(HOST_WIN)
						.id(HOST_WIN)
						.type(HostType.MS_WINDOWS)
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
					.host(HardwareHost.builder().hostname(HOST_LINUX).id(HOST_LINUX).type(HostType.LINUX).build())

					.build();

			final HostMonitoring hostMonitoring = new HostMonitoring();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
			assertEquals(CriterionTestResult.builder().result("").success(false)
					.message("Hostname " + HOST_LINUX + " - No OS command configuration for this host. Returning an empty result").build(),
					criterionVisitor.visit(new Ipmi()));
		}
		final SshProtocol ssh = SshProtocol.builder().username("root").password("nationale".toCharArray()).build();
		{
			// wrong IPMIToolCommand
			final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
					.host(HardwareHost.builder().hostname(HOST_LINUX).id(HOST_LINUX).type(HostType.LINUX).build())
					.protocolConfigurations(Map.of(HttpProtocol.class, HttpProtocol.builder().build(),
							OsCommandConfig.class, OsCommandConfig.builder().build(),
							SshProtocol.class, ssh))
					.build();

			final HostMonitoring hostMonitoring = new HostMonitoring();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
			assertFalse(criterionVisitor.visit(new Ipmi()).isSuccess());

		}
		{
			// wrong result when running IPMIToolCommand
			final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
					.host(HardwareHost.builder().hostname(HOST_LINUX).id(HOST_LINUX).type(HostType.LINUX).build())
					.protocolConfigurations(Map.of(HttpProtocol.class, OsCommandConfig.builder().build(),
							OsCommandConfig.class, OsCommandConfig.builder().build(),
							SshProtocol.class, ssh))
					.build();

			final HostMonitoring hostMonitoring = new HostMonitoring();
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
			try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
				oscmd.when(() -> OsCommandHelper.runSshCommand(anyString(), eq(HOST_LINUX), eq(ssh), anyInt(), isNull(), isNull())).thenReturn("wrong result");
				assertFalse(criterionVisitor.visit(new Ipmi()).isSuccess());
			}

		}

		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(HOST_LINUX).id(HOST_LINUX).type(HostType.LINUX).build())
				.protocolConfigurations(Map.of(HttpProtocol.class, OsCommandConfig.builder().build(),
						OsCommandConfig.class, OsCommandConfig.builder().build(),
						SshProtocol.class, ssh))
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
			oscmd.when(() -> OsCommandHelper.runSshCommand(anyString(), eq(HOST_LINUX), any(SshProtocol.class), anyInt(), isNull(), isNull())).thenReturn(ipmiResultExample);
			assertEquals(CriterionTestResult.builder().result(ipmiResultExample).success(true)
					.message("Successfully connected to the IPMI BMC chip with the in-band driver interface.").build(),
					criterionVisitor.visit(new Ipmi()));
		}

		// run localhost command
		final EngineConfiguration engineConfigurationLocal = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.SUN_SOLARIS)
						.build())
				.protocolConfigurations(Map.of(HttpProtocol.class, OsCommandConfig.builder().build(),
						OsCommandConfig.class, OsCommandConfig.builder().build(),
						SshProtocol.class, ssh))
				.build();
		hostMonitoring.setLocalhost(true);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfigurationLocal).when(strategyConfig).getEngineConfiguration();
		// here the try is important because it only will mock the static reference for
		// the following context.
		// Otherwise even for other contexts/methods it will always return the same
		// result (it is static..)
		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
			oscmd.when(() -> OsCommandHelper.runLocalCommand(any(), anyInt(), isNull())).thenReturn(ipmiResultExample);
			assertEquals(CriterionTestResult.builder().result(ipmiResultExample).success(true)
					.message("Successfully connected to the IPMI BMC chip with the in-band driver interface.").build(),
					criterionVisitor.visit(new Ipmi()));
		}

	}

	@Test
	void testRunOsCommand() throws Exception {
		final SshProtocol ssh = SshProtocol.builder().username("root").password("nationale".toCharArray()).build();

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
		final SshProtocol ssh = SshProtocol.builder().username("root").password("nationale".toCharArray()).build();
		final OsCommandConfig osCommandConfig = OsCommandConfig.builder().build();
		{
			// test Solaris
			final HostMonitoring hostMonitoring = new HostMonitoring();
			hostMonitoring.setLocalhost(true);
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
				oscmd.when(() -> OsCommandHelper.runLocalCommand(any(), eq(120), isNull())).thenReturn("5.10");
				final String cmdResult = criterionVisitor.buildIpmiCommand(HostType.SUN_SOLARIS, "toto", ssh,
						osCommandConfig, 120);
				assertNotNull(cmdResult);
				assertTrue(cmdResult.startsWith("PATH")); // Successful command
			}

			try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
				oscmd.when(() -> OsCommandHelper.runLocalCommand(any(), eq(120), isNull())).thenReturn("blabla");
				final String cmdResult = criterionVisitor.buildIpmiCommand(HostType.SUN_SOLARIS, "toto", ssh,
						osCommandConfig, 120);
				assertNotNull(cmdResult);
				assertTrue(cmdResult.contains("Could not")); // Not Successful command the response starts with
				// Couldn't identify
			}

			try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {
				osCommandConfig.setUseSudo(true);
				oscmd.when(() -> OsCommandHelper.runLocalCommand(any(), eq(120), isNull())).thenReturn("5.10");
				final String cmdResult = criterionVisitor.buildIpmiCommand(HostType.SUN_SOLARIS, "toto", ssh,
						osCommandConfig, 120);
				assertNotNull(cmdResult);
				assertTrue(cmdResult.contains("sudo")); // Successful sudo command
			}
		}

		{
			// test Linux
			osCommandConfig.setUseSudo(false);
			final String cmdResult = criterionVisitor.buildIpmiCommand(HostType.LINUX, "toto", ssh, osCommandConfig, 120);
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

			final String expectedMessage = "Unknown Solaris version";
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
				.host(HardwareHost.builder()
						.hostname(MANAGEMENT_CARD_HOST)
						.id(MANAGEMENT_CARD_HOST)
						.type(HostType.MGMT_CARD_BLADE_ESXI)
						.build())
				.protocolConfigurations(Collections.emptyMap())
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(new Ipmi()));
	}

	@Test
	void testVisitIPMIOutOfBand() throws Exception {
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder()
						.hostname(MANAGEMENT_CARD_HOST)
						.id(MANAGEMENT_CARD_HOST)
						.type(HostType.MGMT_CARD_BLADE_ESXI)
						.build())
				.protocolConfigurations(Map.of(IpmiOverLanProtocol.class, IpmiOverLanProtocol
						.builder()
						.username("username")
						.password("password".toCharArray()).build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn("System power state is up").when(matsyaClientsExecutor)
		.executeIpmiDetection(eq(MANAGEMENT_CARD_HOST), any(IpmiOverLanProtocol.class));
		assertEquals(CriterionTestResult
				.builder()
				.result("System power state is up")
				.message("Successfully connected to the IPMI BMC chip with the IPMI-over-LAN interface.")
				.success(true)
				.build(), criterionVisitor.visit(new Ipmi()));
	}

	@Test
	void testVisitIPMIOutOfBandNullResult() throws Exception {
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder()
						.hostname(MANAGEMENT_CARD_HOST)
						.id(MANAGEMENT_CARD_HOST)
						.type(HostType.MGMT_CARD_BLADE_ESXI)
						.build())
				.protocolConfigurations(Map.of(IpmiOverLanProtocol.class, IpmiOverLanProtocol
						.builder()
						.username("username")
						.password("password".toCharArray()).build()))
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(null).when(matsyaClientsExecutor)
		.executeIpmiDetection(eq(MANAGEMENT_CARD_HOST), any(IpmiOverLanProtocol.class));
		assertEquals(CriterionTestResult
				.builder()
				.message("Received <null> result after connecting to the IPMI BMC chip with the IPMI-over-LAN interface.")
				.build(), criterionVisitor.visit(new Ipmi()));
	}

	@Test
	void testVisitKMVersion() {
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(new KmVersion()));
	}

	@Test
	void testVisitOS() {
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(PC14).id(PC14).type(HostType.MS_WINDOWS).build())
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult successfulTestResult = CriterionTestResult
				.builder()
				.message("Successful OS detection operation")
				.result("Configured OS type : NT")
				.success(true)
				.build();

		final CriterionTestResult failedTestResult = CriterionTestResult
				.builder()
				.message("Failed OS detection operation")
				.result("Configured OS type : NT")
				.success(false)
				.build();

		final Os os = Os.builder().build();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OsType.NT));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OsType.NT));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));

		os.setExclude(Set.of(OsType.LINUX));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OsType.LINUX));
		os.setExclude(Collections.emptySet());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		criterionVisitor.visit(os);
		assertEquals(failedTestResult, criterionVisitor.visit(os));

		successfulTestResult.setResult("Configured OS type : SOLARIS");
		failedTestResult.setResult("Configured OS type : SOLARIS");
		engineConfiguration.setHost(HardwareHost.builder().hostname(PC14).id(PC14).type(HostType.SUN_SOLARIS).build());

		os.setKeepOnly(Set.of(OsType.LINUX));
		os.setExclude(Collections.emptySet());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OsType.SOLARIS));
		os.setExclude(Collections.emptySet());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OsType.SUNOS));
		os.setExclude(Collections.emptySet());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Set.of(OsType.SUNOS, OsType.SOLARIS));
		os.setExclude(Collections.emptySet());
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OsType.LINUX));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(successfulTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OsType.SOLARIS));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OsType.SUNOS));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OsType.SUNOS, OsType.SOLARIS));
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(failedTestResult, criterionVisitor.visit(os));
	}

	@Test
	void testIsOsTypeIncluded() {
		final Os os = Os.builder().build();
		final List<OsType> osTypeList = Arrays.asList(OsType.STORAGE, OsType.NETWORK, OsType.LINUX);
		assertTrue(criterionVisitor.isOsTypeIncluded(osTypeList, os));

		os.setKeepOnly(Set.of(OsType.NT));
		assertFalse(criterionVisitor.isOsTypeIncluded(osTypeList, os));

		os.setKeepOnly(Set.of(OsType.LINUX));
		assertTrue(criterionVisitor.isOsTypeIncluded(osTypeList, os));

		os.setKeepOnly(Collections.emptySet());
		os.setExclude(Set.of(OsType.NT));
		assertTrue(criterionVisitor.isOsTypeIncluded(osTypeList, os));

		os.setExclude(Set.of(OsType.LINUX));
		assertFalse(criterionVisitor.isOsTypeIncluded(osTypeList, os));
	}

	@Test
	void testVisitOsCommandNull() {
		final OsCommand osCommand = null;

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().toLowerCase().contains("malformed"));
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandLineNull() {
		final OsCommand osCommand = new OsCommand();
		osCommand.setExpectedResult("Agent Rev:");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Error in OsCommand test:\n" + osCommand.toString()
				+ "\n\n"
				+ "Malformed OSCommand criterion.",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandExpectedResultNull() {
		final OsCommand osCommand = new OsCommand();
		osCommand.setCommandLine("naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"OsCommand test succeeded:\n" + osCommand.toString() +
					"\n\n" +
					"Result: CommandLine or ExpectedResult are empty. Skipping this test.",
				criterionTestResult.getMessage());
		assertEquals("CommandLine or ExpectedResult are empty. Skipping this test.", criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandLineEmpty() {
		final OsCommand osCommand = new OsCommand();
		osCommand.setCommandLine("");
		osCommand.setExpectedResult("Agent Rev:");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"OsCommand test succeeded:\n" + osCommand.toString() +
					"\n\n" +
					"Result: CommandLine or ExpectedResult are empty. Skipping this test.",
				criterionTestResult.getMessage());
		assertEquals("CommandLine or ExpectedResult are empty. Skipping this test.", criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandExpectedResultEmpty() {
		final OsCommand osCommand = new OsCommand();
		osCommand.setCommandLine("naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent");
		osCommand.setExpectedResult("");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"OsCommand test succeeded:\n" + osCommand.toString() +
					"\n\n" +
					"Result: CommandLine or ExpectedResult are empty. Skipping this test.",
				criterionTestResult.getMessage());
		assertEquals("CommandLine or ExpectedResult are empty. Skipping this test.", criterionTestResult.getResult());
	}

	@Test
	void testVisitOsCommandRemoteNoUser() {
		final OsCommand osCommand = new OsCommand();
		osCommand.setCommandLine("naviseccli -User %{USERNAME} -Password %{PASSWORD} -Address %{HOSTNAME} -Scope 1 getagent");
		osCommand.setExpectedResult("Agent Rev:");
		osCommand.setExecuteLocally(false);
		osCommand.setErrorMessage("Unable to connect using Navisphere");

		final SshProtocol sshProtocol = SshProtocol.builder()
				.username(" ")
				.password("pwd".toCharArray())
				.build();

		final HardwareHost hardwareHost = new HardwareHost("id", "host", HostType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.host(hardwareHost)
				.build();

		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Error in OsCommand test:\n" + osCommand.toString() +
				"\n\n" +
				"No credentials provided.",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testVisitOsCommandWindowsError() {
		final OsCommand osCommand = new OsCommand();
		osCommand.setCommandLine("PAUSE");
		osCommand.setExpectedResult(" ");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No date.");

		final OsCommandConfig osCommandConfig = new OsCommandConfig();
		osCommandConfig.setTimeout(1L);

		final SshProtocol sshProtocol = SshProtocol.builder()
				.username(" ")
				.password("pwd".toCharArray())
				.build();

		final HardwareHost hardwareHost = new HardwareHost("id", "localhost", HostType.MS_WINDOWS);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol, osCommandConfig.getClass(), osCommandConfig))
				.host(hardwareHost)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Error in OsCommand test:\n" + osCommand.toString() +
						"\n\n" +
						"TimeoutException: Command \"PAUSE\" execution has timed out after 1 s",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(LINUX)
	void testVisitOsCommandLinuxError() {
		final OsCommand osCommand = new OsCommand();
		osCommand.setCommandLine("sleep 5");
		osCommand.setExpectedResult(" ");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No date.");
		osCommand.setTimeout(1L);

		final OsCommandConfig osCommandConfig = new OsCommandConfig();
		osCommandConfig.setTimeout(1L);

		final SshProtocol sshProtocol = SshProtocol.builder()
				.username(" ")
				.password("pwd".toCharArray())
				.build();

		final HardwareHost hardwareHost = new HardwareHost("id", "localhost", HostType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol, osCommandConfig.getClass(), osCommandConfig))
				.host(hardwareHost)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Error in OsCommand test:\n" + osCommand.toString() +
						"\n\n" +
						"TimeoutException: Command \"sleep 5\" execution has timed out after 1 s",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testVisitOsCommandLocalWindowsFailedToMatchCriteria() {

		final String result = "Test";

		final OsCommand osCommand = new OsCommand();
		osCommand.setCommandLine("ECHO Test");
		osCommand.setExpectedResult("Nothing");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No display.");

		final SshProtocol sshProtocol = SshProtocol.builder()
				.username(" ")
				.password("pwd".toCharArray())
				.build();

		final HardwareHost hardwareHost = new HardwareHost("id", "localhost", HostType.MS_WINDOWS);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.host(hardwareHost)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"OsCommand test ran but failed:\n" + osCommand.toString() +
						"\n\n" +
						"Actual result:\n" + result,
						criterionTestResult.getMessage());
		assertEquals(result, criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(LINUX)
	void testVisitOsCommandLocalLinuxFailedToMatchCriteria() {

		final String result = "Test";

		final OsCommand osCommand = new OsCommand();
		osCommand.setCommandLine("echo Test");
		osCommand.setExpectedResult("Nothing");
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No display.");

		final SshProtocol sshProtocol = SshProtocol.builder()
				.username(" ")
				.password("pwd".toCharArray())
				.build();

		final HardwareHost hardwareHost = new HardwareHost("id", "localhost", HostType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.host(hardwareHost)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"OsCommand test ran but failed:\n" + osCommand.toString() +
						"\n\n" +
						"Actual result:\n" + result,
						criterionTestResult.getMessage());
		assertEquals(result, criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testVisitOsCommandLocalWindows() {

		final String result = "Test";

		final OsCommand osCommand = new OsCommand();
		osCommand.setCommandLine("ECHO Test");
		osCommand.setExpectedResult(result);
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No display.");

		final SshProtocol sshProtocol = SshProtocol.builder()
				.username(" ")
				.password("pwd".toCharArray())
				.build();

		final HardwareHost hardwareHost = new HardwareHost("id", "localhost", HostType.MS_WINDOWS);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.host(hardwareHost)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"OsCommand test succeeded:\n" + osCommand.toString() +
					"\n\n" +
					"Result: " + result,
				criterionTestResult.getMessage());
		assertEquals(result, criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(LINUX)
	void testVisitOsCommandLocalLinux() {

		final String result = "Test";

		final OsCommand osCommand = new OsCommand();
		osCommand.setCommandLine("echo Test");
		osCommand.setExpectedResult(result);
		osCommand.setExecuteLocally(true);
		osCommand.setErrorMessage("No display.");

		final SshProtocol sshProtocol = SshProtocol.builder()
				.username(" ")
				.password("pwd".toCharArray())
				.build();

		final HardwareHost hardwareHost = new HardwareHost("id", "localhost", HostType.LINUX);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(sshProtocol.getClass(), sshProtocol))
				.host(hardwareHost)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"OsCommand test succeeded:\n" + osCommand.toString() +
					"\n\n" +
					"Result: " + result,
				criterionTestResult.getMessage());
		assertEquals(result, criterionTestResult.getResult());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testVisitOsCommandMultiLinesCaseInsensitiveWin() {

		assertOsCommandMultiLinesCaseInsensitive("echo First & echo Second");

	}

	@Test
	@EnabledOnOs(LINUX)
	void testVisitOsCommandMultiLinesCaseInsensitiveLinux() {

		assertOsCommandMultiLinesCaseInsensitive("printf '%s\\n%s' First Second");

	}

	/**
	 * Assert {@link OsCommand} returning multiple lines with case insensitive
	 * expected result
	 *
	 * @param cmd the command we wish to run
	 */
	private void assertOsCommandMultiLinesCaseInsensitive(final String cmd) {

		final OsCommand osCommand = OsCommand.builder()
				.commandLine(cmd)
				.expectedResult("^second")
				.executeLocally(true)
				.errorMessage("No display.")
				.build();

		final OsCommandConfig osCommandConfig = OsCommandConfig.builder().build();

		final HardwareHost hardwareHost = new HardwareHost("id", "localhost", HostType.STORAGE);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(OsCommandConfig.class, osCommandConfig))
				.host(hardwareHost)
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		{
			// Success
			final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);
			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
		}

		{
			// Failed
			osCommand.setExpectedResult("^doesn't match");

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(osCommand);
			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
		}
	}

	@Test
	void testVisitProcessProcessNull() {
		final Process process = null;

        final EngineConfiguration engineConfiguration = EngineConfiguration
                .builder()
                .host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX).build())
                .build();
        doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(process));
	}

	@Test
	void testVisitProcessCommandLineNull() {
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
                .host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX).build())
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		final Process process = new Process();
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(process));
	}

	@Test
	void testVisitProcessCommandLineEmpty() {
		final Process process = new Process();
		process.setProcessCommandLine("");

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
                .host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX).build())
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No test will be performed.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitProcessNotLocalHost() {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");
		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX).build())
				.build();
		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(new HostMonitoring()).when(strategyConfig).getHostMonitoring();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No test will be performed remotely.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitProcessUnknownOS() {
		final Process process = new Process();
		process.setProcessCommandLine("MBM[5-9]\\.exe");

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
                .host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX).build())
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.empty());

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

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
                .host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX).build())
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.of(LocalOsHandler.WINDOWS));
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOsVersion).thenReturn(Optional.of("5.1"));

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

		final WmiProtocol wmiProtocol = WmiProtocol.builder()
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

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.of(LocalOsHandler.WINDOWS));
			mockedLocalOSHandler.when(LocalOsHandler::getSystemOsVersion).thenReturn(Optional.of("5.1"));

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

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX).build())
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
				final MockedStatic<CriterionProcessVisitor> mockedCriterionProcessVisitorImpl = mockStatic(CriterionProcessVisitor.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.of(LocalOsHandler.LINUX));
			mockedCriterionProcessVisitorImpl.when(CriterionProcessVisitor::listAllLinuxProcesses).thenReturn(
					List.of(
							List.of("1", "ps", "root", "0", "ps -A -o pid,comm,ruser,ppid,args"),
							List.of("10564","eclipse.exe", "user", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")));

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"No currently running processes match the following regular expression:\n" +
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

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
                .host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX).build())
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class);
				final MockedStatic<CriterionProcessVisitor> mockedCriterionProcessVisitorImpl = mockStatic(CriterionProcessVisitor.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.of(LocalOsHandler.LINUX));
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

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX).build())
				.build();

		final HostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setLocalhost(true);

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

		try (final MockedStatic<LocalOsHandler> mockedLocalOSHandler = mockStatic(LocalOsHandler.class)) {
			mockedLocalOSHandler.when(LocalOsHandler::getOs).thenReturn(Optional.of(LocalOsHandler.AIX));

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(process);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals("Process presence check: No tests will be performed for OS: aix.", criterionTestResult.getMessage());
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
				.host(HardwareHost.builder()
						.hostname(HOST_WIN)
						.build())
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final Service service = new Service();
		service.setServiceName("TWGIPC");

		assertTrue(criterionVisitor.visit(service).getMessage().contains(CriterionVisitor.NEITHER_WMI_NOR_WINRM_ERROR));
	}

	@Test
	void testVisitServiceCheckOsNull() {
		final WmiProtocol wmiProtocol = WmiProtocol.builder()
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder()
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
		assertTrue(criterionTestResult.getMessage().contains("Host OS is not Windows"));
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitServiceCheckOsNotWindows() {
		final WmiProtocol wmiProtocol = WmiProtocol.builder()
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder()
						.hostname(HOST_WIN)
						.type(HostType.LINUX)
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
		final WmiProtocol wmiProtocol = WmiProtocol.builder()
				.username(USERNAME)
				.password(PASSWORD)
				.timeout(TIME_OUT)
				.build();

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder()
						.hostname(HOST_WIN)
						.id(HOST_WIN)
						.type(HostType.MS_WINDOWS)
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
				&& engineConfiguration.getProtocolConfigurations().get(SnmpProtocol.class) != null) {

			return;
		}

		final SnmpProtocol protocol = SnmpProtocol
				.builder()
				.community("public")
				.version(SnmpVersion.V1)
				.port(161)
				.timeout(120L)
				.build();

		engineConfiguration = EngineConfiguration
				.builder()
				.host(HardwareHost.builder().hostname(ECS1_01).id(ECS1_01).type(HostType.LINUX).build())
				.protocolConfigurations(Map.of(SnmpProtocol.class, protocol))
				.build();
	}

	@Test
	void testVisitSNMPGetException() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doThrow(new TimeoutException("SNMPGet timeout")).when(matsyaClientsExecutor).executeSNMPGet(any(),
				any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGet.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - SNMP test failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 was unsuccessful due to an exception. Message: SNMPGet timeout")
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNullResult() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(null).when(matsyaClientsExecutor).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGet.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - SNMP test failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 was unsuccessful due to a null result")
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetEmptyResult() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(EMPTY).when(matsyaClientsExecutor).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGet.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - SNMP test failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 was unsuccessful due to an empty result.")
				.result(EMPTY).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetSuccessWithNoExpectedResult() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(UCS_SYSTEM_CISCO_RESULT).when(matsyaClientsExecutor).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGet.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - Successful SNMP Get of 1.3.6.1.4.1.674.10893.1.20. Returned result: UCS System Cisco.")
				.result(UCS_SYSTEM_CISCO_RESULT)
				.success(true).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetExpectedResultNotMatches() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(UCS_SYSTEM_CISCO_RESULT).when(matsyaClientsExecutor).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGet.builder().oid(OID).expectedResult(VERSION).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - SNMP test failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 was successful but the value of the returned OID did not match with the expected result. Expected value: 2.4.6 - returned value UCS System Cisco.")
				.result(UCS_SYSTEM_CISCO_RESULT)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetExpectedResultMatches() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(UCS_SYSTEM_CISCO_RESULT).when(matsyaClientsExecutor).executeSNMPGet(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGet.builder().oid(OID).expectedResult(UCS_EXPECTED).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - Successful SNMP Get of 1.3.6.1.4.1.674.10893.1.20. Returned result: UCS System Cisco")
				.result(UCS_SYSTEM_CISCO_RESULT)
				.success(true)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetReturnsEmptyResult() {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit((SnmpGet) null));
		assertEquals(CriterionTestResult.empty(),
				criterionVisitor.visit(SnmpGet.builder().oid(null).build()));
		assertNull(criterionVisitor.visit(SnmpGet.builder().oid(OID).build()).getResult());
	}

	@Test
	void testVisitSshInteractiveNull() {

		final SshInteractive sshInteractive = null;

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().toLowerCase().contains("malformed"));
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitSshInteractiveStepsNull() {

		final SshInteractive sshInteractive = SshInteractive.builder().build();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().toLowerCase().contains("malformed"));
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitSshInteractiveNoCredential() throws Exception {

		final SshInteractive sshInteractive = new SshInteractive();
		sshInteractive.setExpectedResult("HP.* BladeSystem Onboard Administrator");

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()))
				.host(new HardwareHost("id", "host", HostType.LINUX))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Error in SshInteractive test:\n" + sshInteractive.toString() +
				"\n\n" +
				"No credentials provided.",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitSshInteractiveNotFoundStepResult() throws Exception {

		final WaitFor waitFor = new WaitFor();
		waitFor.setIndex(1);
		waitFor.setText("$");

		final List<Step> steps = List.of(waitFor);

		final SshInteractive sshInteractive = new SshInteractive();
		sshInteractive.setIndex(1);
		sshInteractive.setExpectedResult("HP.* BladeSystem Onboard Administrator");
		sshInteractive.setSteps(steps);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()))
				.host(new HardwareHost("id", "host", HostType.LINUX))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			final String message = "Step(1) WaitFor: hostname: host - Disconnected or timeout while waiting for \"$\" through SSH";

			final StepException stepException = new StepException(message);

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive detection.criteria(1)")).thenThrow(stepException);

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"Error in SshInteractive test:\n" + sshInteractive.toString() +
					"\n\n" +
					message,
					criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitSshInteractiveIOException() throws Exception {

		final WaitFor waitFor = new WaitFor();
		waitFor.setIndex(1);
		waitFor.setText("$");

		final List<Step> steps = List.of(waitFor);

		final SshInteractive sshInteractive = new SshInteractive();
		sshInteractive.setIndex(1);
		sshInteractive.setExpectedResult("HP.* BladeSystem Onboard Administrator");
		sshInteractive.setSteps(steps);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()))
				.host(new HardwareHost("id", "host", HostType.LINUX))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			final String stepName = "Step(1) WaitFor: hostname: host";

			final IOException ioException = new IOException("Error in read");
			final StepException stepException = new StepException(stepName, ioException);

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive detection.criteria(1)")).thenThrow(stepException);

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"Error in SshInteractive test:\n" + sshInteractive.toString() +
					"\n\n" +
					stepName + " - IOException: Error in read",
					criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitSshInteractiveExpectedResultNullAndResultEmpy() {

		final WaitFor waitFor = new WaitFor();
		waitFor.setIndex(1);
		waitFor.setText("$");

		final List<Step> steps = List.of(waitFor);

		final SshInteractive sshInteractive = new SshInteractive();
		sshInteractive.setIndex(1);
		sshInteractive.setSteps(steps);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()))
				.host(new HardwareHost("id", "host", HostType.LINUX))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive detection.criteria(1)")).thenReturn(List.of());

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"SshInteractive test ran but failed:\n" + sshInteractive.toString() +
					"\n\n" +
					"Actual result:\n",
					criterionTestResult.getMessage());
			assertEquals(HardwareConstants.EMPTY, criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitSshInteractiveExpectedResultAndResultEmpty() {

		final WaitFor waitFor = new WaitFor();
		waitFor.setIndex(1);
		waitFor.setText("$");

		final List<Step> steps = List.of(waitFor);

		final SshInteractive sshInteractive = new SshInteractive();
		sshInteractive.setIndex(1);
		sshInteractive.setExpectedResult(HardwareConstants.EMPTY);
		sshInteractive.setSteps(steps);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()))
				.host(new HardwareHost("id", "host", HostType.LINUX))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive detection.criteria(1)")).thenReturn(List.of());

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"SshInteractive test ran but failed:\n" + sshInteractive.toString() +
					"\n\n" +
					"Actual result:\n",
					criterionTestResult.getMessage());
			assertEquals(HardwareConstants.EMPTY, criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitSshInteractiveResultFailure() throws Exception {

		final WaitFor waitFor = new WaitFor();
		waitFor.setIndex(1);
		waitFor.setText("$");

		final List<Step> steps = List.of(waitFor);

		final SshInteractive sshInteractive = new SshInteractive();
		sshInteractive.setIndex(1);
		sshInteractive.setExpectedResult("HP.* BladeSystem Onboard Administrator");
		sshInteractive.setSteps(steps);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()))
				.host(new HardwareHost("id", "host", HostType.LINUX))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			final String result = "Emulator screen";

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive detection.criteria(1)")).thenReturn(List.of(result));

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"SshInteractive test ran but failed:\n" + sshInteractive.toString() +
					"\n\n" +
					"Actual result:\n" + result,
					criterionTestResult.getMessage());
			assertEquals(result, criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitSshInteractiveExpectedResultNullAndResultNotEmpy() {

		final WaitFor waitFor = new WaitFor();
		waitFor.setIndex(1);
		waitFor.setText("$");

		final List<Step> steps = List.of(waitFor);

		final SshInteractive sshInteractive = new SshInteractive();
		sshInteractive.setIndex(1);
		sshInteractive.setSteps(steps);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()))
				.host(new HardwareHost("id", "host", HostType.LINUX))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			final String result = "\n\n\nHP BladeSystem Onboard Administrator\n" +
					"(C) Copyright 2006-2015 Hewlett-Packard Development Company, L.P.\n";

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive detection.criteria(1)")).thenReturn(List.of(result));

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"SshInteractive test succeeded:\n" + sshInteractive.toString() +
						"\n\n" +
						"Result: " + result,
					criterionTestResult.getMessage());
			assertEquals(result, criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitSshInteractiveExpectedResultAndResultNotEmpty() {

		final WaitFor waitFor = new WaitFor();
		waitFor.setIndex(1);
		waitFor.setText("$");

		final List<Step> steps = List.of(waitFor);

		final SshInteractive sshInteractive = new SshInteractive();
		sshInteractive.setIndex(1);
		sshInteractive.setExpectedResult(HardwareConstants.EMPTY);
		sshInteractive.setSteps(steps);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()))
				.host(new HardwareHost("id", "host", HostType.LINUX))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			final String result = "\n\n\nHP BladeSystem Onboard Administrator\n" +
					"(C) Copyright 2006-2015 Hewlett-Packard Development Company, L.P.\n";

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive detection.criteria(1)")).thenReturn(List.of(result));

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"SshInteractive test succeeded:\n" + sshInteractive.toString() +
						"\n\n" +
						"Result: " + result,
					criterionTestResult.getMessage());
			assertEquals(result, criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitSshInteractiveOK() throws Exception {

		final WaitFor waitFor = new WaitFor();
		waitFor.setIndex(1);
		waitFor.setText("$");

		final List<Step> steps = List.of(waitFor);

		final SshInteractive sshInteractive = new SshInteractive();
		sshInteractive.setIndex(1);
		sshInteractive.setExpectedResult("HP.* BladeSystem Onboard Administrator");
		sshInteractive.setSteps(steps);

		final EngineConfiguration engineConfiguration = EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()))
				.host(new HardwareHost("id", "host", HostType.LINUX))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			final String result = "\n\n\nHP BladeSystem Onboard Administrator\n" +
					"(C) Copyright 2006-2015 Hewlett-Packard Development Company, L.P.\n";

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive detection.criteria(1)")).thenReturn(List.of(result));

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"SshInteractive test succeeded:\n" + sshInteractive.toString() +
						"\n\n" +
						"Result: " + result,
					criterionTestResult.getMessage());
		}
		
		// Test to see if case insensitive matches are allowed
		
		try (final MockedStatic<SshInteractiveHelper> mockedSshInteractiveHelper = mockStatic(SshInteractiveHelper.class)) {

			String result = "\n\n\nHP BLADESYSTEM ONBOARD ADMINISTRATOR\n" +
					"(C) Copyright 2006-2015 Hewlett-Packard Development Company, L.P.\n";

			mockedSshInteractiveHelper.when(() -> SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, "sshInteractive detection.criteria(1)")).thenReturn(List.of(result));

			final CriterionTestResult criterionTestResult = criterionVisitor.visit(sshInteractive);

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"SshInteractive test succeeded:\n" + sshInteractive.toString() +
						"\n\n" +
						"Result: " + result,
					criterionTestResult.getMessage());
		}
	}

	@Test
	void testVisitUCS() {
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit(new Ucs()));
	}

	@Test
	void testVisitSNMPGetNextException() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doThrow(new TimeoutException("SNMPGetNext timeout")).when(matsyaClientsExecutor).executeSNMPGetNext(any(),
				any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGetNext.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - SNMP test failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 was unsuccessful due to an exception. Message: SNMPGetNext timeout")
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextNullResult() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(null).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGetNext.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - SNMP test failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 was unsuccessful due to a null result.")
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextEmptyResult() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(EMPTY).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGetNext.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - SNMP test failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 was unsuccessful due to an empty result.")
				.result(EMPTY).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextNotSameSubTreeOID() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(RESULT_1).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGetNext.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - SNMP test failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 was successful but the returned OID is not under the same tree. Returned OID: 1.3.6.1.4.1.674.99999.1.20.1.")
				.result(RESULT_1).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextSuccessWithNoExpectedResult() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(RESULT_2).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGetNext.builder().oid(OID).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - Successful SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20. Returned result: 1.3.6.1.4.1.674.10893.1.20.1 ASN_INTEGER 1.")
				.result(RESULT_2)
				.success(true).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextExpectedResultNotMatches() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(RESULT_2).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGetNext.builder().oid(OID).expectedResult(VERSION).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - SNMP test failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 was successful but the value of the returned OID did not match with the expected result. Expected value: 2.4.6 - returned value 1.")
				.result(RESULT_2)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextExpectedResultMatches() throws Exception {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		doReturn(RESULT_3).when(matsyaClientsExecutor).executeSNMPGetNext(any(), any(), any(), eq(false));
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGetNext.builder().oid(OID).expectedResult(VERSION).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - Successful SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20. Returned result: 1.3.6.1.4.1.674.10893.1.20.1 ASN_OCT 2.4.6.")
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
		final CriterionTestResult actual = criterionVisitor.visit(SnmpGetNext.builder().oid(OID).expectedResult(VERSION).build());
		final CriterionTestResult expected = CriterionTestResult.builder().message(
				"Hostname ecs1-01 - SNMP test failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 was successful but the value cannot be extracted. Returned result: 1.3.6.1.4.1.674.10893.1.20.1 ASN_OCT.")
				.result(RESULT_4)
				.build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSNMPGetNextReturnsEmptyResult() {

		initSNMP();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		assertEquals(CriterionTestResult.empty(), criterionVisitor.visit((SnmpGetNext) null));
		assertEquals(CriterionTestResult.empty(),
				criterionVisitor.visit(SnmpGetNext.builder().oid(null).build()));
		assertNull(criterionVisitor.visit(SnmpGetNext.builder().oid(OID).build()).getResult());
	}

	@Test
	void testVisitWmiBadCriterion() {
		CriterionTestResult result = criterionVisitor.visit(Wmi.builder()
				.wbemNamespace(AUTOMATIC)
				.expectedResult(null)
				.build());
		assertTrue(result.getMessage().contains("Malformed criterion"));
		result = criterionVisitor.visit((Wmi) null);
		assertTrue(result.getMessage().contains("Malformed criterion"));
	}

	@Test
	void testVisitWmiNoProtocol() {
		final Wmi wmi = Wmi.builder().wbemQuery(WMI_WQL).wbemNamespace(AUTOMATIC).expectedResult(null).build();
		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(PC14).id(PC14).type(HostType.MS_WINDOWS).build())
				.protocolConfigurations(Map.of(SnmpProtocol.class, new SnmpProtocol()))
				.build();

		doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();

		assertTrue(criterionVisitor.visit(wmi).getMessage().contains(CriterionVisitor.NEITHER_WMI_NOR_WINRM_ERROR));
	}

}
