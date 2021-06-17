package com.sentrysoftware.hardware.cli.component.cli;

import com.sentrysoftware.hardware.cli.component.cli.protocols.HTTPCredentials;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.target.TargetType;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HardwareSentryCLITest {

	private static final String HOST_OPTION = "-host";
	private static final String DEVICE_TYPE_OPTION = "-dt";

	private static final String HTTPS_OPTION = "--https";
	private static final String HTTP_PORT_OPTION = "--http-port";
	private static final String HTTP_TIMEOUT_OPTION = "--http-timeout";
	private static final String HTTP_USERNAME_OPTION = "--http-username";
	private static final String HTTP_PASSWORD_OPTION = "--http-password";

	@Test
	void argumentsTest() {
		String[] args_hdfs = { "-host", "hostaa", 
				"-dt", "HP_UX", 
				"--snmp-version", "V2C",
				"--snmp-port", "200", 
				"--snmp-community", "private", 
				"--snmp-timeout", "60", 
				"-hdfs", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCLI sentryCli = new HardwareSentryCLI();
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
				"--snmp-version", "V2C",
				"--snmp-port", "200", 
				"--snmp-community", "private", 
				"--snmp-timeout", "60", 
				"-hdfsExcluded", "hdfs1,hdfs2" };
		
		sentryCli = new HardwareSentryCLI();
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
				"--snmp-version", "V1",
				"-hdfs", "hdfs1,hdfs2" };
		
		sentryCli = new HardwareSentryCLI();
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

		final CommandLine commandLine = new CommandLine(new HardwareSentryCLI());

		assertThrows(MissingParameterException.class, () -> commandLine.parseArgs(args_required_host));

	}

	@Test
	void argumentsWBEMTest() {
		String[] args_hdfs = { "-host", "hostaa", 
				"-dt", "HP_UX", 
				"--wbem-protocol", "HTTP", 
				"--wbem-port", "5989",
				"--wbem-namespace", "root/emc", 
				"--wbem-timeout", "120", 
				"--wbem-username", "admin", 
				"--wbem-password", "#1Password", 
				"-hdfs", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCLI sentryCli = new HardwareSentryCLI();
		new CommandLine(sentryCli).parseArgs(args_hdfs);

		assertEquals("hostaa", sentryCli.getHostname());
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(WBEMProtocol.WBEMProtocols.HTTP,
				WBEMProtocol.WBEMProtocols.getValue(sentryCli.getWbemCredentials().getProtocol()));
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
		assertEquals(WBEMProtocol.WBEMProtocols.HTTPS,
				WBEMProtocol.WBEMProtocols.getValue(sentryCli.getWbemCredentials().getProtocol()));
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
		final String PORT = "443";
		final String TIMEOUT = "60";

		// No HTTP credentials
		String[] arguments = {HOST_OPTION, HOST, DEVICE_TYPE_OPTION, TargetType.LINUX.toString()};
		HardwareSentryCLI hardwareSentryCLI = new HardwareSentryCLI();
		CommandLine commandLine = new CommandLine(hardwareSentryCLI);
		commandLine.parseArgs(arguments);
		assertEquals(HOST, hardwareSentryCLI.getHostname());
		assertEquals(TargetType.LINUX, hardwareSentryCLI.getDeviceType());
		assertNull(hardwareSentryCLI.getHttpCredentials());

		// Default values
		arguments = new String[]{HOST_OPTION, HOST, DEVICE_TYPE_OPTION, TargetType.LINUX.toString(),
			HTTP_USERNAME_OPTION, USER, HTTP_PASSWORD_OPTION, USER};
		commandLine.parseArgs(arguments);
		assertEquals(HOST, hardwareSentryCLI.getHostname());
		assertEquals(TargetType.LINUX, hardwareSentryCLI.getDeviceType());
		HTTPCredentials httpCredentials = hardwareSentryCLI.getHttpCredentials();
		assertNotNull(httpCredentials);
		assertFalse(httpCredentials.isHttps());
		assertEquals(8080, httpCredentials.getPort());
		assertEquals(120L, httpCredentials.getTimeout());
		assertEquals(USER, httpCredentials.getUsername());
		assertEquals(USER, httpCredentials.getPassword());

		// Explicit values
		arguments = new String[]{HOST_OPTION, HOST, DEVICE_TYPE_OPTION, TargetType.LINUX.toString(),
			HTTP_USERNAME_OPTION, USER, HTTP_PASSWORD_OPTION, USER, HTTPS_OPTION,
			HTTP_PORT_OPTION, PORT, HTTP_TIMEOUT_OPTION, TIMEOUT};
		commandLine.parseArgs(arguments);
		assertEquals(HOST, hardwareSentryCLI.getHostname());
		assertEquals(TargetType.LINUX, hardwareSentryCLI.getDeviceType());
		httpCredentials = hardwareSentryCLI.getHttpCredentials();
		assertNotNull(httpCredentials);
		assertTrue(httpCredentials.isHttps());
		assertEquals(Integer.parseInt(PORT), httpCredentials.getPort());
		assertEquals(Long.parseLong(TIMEOUT), httpCredentials.getTimeout());
		assertEquals(USER, httpCredentials.getUsername());
		assertEquals(USER, httpCredentials.getPassword());
	}

	@Test
	void argumentsWMITest() {
		String[] args_hdfs = { "-host", "hostaa", 
				"-dt", "MS_WINDOWS", 
				"--wmi-namespace", "root\\ibmsd",
				"--wmi-username", "admin", 
				"--wmi-password", "#1Password", 
				"-hdfs", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCLI sentryCli = new HardwareSentryCLI();
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
