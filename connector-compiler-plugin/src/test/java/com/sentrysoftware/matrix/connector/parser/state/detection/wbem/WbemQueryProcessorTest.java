package com.sentrysoftware.matrix.connector.parser.state.detection.wbem;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class WbemQueryProcessorTest {

	private final WbemQueryProcessor wbemQueryProcessor = new WbemQueryProcessor();

	private final Connector connector = new Connector();

	private static final String WBEM_QUERY_KEY = "detection.criteria(1).wbemquery";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		WBEM wbem = WBEM.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(wbem)).build();
		connector.setDetection(detection);
		assertNull(wbem.getWbemQuery());
		wbemQueryProcessor.parse(WBEM_QUERY_KEY, FOO, connector);
		assertEquals(FOO, wbem.getWbemQuery());
	}
}