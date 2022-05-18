package com.sentrysoftware.hardware.cli.component.cli.protocols;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.cli.component.cli.HardwareSentryCli;
import com.sentrysoftware.matrix.engine.protocol.IpmiOverLanProtocol;

import picocli.CommandLine;

class IpmiConfigTest {

	private static final String HOSTNAME = "hostname";
	private static final String TYPE = "oob";
	private static final String DEFAULT_USERNAME = "default";
	private static final char[] DEFAULT_PASSWORD = "password".toCharArray();

	@Test
	void testNoIpmi() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(HOSTNAME, "-t", TYPE);
		assertNull(cli.getIpmiConfigCli());
	}

	@Test
	void testIpmiFull() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(
				HOSTNAME,
				"-t", TYPE,
				"--ipmi",
				"--ipmi-username", "custom",
				"--ipmi-password", "other",
				"--ipmi-bmc-key", "0666",
				"--ipmi-timeout", "37"
		);
		IpmiOverLanProtocol proto = cli.getIpmiConfigCli().toProtocol(DEFAULT_USERNAME, DEFAULT_PASSWORD);
		assertNotNull(proto);
		assertEquals("custom", proto.getUsername());
		assertArrayEquals("other".toCharArray(), proto.getPassword());
		assertEquals(37, proto.getTimeout());
		assertFalse(proto.isSkipAuth());
		assertArrayEquals(new byte[] { 0x06, 0x66 }, proto.getBmcKey());
	}

	@Test
	void testIpmiDefaultUsername() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(
				HOSTNAME,
				"-t", TYPE,
				"--ipmi",
				"--ipmi-skip-auth"
		);
		IpmiOverLanProtocol proto = cli.getIpmiConfigCli().toProtocol(DEFAULT_USERNAME, DEFAULT_PASSWORD);
		assertNotNull(proto);
		assertEquals(DEFAULT_USERNAME, proto.getUsername());
		assertArrayEquals(DEFAULT_PASSWORD, proto.getPassword());
		assertTrue(proto.isSkipAuth());
		assertNull(proto.getBmcKey());
	}

}
