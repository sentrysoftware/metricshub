package com.sentrysoftware.hardware.cli.component.cli.protocols;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.cli.component.cli.HardwareSentryCli;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

class HttpConfigTest {

	private static final String HOSTNAME = "hostname";
	private static final String TYPE = "win";
	private static final String DEFAULT_USERNAME = "default";
	private static final char[] DEFAULT_PASSWORD = "password".toCharArray();

	@Test
	void testNoHttp() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(HOSTNAME, "-t", TYPE);
		assertNull(cli.getHttpConfigCli());
	}

	@Test
	void testHttpFull() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(
				HOSTNAME,
				"-t", TYPE,
				"--http",
				"--http-username", "custom",
				"--http-password", "other",
				"--http-timeout", "37",
				"--http-port", "8080"
		);
		HTTPProtocol proto = cli.getHttpConfigCli().toProtocol(DEFAULT_USERNAME, DEFAULT_PASSWORD);
		assertNotNull(proto);
		assertFalse(proto.getHttps());
		assertEquals("custom", proto.getUsername());
		assertArrayEquals("other".toCharArray(), proto.getPassword());
		assertEquals(37, proto.getTimeout());
		assertEquals(8080, proto.getPort());
	}

	@Test
	void testHttpFullDefaultUsername() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(
				HOSTNAME,
				"-t", TYPE,
				"--http",
				"--http-timeout", "37"
		);
		HTTPProtocol proto = cli.getHttpConfigCli().toProtocol(DEFAULT_USERNAME, DEFAULT_PASSWORD);
		assertNotNull(proto);
		assertFalse(proto.getHttps());
		assertEquals(DEFAULT_USERNAME, proto.getUsername());
		assertArrayEquals(DEFAULT_PASSWORD, proto.getPassword());
		assertEquals(37, proto.getTimeout());
		assertEquals(80, proto.getPort());
	}

	@Test
	void testHttps() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(
				HOSTNAME,
				"-t", TYPE,
				"--https"
		);
		HTTPProtocol proto = cli.getHttpConfigCli().toProtocol(null, null);
		assertNotNull(proto);
		assertTrue(proto.getHttps());
		assertNull(proto.getUsername());
		assertNull(proto.getPassword());
		assertEquals(443, proto.getPort());
	}

	@Test
	void testHttpWithHttps() {
		HardwareSentryCli cli = new HardwareSentryCli();
		assertThrows(ParameterException.class, () -> new CommandLine(cli).parseArgs(
				HOSTNAME,
				"-t", TYPE,
				"--http", "--https"
		));
	}

}
