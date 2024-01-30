package org.sentrysoftware.metricshub.engine.client.http;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UrlTest {

	private static final String FULL_URL_1 = "http://rd-vm:5985/path";
	private static final String FULL_URL_2 = "http://rd-vm:5985/path/subpath";
	private static final String URL_1 = "http://rd-vm:5985/path";
	private static final String URL_2 = "http://rd-vm:5985/path/";
	private static final String PATH_1 = "subpath";
	private static final String PATH_2 = "/subpath";
	private static final String PATH_3 = "/path";
	private static final String HOSTNAME = "rd-vm";
	private static final Integer PORT = 5985;
	private static final String PROTOCOL = "http";
	private static final String EMPTY = "";

	@Test
	void testFormatUrlAndPath() {
		assertEquals(FULL_URL_2, Url.format(URL_1, PATH_1));
		assertEquals(FULL_URL_2, Url.format(URL_1, PATH_2));
		assertEquals(FULL_URL_2, Url.format(URL_2, PATH_1));
		assertEquals(FULL_URL_2, Url.format(URL_2, PATH_2));
	}

	@Test
	void testFormatPathOnly() {
		assertEquals(Url.format(HOSTNAME, PORT, PATH_3, PROTOCOL), FULL_URL_1);
		assertThrows(IllegalArgumentException.class, () -> Url.format(null, PORT, PATH_3, PROTOCOL));
		assertThrows(IllegalArgumentException.class, () -> Url.format(HOSTNAME, null, PATH_3, PROTOCOL));
		assertThrows(IllegalArgumentException.class, () -> Url.format(HOSTNAME, PORT, PATH_3, null));
	}

	@Test
	void testUrlProcessing() {
		assertEquals(Url.format(PROTOCOL, HOSTNAME, PORT, PATH_1, FULL_URL_1), FULL_URL_2);
		assertThrows(IllegalArgumentException.class, () -> Url.format(null, null, null, PATH_3, EMPTY));
	}

	@Test
	void testUrlProcessingWithUrlOnly() {
		assertEquals(Url.format(PROTOCOL, HOSTNAME, PORT, EMPTY, FULL_URL_1), FULL_URL_1);
	}

	@Test
	void testUrlProcessingWithPathOnly() {
		assertEquals(Url.format(PROTOCOL, HOSTNAME, PORT, PATH_3, EMPTY), FULL_URL_1);
	}

	@Test
	void testUrlProcessingWithoutPathNorUrl() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> Url.format(PROTOCOL, HOSTNAME, PORT, EMPTY, EMPTY));
	}
}
