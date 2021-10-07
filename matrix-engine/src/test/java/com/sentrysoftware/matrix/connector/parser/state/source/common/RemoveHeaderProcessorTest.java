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
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.parser.state.source.common.RemoveHeaderProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.oscommand.OsCommandProcessor;

class RemoveHeaderProcessorTest {

	private static final String REMOVE_HEADER_DISCOVERY = "DiskController.Discovery.Source(1).removeHeader";
	private static final String REMOVE_HEADER_COLLECT = "PhysicalDisk.Collect.Source(1).removeHeader";
	private static final RemoveHeaderProcessor REMOVE_HEADER_PROCESSOR = new RemoveHeaderProcessor(OSCommandSource.class, OsCommandProcessor.OS_COMMAND_TYPE);
	private static final String VALUE = "1";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(OSCommandSource.class, REMOVE_HEADER_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("OSCommand", REMOVE_HEADER_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {
		assertThrows(IllegalArgumentException.class, () -> REMOVE_HEADER_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = REMOVE_HEADER_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = REMOVE_HEADER_PROCESSOR.getMatcher("DiskController.Discovery.Source(2).CommandLine=\"/bin/sh %EmbeddedFile(1)%\"");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = REMOVE_HEADER_PROCESSOR.getMatcher(REMOVE_HEADER_DISCOVERY);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
		{
			final Matcher matcher = REMOVE_HEADER_PROCESSOR.getMatcher(REMOVE_HEADER_COLLECT);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {
		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> REMOVE_HEADER_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> REMOVE_HEADER_PROCESSOR.parse(REMOVE_HEADER_DISCOVERY, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> REMOVE_HEADER_PROCESSOR.parse(REMOVE_HEADER_COLLECT, VALUE, null));

		// check criterion not found
		assertThrows(IllegalArgumentException.class, () -> REMOVE_HEADER_PROCESSOR.parse(REMOVE_HEADER_DISCOVERY, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> REMOVE_HEADER_PROCESSOR.parse(REMOVE_HEADER_COLLECT, VALUE, CONNECTOR));
		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Discovery discovery = Discovery.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).discovery(discovery).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			assertThrows(IllegalStateException.class, () -> REMOVE_HEADER_PROCESSOR.parse(REMOVE_HEADER_DISCOVERY, "x", CONNECTOR));
		}
		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Collect collect = Collect.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.PHYSICAL_DISK).collect(collect).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			assertThrows(IllegalStateException.class, () -> REMOVE_HEADER_PROCESSOR.parse(REMOVE_HEADER_COLLECT, "x", CONNECTOR));
		}

		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Discovery discovery = Discovery.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).discovery(discovery).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			REMOVE_HEADER_PROCESSOR.parse(REMOVE_HEADER_DISCOVERY, VALUE, CONNECTOR);
			assertEquals(1, ((OSCommandSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getRemoveHeader()); 
		}
		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Collect collect = Collect.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.PHYSICAL_DISK).collect(collect).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			REMOVE_HEADER_PROCESSOR.parse(REMOVE_HEADER_COLLECT, VALUE, CONNECTOR);
			assertEquals(1, ((OSCommandSource) CONNECTOR.getHardwareMonitors().get(0).getCollect().getSources().get(0)).getRemoveHeader()); 
		}
	}
}
