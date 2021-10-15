package com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step;

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
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive.SshInteractive;

class TypeProcessorTest {

	private static final String TYPE_NAME = "Detection.Criteria(1).Step(1).Type";
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
			final Matcher matcher = TYPE_PROCESSOR.getMatcher(TYPE_NAME);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {

		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(TYPE_NAME, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(TYPE_NAME, VALUE, null));

		// check criterion not found
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(TYPE_NAME, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(TYPE_NAME, VALUE, Connector.builder().detection(Detection.builder().build()).build()));

		final SshInteractive sshInteractive = SshInteractive.builder()
				.index(1)
				.build();

		final Detection detection = Detection.builder().criteria(List.of(sshInteractive)).build();
		CONNECTOR.setDetection(detection);

		// check invalid type
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(TYPE_NAME, "unknown", CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TYPE_PROCESSOR.parse(TYPE_NAME, "WaitFor", CONNECTOR));
		
		TYPE_PROCESSOR.parse(TYPE_NAME, VALUE, CONNECTOR);
		new TypeProcessor(Sleep.class, "Sleep").parse("Detection.Criteria(1).Step(2).Type", "Sleep", CONNECTOR);
		new TypeProcessor(GetAvailable.class, "GetAvailable").parse("Detection.Criteria(1).Step(3).Type", "GetAvailable", CONNECTOR);
		new TypeProcessor(WaitFor.class, "WaitFor").parse("Detection.Criteria(1).Step(4).Type", "WaitFor", CONNECTOR);
		new TypeProcessor(WaitForPrompt.class, "WaitForPrompt").parse("Detection.Criteria(1).Step(5).Type", "WaitForPrompt", CONNECTOR);
		new TypeProcessor(GetUntilPrompt.class, "GetUntilPrompt").parse("Detection.Criteria(1).Step(6).Type", "GetUntilPrompt", CONNECTOR);
		new TypeProcessor(SendUsername.class, "SendUsername").parse("Detection.Criteria(1).Step(7).Type", "SendUsername", CONNECTOR);
		new TypeProcessor(SendPassword.class, "SendPassword").parse("Detection.Criteria(1).Step(8).Type", "SendPassword", CONNECTOR);

		final List<Class<? extends Step>> expected = List.of(
				SendText.class,
				Sleep.class,
				GetAvailable.class,
				WaitFor.class,
				WaitForPrompt.class,
				GetUntilPrompt.class,
				SendUsername.class,
				SendPassword.class);

		final List<Class<? extends Step>> actualSteps = ((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().stream()
				.map(step -> step.getClass())
				.collect(Collectors.toList());
				
		assertEquals(expected, actualSteps);
	}
}
