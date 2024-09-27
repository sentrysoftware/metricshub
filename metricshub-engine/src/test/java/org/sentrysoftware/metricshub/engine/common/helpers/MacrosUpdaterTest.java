package org.sentrysoftware.metricshub.engine.common.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOSTNAME_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.PASSWORD_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.USERNAME_MACRO;

import org.junit.jupiter.api.Test;

class MacrosUpdaterTest {

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
			MacrosUpdater.BASIC_AUTH_BASE64_MACRO,
			MacrosUpdater.SHA256_AUTH_MACRO,
			MacrosUpdater.PASSWORD_BASE64_MACRO,
			MacrosUpdater.USERNAME_ESC_JSON,
			MacrosUpdater.PASSWORD_ESC_JSON
		);

		final String result = MacrosUpdater.update(text, "user", "pwd".toCharArray(), "token", "hostname");
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
	void testEscapeJsonSpecialCharacters() {
		assertEquals("test\\\"test", MacrosUpdater.escapeJsonSpecialCharacters("test\"test"));
		assertEquals("test\\ttest", MacrosUpdater.escapeJsonSpecialCharacters("test\ttest"));
		assertEquals("test\\ntest", MacrosUpdater.escapeJsonSpecialCharacters("test\ntest"));
		assertEquals("test\\rtest", MacrosUpdater.escapeJsonSpecialCharacters("test\rtest"));
		assertEquals("te\\\\st", MacrosUpdater.escapeJsonSpecialCharacters("te\\st"));
	}

	@Test
	public void testEscapeUrlSpecialCharacters() {
		assertEquals("Hello%20World", MacrosUpdater.escapeUrlSpecialCharacters("Hello World"));
		assertEquals("test%40example.com", MacrosUpdater.escapeUrlSpecialCharacters("test@example.com"));
		assertEquals("%24%26%2F%3F", MacrosUpdater.escapeUrlSpecialCharacters("$&/?"));
		assertEquals("%7Bfoo%7D", MacrosUpdater.escapeUrlSpecialCharacters("{foo}"));
		assertEquals("%7E%23", MacrosUpdater.escapeUrlSpecialCharacters("~#"));
	}

	@Test
	public void testEscapeRegexSpecialCharacters() {
		// Test escaping of common regex characters
		assertEquals("\\Q.\\E", MacrosUpdater.escapeRegexSpecialCharacters("."));
		assertEquals("\\Q^\\E", MacrosUpdater.escapeRegexSpecialCharacters("^"));
		assertEquals("\\Q$\\E", MacrosUpdater.escapeRegexSpecialCharacters("$"));
		assertEquals("\\Q*\\E", MacrosUpdater.escapeRegexSpecialCharacters("*"));
		assertEquals("\\Q+\\E", MacrosUpdater.escapeRegexSpecialCharacters("+"));
		assertEquals("\\Q?\\E", MacrosUpdater.escapeRegexSpecialCharacters("?"));
		assertEquals("\\Q{\\E", MacrosUpdater.escapeRegexSpecialCharacters("{"));
		assertEquals("\\Q}\\E", MacrosUpdater.escapeRegexSpecialCharacters("}"));
		assertEquals("\\Q[\\E", MacrosUpdater.escapeRegexSpecialCharacters("["));
		assertEquals("\\Q]\\E", MacrosUpdater.escapeRegexSpecialCharacters("]"));
		assertEquals("\\Q(\\E", MacrosUpdater.escapeRegexSpecialCharacters("("));
		assertEquals("\\Q)\\E", MacrosUpdater.escapeRegexSpecialCharacters(")"));
		assertEquals("\\Q|\\E", MacrosUpdater.escapeRegexSpecialCharacters("|"));
		assertEquals("\\Q\\\\E", MacrosUpdater.escapeRegexSpecialCharacters("\\"));
		assertEquals("\\Q/\\E", MacrosUpdater.escapeRegexSpecialCharacters("/"));

		// Test a combined string with multiple special characters
		assertEquals("\\Q(a*b+c)\\E", MacrosUpdater.escapeRegexSpecialCharacters("(a*b+c)"));

		// Test string with no special regex characters (should remain unchanged)
		assertEquals("\\Qabc123\\E", MacrosUpdater.escapeRegexSpecialCharacters("abc123"));

		// Test an empty string input (should remain unchanged)
		assertEquals("\\Q\\E", MacrosUpdater.escapeRegexSpecialCharacters(""));

		// Test complex string containing both escaped special characters and normal characters
		assertEquals("\\Qabc(def)[ghi].\\E", MacrosUpdater.escapeRegexSpecialCharacters("abc(def)[ghi]."));
	}

	@Test
	public void testEscapeXmlSpecialCharacters() {
		assertEquals("&lt;tag&gt;", MacrosUpdater.escapeXmlSpecialCharacters("<tag>"));
		assertEquals("Fish &amp; Chips", MacrosUpdater.escapeXmlSpecialCharacters("Fish & Chips"));
		assertEquals("&quot;Hello&quot;", MacrosUpdater.escapeXmlSpecialCharacters("\"Hello\""));
		assertEquals("It&apos;s mine", MacrosUpdater.escapeXmlSpecialCharacters("It's mine"));
	}

	@Test
	public void testEscapeWindowsCmdSpecialCharacters() {
		assertEquals("^&^|^<^>", MacrosUpdater.escapeWindowsCmdSpecialCharacters("&|<>"));
		assertEquals("Hello ^^World", MacrosUpdater.escapeWindowsCmdSpecialCharacters("Hello ^World"));
		assertEquals("foo^(bar^)", MacrosUpdater.escapeWindowsCmdSpecialCharacters("foo(bar)"));
		assertEquals("^\"Hello^\"", MacrosUpdater.escapeWindowsCmdSpecialCharacters("\"Hello\""));
	}

	@Test
	public void testEscapePowershellSpecialCharacters() {
		assertEquals(
			"This is a test with `\"double quotes`\" and `$variables`.",
			MacrosUpdater.escapePowershellSpecialCharacters("This is a test with \"double quotes\" and $variables.")
		);
		assertEquals(
			"Escaped characters: `\n, `\t, `\r, `$, `{, `}, `[, `], `#, and `\0`.",
			MacrosUpdater.escapePowershellSpecialCharacters("Escaped characters: \n, \t, \r, $, {, }, [, ], #, and \0.")
		);
		assertEquals(
			"No special characters here",
			MacrosUpdater.escapePowershellSpecialCharacters("No special characters here")
		);
	}

	@Test
	public void testEscapeBashSpecialCharacters() {
		assertEquals("\\$\\!\\*", MacrosUpdater.escapeBashSpecialCharacters("$!*"));
		assertEquals("\\&\\|\\<\\>", MacrosUpdater.escapeBashSpecialCharacters("&|<>"));
		assertEquals("\\\\'Hello\\\\'", MacrosUpdater.escapeBashSpecialCharacters("'Hello'"));
		assertEquals("\\\\\"Hello\\\\\"", MacrosUpdater.escapeBashSpecialCharacters("\"Hello\""));
	}

	@Test
	public void testEscapeSqlSpecialCharacters() {
		assertEquals("O''Reilly", MacrosUpdater.escapeSqlSpecialCharacters("O'Reilly"));
		assertEquals("Line\\nBreak", MacrosUpdater.escapeSqlSpecialCharacters("Line\nBreak"));
		assertEquals("Column\\tTab", MacrosUpdater.escapeSqlSpecialCharacters("Column\tTab"));
		assertEquals("\\\\\"Quote\\\\\"", MacrosUpdater.escapeSqlSpecialCharacters("\"Quote\""));
	}
}
