package com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive.SshInteractive;

class PortProcessorTest {

	private static final String PORT_NAME = "Detection.Criteria(1).Port";
	private static final PortProcessor PORT_PROCESSOR = new PortProcessor();
	private static final String VALUE = "23";
	private static final Connector CONNECTOR = new Connector();

	@Test
	void testGetType() {
		assertEquals(SshInteractive.class, PORT_PROCESSOR.getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals("TelnetInteractive", PORT_PROCESSOR.getTypeValue());
	}

	@Test
	void testGetMatcher() {

		assertThrows(IllegalArgumentException.class, () -> PORT_PROCESSOR.getMatcher(null));
		{
			final Matcher matcher = PORT_PROCESSOR.getMatcher("");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = PORT_PROCESSOR.getMatcher("detection.criteria(1).exclude");
			assertNotNull(matcher);
			assertFalse(matcher.find());
		}
		{
			final Matcher matcher = PORT_PROCESSOR.getMatcher(PORT_NAME);
			assertNotNull(matcher);
			assertTrue(matcher.find());
		}
	}

	@Test
	void testParse() {

		// check non null arguments
		assertThrows(IllegalArgumentException.class, () -> PORT_PROCESSOR.parse(null, VALUE, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> PORT_PROCESSOR.parse(PORT_NAME, null, CONNECTOR));
		assertThrows(IllegalArgumentException.class, () -> PORT_PROCESSOR.parse(PORT_NAME, VALUE, null));

		// check criterion not found
		assertThrows(IllegalStateException.class, () -> PORT_PROCESSOR.parse(PORT_NAME, VALUE, CONNECTOR));

		final SshInteractive sshInteractive = SshInteractive.builder().index(1).build();
		final Detection detection = Detection.builder().criteria(List.of(sshInteractive)).build();
		CONNECTOR.setDetection(detection);
		PORT_PROCESSOR.parse(PORT_NAME, VALUE, CONNECTOR);
	}
}
