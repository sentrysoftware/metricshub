package com.sentrysoftware.metricshub.engine.common.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JUtilsTest {

	@Test
	void sha256ProcessorTestOK() {
		final String input = "username_password";
		final String result = "c557c390026e598a852fe5d2fcdad83d7c759451b0aa355854bbc57db4b5e817"; //SHA-256 of "username_password"
		assertEquals(result, JUtils.encodeSha256(input));
	}

	@Test
	void sha256ProcessorTestEmpty() {
		assertNotNull(JUtils.encodeSha256(""));
	}

	@Test
	void sha256ProcessorTestNull() {
		assertNull(JUtils.encodeSha256(null));
	}
}