package com.sentrysoftware.hardware.cli.component.cli;

import com.sentrysoftware.hardware.cli.component.cli.protocols.HttpCredentials;
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
				"-hdfs", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCli sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_hdfs);

		assertEquals("hostaa", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V2C, sentryCli.getSnmpCredentials().getSnmpVersion());
		assertEquals(200, sentryCli.getSnmpCredentials().getPort());
		assertEquals("private", sentryCli.getSnmpCredentials().getCommunity());
		assertEquals(60, sentryCli.getSnmpCredentials().getTimeout());
		assertEquals(sentryCli.getHdfs(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		assertNull(sentryCli.getHdfsExclusion());


		String[] args_exclud_hdfs = { "-host", "hosta",
				"-dt", "HP_UX",
				"--snmp", "2",
				"--snmp-port", "200",
				"--snmp-community", "private",
				"--snmp-timeout", "60",
				"-hdfsExcluded", "hdfs1,hdfs2" };

		sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_exclud_hdfs);
		assertEquals("hosta", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V2C, sentryCli.getSnmpCredentials().getSnmpVersion());
		assertEquals(200, sentryCli.getSnmpCredentials().getPort());
		assertEquals("private", sentryCli.getSnmpCredentials().getCommunity());
		assertEquals(60, sentryCli.getSnmpCredentials().getTimeout());
		assertEquals(sentryCli.getHdfsExclusion(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2")));
		assertNull(sentryCli.getHdfs());

		String[] args_default_snmp = { "-host", "hostaa",
				"-dt", "HP_UX",
				"--snmp", "1",
				"-hdfs", "hdfs1,hdfs2" };

		sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_default_snmp);
		assertEquals("hostaa", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V1, sentryCli.getSnmpCredentials().getSnmpVersion());
		assertEquals(161, sentryCli.getSnmpCredentials().getPort());
		assertEquals("public", sentryCli.getSnmpCredentials().getCommunity());
		assertEquals(120, sentryCli.getSnmpCredentials().getTimeout());
		assertEquals(sentryCli.getHdfs(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2")));
		assertNull(sentryCli.getHdfsExclusion());

		String[] args_required_host = {
				"-dt", "HP_UX",
				"-hdfs", "hdfs1,hdfs2" };

		final CommandLine commandLine = new CommandLine(new HardwareSentryCli());

		assertThrows(MissingParameterException.class, () -> commandLine.parseArgs(args_required_host));

	}

	@Test
	void wbemArgumentsTest() {
		String[] args_hdfs = { "-host", "hostaa",
				"-dt", "HP_UX",
				"--wbem-protocol", "HTTP",
				"--wbem-port", "5989",
				"--wbem-namespace", "root/emc",
				"--wbem-timeout", "120",
				"--wbem-username", "admin",
				"--wbem-password", "#1Password",
				"-hdfs", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCli sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_hdfs);

		assertEquals("hostaa", sentryCli.getHostname());
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(WBEMProtocol.WBEMProtocols.HTTP, sentryCli.getWbemCredentials().getProtocol());
		assertEquals(5989, sentryCli.getWbemCredentials().getPort());
		assertEquals("root/emc", sentryCli.getWbemCredentials().getNamespace());
		assertEquals(120, sentryCli.getWbemCredentials().getTimeout());
		assertEquals("admin", sentryCli.getWbemCredentials().getUsername());
		assertEquals("#1Password", sentryCli.getWbemCredentials().getPassword());
		assertEquals(sentryCli.getHdfs(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		assertNull(sentryCli.getHdfsExclusion());

		String[] args_hdfs2 = { "-host", "dev-hv-01",
				"-dt", "MS_WINDOWS",
				"--wbem-protocol", "https",
				"--wbem-port", "5989",
				"--wbem-namespace", "root/emc",
				"--wbem-timeout", "120",
				"--wbem-username", "admin",
				"--wbem-password", "#1Password",
				"-hdfs", "hdfs1,hdfs2,hdfs3" };

		new CommandLine(sentryCli).parseArgs(args_hdfs2);

		assertEquals("dev-hv-01", sentryCli.getHostname());
		assertEquals(TargetType.MS_WINDOWS, sentryCli.getDeviceType());
		assertEquals(WBEMProtocol.WBEMProtocols.HTTPS, sentryCli.getWbemCredentials().getProtocol());
		assertEquals(5989, sentryCli.getWbemCredentials().getPort());
		assertEquals("root/emc", sentryCli.getWbemCredentials().getNamespace());
		assertEquals(120, sentryCli.getWbemCredentials().getTimeout());
		assertEquals("admin", sentryCli.getWbemCredentials().getUsername());
		assertEquals("#1Password", sentryCli.getWbemCredentials().getPassword());
		assertEquals(sentryCli.getHdfs(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		assertNull(sentryCli.getHdfsExclusion());

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
		assertNull(hardwareSentryCli.getHttpCredentials());

		// Default values
		arguments = new String[]{HOST_OPTION, HOST, DEVICE_TYPE_OPTION, TargetType.LINUX.toString(),
			HTTP_USERNAME_OPTION, USER, HTTP_PASSWORD_OPTION, PASS};
		commandLine.parseArgs(arguments);
		assertEquals(HOST, hardwareSentryCli.getHostname());
		assertEquals(TargetType.LINUX, hardwareSentryCli.getDeviceType());
		HttpCredentials httpCredentials = hardwareSentryCli.getHttpCredentials();
		assertNotNull(httpCredentials);
		assertNull(httpCredentials.getHttpOrHttps());
		assertEquals(443, httpCredentials.getPort());
		assertEquals(120L, httpCredentials.getTimeout());
		assertEquals(USER, httpCredentials.getUsername());
		assertEquals(PASS, new String(httpCredentials.getPassword()));

		// Explicit values
		arguments = new String[]{HOST_OPTION, HOST, DEVICE_TYPE_OPTION, TargetType.LINUX.toString(),
			HTTP_USERNAME_OPTION, USER, HTTP_PASSWORD_OPTION, PASS, HTTP_OPTION,
			HTTP_PORT_OPTION, PORT, HTTP_TIMEOUT_OPTION, TIMEOUT};
		commandLine.parseArgs(arguments);
		assertEquals(HOST, hardwareSentryCli.getHostname());
		assertEquals(TargetType.LINUX, hardwareSentryCli.getDeviceType());
		httpCredentials = hardwareSentryCli.getHttpCredentials();
		assertNotNull(httpCredentials);
		assertTrue(httpCredentials.getHttpOrHttps().isHttp());
		assertEquals(Integer.parseInt(PORT), httpCredentials.getPort());
		assertEquals(Long.parseLong(TIMEOUT), httpCredentials.getTimeout());
		assertEquals(USER, httpCredentials.getUsername());
		assertEquals(PASS, new String(httpCredentials.getPassword()));
	}

	@Test
	void wmiArgumentsTest() {
		String[] args_hdfs = { "-host", "hostaa",
				"-dt", "MS_WINDOWS",
				"--wmi-namespace", "root\\ibmsd",
				"--wmi-username", "admin",
				"--wmi-password", "#1Password",
				"-hdfs", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCli sentryCli = new HardwareSentryCli();
		new CommandLine(sentryCli).parseArgs(args_hdfs);

		assertEquals("hostaa", sentryCli.getHostname());
		assertEquals(TargetType.MS_WINDOWS, sentryCli.getDeviceType());
		assertEquals("root\\ibmsd", sentryCli.getWmiCredentials().getNamespace());
		assertEquals(120L, sentryCli.getWmiCredentials().getTimeout());
		assertEquals("admin", sentryCli.getWmiCredentials().getUsername());
		assertEquals("#1Password", sentryCli.getWmiCredentials().getPassword());
		assertEquals(sentryCli.getHdfs(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
	}
}
