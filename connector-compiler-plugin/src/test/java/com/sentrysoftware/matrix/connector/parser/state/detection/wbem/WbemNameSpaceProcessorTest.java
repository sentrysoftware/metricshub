package com.sentrysoftware.matrix.connector.parser.state.detection.wbem;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WbemNameSpaceProcessorTest {

	private final WbemNameSpaceProcessor wbemNameSpaceProcessor = new WbemNameSpaceProcessor();

	private final Connector connector = new Connector();

	private static final String WBEM_NAMESPACE_KEY = "detection.criteria(1).wbemnamespace";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		WBEM wbem = WBEM.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(wbem)).build();
		connector.setDetection(detection);
		assertNull(wbem.getWbemNamespace());
		wbemNameSpaceProcessor.parse(WBEM_NAMESPACE_KEY, FOO, connector);
		assertEquals(FOO, wbem.getWbemNamespace());
	}
}