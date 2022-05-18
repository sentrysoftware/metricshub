package com.sentrysoftware.hardware.cli.component.cli.protocols;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.cli.component.cli.HardwareSentryCli;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;

import picocli.CommandLine;

public class SnmpConfigTest {

	private static final String HOSTNAME = "hostname";
	private static final String TYPE = "oob";
	private static final String DEFAULT_USERNAME = "default";
	private static final char[] DEFAULT_PASSWORD = "password".toCharArray();

	@Test
	void testNoSnmp() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(HOSTNAME, "-t", TYPE);
		assertNull(cli.getSnmpConfigCli());
	}

	@Test
	void testSnmpV3Full() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(
				HOSTNAME,
				"-t", TYPE,
				"--snmp", "v3md5",
				"--snmp-username", "custom",
				"--snmp-password", "other",
				"--snmp-privacy", "aes",
				"--snmp-privacy-password", "please",
				"--snmp-timeout", "42"
		);
		SnmpProtocol proto = cli.getSnmpConfigCli().toProtocol(DEFAULT_USERNAME, DEFAULT_PASSWORD);
		assertNotNull(proto);
		assertEquals(SnmpProtocol.SnmpVersion.V3_MD5, proto.getVersion());
		assertEquals("custom", proto.getUsername());
		assertArrayEquals("other".toCharArray(), proto.getPassword());
		assertEquals(SnmpProtocol.Privacy.AES, proto.getPrivacy());
		assertArrayEquals("please".toCharArray(), proto.getPrivacyPassword());
		assertEquals(42, proto.getTimeout());
	}

	@Test
	void testSnmpV3Defaults() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(
				HOSTNAME,
				"-t", TYPE,
				"--snmp", "3",
				"--snmp-privacy", "aes",
				"--snmp-privacy-password", "please"
		);
		SnmpProtocol proto = cli.getSnmpConfigCli().toProtocol(DEFAULT_USERNAME, DEFAULT_PASSWORD);
		assertNotNull(proto);
		assertEquals(SnmpProtocol.SnmpVersion.V3_SHA, proto.getVersion());
		assertEquals(DEFAULT_USERNAME, proto.getUsername());
		assertArrayEquals(DEFAULT_PASSWORD, proto.getPassword());
		assertEquals(SnmpProtocol.Privacy.AES, proto.getPrivacy());
		assertArrayEquals("please".toCharArray(), proto.getPrivacyPassword());
	}

	@Test
	void testSnmpV1() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(
				HOSTNAME,
				"-t", TYPE,
				"--snmp", "v1",
				"--snmp-community", "comm"
		);
		SnmpProtocol proto = cli.getSnmpConfigCli().toProtocol(DEFAULT_USERNAME, DEFAULT_PASSWORD);
		assertNotNull(proto);
		assertEquals(SnmpProtocol.SnmpVersion.V1, proto.getVersion());
		assertEquals("comm", proto.getCommunity());
	}

	@Test
	void testSnmpV2c() {
		HardwareSentryCli cli = new HardwareSentryCli();
		new CommandLine(cli).parseArgs(
				HOSTNAME,
				"-t", TYPE,
				"--snmp", "2",
				"--community", "comm"
		);
		SnmpProtocol proto = cli.getSnmpConfigCli().toProtocol(DEFAULT_USERNAME, DEFAULT_PASSWORD);
		assertNotNull(proto);
		assertEquals(SnmpProtocol.SnmpVersion.V2C, proto.getVersion());
		assertEquals("comm", proto.getCommunity());
	}

}
