package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.Http;

class AuthenticationTokenProcessorTest {

	private final AuthenticationTokenProcessor authTokenProcessor = new AuthenticationTokenProcessor();

	private final Connector connector = new Connector();

	private static final String KEY = "Detection.Criteria(1).AuthenticationToken";
	private static final String AUTH_TOKEN = "%{USERNAME}_%{PASSWORD}";

	@Test
	void testParse() {

		final Http http = Http.builder().index(1).build();
		final Detection detection = Detection.builder().criteria(Collections.singletonList(http)).build();
		connector.setDetection(detection);
		assertNull(http.getAuthenticationToken());
		authTokenProcessor.parse(KEY, AUTH_TOKEN, connector);
		assertEquals(AUTH_TOKEN, http.getAuthenticationToken());
	}
}
