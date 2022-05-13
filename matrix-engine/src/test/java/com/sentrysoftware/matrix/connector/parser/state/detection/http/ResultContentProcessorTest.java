package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.http.ResultContent;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.Http;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResultContentProcessorTest {

	private final ResultContentProcessor resultContentProcessor = new ResultContentProcessor();

	private final Connector connector = new Connector();

	private static final String KEY = "detection.criteria(1).resultcontent";
	private static final String FOO = "FOO";
	private static final String ALL = "ALL";

	@Test
	void testParse() {

		// Invalid ResultContent value
		Http http = Http.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(http)).build();
		connector.setDetection(detection);
		assertThrows(IllegalArgumentException.class, () -> resultContentProcessor.parse(KEY, FOO, connector));

		// Valid ResultContent value
		assertEquals(ResultContent.BODY, http.getResultContent());
		resultContentProcessor.parse(KEY, ALL, connector);
		assertEquals(ResultContent.ALL, http.getResultContent());
	}
}