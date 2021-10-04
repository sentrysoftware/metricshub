package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.HTTP;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MethodProcessorTest {

	private final MethodProcessor methodProcessor = new MethodProcessor();

	private final Connector connector = new Connector();

	private static final String KEY = "detection.criteria(1).method";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		HTTP http = HTTP.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(http)).build();
		connector.setDetection(detection);
		assertNull(http.getMethod());
		methodProcessor.parse(KEY, FOO, connector);
		assertEquals(FOO, http.getMethod());
	}
}