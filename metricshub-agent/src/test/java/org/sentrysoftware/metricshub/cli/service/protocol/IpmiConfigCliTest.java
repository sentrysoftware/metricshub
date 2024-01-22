package org.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.configuration.IpmiConfiguration;

class IpmiConfigCliTest {

	@Test
	void testToProtocol() {
		IpmiConfigCli ipmiConfigCli = new IpmiConfigCli();
		final char[] password = "value".toCharArray();
		final String username = "username";
		final int timeout = 120;
		final IpmiConfiguration ipmiConfigurationExpected = IpmiConfiguration
			.builder()
			.username(username)
			.password(password)
			.timeout(120L)
			.bmcKey(new byte[] {})
			.build();

		ipmiConfigCli.setPassword(password);
		ipmiConfigCli.setUsername(username);
		ipmiConfigCli.setTimeout(timeout);

		assertEquals(ipmiConfigurationExpected, ipmiConfigCli.toProtocol(null, null));

		ipmiConfigCli = new IpmiConfigCli();
		ipmiConfigCli.setTimeout(timeout);

		assertEquals(ipmiConfigurationExpected, ipmiConfigCli.toProtocol(username, password));
	}
}
