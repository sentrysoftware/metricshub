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

class TextProcessorText {

	private static final String TEXT_NAME = "Blade.Discovery.Source(1).Step(5).Text";
	private static final TextProcessor TEXT_PROCESSOR = new TextProcessor(WaitFor.class, "WaitFor");
	private static final String VALUE = "ogin:";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(WaitFor.class, TEXT_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("WaitFor", TEXT_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {

		assertThrows(IllegalArgumentException.class, () -> TEXT_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = TEXT_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = TEXT_PROCESSOR.getMatcher("detection.criteria(1).exclude");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = TEXT_PROCESSOR.getMatcher(TEXT_NAME);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {

		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> TEXT_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TEXT_PROCESSOR.parse(TEXT_NAME, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TEXT_PROCESSOR.parse(TEXT_NAME, VALUE, null));

		assertThrows(IllegalStateException.class, () -> TEXT_PROCESSOR.parse(TEXT_NAME, VALUE, CONNECTOR));

		final WaitFor step = new WaitFor();
		step.setIndex(5);

		final SshInteractiveSource sshInteractiveSource = SshInteractiveSource.builder()
				.index(1)
				.steps(List.of(new Sleep(), new GetAvailable(), step))
				.build();

		final Discovery discovery = Discovery.builder().sources(List.of(sshInteractiveSource)).build();
		final HardwareMonitor bladeMonitor = HardwareMonitor.builder().type(MonitorType.BLADE).discovery(discovery).build();

		final Collect collect = Collect.builder().sources(List.of(sshInteractiveSource)).build();
		final HardwareMonitor networkCardMonitor = HardwareMonitor.builder().type(MonitorType.ENCLOSURE).collect(collect).build();

		CONNECTOR.setHardwareMonitors(List.of(bladeMonitor, networkCardMonitor));

		TEXT_PROCESSOR.parse(TEXT_NAME, VALUE, CONNECTOR);
		assertEquals(VALUE, ((WaitFor) ((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getSteps().get(2)).getText());

		TEXT_PROCESSOR.parse("Enclosure.Collect.Source(1).Step(5).Text", VALUE, CONNECTOR);
		assertEquals(VALUE, ((WaitFor) ((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(1).getCollect().getSources().get(0)).getSteps().get(2)).getText());
	}
}
