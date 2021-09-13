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

import com.sentrysoftware.hardware.cli.component.cli.protocols.HttpConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.IpmiConfig;
import com.sentrysoftware.hardware.cli.component.cli.protocols.SnmpConfig;
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

}
