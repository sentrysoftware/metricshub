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
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;

class CommandLineProcessorTest {

	private static final String COMMAND_LINE_DISCOVERY = "DiskController.Discovery.Source(1).CommandLine";
	private static final String COMMAND_LINE_COLLECT = "PhysicalDisk.Collect.Source(1).CommandLine";
	private static final CommandLineProcessor COMMAND_LINE_PROCESSOR = new CommandLineProcessor();
	private static final String VALUE = "/bin/sh %EmbeddedFile(1)%";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(OSCommandSource.class, COMMAND_LINE_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("OSCommand", COMMAND_LINE_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {
		assertThrows(IllegalArgumentException.class, () -> COMMAND_LINE_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = COMMAND_LINE_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = COMMAND_LINE_PROCESSOR.getMatcher("DiskController.Discovery.Source(2).KeepOnlyRegExp=\":ext_bus:\"");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = COMMAND_LINE_PROCESSOR.getMatcher(COMMAND_LINE_DISCOVERY);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
		{
			final Matcher matcher = COMMAND_LINE_PROCESSOR.getMatcher(COMMAND_LINE_COLLECT);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {
		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> COMMAND_LINE_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> COMMAND_LINE_PROCESSOR.parse(COMMAND_LINE_DISCOVERY, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> COMMAND_LINE_PROCESSOR.parse(COMMAND_LINE_COLLECT, VALUE, null));

		// check criterion not found
		assertThrows(IllegalArgumentException.class, () -> COMMAND_LINE_PROCESSOR.parse(COMMAND_LINE_DISCOVERY, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> COMMAND_LINE_PROCESSOR.parse(COMMAND_LINE_COLLECT, VALUE, CONNECTOR));

		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Discovery discovery = Discovery.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).discovery(discovery).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			COMMAND_LINE_PROCESSOR.parse(COMMAND_LINE_DISCOVERY, VALUE, CONNECTOR);
			assertEquals(VALUE, ((OSCommandSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getCommandLine()); 
		}
		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Collect collect = Collect.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.PHYSICAL_DISK).collect(collect).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			COMMAND_LINE_PROCESSOR.parse(COMMAND_LINE_COLLECT, VALUE, CONNECTOR);
			assertEquals(VALUE, ((OSCommandSource) CONNECTOR.getHardwareMonitors().get(0).getCollect().getSources().get(0)).getCommandLine()); 
		}
	}
}
