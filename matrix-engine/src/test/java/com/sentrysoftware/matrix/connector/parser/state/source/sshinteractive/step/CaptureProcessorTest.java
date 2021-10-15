package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.GetAvailable;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitFor;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;

class CaptureProcessorTest {

	private static final String DISCOVERY_CAPTURE_NAME = "Blade.Discovery.Source(1).Step(2).Capture";
	private static final String COLLECT_CAPTURE_NAME = "NetworkCard.Collect.Source(1).Step(2).Capture";
	private static final CaptureProcessor CAPTURE_PROCESSOR = new CaptureProcessor(GetAvailable.class, "GetAvailable");
	private static final String VALUE = "True";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(GetAvailable.class, CAPTURE_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("GetAvailable", CAPTURE_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {

		assertThrows(IllegalArgumentException.class, () -> CAPTURE_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = CAPTURE_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = CAPTURE_PROCESSOR.getMatcher("detection.criteria(1).exclude");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = CAPTURE_PROCESSOR.getMatcher(DISCOVERY_CAPTURE_NAME);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {

		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> CAPTURE_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> CAPTURE_PROCESSOR.parse(DISCOVERY_CAPTURE_NAME, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> CAPTURE_PROCESSOR.parse(DISCOVERY_CAPTURE_NAME, VALUE, null));

		assertThrows(IllegalStateException.class, () -> CAPTURE_PROCESSOR.parse(DISCOVERY_CAPTURE_NAME, VALUE, CONNECTOR));

		final SshInteractiveSource sshInteractiveSource = SshInteractiveSource.builder()
				.index(1)
				.steps(List.of(WaitFor.builder().index(1).build(), GetAvailable.builder().index(2).build()))
				.build();

		final Discovery discovery = Discovery.builder().sources(List.of(sshInteractiveSource)).build();
		final HardwareMonitor bladeMonitor = HardwareMonitor.builder().type(MonitorType.BLADE).discovery(discovery).build();

		final Collect collect = Collect.builder().sources(List.of(sshInteractiveSource)).build();
		final HardwareMonitor networkCardMonitor = HardwareMonitor.builder().type(MonitorType.NETWORK_CARD).collect(collect).build();

		CONNECTOR.setHardwareMonitors(List.of(bladeMonitor, networkCardMonitor));

		CAPTURE_PROCESSOR.parse(DISCOVERY_CAPTURE_NAME, "0", CONNECTOR);
		assertFalse(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(DISCOVERY_CAPTURE_NAME, "FALSE", CONNECTOR);
		assertFalse(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(DISCOVERY_CAPTURE_NAME, "no", CONNECTOR);
		assertFalse(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(COLLECT_CAPTURE_NAME, "0", CONNECTOR);
		assertFalse(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(1).getCollect().getSources().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(COLLECT_CAPTURE_NAME, "FALSE", CONNECTOR);
		assertFalse(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(1).getCollect().getSources().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(COLLECT_CAPTURE_NAME, "no", CONNECTOR);
		assertFalse(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(1).getCollect().getSources().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(DISCOVERY_CAPTURE_NAME, VALUE, CONNECTOR);
		assertTrue(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(DISCOVERY_CAPTURE_NAME, "1", CONNECTOR);
		assertTrue(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(DISCOVERY_CAPTURE_NAME, "YES", CONNECTOR);
		assertTrue(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(COLLECT_CAPTURE_NAME, "1", CONNECTOR);
		assertTrue(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(1).getCollect().getSources().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(COLLECT_CAPTURE_NAME, "YES", CONNECTOR);
		assertTrue(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(1).getCollect().getSources().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(COLLECT_CAPTURE_NAME, "true", CONNECTOR);
		assertTrue(((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(1).getCollect().getSources().get(0)).getSteps().get(1).isCapture());
	}
}
