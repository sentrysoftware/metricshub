package org.sentrysoftware.metricshub.engine.common.helpers;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import static org.sentrysoftware.metricshub.engine.common.helpers.JUtils.encodeSha256;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.EMPTY;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Utility class for updating HTTP macros in a text string.
 * Replaces known HTTP macros with literal target sequences such as username,
 * password, authentication-token, base64-password, base64-auth, and sha256-auth.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MacrosUpdater {

	/**
	 * Replaces each known HTTP macro in the given text with the corresponding values.
	 *
	 * Supported macros: %{USERNAME}, %{PASSWORD}, %{HOSTNAME}, %{AUTHENTICATIONTOKEN},
	 * %{PASSWORD_BASE64}, %{BASIC_AUTH}, %{SHA256}, along with various escape formats like JSON, XML, URL, etc.
	 *
	 * @param text                The text with macros to update.
	 * @param username            The HTTP username.
	 * @param password            The HTTP password.
	 * @param authenticationToken The HTTP authentication token.
	 * @param hostname            The remote hostname.
	 * @return The updated string with replaced macros.
	 */
	public static String update(
		String text,
		String username,
		char[] password,
		String authenticationToken,
		@NonNull final String hostname
	) {
		if (text == null || text.isEmpty()) {
			return EMPTY;
		}

		// Null values control
		final String passwordAsString = password != null ? String.valueOf(password) : EMPTY;
		username = username != null ? username : EMPTY;
		authenticationToken = authenticationToken != null ? authenticationToken : EMPTY;

		String updatedContent = text;
		if (text.contains("%{")) {
			final Map<String, String> simpleMacroNameToField = Map.of(
				"USERNAME",
				username,
				"PASSWORD",
				passwordAsString,
				"HOSTNAME",
				hostname,
				"AUTHENTICATIONTOKEN",
				authenticationToken
			);

			final Pattern pattern = Pattern.compile("%\\{(\\w+?)(?:_ESC_(\\w+))?\\}");
			final Matcher matcher = pattern.matcher(text);

			while (matcher.find()) {
				final String macroName = matcher.group(1);
				final String escapeType = matcher.group(2);
				updatedContent =
					processMacro(
						updatedContent,
						matcher.group(0),
						macroName,
						escapeType,
						passwordAsString,
						username,
						authenticationToken,
						simpleMacroNameToField
					);
			}
		}

		return updatedContent;
	}

	/**
	 * Processes the macro found in the text, replacing it with the corresponding value based on the macro name.
	 *
	 * @param content             The content string with macros.
	 * @param matchedString       The matched macro string.
	 * @param macroName           The name of the macro to replace.
	 * @param escapeType          The escape type for the macro value (e.g., JSON, XML).
	 * @param passwordAsString    The HTTP password as a string.
	 * @param username            The HTTP username.
	 * @param authenticationToken  The HTTP authentication token.
	 * @param macroNameField      A map of macro names and their corresponding values.
	 * @return The content with the macro replaced by the corresponding value.
	 */
	private static String processMacro(
		final String content,
		final String matchedString,
		final String macroName,
		final String escapeType,
		final String passwordAsString,
		final String username,
		final String authenticationToken,
		final Map<String, String> macroNameField
	) {
		String updatedContent = content;
		if (macroName.startsWith("PASSWORD_BASE64")) {
			// PasswordBase64 macros replacement
			updatedContent = replacePasswordBase64(updatedContent, escapeType, matchedString, passwordAsString);
		} else if (macroName.startsWith("BASIC_AUTH_BASE64")) {
			// BasicAuthBase64 macros replacement
			updatedContent =
				replaceBasicAuthBase64MacroValue(updatedContent, escapeType, matchedString, username, passwordAsString);
		} else if (macroName.startsWith("SHA256_AUTH")) {
			// Sha256 macros replacement
			updatedContent = replaceSha256MacroValue(updatedContent, escapeType, matchedString, authenticationToken);
		} else {
			// Simple macro replacement: username, password, hostname and authenticationToken macros
			updatedContent = updateSimpleMacro(updatedContent, matchedString, macroName, escapeType, macroNameField);
		}
		return updatedContent;
	}

	/**
	 * Updates simple macros (e.g., USERNAME, PASSWORD) in the content based on the provided escape type.
	 * Replaces the macro with the corresponding value from the macroNameField map.
	 *
	 * @param content        The content string with macros.
	 * @param matchedString  The matched macro string.
	 * @param macroName      The name of the macro to be replaced.
	 * @param escapeType     The escape type (e.g., JSON, XML) for the macro value.
	 * @param macroNameField A map of macro names and their corresponding values.
	 * @return The content with the macro replaced by the corresponding value.
	 */
	private static String updateSimpleMacro(
		final String content,
		final String matchedString,
		final String macroName,
		final String escapeType,
		final Map<String, String> macroNameField
	) {
		final String replacement = macroNameField.getOrDefault(macroName, EMPTY);
		final String escapedReplacement = escapeType != null ? escapeReplacement(replacement, escapeType) : replacement;
		return content.replace(matchedString, escapedReplacement);
	}

	/**
	 * Escapes special characters in a string based on the provided escape type.
	 *
	 * @param replacement The string to escape.
	 * @param escapeType The type of escape to apply (e.g., JSON, XML, URL).
	 * @return The escaped string.
	 */
	private static String escapeReplacement(final String replacement, final String escapeType) {
		return switch (escapeType) {
			case "JSON" -> escapeJsonSpecialCharacters(replacement);
			case "XML" -> escapeXmlSpecialCharacters(replacement);
			case "URL" -> escapeUrlSpecialCharacters(replacement);
			case "REGEX" -> escapeRegexSpecialCharacters(replacement);
			case "WINDOWS", "CMD" -> escapeWindowsCmdSpecialCharacters(replacement);
			case "POWERSHELL" -> escapePowershellSpecialCharacters(replacement);
			case "LINUX", "BASH" -> escapeBashSpecialCharacters(replacement);
			case "SQL" -> escapeSqlSpecialCharacters(replacement);
			default -> replacement;
		};
	}

	/**
	 * Replaces the %{BASIC_AUTH} macro with the corresponding base64-encoded username and password.
	 *
	 * @param valueToUpdate    The string to update.
	 * @param escapeType       The escape type to apply (e.g., JSON, XML).
	 * @param matchedString     The matched macro string.
	 * @param username         The username for basic authentication.
	 * @param passwordAsString The password for basic authentication.
	 * @return The updated string with the %{BASIC_AUTH} macro replaced.
	 */
	private static String replaceBasicAuthBase64MacroValue(
		final String valueToUpdate,
		final String escapeType,
		final String matchedString,
		final String username,
		final String passwordAsString
	) {
		// Join the username and password with a colon `username:password`
		// and encode the resulting string in `base64`
		// then replace the macro with the resulting value
		final String formattedBasicAuthString = String.format(
			"%s:%s",
			escapeReplacement(username, escapeType),
			escapeReplacement(passwordAsString, escapeType)
		);
		final String escapedValue = Base64
			.getEncoder()
			.encodeToString((formattedBasicAuthString).getBytes(StandardCharsets.UTF_8));
		return valueToUpdate.replace(matchedString, escapedValue);
	}

	/**
	 * Replaces the %{SHA256} macro with the corresponding SHA-256 encoded authentication token.
	 *
	 * @param valueToUpdate          The string to update.
	 * @param escapeType             The escape type to apply (e.g., JSON, XML).
	 * @param matchedString           The matched macro string.
	 * @param authenticationToken     The authentication token to encode.
	 * @return The updated string with the %{SHA256} macro replaced.
	 */
	private static String replaceSha256MacroValue(
		final String valueToUpdate,
		final String escapeType,
		final String matchedString,
		final String authenticationToken
	) {
		// Encode the authentication token into SHA256 string
		// then replace the macro with the resulting value
		if (authenticationToken == null || authenticationToken.isEmpty()) {
			return valueToUpdate.replace(matchedString, EMPTY);
		}
		final String hashedToken = encodeSha256(authenticationToken);
		return valueToUpdate.replace(matchedString, escapeReplacement(hashedToken, escapeType));
	}

	/**
	 * Replaces the %{PASSWORD_BASE64} macro with the base64-encoded password.
	 *
	 * @param valueToUpdate    The string to update.
	 * @param escapeType       The escape type to apply (e.g., JSON, XML).
	 * @param matchedString     The matched macro string.
	 * @param passwordAsString The password to encode.
	 * @return The updated string with the %{PASSWORD_BASE64} macro replaced.
	 */
	private static String replacePasswordBase64(
		final String valueToUpdate,
		final String escapeType,
		final String matchedString,
		final String passwordAsString
	) {
		// Encode the password into a base64 string
		// then replace the macro with the resulting value
		final String escapedValue = Base64
			.getEncoder()
			.encodeToString(escapeReplacement(passwordAsString, escapeType).getBytes(StandardCharsets.UTF_8));
		return valueToUpdate.replace(matchedString, escapedValue);
	}

	/**
	 * Escape special characters in a JSON string value.
	 *
	 * @param value The value to escape.
	 * @return The escaped JSON string.
	 */
	static String escapeJsonSpecialCharacters(final String value) {
		return value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t");
	}

	/**
	 * Escapes special URL characters by replacing them with their percent-encoded equivalents.
	 *
	 * @param value The string to escape.
	 * @return The URL-encoded string.
	 */
	static String escapeUrlSpecialCharacters(final String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	/**
	 * Escapes special XML characters by replacing them with their respective escape codes.
	 *
	 * @param value The string to escape.
	 * @return The XML-escaped string.
	 */
	static String escapeXmlSpecialCharacters(final String value) {
		return value
			.replace("&", "&amp;")
			.replace("\"", "&quot;")
			.replace("'", "&apos;")
			.replace("<", "&lt;")
			.replace(">", "&gt;");
	}

	/**
	 * Escape regex special characters in a string.
	 *
	 * @param value The string to escape.
	 * @return The escaped string.
	 */
	static String escapeRegexSpecialCharacters(final String value) {
		return value
			.replace("\\", "\\\\")
			.replace("*", "\\*")
			.replace("+", "\\+")
			.replace("?", "\\?")
			.replace("|", "\\|")
			.replace("{", "\\{")
			.replace("}", "\\}")
			.replace("[", "\\[")
			.replace("]", "\\]")
			.replace("(", "\\(")
			.replace(")", "\\)")
			.replace("^", "\\^")
			.replace("$", "\\$")
			.replace(".", "\\.");
	}

	/**
	 * Escape special characters in a Windows command string.
	 *
	 * @param value The string to escape.
	 * @return The escaped string.
	 */
	static String escapeWindowsCmdSpecialCharacters(final String value) {
		return value.replace("\"", "\\\"");
	}

	/**
	 * Escape special characters in a Powershell string.
	 *
	 * @param value The string to escape.
	 * @return The escaped string.
	 */
	static String escapePowershellSpecialCharacters(final String value) {
		return value.replace("'", "''").replace("\"", "`\"");
	}

	/**
	 * Escape special characters in a Bash command string.
	 *
	 * @param value The string to escape.
	 * @return The escaped string.
	 */
	static String escapeBashSpecialCharacters(final String value) {
		return value.replace("$", "\\$").replace("`", "\\`").replace("\"", "\\\"");
	}

	/**
	 * Escape special characters in a SQL query string.
	 *
	 * @param value The string to escape.
	 * @return The escaped string.
	 */
	static String escapeSqlSpecialCharacters(final String value) {
		return value.replace("'", "''");
	}
}
