package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

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

class TimeoutProcessorTest {

	private static final String TIMEOUT_DISCOVERY = "DiskController.Discovery.Source(1).Timeout";
	private static final String TIMEOUT_COLLECT = "PhysicalDisk.Collect.Source(1).timeout";
	private static final TimeoutProcessor TIMEOUT_PROCESSOR = new TimeoutProcessor();
	private static final String VALUE = "120";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(OsCommandSource.class, TIMEOUT_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("OSCommand", TIMEOUT_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {
		assertThrows(IllegalArgumentException.class, () -> TIMEOUT_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = TIMEOUT_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = TIMEOUT_PROCESSOR.getMatcher("DiskController.Discovery.Source(2).CommandLine=\"/bin/sh %EmbeddedFile(1)%\"");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = TIMEOUT_PROCESSOR.getMatcher(TIMEOUT_DISCOVERY);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
		{
			final Matcher matcher = TIMEOUT_PROCESSOR.getMatcher(TIMEOUT_COLLECT);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {
		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> TIMEOUT_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TIMEOUT_PROCESSOR.parse(TIMEOUT_DISCOVERY, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TIMEOUT_PROCESSOR.parse(TIMEOUT_COLLECT, VALUE, null));

		// check criterion not found
		assertThrows(IllegalStateException.class, () -> TIMEOUT_PROCESSOR.parse(TIMEOUT_DISCOVERY, VALUE, CONNECTOR));
		assertThrows(IllegalStateException.class, () -> TIMEOUT_PROCESSOR.parse(TIMEOUT_COLLECT, VALUE, CONNECTOR));
		{
			final OsCommandSource osCommandSource = OsCommandSource.builder().index(1).build();
			final Discovery discovery = Discovery.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).discovery(discovery).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			assertThrows(IllegalStateException.class, () -> TIMEOUT_PROCESSOR.parse(TIMEOUT_DISCOVERY, "x", CONNECTOR));
		}
		{
			final OsCommandSource osCommandSource = OsCommandSource.builder().index(1).build();
			final Collect collect = Collect.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.PHYSICAL_DISK).collect(collect).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			assertThrows(IllegalStateException.class, () -> TIMEOUT_PROCESSOR.parse(TIMEOUT_COLLECT, "x", CONNECTOR));
		}

		{
			final OsCommandSource osCommandSource = OsCommandSource.builder().index(1).build();
			final Discovery discovery = Discovery.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).discovery(discovery).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			TIMEOUT_PROCESSOR.parse(TIMEOUT_DISCOVERY, VALUE, CONNECTOR);
			assertEquals(120L, ((OsCommandSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getTimeout()); 
		}
		{
			final OsCommandSource osCommandSource = OsCommandSource.builder().index(1).build();
			final Collect collect = Collect.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.PHYSICAL_DISK).collect(collect).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			TIMEOUT_PROCESSOR.parse(TIMEOUT_COLLECT, VALUE, CONNECTOR);
			assertEquals(120L, ((OsCommandSource) CONNECTOR.getHardwareMonitors().get(0).getCollect().getSources().get(0)).getTimeout()); 
		}
	}
}
