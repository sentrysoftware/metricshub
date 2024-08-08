package org.sentrysoftware.metricshub.extension.http.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOSTNAME_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.PASSWORD_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.USERNAME_MACRO;

import org.junit.jupiter.api.Test;

class HttpMacrosUpdaterTest {

	@Test
	void testUpdate() {
		final String text = String.format(
			"""
			%s
			%s
			%s
			%s
			%s
			%s
			%s
			%s
			""",
			HOSTNAME_MACRO,
			PASSWORD_MACRO,
			USERNAME_MACRO,
			HttpMacrosUpdater.BASIC_AUTH_BASE64_MACRO,
			HttpMacrosUpdater.SHA256_AUTH_MACRO,
			HttpMacrosUpdater.PASSWORD_BASE64_MACRO,
			HttpMacrosUpdater.USERNAME_JSON,
			HttpMacrosUpdater.PASSWORD_JSON
		);

		final String result = HttpMacrosUpdater.update(text, "user", "pwd".toCharArray(), "token", "hostname");
		final String expected =
			"""
			hostname
			pwd
			user
			dXNlcjpwd2Q=
			3c469e9d6c5875d37a43f353d4f88e61fcf812c66eee3457465a40b0da4153e0
			cHdk
			user
			pwd
			""";
		assertEquals(expected, result);
	}

	@Test
	void testJsonValueEscape() {
		assertEquals("test\\\"test", HttpMacrosUpdater.jsonValueEscape("test\"test"));
		assertEquals("test\\ttest", HttpMacrosUpdater.jsonValueEscape("test\ttest"));
		assertEquals("test\\ntest", HttpMacrosUpdater.jsonValueEscape("test\ntest"));
		assertEquals("test\\rtest", HttpMacrosUpdater.jsonValueEscape("test\rtest"));
		assertEquals("te\\\\st", HttpMacrosUpdater.jsonValueEscape("te\\st"));
	}
}
