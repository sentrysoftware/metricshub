package com.sentrysoftware.matrix.engine.strategy.matsya;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AUTHENTICATION_TOKEN_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BASIC_AUTH_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HOSTNAME_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_BASE64_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PASSWORD_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USERNAME_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SHA256_AUTH_MACRO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matsya.jutils.JUtils;

class HttpMacrosUpdaterTest {

	@Test
	void testUpdate() {
		final String hostname = "server-01";
		final String username = "username";
		final String password = "password";
		final String authToken = "920b4705-e564-4ea0-b81d-e522a0cc356e";

		// Hostname
		{

			final String baseUrl = "https://%s/";
			final String url = String.format(baseUrl, HOSTNAME_MACRO);
			final String expected = String.format(baseUrl, hostname);
			assertEquals(expected, HttpMacrosUpdater.update(url, null, null, null, hostname));
			assertEquals(baseUrl, HttpMacrosUpdater.update(baseUrl, null, null, null, hostname));
			assertEquals(EMPTY, HttpMacrosUpdater.update(null, null, null, null, hostname));
		}

		// Authentication token
		{

			final String baseUrl = "https://server-1/";
			final String url = baseUrl + AUTHENTICATION_TOKEN_MACRO;
			final String expected = baseUrl + authToken;

			assertEquals(expected, HttpMacrosUpdater.update(url, null, null, authToken, hostname));
			assertEquals(baseUrl, HttpMacrosUpdater.update(url, null, null, null, hostname));
			assertEquals(baseUrl, HttpMacrosUpdater.update(baseUrl, null, null, null, hostname));
		}

		// Username
		{
			final String baseBody = "{\"username\" : \"%s\"}";
			final String baseBodyEmptyUser = "{\"username\" : \"\"}";
			final String body = String.format(baseBody, USERNAME_MACRO);
			final String expected = String.format(baseBody, username);
			assertEquals(expected, HttpMacrosUpdater.update(body, username, null, null, hostname));
			assertEquals(baseBodyEmptyUser, HttpMacrosUpdater.update(body, null, null, null, hostname));
			assertEquals(baseBody, HttpMacrosUpdater.update(baseBody, null, null, null, hostname));
		}

		// Password
		{
			final String baseBody = "{\"password\" : \"%s\"}";
			final String baseBodyEmptyPass = "{\"password\" : \"\"}";
			final String body = String.format(baseBody, PASSWORD_MACRO);
			final String expected = String.format(baseBody, password);
			assertEquals(expected, HttpMacrosUpdater.update(body, null, password.toCharArray(), null, hostname));
			assertEquals(baseBodyEmptyPass, HttpMacrosUpdater.update(body, null, null, null, hostname));
			assertEquals(baseBody, HttpMacrosUpdater.update(baseBody, null, null, null, hostname));
		}

		// Password base64
		{
			final String passwordBase64 = Base64.getEncoder().encodeToString(password.getBytes());
			final String baseBody = "{\"password\" : \"%s\"}";
			final String baseBodyEmptyPass = "{\"password\" : \"\"}";
			final String body = String.format(baseBody, PASSWORD_BASE64_MACRO);
			final String expected = String.format(baseBody, passwordBase64);
			assertEquals(expected, HttpMacrosUpdater.update(body, null, password.toCharArray(), null, hostname));
			assertEquals(baseBodyEmptyPass, HttpMacrosUpdater.update(body, null, null, null, hostname));
			assertEquals(baseBody, HttpMacrosUpdater.update(baseBody, null, null, null, hostname));
		}

		// Basic authentication
		{
			final String baseHeader = "Authorization: %s";
			final String baseHeaderEmpty = "Authorization: Og==";
			final String header = String.format(baseHeader, BASIC_AUTH_BASE64_MACRO);
			final String expected = String.format(
				baseHeader,
				Base64
					.getEncoder()
					.encodeToString(
						String.format("%s:%s", username, password).getBytes()
					)
			);
			assertEquals(expected, HttpMacrosUpdater.update(header, username, password.toCharArray(), null, hostname));
			assertEquals(baseHeaderEmpty, HttpMacrosUpdater.update(header, null, null, null, hostname));
			assertEquals(baseHeader, HttpMacrosUpdater.update(baseHeader, null, null, null, hostname));
		}

		// SHA256 authentication
		{
			final String baseUrl = "https://server-1/";
			final String url = baseUrl + SHA256_AUTH_MACRO;
			final String expected = baseUrl + JUtils.encodeSha256(authToken);

			assertEquals(expected, HttpMacrosUpdater.update(url, null, null, authToken, hostname));
			assertEquals(baseUrl + JUtils.encodeSha256(EMPTY), HttpMacrosUpdater.update(url, null, null, null, hostname));
			assertEquals(baseUrl , HttpMacrosUpdater.update(baseUrl, null, null, null, hostname));
		}

	}

}
