package com.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.metricshub.engine.configuration.TransportProtocols;
import com.sentrysoftware.metricshub.engine.configuration.WinRmConfiguration;
import org.junit.jupiter.api.Test;

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
}
