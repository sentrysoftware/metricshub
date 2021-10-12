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

class RemoveFooterProcessorTest {

	private static final String REMOVE_FOOTER_DISCOVERY = "DiskController.Discovery.Source(1).removeFooter";
	private static final String REMOVE_FOOTER_COLLECT = "PhysicalDisk.Collect.Source(1).removeFooter";
	private static final RemoveFooterProcessor REMOVE_FOOTER_PROCESSOR = new RemoveFooterProcessor();
	private static final String VALUE = "1";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(OSCommandSource.class, REMOVE_FOOTER_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("OSCommand", REMOVE_FOOTER_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {
		assertThrows(IllegalArgumentException.class, () -> REMOVE_FOOTER_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = REMOVE_FOOTER_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = REMOVE_FOOTER_PROCESSOR.getMatcher("DiskController.Discovery.Source(2).CommandLine=\"/bin/sh %EmbeddedFile(1)%\"");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = REMOVE_FOOTER_PROCESSOR.getMatcher(REMOVE_FOOTER_DISCOVERY);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
		{
			final Matcher matcher = REMOVE_FOOTER_PROCESSOR.getMatcher(REMOVE_FOOTER_COLLECT);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {
		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> REMOVE_FOOTER_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> REMOVE_FOOTER_PROCESSOR.parse(REMOVE_FOOTER_DISCOVERY, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> REMOVE_FOOTER_PROCESSOR.parse(REMOVE_FOOTER_COLLECT, VALUE, null));

		// check criterion not found
		assertThrows(IllegalStateException.class, () -> REMOVE_FOOTER_PROCESSOR.parse(REMOVE_FOOTER_DISCOVERY, VALUE, CONNECTOR));
		assertThrows(IllegalStateException.class, () -> REMOVE_FOOTER_PROCESSOR.parse(REMOVE_FOOTER_COLLECT, VALUE, CONNECTOR));
		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Discovery discovery = Discovery.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).discovery(discovery).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			assertThrows(IllegalStateException.class, () -> REMOVE_FOOTER_PROCESSOR.parse(REMOVE_FOOTER_DISCOVERY, "x", CONNECTOR));
		}
		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Collect collect = Collect.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.PHYSICAL_DISK).collect(collect).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			assertThrows(IllegalStateException.class, () -> REMOVE_FOOTER_PROCESSOR.parse(REMOVE_FOOTER_COLLECT, "x", CONNECTOR));
		}

		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Discovery discovery = Discovery.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).discovery(discovery).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			REMOVE_FOOTER_PROCESSOR.parse(REMOVE_FOOTER_DISCOVERY, VALUE, CONNECTOR);
			assertEquals(1, ((OSCommandSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getRemoveFooter()); 
		}
		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Collect collect = Collect.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.PHYSICAL_DISK).collect(collect).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			REMOVE_FOOTER_PROCESSOR.parse(REMOVE_FOOTER_COLLECT, VALUE, CONNECTOR);
			assertEquals(1, ((OSCommandSource) CONNECTOR.getHardwareMonitors().get(0).getCollect().getSources().get(0)).getRemoveFooter()); 
		}
	}
}
