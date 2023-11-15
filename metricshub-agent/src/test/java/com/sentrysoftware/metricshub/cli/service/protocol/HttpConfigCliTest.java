package com.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.metricshub.cli.service.protocol.HttpConfigCli.HttpOrHttps;
import com.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import org.junit.jupiter.api.Test;

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
}
