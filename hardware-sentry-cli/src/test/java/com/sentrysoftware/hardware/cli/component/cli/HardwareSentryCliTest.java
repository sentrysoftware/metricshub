package com.sentrysoftware.hardware.cli.component.cli;

import com.sentrysoftware.hardware.cli.component.cli.protocols.HttpConfig;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.target.TargetType;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class HardwareSentryCliTest {

	private static final String HOST_OPTION = "-host";
	private static final String DEVICE_TYPE_OPTION = "-dt";

	private static final String HTTP_OPTION = "--http";
	private static final String HTTP_PORT_OPTION = "--http-port";
	private static final String HTTP_TIMEOUT_OPTION = "--http-timeout";
	private static final String HTTP_USERNAME_OPTION = "--http-username";
	private static final String HTTP_PASSWORD_OPTION = "--http-password";

	@Test
	void snmpArgumentsTest() {
		String[] args_hdfs = { "-host", "hostaa",
				"-dt", "HP_UX",
				"--snmp", "v2c",
				"--snmp-port", "200",
				"--snmp-community", "private",
				"--snmp-timeout", "60",
				"-hdf", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCli sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_hdfs);

		assertEquals("hostaa", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V2C, sentryCli.getSnmpConfig().getSnmpVersion());
		assertEquals(200, sentryCli.getSnmpConfig().getPort());
		assertEquals("private", sentryCli.getSnmpConfig().getCommunity());
		assertEquals(60, sentryCli.getSnmpConfig().getTimeout());
		assertEquals(sentryCli.getConnectors(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		assertNull(sentryCli.getExcludedConnectors());


		String[] args_exclud_hdfs = { "-host", "hosta",
				"-dt", "HP_UX",
				"--snmp", "2",
				"--snmp-port", "200",
				"--snmp-community", "private",
				"--snmp-timeout", "60",
				"-exclude", "hdfs1,hdfs2" };

		sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_exclud_hdfs);
		assertEquals("hosta", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V2C, sentryCli.getSnmpConfig().getSnmpVersion());
		assertEquals(200, sentryCli.getSnmpConfig().getPort());
		assertEquals("private", sentryCli.getSnmpConfig().getCommunity());
		assertEquals(60, sentryCli.getSnmpConfig().getTimeout());
		assertEquals(sentryCli.getExcludedConnectors(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2")));
		assertNull(sentryCli.getConnectors());

		String[] args_default_snmp = { "-host", "hostaa",
				"-dt", "HP_UX",
				"--snmp", "1",
				"-hdf", "hdfs1,hdfs2" };

		sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_default_snmp);
		assertEquals("hostaa", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V1, sentryCli.getSnmpConfig().getSnmpVersion());
		assertEquals(161, sentryCli.getSnmpConfig().getPort());
		assertEquals("public", sentryCli.getSnmpConfig().getCommunity());
		assertEquals(120, sentryCli.getSnmpConfig().getTimeout());
		assertEquals(sentryCli.getConnectors(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2")));
		assertNull(sentryCli.getExcludedConnectors());

		String[] args_required_host = {
				"-dt", "HP_UX",
				"-hdf", "hdfs1,hdfs2" };

		final CommandLine commandLine = new CommandLine(new HardwareSentryCli());

		assertThrows(MissingParameterException.class, () -> commandLine.parseArgs(args_required_host));

	}

	@Test
	void wbemArgumentsTest() {
		String[] args_hdfs = { "-host", "hostaa",
				"-dt", "HP_UX",
				"--wbem-transport", "HTTP",
				"--wbem-port", "5989",
				"--wbem-force-namespace", "root/emc",
				"--wbem-timeout", "120",
				"--wbem-username", "admin",
				"--wbem-password", "#1Password",
				"-hdf", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCli sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_hdfs);

		assertEquals("hostaa", sentryCli.getHostname());
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(WBEMProtocol.WBEMProtocols.HTTP, sentryCli.getWbemConfig().getProtocol());
		assertEquals(5989, sentryCli.getWbemConfig().getPort());
		assertEquals("root/emc", sentryCli.getWbemConfig().getNamespace());
		assertEquals(120, sentryCli.getWbemConfig().getTimeout());
		assertEquals("admin", sentryCli.getWbemConfig().getUsername());
		assertArrayEquals("#1Password".toCharArray(), sentryCli.getWbemConfig().getPassword());
		assertEquals(sentryCli.getConnectors(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		assertNull(sentryCli.getExcludedConnectors());

		String[] args_hdfs2 = { "-host", "dev-hv-01",
				"-dt", "MS_WINDOWS",
				"--wbem-transport", "https",
				"--wbem-port", "5989",
				"--wbem-force-namespace", "root/emc",
				"--wbem-timeout", "120",
				"--wbem-username", "admin",
				"--wbem-password", "#1Password",
				"-hdf", "hdfs1,hdfs2,hdfs3" };

		new CommandLine(sentryCli).parseArgs(args_hdfs2);

		assertEquals("dev-hv-01", sentryCli.getHostname());
		assertEquals(TargetType.MS_WINDOWS, sentryCli.getDeviceType());
		assertEquals(WBEMProtocol.WBEMProtocols.HTTPS, sentryCli.getWbemConfig().getProtocol());
		assertEquals(5989, sentryCli.getWbemConfig().getPort());
		assertEquals("root/emc", sentryCli.getWbemConfig().getNamespace());
		assertEquals(120, sentryCli.getWbemConfig().getTimeout());
		assertEquals("admin", sentryCli.getWbemConfig().getUsername());
		assertArrayEquals("#1Password".toCharArray(), sentryCli.getWbemConfig().getPassword());
		assertEquals(sentryCli.getConnectors(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		assertNull(sentryCli.getExcludedConnectors());

	}

	@Test
	void httpCredentialsTest() {

		final String HOST = "host";
		final String USER = "user";
		final String PASS = "pass";
		final String PORT = "443";
		final String TIMEOUT = "60";

		// No HTTP credentials
		String[] arguments = {HOST_OPTION, HOST, DEVICE_TYPE_OPTION, TargetType.LINUX.toString()};
		HardwareSentryCli hardwareSentryCli = new HardwareSentryCli();
		CommandLine commandLine = new CommandLine(hardwareSentryCli);
		commandLine.parseArgs(arguments);
		assertEquals(HOST, hardwareSentryCli.getHostname());
		assertEquals(TargetType.LINUX, hardwareSentryCli.getDeviceType());
		assertNull(hardwareSentryCli.getHttpConfig());

		// Default values
		arguments = new String[]{HOST_OPTION, HOST, DEVICE_TYPE_OPTION, TargetType.LINUX.toString(),
			HTTP_USERNAME_OPTION, USER, HTTP_PASSWORD_OPTION, PASS};
		commandLine.parseArgs(arguments);
		assertEquals(HOST, hardwareSentryCli.getHostname());
		assertEquals(TargetType.LINUX, hardwareSentryCli.getDeviceType());
		HttpConfig httpConfig = hardwareSentryCli.getHttpConfig();
		assertNotNull(httpConfig);
		assertNull(httpConfig.getHttpOrHttps());
		assertEquals(443, httpConfig.getPort());
		assertEquals(120L, httpConfig.getTimeout());
		assertEquals(USER, httpConfig.getUsername());
		assertEquals(PASS, new String(httpConfig.getPassword()));

		// Explicit values
		arguments = new String[]{HOST_OPTION, HOST, DEVICE_TYPE_OPTION, TargetType.LINUX.toString(),
			HTTP_USERNAME_OPTION, USER, HTTP_PASSWORD_OPTION, PASS, HTTP_OPTION,
			HTTP_PORT_OPTION, PORT, HTTP_TIMEOUT_OPTION, TIMEOUT};
		commandLine.parseArgs(arguments);
		assertEquals(HOST, hardwareSentryCli.getHostname());
		assertEquals(TargetType.LINUX, hardwareSentryCli.getDeviceType());
		httpConfig = hardwareSentryCli.getHttpConfig();
		assertNotNull(httpConfig);
		assertTrue(httpConfig.getHttpOrHttps().isHttp());
		assertEquals(Integer.parseInt(PORT), httpConfig.getPort());
		assertEquals(Long.parseLong(TIMEOUT), httpConfig.getTimeout());
		assertEquals(USER, httpConfig.getUsername());
		assertEquals(PASS, new String(httpConfig.getPassword()));
	}

	@Test
	void wmiArgumentsTest() {
		{
			String[] args_hdfs = { "-host", "hostaa",
					"-dt", "MS_WINDOWS",
					"--wmi-force-namespace", "root\\ibmsd",
					"--wmi-username", "admin",
					"--wmi-password", "#1Password",
					"-hdf", "hdfs1,hdfs2,hdfs3" };
			HardwareSentryCli sentryCli = new HardwareSentryCli();
			new CommandLine(sentryCli).parseArgs(args_hdfs);

			assertEquals("hostaa", sentryCli.getHostname());
			assertEquals(TargetType.MS_WINDOWS, sentryCli.getDeviceType());
			assertEquals("root\\ibmsd", sentryCli.getWmiConfig().getNamespace());
			assertEquals(120L, sentryCli.getWmiConfig().getTimeout());
			assertEquals("admin", sentryCli.getWmiConfig().getUsername());
			assertArrayEquals("#1Password".toCharArray(), sentryCli.getWmiConfig().getPassword());
			assertEquals(sentryCli.getConnectors(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		}

		{
			String[] args_hdfs = {
					"-host", "localhost",
					"-dt", "MS_WINDOWS",
					"--wmi"
			};
			HardwareSentryCli sentryCli = new HardwareSentryCli();
			new CommandLine(sentryCli).parseArgs(args_hdfs);

			assertEquals("localhost", sentryCli.getHostname());
			assertEquals(TargetType.MS_WINDOWS, sentryCli.getDeviceType());
			assertNotNull(sentryCli.getWmiConfig());
		}

	}
}
