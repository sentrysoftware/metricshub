package org.sentrysoftware.metricshub.engine.client.http;

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
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOSTNAME_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.PASSWORD_MACRO;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.USERNAME_MACRO;

import java.util.Base64;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpMacrosUpdater {

	private static final String PASSWORD_BASE64_MACRO = "%{PASSWORD_BASE64}";
	private static final String BASIC_AUTH_BASE64_MACRO = "%{BASIC_AUTH_BASE64}";
	private static final String SHA256_AUTH_MACRO = "%{SHA256_AUTH}";

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
			.replace("%{AUTHENTICATIONTOKEN}", authenticationToken);

		// Encode the password into a base64 string
		// then replace the macro with the resulting value
		if (updatedContent.indexOf(PASSWORD_BASE64_MACRO) != -1) {
			updatedContent =
				updatedContent.replace(PASSWORD_BASE64_MACRO, Base64.getEncoder().encodeToString(passwordAsString.getBytes()));
		}

		// Join the username and password with a colon `username:password`
		// and encode the resulting string in `base64`
		// then replace the macro with the resulting value
		if (updatedContent.indexOf(BASIC_AUTH_BASE64_MACRO) != -1) {
			updatedContent =
				updatedContent.replace(
					BASIC_AUTH_BASE64_MACRO,
					Base64.getEncoder().encodeToString(String.format("%s:%s", username, passwordAsString).getBytes())
				);
		}

		// Encode the authentication token into SHA256 string
		// then replace the macro with the resulting value
		if (updatedContent.indexOf(SHA256_AUTH_MACRO) != -1) {
			updatedContent = updatedContent.replace(SHA256_AUTH_MACRO, encodeSha256(authenticationToken));
		}

		return updatedContent;
	}
}
