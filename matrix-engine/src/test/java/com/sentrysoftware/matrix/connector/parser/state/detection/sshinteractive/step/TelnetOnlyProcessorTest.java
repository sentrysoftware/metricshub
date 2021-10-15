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

class TelnetOnlyProcessorTest {

	private static final String TELNET_ONLY_NAME = "Detection.Criteria(1).Step(2).TelnetOnly";
	private static final TelnetOnlyProcessor TELNET_ONLY_PROCESSOR = new TelnetOnlyProcessor(GetAvailable.class, "GetAvailable");
	private static final String VALUE = "1";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(GetAvailable.class, TELNET_ONLY_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("GetAvailable", TELNET_ONLY_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {

		assertThrows(IllegalArgumentException.class, () -> TELNET_ONLY_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = TELNET_ONLY_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = TELNET_ONLY_PROCESSOR.getMatcher("detection.criteria(1).exclude");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = TELNET_ONLY_PROCESSOR.getMatcher(TELNET_ONLY_NAME);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {

		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> TELNET_ONLY_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TELNET_ONLY_PROCESSOR.parse(TELNET_ONLY_NAME, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> TELNET_ONLY_PROCESSOR.parse(TELNET_ONLY_NAME, VALUE, null));

		// check criterion not found
		assertThrows(IllegalStateException.class, () -> TELNET_ONLY_PROCESSOR.parse(TELNET_ONLY_NAME, VALUE, CONNECTOR));
		assertThrows(IllegalStateException.class, () -> TELNET_ONLY_PROCESSOR.parse(TELNET_ONLY_NAME, VALUE, Connector.builder().detection(Detection.builder().build()).build()));

		final SshInteractive sshInteractive = SshInteractive.builder()
				.index(1)
				.steps(List.of(WaitFor.builder().index(1).build(), GetAvailable.builder().index(2).build()))
				.build();

		final Detection detection = Detection.builder().criteria(List.of(sshInteractive)).build();
		CONNECTOR.setDetection(detection);

		TELNET_ONLY_PROCESSOR.parse(TELNET_ONLY_NAME, "0", CONNECTOR);
		assertFalse(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isIgnored());

		TELNET_ONLY_PROCESSOR.parse(TELNET_ONLY_NAME, "FALSE", CONNECTOR);
		assertFalse(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isIgnored());

		TELNET_ONLY_PROCESSOR.parse(TELNET_ONLY_NAME, "no", CONNECTOR);
		assertFalse(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isIgnored());

		TELNET_ONLY_PROCESSOR.parse(TELNET_ONLY_NAME, VALUE, CONNECTOR);
		assertTrue(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isIgnored());

		TELNET_ONLY_PROCESSOR.parse(TELNET_ONLY_NAME, "1", CONNECTOR);
		assertTrue(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isIgnored());

		TELNET_ONLY_PROCESSOR.parse(TELNET_ONLY_NAME, "YES", CONNECTOR);
		assertTrue(((SshInteractive) CONNECTOR.getDetection().getCriteria().get(0)).getSteps().get(1).isIgnored());
	}
}
