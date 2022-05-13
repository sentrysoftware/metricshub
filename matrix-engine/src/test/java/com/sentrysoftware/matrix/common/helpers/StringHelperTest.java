package com.sentrysoftware.matrix.common.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;

import org.junit.jupiter.api.Test;

class StringHelperTest {

	@Test
	void testGetStackMessages() {
		assertNotNull(StringHelper.getStackMessages(null));
		assertTrue(StringHelper.getStackMessages(null).isEmpty());
		assertEquals("Exception: Error 2\n"
				+ "Caused by Exception: Error 1",
				StringHelper.getStackMessages(new Exception("Error 2", new Exception("Error 1"))));
		assertEquals("Exception: Error 2\n"
				+ "Caused by Exception: Error 1",
				StringHelper.getStackMessages(new Exception("Error 2", new Exception("Error 1", null))));
	}

	@Test
	void testPrettyHttpHeaders() {
		assertEquals(HardwareConstants.EMPTY, StringHelper.prettyHttpHeaders(Collections.emptyMap()));
		assertEquals(HardwareConstants.EMPTY, StringHelper.prettyHttpHeaders(null));
		assertEquals("Connection: keep-alive\nContent-Type: application/json",
				StringHelper.prettyHttpHeaders(Map.of("Connection", "keep-alive", "Content-Type", "application/json")));
	}

	@Test
	void testAddNonNull() {
		final StringJoiner stringJoiner = new StringJoiner(";");
		StringHelper.addNonNull(stringJoiner, "prefix", null);
		StringHelper.addNonNull(stringJoiner, "prefix1=", "val1");
		StringHelper.addNonNull(stringJoiner, "prefix2=", 10);
		assertEquals("prefix1=val1;prefix2=10", stringJoiner.toString());
	}
}
