package com.sentrysoftware.matrix.connector.parser.state.source.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OsCommandSource;
import com.sentrysoftware.matrix.connector.parser.state.source.oscommand.OsCommandProcessor;

class KeepOnlyRegExpProcessorTest {

	private static final String KEEP_ONLY_REGEXP_DISCOVERY = "DiskController.Discovery.Source(1).KeepOnlyRegExp";
	private static final String KEEP_ONLY_REGEXP_COLLECT = "PhysicalDisk.Collect.Source(1).KeepOnlyRegExp";
	private static final KeepOnlyRegExpProcessor KEEP_ONLY_REGEXP_PROCESSOR = new KeepOnlyRegExpProcessor(OsCommandSource.class, OsCommandProcessor.OS_COMMAND_TYPE);
	private static final String VALUE = ":ext_bus:";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(OsCommandSource.class, KEEP_ONLY_REGEXP_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("OSCommand", KEEP_ONLY_REGEXP_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {
		assertThrows(IllegalArgumentException.class, () -> KEEP_ONLY_REGEXP_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = KEEP_ONLY_REGEXP_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = KEEP_ONLY_REGEXP_PROCESSOR.getMatcher("DiskController.Discovery.Source(2).CommandLine=\"/bin/sh %EmbeddedFile(1)%\"");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = KEEP_ONLY_REGEXP_PROCESSOR.getMatcher(KEEP_ONLY_REGEXP_DISCOVERY);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
		{
			final Matcher matcher = KEEP_ONLY_REGEXP_PROCESSOR.getMatcher(KEEP_ONLY_REGEXP_COLLECT);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {
		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> KEEP_ONLY_REGEXP_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> KEEP_ONLY_REGEXP_PROCESSOR.parse(KEEP_ONLY_REGEXP_DISCOVERY, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> KEEP_ONLY_REGEXP_PROCESSOR.parse(KEEP_ONLY_REGEXP_COLLECT, VALUE, null));

		// check criterion not found
		assertThrows(IllegalArgumentException.class, () -> KEEP_ONLY_REGEXP_PROCESSOR.parse(KEEP_ONLY_REGEXP_DISCOVERY, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> KEEP_ONLY_REGEXP_PROCESSOR.parse(KEEP_ONLY_REGEXP_COLLECT, VALUE, CONNECTOR));

		{
			final OsCommandSource osCommandSource = OsCommandSource.builder().index(1).build();
			final Discovery discovery = Discovery.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).discovery(discovery).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			KEEP_ONLY_REGEXP_PROCESSOR.parse(KEEP_ONLY_REGEXP_DISCOVERY, VALUE, CONNECTOR);
			assertEquals(VALUE, ((OsCommandSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getKeepOnlyRegExp()); 
		}
		{
			final OsCommandSource osCommandSource = OsCommandSource.builder().index(1).build();
			final Collect collect = Collect.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.PHYSICAL_DISK).collect(collect).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			KEEP_ONLY_REGEXP_PROCESSOR.parse(KEEP_ONLY_REGEXP_COLLECT, VALUE, CONNECTOR);
			assertEquals(VALUE, ((OsCommandSource) CONNECTOR.getHardwareMonitors().get(0).getCollect().getSources().get(0)).getKeepOnlyRegExp()); 
		}
	}
}
