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

class TextProcessorText {

	private static final String TEXT_NAME = "Detection.Criteria(1).Step(3).Text";
	private static final TextProcessor TEXT_PROCESSOR = new TextProcessor(SendText.class, "SendText");
	private static final String VALUE = "Hello World";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(SendText.class, TEXT_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("SendText", TEXT_PROCESSOR.getTypeValue());
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

		// check criterion not found
		assertThrows(IllegalStateException.class, () -> TEXT_PROCESSOR.parse(TEXT_NAME, VALUE, CONNECTOR));
		assertThrows(IllegalStateException.class, () -> TEXT_PROCESSOR.parse(TEXT_NAME, VALUE, Connector.builder().detection(Detection.builder().build()).build()));

		final SshInteractive sshInteractive = SshInteractive.builder()
				.index(1)
				.steps(List.of(WaitFor.builder().index(1).build(), Sleep.builder().index(2).build(), SendText.builder().index(3).build()))
				.build();

		final Detection detection = Detection.builder().criteria(List.of(sshInteractive)).build();
		CONNECTOR.setDetection(detection);

		TEXT_PROCESSOR.parse(TEXT_NAME, VALUE, CONNECTOR);
		assertEquals(VALUE, ((SendText) ((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(2)).getText());
	}
}
