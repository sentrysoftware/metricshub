package com.sentrysoftware.matrix.connector.model.common.http.url;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.sentrysoftware.matrix.connector.model.common.http.url.Url.*;
import org.junit.jupiter.api.Test;

class UrlTest {

	@Test
	void getContentTest() {
		assertEquals("https://address:9999/api/json/address", format("address", 9999, "/api/json/%{HOSTNAME}", "https"));
		assertEquals("https://address:9999/api/json/", format("address", 9999, "api/json/", "https"));
	}
}