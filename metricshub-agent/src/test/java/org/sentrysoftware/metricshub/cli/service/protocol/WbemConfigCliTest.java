package org.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.configuration.TransportProtocols;
import org.sentrysoftware.metricshub.engine.configuration.WbemConfiguration;

class WbemConfigCliTest {

	@Test
	void testToProtocol() {
		final WbemConfigCli wbemConfigCli = new WbemConfigCli();
		final char[] password = "value".toCharArray();
		final String username = "username";
		final String namespace = "root/sentrysoftware";
		final int timeout = 120;
		wbemConfigCli.setPassword(password);
		wbemConfigCli.setUsername(username);
		wbemConfigCli.setTimeout(timeout);
		wbemConfigCli.setProtocol(TransportProtocols.HTTPS);
		wbemConfigCli.setNamespace(namespace);
		final WbemConfiguration wbemConfiguration = WbemConfiguration
			.builder()
			.username(username)
			.password(password)
			.timeout(120L)
			.port(5989)
			.namespace(namespace)
			.protocol(TransportProtocols.HTTPS)
			.build();
		assertEquals(wbemConfiguration, wbemConfigCli.toProtocol(null, null));

		assertEquals(wbemConfiguration, wbemConfigCli.toProtocol(username, password));
	}

	@Test
	void testGetOrDeducePortNumber() {
		final WbemConfigCli wbemConfigCli = new WbemConfigCli();
		final int expectedPortNumber = 4443;
		wbemConfigCli.setPort(expectedPortNumber);
		assertEquals(expectedPortNumber, wbemConfigCli.getOrDeducePortNumber());
		wbemConfigCli.setPort(null);
		wbemConfigCli.setProtocol(TransportProtocols.HTTPS);
		assertEquals(5989, wbemConfigCli.getOrDeducePortNumber());
		wbemConfigCli.setProtocol(TransportProtocols.HTTP);
		assertEquals(5988, wbemConfigCli.getOrDeducePortNumber());
	}
}
