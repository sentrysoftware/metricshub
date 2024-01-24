package org.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.cli.service.protocol.HttpConfigCli.HttpOrHttps;
import org.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;

class HttpConfigCliTest {

	@Test
	void testToProtocol() {
		HttpConfigCli httpConfigCli = new HttpConfigCli();
		final char[] password = "p4ssw0rd".toCharArray();
		final String username = "username";
		final int timeout = 120;
		final HttpOrHttps httpOrHttps = new HttpOrHttps();
		httpConfigCli.setPassword(password);
		httpConfigCli.setUsername(username);
		httpConfigCli.setTimeout(timeout);
		httpConfigCli.setHttpOrHttps(httpOrHttps);
		assertEquals(
			HttpConfiguration.builder().username(username).password(password).timeout(120L).port(80).https(false).build(),
			httpConfigCli.toProtocol(null, null)
		);
		httpConfigCli = new HttpConfigCli();
		httpConfigCli.setTimeout(timeout);
		httpConfigCli.setHttpOrHttps(httpOrHttps);
		assertEquals(
			HttpConfiguration.builder().username(username).password(password).timeout(120L).port(80).https(false).build(),
			httpConfigCli.toProtocol(username, password)
		);
	}

	@Test
	void testGetOrDeducePortNumber() {
		final HttpConfigCli httpConfigCli = new HttpConfigCli();
		final int expectedPortNumber = 4443;
		httpConfigCli.setPort(expectedPortNumber);
		assertEquals(expectedPortNumber, httpConfigCli.getOrDeducePortNumber());
		httpConfigCli.setPort(null);
		httpConfigCli.httpOrHttps = new HttpOrHttps();
		httpConfigCli.httpOrHttps.http = false;
		httpConfigCli.httpOrHttps.https = true;
		assertEquals(443, httpConfigCli.getOrDeducePortNumber());

		httpConfigCli.httpOrHttps.http = true;
		httpConfigCli.httpOrHttps.https = false;
		assertEquals(80, httpConfigCli.getOrDeducePortNumber());
	}
}
