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

class SeparatorsProcessorTest {

	private static final String SEPARATORS_DISCOVERY = "DiskController.Discovery.Source(1).Separators";
	private static final String SEPARATORS_COLLECT = "PhysicalDisk.Collect.Source(1).Separators";
	private static final SeparatorsProcessor SEPARATORS_PROCESSOR = new SeparatorsProcessor();
	private static final String VALUE = ":";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(OSCommandSource.class, SEPARATORS_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("OSCommand", SEPARATORS_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {
		assertThrows(IllegalArgumentException.class, () -> SEPARATORS_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = SEPARATORS_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = SEPARATORS_PROCESSOR.getMatcher("DiskController.Discovery.Source(2).CommandLine=\"/bin/sh %EmbeddedFile(1)%\"");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = SEPARATORS_PROCESSOR.getMatcher(SEPARATORS_DISCOVERY);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
		{
			final Matcher matcher = SEPARATORS_PROCESSOR.getMatcher(SEPARATORS_COLLECT);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {
		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> SEPARATORS_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> SEPARATORS_PROCESSOR.parse(SEPARATORS_DISCOVERY, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> SEPARATORS_PROCESSOR.parse(SEPARATORS_COLLECT, VALUE, null));

		// check criterion not found
		assertThrows(IllegalArgumentException.class, () -> SEPARATORS_PROCESSOR.parse(SEPARATORS_DISCOVERY, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> SEPARATORS_PROCESSOR.parse(SEPARATORS_COLLECT, VALUE, CONNECTOR));

		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Discovery discovery = Discovery.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).discovery(discovery).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			SEPARATORS_PROCESSOR.parse(SEPARATORS_DISCOVERY, VALUE, CONNECTOR);
			assertEquals(VALUE, ((OSCommandSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getSeparators()); 
		}
		{
			final OSCommandSource osCommandSource = OSCommandSource.builder().index(1).build();
			final Collect collect = Collect.builder().sources(List.of(osCommandSource)).build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.PHYSICAL_DISK).collect(collect).build();
			CONNECTOR.setHardwareMonitors(List.of(hardwareMonitor));
			SEPARATORS_PROCESSOR.parse(SEPARATORS_COLLECT, VALUE, CONNECTOR);
			assertEquals(VALUE, ((OSCommandSource) CONNECTOR.getHardwareMonitors().get(0).getCollect().getSources().get(0)).getSeparators()); 
		}
	}
}
