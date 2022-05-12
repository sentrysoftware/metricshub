package com.sentrysoftware.matrix.connector.parser.state.detection.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OSCommand;

class TimeoutProcessorTest {

	@Test
	void testGetMatcher() {
		TimeoutProcessor tp = new TimeoutProcessor(OSCommand.class, "OSCommand");
		assertTrue(tp.getMatcher("detection.criteria(1).timeout").find());
		assertFalse(tp.getMatcher("cpu.discovery.source(1).timeout").find());
		assertFalse(tp.getMatcher("detection.criteria(1).othertimeout").find());
	}

	@Test
	void testParse() {

		// Setup a fake connector
		Connector connector = new Connector();
		OSCommand osCommandCriteria = OSCommand.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(osCommandCriteria)).build();
		connector.setDetection(detection);

		// Pre-check: no timeout set
		assertNull(((OSCommand) connector.getDetection().getCriteria().get(0)).getTimeout());

		// Parse the value
		TimeoutProcessor tp = new TimeoutProcessor(OSCommand.class, "OSCommand");
		tp.parse("detection.criteria(1).timeout", "10", connector);
		assertEquals(10, ((OSCommand) connector.getDetection().getCriteria().get(0)).getTimeout());

		// Now an invalid value
		assertThrows(IllegalStateException.class, () -> tp.parse("detection.criteria(1).timeout", "invalid", connector));
	}

}
