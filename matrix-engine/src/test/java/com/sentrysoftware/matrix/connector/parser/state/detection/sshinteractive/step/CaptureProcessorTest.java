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
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.GetAvailable;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitFor;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive.SshInteractive;

class CaptureProcessorTest {

	private static final String CAPTURE_NAME = "Detection.Criteria(1).Step(2).Capture";
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
			final Matcher matcher = CAPTURE_PROCESSOR.getMatcher(CAPTURE_NAME);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {

		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> CAPTURE_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> CAPTURE_PROCESSOR.parse(CAPTURE_NAME, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> CAPTURE_PROCESSOR.parse(CAPTURE_NAME, VALUE, null));

		// check criterion not found
		assertThrows(IllegalStateException.class, () -> CAPTURE_PROCESSOR.parse(CAPTURE_NAME, VALUE, CONNECTOR));
		assertThrows(IllegalStateException.class, () -> CAPTURE_PROCESSOR.parse(CAPTURE_NAME, VALUE, Connector.builder().detection(Detection.builder().build()).build()));

		final SshInteractive sshInteractive = SshInteractive.builder()
				.index(1)
				.steps(List.of(WaitFor.builder().index(1).build(), GetAvailable.builder().index(2).build()))
				.build();

		final Detection detection = Detection.builder().criteria(List.of(sshInteractive)).build();
		CONNECTOR.setDetection(detection);

		CAPTURE_PROCESSOR.parse(CAPTURE_NAME, "0", CONNECTOR);
		assertFalse(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(CAPTURE_NAME, "FALSE", CONNECTOR);
		assertFalse(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(CAPTURE_NAME, "no", CONNECTOR);
		assertFalse(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(CAPTURE_NAME, VALUE, CONNECTOR);
		assertTrue(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(CAPTURE_NAME, "1", CONNECTOR);
		assertTrue(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isCapture());

		CAPTURE_PROCESSOR.parse(CAPTURE_NAME, "YES", CONNECTOR);
		assertTrue(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isCapture());
	}
}
