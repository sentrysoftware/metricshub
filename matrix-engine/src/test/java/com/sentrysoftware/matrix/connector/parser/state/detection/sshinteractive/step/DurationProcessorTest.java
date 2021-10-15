package com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendText;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Sleep;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitFor;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive.SshInteractive;

class DurationProcessorTest {

	private static final String DURATION_NAME = "Detection.Criteria(1).Step(3).Duration";
	private static final DurationProcessor DURATION_PROCESSOR = new DurationProcessor();
	private static final String VALUE = "120";
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

		// check criterion not found
		assertThrows(IllegalStateException.class, () -> DURATION_PROCESSOR.parse(DURATION_NAME, VALUE, CONNECTOR));
		assertThrows(IllegalStateException.class, () -> DURATION_PROCESSOR.parse(DURATION_NAME, VALUE, Connector.builder().detection(Detection.builder().build()).build()));

		final SshInteractive sshInteractive = SshInteractive.builder()
				.index(1)
				.steps(List.of(SendText.builder().index(1).build(), WaitFor.builder().index(2).build(), Sleep.builder().index(3).build()))
				.build();

		final Detection detection = Detection.builder().criteria(List.of(sshInteractive)).build();
		CONNECTOR.setDetection(detection);

		assertThrows(IllegalStateException.class, () -> DURATION_PROCESSOR.parse(DURATION_NAME, "X", CONNECTOR));

		DURATION_PROCESSOR.parse(DURATION_NAME, VALUE, CONNECTOR);
		assertEquals(120L, ((Sleep) ((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(2)).getDuration());
	}
}
