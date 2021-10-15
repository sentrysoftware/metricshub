package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.GetAvailable;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.GetUntilPrompt;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendPassword;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendText;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendUsername;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Sleep;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitFor;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitForPrompt;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;

class TypeProcessorTest {

	private static final String DISCOVERY_TYPE_NAME = "Blade.Discovery.Source(1).Step(1).Type";
	private static final String COLLECT_TYPE_NAME = "NetworkCard.Collect.Source(1).Step(1).Type";
	private static final TypeProcessor TYPE_PROCESSOR = new TypeProcessor(SendText.class, "SendText");
	private static final String VALUE = "SendText";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(SendText.class, TYPE_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("SendText", TYPE_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {

		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = TYPE_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = TYPE_PROCESSOR.getMatcher("detection.criteria(1).exclude");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = TYPE_PROCESSOR.getMatcher(DISCOVERY_TYPE_NAME);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {

		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(DISCOVERY_TYPE_NAME, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(DISCOVERY_TYPE_NAME, VALUE, null));

		assertThrows(IllegalStateException.class, () -> TYPE_PROCESSOR.parse(DISCOVERY_TYPE_NAME, VALUE, CONNECTOR));

		final Discovery discovery = Discovery.builder().sources(List.of(SshInteractiveSource.builder().index(1).build())).build();
		final HardwareMonitor bladeMonitor = HardwareMonitor.builder().type(MonitorType.BLADE).discovery(discovery).build();

		final Collect collect = Collect.builder().sources(List.of(SshInteractiveSource.builder().index(1).build())).build();
		final HardwareMonitor networkCardMonitor = HardwareMonitor.builder().type(MonitorType.NETWORK_CARD).collect(collect).build();

		CONNECTOR.setHardwareMonitors(List.of(bladeMonitor, networkCardMonitor));

		// check invalid type
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(DISCOVERY_TYPE_NAME, "unknown", CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(DISCOVERY_TYPE_NAME, "WaitFor", CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(COLLECT_TYPE_NAME, "unknown", CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(COLLECT_TYPE_NAME, "WaitFor", CONNECTOR));

		TYPE_PROCESSOR.parse(DISCOVERY_TYPE_NAME, VALUE, CONNECTOR);
		
		new TypeProcessor(Sleep.class, "Sleep").parse("Blade.Discovery.Source(1).Step(2).Type", "Sleep", CONNECTOR);
		new TypeProcessor(GetAvailable.class, "GetAvailable").parse("Blade.Discovery.Source(1).Step(3).Type", "GetAvailable", CONNECTOR);
		new TypeProcessor(WaitFor.class, "WaitFor").parse("Blade.Discovery.Source(1).Step(4).Type", "WaitFor", CONNECTOR);
		new TypeProcessor(WaitForPrompt.class, "WaitForPrompt").parse("Blade.Discovery.Source(1).Step(5).Type", "WaitForPrompt", CONNECTOR);
		new TypeProcessor(GetUntilPrompt.class, "GetUntilPrompt").parse("Blade.Discovery.Source(1).Step(6).Type", "GetUntilPrompt", CONNECTOR);
		new TypeProcessor(SendUsername.class, "SendUsername").parse("Blade.Discovery.Source(1).Step(7).Type", "SendUsername", CONNECTOR);
		new TypeProcessor(SendPassword.class, "SendPassword").parse("Blade.Discovery.Source(1).Step(8).Type", "SendPassword", CONNECTOR);

		TYPE_PROCESSOR.parse(COLLECT_TYPE_NAME, VALUE, CONNECTOR);

		new TypeProcessor(SendPassword.class, "SendPassword").parse("NetworkCard.Collect.Source(1).Step(2).Type", "SendPassword", CONNECTOR);
		new TypeProcessor(SendUsername.class, "SendUsername").parse("NetworkCard.Collect.Source(1).Step(3).Type", "SendUsername", CONNECTOR);
		new TypeProcessor(GetUntilPrompt.class, "GetUntilPrompt").parse("NetworkCard.Collect.Source(1).Step(4).Type", "GetUntilPrompt", CONNECTOR);
		new TypeProcessor(WaitForPrompt.class, "WaitForPrompt").parse("NetworkCard.Collect.Source(1).Step(5).Type", "WaitForPrompt", CONNECTOR);
		new TypeProcessor(WaitFor.class, "WaitFor").parse("NetworkCard.Collect.Source(1).Step(6).Type", "WaitFor", CONNECTOR);
		new TypeProcessor(GetAvailable.class, "GetAvailable").parse("NetworkCard.Collect.Source(1).Step(7).Type", "GetAvailable", CONNECTOR);
		new TypeProcessor(Sleep.class, "Sleep").parse("NetworkCard.Collect.Source(1).Step(8).Type", "Sleep", CONNECTOR);

		final List<Class<? extends Step>> expectedDiscovery = List.of(
				SendText.class,
				Sleep.class,
				GetAvailable.class,
				WaitFor.class,
				WaitForPrompt.class,
				GetUntilPrompt.class,
				SendUsername.class,
				SendPassword.class);

		final List<Class<? extends Step>> actualDiscoverySteps = ((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(0).getDiscovery().getSources().get(0)).getSteps().stream()
				.map(step -> step.getClass())
				.collect(Collectors.toList());
				
		assertEquals(expectedDiscovery, actualDiscoverySteps);

		final List<Class<? extends Step>> expectedCollect = List.of(
				SendText.class,
				SendPassword.class,
				SendUsername.class,
				GetUntilPrompt.class,
				WaitForPrompt.class,
				WaitFor.class,
				GetAvailable.class,
				Sleep.class);

		final List<Class<? extends Step>> actualCollectSteps = ((SshInteractiveSource) CONNECTOR.getHardwareMonitors().get(1).getCollect().getSources().get(0)).getSteps().stream()
				.map(step -> step.getClass())
				.collect(Collectors.toList());
				
		assertEquals(expectedCollect, actualCollectSteps);
	}
}
