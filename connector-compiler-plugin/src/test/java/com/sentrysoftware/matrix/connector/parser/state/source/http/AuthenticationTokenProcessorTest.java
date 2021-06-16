package com.sentrysoftware.matrix.connector.parser.state.source.http;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;

class AuthenticationTokenProcessorTest {

	private static final String AUTHENTICATION_TOKEN_KEY = "enclosure.discovery.source(3).authenticationtoken";
	private static final String VALUE = "%enclosure.discovery.source(1)%";
	private static final String RESULT = "enclosure.discovery.source(1)";

	@Test
	void testParse() {

		HTTPSource httpSource = HTTPSource.builder().index(3).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(httpSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();
		Connector connector = new Connector();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		new AuthenticationTokenProcessor().parse(AUTHENTICATION_TOKEN_KEY, VALUE, connector);
		assertEquals(RESULT, httpSource.getAuthenticationToken());
	}
}
