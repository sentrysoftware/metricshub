package com.sentrysoftware.hardware.cli.component.cli;

import com.sentrysoftware.hardware.cli.component.cli.protocols.HttpConfigCli;
import com.sentrysoftware.hardware.cli.component.cli.protocols.SnmpConfigCli;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WbemConfigCli;
import com.sentrysoftware.hardware.cli.component.cli.protocols.WmiConfigCli;
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

	private static final String DEVICE_TYPE_OPTION = "--type";

	private static final String HTTP_OPTION = "--http";
	private static final String HTTP_PORT_OPTION = "--http-port";
	private static final String HTTP_TIMEOUT_OPTION = "--http-timeout";
	private static final String HTTP_USERNAME_OPTION = "--http-username";
	private static final String HTTP_PASSWORD_OPTION = "--http-password";

	@Test
	void snmpArgumentsTest() {
		String[] args_hdfs = { "hostaa",
				"-t", "hpux",
				"--snmp", "v2c",
				"--snmp-port", "200",
				"--snmp-community", "private",
				"--snmp-timeout", "60",
				"-f", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCli sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_hdfs);

		assertEquals("hostaa", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V2C, sentryCli.getSnmpConfigCli().getSnmpVersion());
		assertEquals(200, sentryCli.getSnmpConfigCli().getPort());
		assertEquals("private", sentryCli.getSnmpConfigCli().getCommunity());
		assertEquals(60, sentryCli.getSnmpConfigCli().getTimeout());
		assertEquals(sentryCli.getConnectors(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		assertNull(sentryCli.getExcludedConnectors());


		String[] args_exclud_hdfs = { "hosta",
				"-t", "hp-ux",
				"--snmp", "2",
				"--snmp-port", "200",
				"--snmp-community", "private",
				"--snmp-timeout", "60",
				"-x", "hdfs1,hdfs2" };

		sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_exclud_hdfs);
		assertEquals("hosta", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V2C, sentryCli.getSnmpConfigCli().getSnmpVersion());
		assertEquals(200, sentryCli.getSnmpConfigCli().getPort());
		assertEquals("private", sentryCli.getSnmpConfigCli().getCommunity());
		assertEquals(60, sentryCli.getSnmpConfigCli().getTimeout());
		assertEquals(sentryCli.getExcludedConnectors(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2")));
		assertNull(sentryCli.getConnectors());

		String[] args_default_snmp = { "hostaa",
				"-t", "HPUX",
				"--snmp", "1",
				"-f", "hdfs1,hdfs2" };

		sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_default_snmp);
		assertEquals("hostaa", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V1, sentryCli.getSnmpConfigCli().getSnmpVersion());
		assertEquals(161, sentryCli.getSnmpConfigCli().getPort());
		assertEquals("public", sentryCli.getSnmpConfigCli().getCommunity());
		assertEquals(SnmpConfigCli.DEFAULT_TIMEOUT, sentryCli.getSnmpConfigCli().getTimeout());
		assertEquals(sentryCli.getConnectors(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2")));
		assertNull(sentryCli.getExcludedConnectors());

		String[] args_required_host = {
				"-t", "hpux",
				"-f", "hdfs1,hdfs2" };

		final CommandLine commandLine = new CommandLine(new HardwareSentryCli());

		assertThrows(MissingParameterException.class, () -> commandLine.parseArgs(args_required_host));

	}

	@Test
	void wbemArgumentsTest() {
		String[] args_hdfs = { "hostaa",
				"-t", "hpux",
				"--wbem-transport", "HTTP",
				"--wbem-port", "5989",
				"--wbem-force-namespace", "root/emc",
				"--wbem-timeout", "120",
				"--wbem-username", "admin",
				"--wbem-password", "#1Password",
				"-f", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCli sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_hdfs);

		assertEquals("hostaa", sentryCli.getHostname());
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(WBEMProtocol.WBEMProtocols.HTTP, sentryCli.getWbemConfigCli().getProtocol());
		assertEquals(5989, sentryCli.getWbemConfigCli().getPort());
		assertEquals("root/emc", sentryCli.getWbemConfigCli().getNamespace());
		assertEquals(120, sentryCli.getWbemConfigCli().getTimeout());
		assertEquals("admin", sentryCli.getWbemConfigCli().getUsername());
		assertArrayEquals("#1Password".toCharArray(), sentryCli.getWbemConfigCli().getPassword());
		assertEquals(sentryCli.getConnectors(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		assertNull(sentryCli.getExcludedConnectors());

		String[] args_hdfs2 = { "dev-hv-01",
				"-t", "win",
				"--wbem-transport", "https",
				"--wbem-port", "5989",
				"--wbem-force-namespace", "root/emc",
				"--wbem-username", "admin",
				"--wbem-password", "#1Password",
				"-f", "hdfs1,hdfs2,hdfs3" };

		new CommandLine(sentryCli).parseArgs(args_hdfs2);

		assertEquals("dev-hv-01", sentryCli.getHostname());
		assertEquals(TargetType.MS_WINDOWS, sentryCli.getDeviceType());
		assertEquals(WBEMProtocol.WBEMProtocols.HTTPS, sentryCli.getWbemConfigCli().getProtocol());
		assertEquals(5989, sentryCli.getWbemConfigCli().getPort());
		assertEquals("root/emc", sentryCli.getWbemConfigCli().getNamespace());
		assertEquals(WbemConfigCli.DEFAULT_TIMEOUT, sentryCli.getWbemConfigCli().getTimeout());
		assertEquals("admin", sentryCli.getWbemConfigCli().getUsername());
		assertArrayEquals("#1Password".toCharArray(), sentryCli.getWbemConfigCli().getPassword());
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
		String[] arguments = {HOST, DEVICE_TYPE_OPTION, TargetType.LINUX.toString()};
		HardwareSentryCli hardwareSentryCli = new HardwareSentryCli();
		CommandLine commandLine = new CommandLine(hardwareSentryCli);
		commandLine.parseArgs(arguments);
		assertEquals(HOST, hardwareSentryCli.getHostname());
		assertEquals(TargetType.LINUX, hardwareSentryCli.getDeviceType());
		assertNull(hardwareSentryCli.getHttpConfigCli());

		// Default values
		arguments = new String[]{HOST, DEVICE_TYPE_OPTION, TargetType.LINUX.toString(),
			HTTP_USERNAME_OPTION, USER, HTTP_PASSWORD_OPTION, PASS};
		commandLine.parseArgs(arguments);
		assertEquals(HOST, hardwareSentryCli.getHostname());
		assertEquals(TargetType.LINUX, hardwareSentryCli.getDeviceType());
		HttpConfigCli httpConfigCli = hardwareSentryCli.getHttpConfigCli();
		assertNotNull(httpConfigCli);
		assertNull(httpConfigCli.getHttpOrHttps());
		assertNull(httpConfigCli.getPort());
		assertEquals(HttpConfigCli.DEFAULT_TIMEOUT, httpConfigCli.getTimeout());
		assertEquals(USER, httpConfigCli.getUsername());
		assertEquals(PASS, new String(httpConfigCli.getPassword()));

		// Explicit values
		arguments = new String[]{HOST, DEVICE_TYPE_OPTION, TargetType.LINUX.toString(),
			HTTP_USERNAME_OPTION, USER, HTTP_PASSWORD_OPTION, PASS, HTTP_OPTION,
			HTTP_PORT_OPTION, PORT, HTTP_TIMEOUT_OPTION, TIMEOUT};
		commandLine.parseArgs(arguments);
		assertEquals(HOST, hardwareSentryCli.getHostname());
		assertEquals(TargetType.LINUX, hardwareSentryCli.getDeviceType());
		httpConfigCli = hardwareSentryCli.getHttpConfigCli();
		assertNotNull(httpConfigCli);
		assertTrue(httpConfigCli.getHttpOrHttps().isHttp());
		assertEquals(Integer.parseInt(PORT), httpConfigCli.getPort());
		assertEquals(Long.parseLong(TIMEOUT), httpConfigCli.getTimeout());
		assertEquals(USER, httpConfigCli.getUsername());
		assertEquals(PASS, new String(httpConfigCli.getPassword()));
	}

	@Test
	void wmiArgumentsTest() {
		{
			String[] args_hdfs = { "hostaa",
					"-t", "win",
					"--wmi-force-namespace", "root\\ibmsd",
					"--wmi-username", "admin",
					"--wmi-password", "#1Password",
					"-f", "hdfs1,hdfs2,hdfs3" };
			HardwareSentryCli sentryCli = new HardwareSentryCli();
			new CommandLine(sentryCli).parseArgs(args_hdfs);

			assertEquals("hostaa", sentryCli.getHostname());
			assertEquals(TargetType.MS_WINDOWS, sentryCli.getDeviceType());
			assertEquals("root\\ibmsd", sentryCli.getWmiConfigCli().getNamespace());
			assertEquals(WmiConfigCli.DEFAULT_TIMEOUT, sentryCli.getWmiConfigCli().getTimeout());
			assertEquals("admin", sentryCli.getWmiConfigCli().getUsername());
			assertArrayEquals("#1Password".toCharArray(), sentryCli.getWmiConfigCli().getPassword());
			assertEquals(sentryCli.getConnectors(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		}

		{
			String[] args_hdfs = {
					"localhost",
					"-t", "windows",
					"--wmi"
			};
			HardwareSentryCli sentryCli = new HardwareSentryCli();
			new CommandLine(sentryCli).parseArgs(args_hdfs);

			assertEquals("localhost", sentryCli.getHostname());
			assertEquals(TargetType.MS_WINDOWS, sentryCli.getDeviceType());
			assertNotNull(sentryCli.getWmiConfigCli());
		}

	}
}
