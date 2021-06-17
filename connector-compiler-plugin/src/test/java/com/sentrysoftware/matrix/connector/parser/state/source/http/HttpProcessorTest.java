package com.sentrysoftware.matrix.connector.parser.state.source.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;

class HttpProcessorTest {

	@Test
	void testGetType() {

		assertEquals(HTTPSource.class, new UrlProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(HttpProcessor.HTTP_TYPE_VALUE, new UrlProcessor().getTypeValue());
	}
}
