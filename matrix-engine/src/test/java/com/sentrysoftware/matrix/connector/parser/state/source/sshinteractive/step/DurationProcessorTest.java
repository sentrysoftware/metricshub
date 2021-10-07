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
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Sleep;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitFor;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;

class DurationProcessorTest {

	private static final String DURATION_NAME = "Blade.Discovery.Source(1).Step(3).Duration";
	private static final DurationProcessor DURATION_PROCESSOR = new DurationProcessor();
	private static final String VALUE = "1";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(Sleep.class, DURATION_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("Sleep", DURATION_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {

		assertThrows(IllegalArgumentException.class, () -> DURATION_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = DURATION_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = DURATION_PROCESSOR.getMatcher("detection.criteria(1).exclude");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = DURATION_PROCESSOR.getMatcher(DURATION_NAME);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {

		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> DURATION_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> DURATION_PROCESSOR.parse(DURATION_NAME, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> DURATION_PROCESSOR.parse(DURATION_NAME, VALUE, null));

		assertThrows(IllegalStateException.class, () -> DURATION_PROCESSOR.parse(DURATION_NAME, VALUE, CONNECTOR));

		final SshInteractiveSource sshInteractiveSource = SshInteractiveSource.builder()
				.index(1)
				.steps(List.of(WaitFor.builder().index(1).build(), GetAvailable.builder().index(2).build(), Sleep.builder().index(3).build()))
				.build();

		final Discovery discovery = Discovery.builder().sources(List.of(sshInteractiveSource)).build();
		final HardwareMonitor bladeMonitor = HardwareMonitor.builder().type(MonitorType.BLADE).discovery(discovery).build();

		final Collect collect = Collect.builder().sources(List.of(sshInteractiveSource)).build();
		final HardwareMonitor networkCardMonitor = HardwareMonitor.builder().type(MonitorType.NETWORK_CARD).collect(collect).build();

		CONNECTOR.setHardwareMonitors(List.of(bladeMonitor, networkCardMonitor));

		assertThrows(IllegalStateException.class, () -> DURATION_PROCESSOR.parse(DURATION_NAME, "X", CONNECTOR));
		assertThrows(IllegalStateException.class, () -> DURATION_PROCESSOR.parse("NetworkCard.Collect.Source(1).Step(3).Duration", "X", CONNECTOR));

		DURATION_PROCESSOR.parse(DURATION_NAME, VALUE, CONNECTOR);
		assertEquals(1L, ((Sleep) ((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getSteps().get(2)).getDuration());

		DURATION_PROCESSOR.parse("NetworkCard.Collect.Source(1).Step(3).Duration", VALUE, CONNECTOR);
		assertEquals(1L, ((Sleep) ((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(1).getCollect().getSources().get(0)).getSteps().get(2)).getDuration());
	}
}
