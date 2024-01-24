package org.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;

class WmiConfigCliTest {

	@Test
	void testToProtocol() {
		WmiConfigCli wmiConfigCli = new WmiConfigCli();
		final char[] password = "value".toCharArray();
		final String username = "username";
		final long timeout = 120;
		final String namespace = "namespace";

		final WmiConfiguration wmiConfiguration = WmiConfiguration
			.builder()
			.username(username)
			.password(password)
			.timeout(120L)
			.namespace(namespace)
			.build();

		wmiConfigCli.setPassword(password);
		wmiConfigCli.setUsername(username);
		wmiConfigCli.setTimeout(timeout);
		wmiConfigCli.setNamespace(namespace);

		assertEquals(wmiConfiguration, wmiConfigCli.toProtocol(null, null));

		wmiConfigCli = new WmiConfigCli();
		wmiConfigCli.setPassword(password);
		wmiConfigCli.setUsername(username);
		wmiConfigCli.setTimeout(timeout);
		wmiConfigCli.setNamespace(namespace);

		assertEquals(wmiConfiguration, wmiConfigCli.toProtocol(username, password));
	}
}
