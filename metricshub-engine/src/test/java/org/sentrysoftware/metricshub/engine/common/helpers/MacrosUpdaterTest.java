package org.sentrysoftware.metricshub.engine.common.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOSTNAME_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.PASSWORD_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.USERNAME_MACRO;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link MacrosUpdater} utility class.
 *
 * <p>This test class verifies that macros within a text string are correctly
 * replaced with their corresponding values, including proper escaping for various contexts
 * such as JSON, XML, SQL, etc.
 */
class MacrosUpdaterTest {

	private static final String PASSWORD_BASE64_MACRO = "%{PASSWORD_BASE64}";
	private static final String BASIC_AUTH_BASE64_MACRO = "%{BASIC_AUTH_BASE64}";
	private static final String SHA256_AUTH_MACRO = "%{SHA256_AUTH}";

	// Escaped macros
	private static final String USERNAME_ESC_JSON = "%{USERNAME_ESC_JSON}";
	private static final String PASSWORD_ESC_JSON = "%{PASSWORD_ESC_JSON}";
	private static final String USERNAME_ESC_XML = "%{USERNAME_ESC_XML}";
	private static final String PASSWORD_ESC_SQL = "%{PASSWORD_ESC_SQL}";
	private static final String USERNAME_ESC_URL = "%{USERNAME_ESC_URL}";
	private static final String PASSWORD_ESC_URL = "%{PASSWORD_ESC_URL}";
	private static final String USERNAME_ESC_REGEX = "%{USERNAME_ESC_REGEX}";
	private static final String PASSWORD_ESC_REGEX = "%{PASSWORD_ESC_REGEX}";
	private static final String USERNAME_ESC_WINDOWS_CMD = "%{USERNAME_ESC_WINDOWS}";
	private static final String PASSWORD_ESC_WINDOWS_CMD = "%{PASSWORD_ESC_WINDOWS}";
	private static final String USERNAME_ESC_POWERSHELL = "%{USERNAME_ESC_POWERSHELL}";
	private static final String PASSWORD_ESC_POWERSHELL = "%{PASSWORD_ESC_POWERSHELL}";
	private static final String USERNAME_ESC_BASH = "%{USERNAME_ESC_BASH}";
	private static final String PASSWORD_ESC_BASH = "%{PASSWORD_ESC_BASH}";

	/**
	 * Tests the primary macro replacement functionality, including basic macros and some escaped macros.
	 */
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
				BASIC_AUTH_BASE64_MACRO,
				SHA256_AUTH_MACRO,
				PASSWORD_BASE64_MACRO,
				USERNAME_ESC_JSON,
				PASSWORD_ESC_JSON
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

	/**
	 * Tests the escaping of special characters in JSON strings.
	 */
	@Test
	void testEscapeJsonSpecialCharacters() {
		assertEquals("test\\\"test", MacrosUpdater.escapeJsonSpecialCharacters("test\"test"));
		assertEquals("test\\ttest", MacrosUpdater.escapeJsonSpecialCharacters("test\ttest"));
		assertEquals("test\\ntest", MacrosUpdater.escapeJsonSpecialCharacters("test\ntest"));
		assertEquals("test\\rtest", MacrosUpdater.escapeJsonSpecialCharacters("test\rtest"));
		assertEquals("te\\\\st", MacrosUpdater.escapeJsonSpecialCharacters("te\\st"));
	}

	/**
	 * Tests the escaping of special characters in URLs.
	 */
	@Test
	public void testEscapeUrlSpecialCharacters() {
		assertEquals("Hello%20World", MacrosUpdater.escapeUrlSpecialCharacters("Hello World"));
		assertEquals("test%40example.com", MacrosUpdater.escapeUrlSpecialCharacters("test@example.com"));
		assertEquals("%24%26%2F%3F", MacrosUpdater.escapeUrlSpecialCharacters("$&/?"));
		assertEquals("%7Bfoo%7D", MacrosUpdater.escapeUrlSpecialCharacters("{foo}"));
		assertEquals("%7E%23", MacrosUpdater.escapeUrlSpecialCharacters("~#"));
	}

	/**
	 * Tests the escaping of special characters in regular expressions.
	 */
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

	/**
	 * Tests the escaping of special characters in XML strings.
	 */
	@Test
	public void testEscapeXmlSpecialCharacters() {
		assertEquals("&lt;tag&gt;", MacrosUpdater.escapeXmlSpecialCharacters("<tag>"));
		assertEquals("Fish &amp; Chips", MacrosUpdater.escapeXmlSpecialCharacters("Fish & Chips"));
		assertEquals("&quot;Hello&quot;", MacrosUpdater.escapeXmlSpecialCharacters("\"Hello\""));
		assertEquals("It&apos;s mine", MacrosUpdater.escapeXmlSpecialCharacters("It's mine"));
	}

	/**
	 * Tests the escaping of special characters in Windows CMD strings.
	 */
	@Test
	public void testEscapeWindowsCmdSpecialCharacters() {
		assertEquals("^&^|^<^>", MacrosUpdater.escapeWindowsCmdSpecialCharacters("&|<>"));
		assertEquals("Hello ^^World", MacrosUpdater.escapeWindowsCmdSpecialCharacters("Hello ^World"));
		assertEquals("foo^(bar^)", MacrosUpdater.escapeWindowsCmdSpecialCharacters("foo(bar)"));
		assertEquals("^\"Hello^\"", MacrosUpdater.escapeWindowsCmdSpecialCharacters("\"Hello\""));
	}

	/**
	 * Tests the escaping of special characters in PowerShell strings.
	 */
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

	/**
	 * Tests the escaping of special characters in Bash strings.
	 */
	@Test
	public void testEscapeBashSpecialCharacters() {
		assertEquals("\\$\\!\\*", MacrosUpdater.escapeBashSpecialCharacters("$!*"));
		assertEquals("\\&\\|\\<\\>", MacrosUpdater.escapeBashSpecialCharacters("&|<>"));
		assertEquals("\\\\'Hello\\\\'", MacrosUpdater.escapeBashSpecialCharacters("'Hello'"));
		assertEquals("\\\\\"Hello\\\\\"", MacrosUpdater.escapeBashSpecialCharacters("\"Hello\""));
	}

	/**
	 * Tests the escaping of special characters in SQL strings.
	 */
	@Test
	public void testEscapeSqlSpecialCharacters() {
		assertEquals("O''Reilly", MacrosUpdater.escapeSqlSpecialCharacters("O'Reilly"));
		assertEquals("Line\\nBreak", MacrosUpdater.escapeSqlSpecialCharacters("Line\nBreak"));
		assertEquals("Column\\tTab", MacrosUpdater.escapeSqlSpecialCharacters("Column\tTab"));
		assertEquals("\\\\\"Quote\\\\\"", MacrosUpdater.escapeSqlSpecialCharacters("\"Quote\""));
	}

	/**
	 * Tests the replacement of the USERNAME_ESC_XML macro with an XML-escaped username.
	 */
	@Test
	void testUpdateWithEscapedUsernameXml() {
		final String text = USERNAME_ESC_XML;
		final String username = "user&name<test>";

		final String result = MacrosUpdater.update(text, username, "pwd".toCharArray(), "token", "hostname");
		final String expected = "user&amp;name&lt;test&gt;";

		assertEquals(expected, result);
	}

	/**
	 * Tests the replacement of the PASSWORD_ESC_SQL macro with an SQL-escaped password.
	 */
	@Test
	void testUpdateWithEscapedPasswordSql() {
		final String text = PASSWORD_ESC_SQL;
		final String password = "p@ss'word";

		final String result = MacrosUpdater.update(text, "user", password.toCharArray(), "token", "hostname");
		final String expected = "p@ss''word";

		assertEquals(expected, result);
	}

	/**
	 * Tests the replacement of the USERNAME_ESC_URL macro with a URL-escaped username.
	 */
	@Test
	void testUpdateWithEscapedUsernameUrl() {
		final String text = USERNAME_ESC_URL;
		final String username = "user name@example.com";

		final String result = MacrosUpdater.update(text, username, "pwd".toCharArray(), "token", "hostname");
		final String expected = "user%20name%40example.com";

		assertEquals(expected, result);
	}

	/**
	 * Tests the replacement of the PASSWORD_ESC_URL macro with a URL-escaped password.
	 */
	@Test
	void testUpdateWithEscapedPasswordUrl() {
		final String text = PASSWORD_ESC_URL;
		final String password = "p@ss word/?";

		final String result = MacrosUpdater.update(text, "user", password.toCharArray(), "token", "hostname");
		final String expected = "p%40ss%20word%2F%3F";

		assertEquals(expected, result);
	}

	/**
	 * Tests the replacement of the USERNAME_ESC_REGEX macro with a regex-escaped username.
	 */
	@Test
	void testUpdateWithEscapedUsernameRegex() {
		final String text = USERNAME_ESC_REGEX;
		final String username = "user.name*";

		final String result = MacrosUpdater.update(text, username, "pwd".toCharArray(), "token", "hostname");
		final String expected = "\\Quser.name*\\E";

		assertEquals(expected, result);
	}

	/**
	 * Tests the replacement of the PASSWORD_ESC_REGEX macro with a regex-escaped password.
	 */
	@Test
	void testUpdateWithEscapedPasswordRegex() {
		final String text = PASSWORD_ESC_REGEX;
		final String password = "p@ss^word$";

		final String result = MacrosUpdater.update(text, "user", password.toCharArray(), "token", "hostname");
		final String expected = "\\Qp@ss^word$\\E";

		assertEquals(expected, result);
	}

	/**
	 * Tests the replacement of the USERNAME_ESC_WINDOWS_CMD macro with a Windows CMD-escaped username.
	 */
	@Test
	void testUpdateWithEscapedUsernameWindowsCmd() {
		final String text = USERNAME_ESC_WINDOWS_CMD;
		final String username = "user&name|cmd<>";

		final String result = MacrosUpdater.update(text, username, "pwd".toCharArray(), "token", "hostname");
		final String expected = "user^&name^|cmd^<^>";

		assertEquals(expected, result);
	}

	/**
	 * Tests the replacement of the PASSWORD_ESC_WINDOWS_CMD macro with a Windows CMD-escaped password.
	 */
	@Test
	void testUpdateWithEscapedPasswordWindowsCmd() {
		final String text = PASSWORD_ESC_WINDOWS_CMD;
		final String password = "p^wd&cmd|";

		final String result = MacrosUpdater.update(text, "user", password.toCharArray(), "token", "hostname");
		final String expected = "p^^wd^&cmd^|";

		assertEquals(expected, result);
	}

	/**
	 * Tests the replacement of the USERNAME_ESC_POWERSHELL macro with a PowerShell-escaped username.
	 */
	@Test
	void testUpdateWithEscapedUsernamePowershell() {
		final String text = USERNAME_ESC_POWERSHELL;
		final String username = "user\"name$var";

		final String result = MacrosUpdater.update(text, username, "pwd".toCharArray(), "token", "hostname");
		final String expected = "user`\"name`$var";

		assertEquals(expected, result);
	}

	/**
	 * Tests the replacement of the PASSWORD_ESC_POWERSHELL macro with a PowerShell-escaped password.
	 */
	@Test
	void testUpdateWithEscapedPasswordPowershell() {
		final String text = PASSWORD_ESC_POWERSHELL;
		final String password = "p@ss`word$";

		final String result = MacrosUpdater.update(text, "user", password.toCharArray(), "token", "hostname");
		final String expected = "p@ss`\\word`$";

		assertEquals(expected, result);
	}

	/**
	 * Tests the replacement of the USERNAME_ESC_BASH macro with a Bash-escaped username.
	 */
	@Test
	void testUpdateWithEscapedUsernameBash() {
		final String text = USERNAME_ESC_BASH;
		final String username = "user'name\"$var";

		final String result = MacrosUpdater.update(text, username, "pwd".toCharArray(), "token", "hostname");
		final String expected = "\\'user\\'name\\\"\\$var\\\"";

		assertEquals(expected, result);
	}

	/**
	 * Tests the replacement of the PASSWORD_ESC_BASH macro with a Bash-escaped password.
	 */
	@Test
	void testUpdateWithEscapedPasswordBash() {
		final String text = PASSWORD_ESC_BASH;
		final String password = "p@ss*word&";

		final String result = MacrosUpdater.update(text, "user", password.toCharArray(), "token", "hostname");
		final String expected = "p@ss\\*word\\&";

		assertEquals(expected, result);
	}
}
