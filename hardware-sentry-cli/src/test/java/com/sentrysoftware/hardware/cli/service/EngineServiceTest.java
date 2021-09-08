package com.sentrysoftware.hardware.cli.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.cli.component.cli.protocols.HttpCredentials;
import com.sentrysoftware.hardware.cli.component.cli.protocols.IpmiCredentials;
import com.sentrysoftware.hardware.cli.component.cli.protocols.SnmpCredentials;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;

class EngineServiceTest {

	EngineService engineService = new EngineService();

	@Test
	void testGetConnectors() {

		final Set<String> allConnectors = Set.of("aa.hdfs", "bb.hdfs", "cc.hdfs", "dd.hdfs", "ee.hdfs");
		final Set<String> selected = Set.of("aa.hdfs", "bb.hdfs", "cc.hdfs");
		final Set<String> excluded = Set.of("aa.hdfs", "bb.hdfs");

		final Set<String> expectedInclusion = new HashSet<>(Arrays.asList("aa.connector", "bb.connector", "cc.connector"));
		Set<String> actual = EngineService.getConnectors(allConnectors, selected);
		assertEquals(expectedInclusion, actual);

		actual = EngineService.getConnectors(allConnectors, Collections.emptySet());
		assertEquals(Collections.emptySet(), actual);

		final Set<String> expectedExclusion = Set.of("aa.connector", "bb.connector");
		actual = EngineService.getConnectors(allConnectors, excluded);
		assertEquals(expectedExclusion, actual);

		actual = EngineService.getConnectors(allConnectors, Collections.emptySet());
		assertEquals(Collections.emptySet(), actual);

		actual = EngineService.getConnectors(allConnectors, null);
		assertEquals(Collections.emptySet(), actual);

	}

	@Test
	void getSnmpCredentialsTest() {

		SnmpCredentials snmpCred = new SnmpCredentials();
		snmpCred.setCommunity("comm1");
		snmpCred.setPassword("pwd1".toCharArray());
		snmpCred.setPort(110);
		snmpCred.setPrivacy(Privacy.AES);
		snmpCred.setPrivacyPassword("privPwd".toCharArray());
		snmpCred.setSnmpVersion(SNMPVersion.V1);
		snmpCred.setTimeout(10);
		snmpCred.setUsername("user");

		SNMPProtocol snmpProtocol = engineService.getSnmpProtocol(snmpCred);

		assertEquals("comm1", snmpProtocol.getCommunity());
		assertArrayEquals("pwd1".toCharArray(), snmpProtocol.getPassword());
		assertEquals(110, snmpProtocol.getPort());
		assertEquals(Privacy.AES, snmpProtocol.getPrivacy());
		assertArrayEquals("privPwd".toCharArray(), snmpProtocol.getPrivacyPassword());
		assertEquals(SNMPVersion.V1, snmpProtocol.getVersion());
		assertEquals(10, snmpProtocol.getTimeout());
		assertEquals("user", snmpProtocol.getUsername());
	}

	@Test
	void getHttpCredentialsTest() {



		// httpCredentials is null
		assertThrows(IllegalArgumentException.class, () -> engineService.getHttpProtocol(null));

		// httpCredentials is not null, password is null
		HttpCredentials httpCredentials = new HttpCredentials();
		HTTPProtocol httpProtocol = engineService.getHttpProtocol(httpCredentials);
		assertNotNull(httpProtocol);
		assertTrue(httpProtocol.getHttps());
		assertEquals(0, httpProtocol.getPort());
		assertEquals(0L, httpProtocol.getTimeout());
		assertNull(httpProtocol.getUsername());
		assertNull(httpProtocol.getPassword());

		// httpCredentials is not null, password is not null
		char[] password = "password".toCharArray();
		httpCredentials.setPassword(password);
		httpProtocol = engineService.getHttpProtocol(httpCredentials);
		assertNotNull(httpProtocol);
		assertTrue(httpProtocol.getHttps());
		assertEquals(0, httpProtocol.getPort());
		assertEquals(0L, httpProtocol.getTimeout());
		assertNull(httpProtocol.getUsername());
		assertTrue(Arrays.equals(password, httpProtocol.getPassword()));
	}

	@Test
	void testGetIpmiProtocol() {
		IpmiCredentials ipmiCredentials = new IpmiCredentials();
		assertNotNull(engineService.getIpmiOverLanProtocol(ipmiCredentials));

		ipmiCredentials.setBmcKey("key");
		ipmiCredentials.setPassword("password".toCharArray());
		ipmiCredentials.setUsername("username");
		ipmiCredentials.setTimeout(120L);

		assertEquals(IPMIOverLanProtocol.builder()
				.bmcKey("key".getBytes())
				.password("password".toCharArray())
				.username("username")
				.skipAuth(false)
				.timeout(120L)
				.build(), engineService.getIpmiOverLanProtocol(ipmiCredentials));
	}
}
