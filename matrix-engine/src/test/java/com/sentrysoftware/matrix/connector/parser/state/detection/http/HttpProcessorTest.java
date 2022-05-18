package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import com.sentrysoftware.matrix.connector.model.detection.criteria.http.Http;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpProcessorTest {

	@Test
	void testGetType() {

		assertEquals(Http.class, new UrlProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(HttpProcessor.HTTP_TYPE_VALUE, new UrlProcessor().getTypeValue());
	}
}