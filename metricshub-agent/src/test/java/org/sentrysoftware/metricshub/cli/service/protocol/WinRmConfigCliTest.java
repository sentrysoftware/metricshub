package org.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.configuration.TransportProtocols;
import org.sentrysoftware.metricshub.engine.configuration.WinRmConfiguration;

class WinRmConfigCliTest {

	@Test
	void testToProtocol() {
		WinRmConfigCli winRmConfigCli = new WinRmConfigCli();
		final char[] password = "p4ssw0rd".toCharArray();
		final String username = "username";
		final int timeout = 120;
		final TransportProtocols transportProtocolHttp = TransportProtocols.HTTP;
		winRmConfigCli.setPassword(password);
		winRmConfigCli.setUsername(username);
		winRmConfigCli.setTimeout(timeout);
		winRmConfigCli.setProtocol(transportProtocolHttp);
		assertEquals(
			WinRmConfiguration
				.builder()
				.username(username)
				.password(password)
				.timeout(120L)
				.port(5985)
				.protocol(transportProtocolHttp)
				.build(),
			winRmConfigCli.toProtocol(null, null)
		);
		winRmConfigCli = new WinRmConfigCli();
		winRmConfigCli.setTimeout(timeout);
		winRmConfigCli.setProtocol(transportProtocolHttp);
		assertEquals(
			WinRmConfiguration
				.builder()
				.username(username)
				.password(password)
				.timeout(120L)
				.port(5985)
				.protocol(transportProtocolHttp)
				.build(),
			winRmConfigCli.toProtocol(username, password)
		);
	}

	@Test
	void testGetOrDeducePortNumber() {
		final WinRmConfigCli winRmConfigCli = new WinRmConfigCli();
		final int expectedPortNumber = 4443;
		winRmConfigCli.setPort(expectedPortNumber);
		assertEquals(expectedPortNumber, winRmConfigCli.getOrDeducePortNumber());
		winRmConfigCli.setPort(null);
		winRmConfigCli.setProtocol(TransportProtocols.HTTPS);
		assertEquals(5986, winRmConfigCli.getOrDeducePortNumber());
		winRmConfigCli.setProtocol(TransportProtocols.HTTP);
		assertEquals(5985, winRmConfigCli.getOrDeducePortNumber());
	}
}
