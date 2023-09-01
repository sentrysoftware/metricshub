package com.sentrysoftware.matrix.matsya.http;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Base64;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.AUTHENTICATION_TOKEN_MACRO;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.BASIC_AUTH_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOSTNAME_MACRO;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.PASSWORD_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.PASSWORD_MACRO;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SHA256_AUTH_MACRO;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.USERNAME_MACRO;
import static com.sentrysoftware.matsya.jutils.JUtils.encodeSha256;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpMacrosUpdater {

	/**
	 * Replaces each known HTTP macro in the given text with the literal target sequences:<br>
	 * username, password, authentication-token, base64-password, base64-auth and sha256-auth
	 *
	 * @param text                The text we wish to update
	 * @param username            The HTTP username
	 * @param password            The HTTP password
	 * @param authenticationToken The HTTP Authentication Token
	 * @param hostname            The remote hostname
	 * @return String value
	 */
	public static String update(
		String text,
		String username,
		char[] password,
		String authenticationToken,
		@NonNull String hostname
	) {

		if (text == null || text.isEmpty()) {
			return EMPTY;
		}

		// Null values control
		final String passwordAsString = password != null ? String.valueOf(password) : EMPTY;
		username = username != null ? username : EMPTY;
		authenticationToken = authenticationToken != null ? authenticationToken : EMPTY;

		// Replace provided macros which don't need processing
		String updatedContent = text
			.replace(USERNAME_MACRO, username)
			.replace(HOSTNAME_MACRO, hostname)
			.replace(PASSWORD_MACRO, passwordAsString)
			.replace(AUTHENTICATION_TOKEN_MACRO, authenticationToken);

		// Encode the password into a base64 string
		// then replace the macro with the resulting value
		if (updatedContent.indexOf(PASSWORD_BASE64_MACRO) != -1) {
			updatedContent = updatedContent
				.replace(
					PASSWORD_BASE64_MACRO,
					Base64.getEncoder().encodeToString(passwordAsString.getBytes())
				);
		}

		// Join the username and password with a colon `username:password`
		// and encode the resulting string in `base64`
		// then replace the macro with the resulting value
		if (updatedContent.indexOf(BASIC_AUTH_BASE64_MACRO) != -1) {
			updatedContent = updatedContent
				.replace(
					BASIC_AUTH_BASE64_MACRO,
					Base64.getEncoder().encodeToString(
						String.format("%s:%s", username, passwordAsString).getBytes()
					)
				);
		}

		// Encode the authentication token into SHA256 string
		// then replace the macro with the resulting value
		if (updatedContent.indexOf(SHA256_AUTH_MACRO) != -1) {
			updatedContent = updatedContent
				.replace(
					SHA256_AUTH_MACRO,
					encodeSha256(authenticationToken)
				);
		}

		return updatedContent;
	}

}