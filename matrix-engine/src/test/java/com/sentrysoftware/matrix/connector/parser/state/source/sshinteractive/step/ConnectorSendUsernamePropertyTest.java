package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

class ConnectorSendUsernamePropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(
				Set.of(
						TypeProcessor.class,
						CaptureProcessor.class,
						TelnetOnlyProcessor.class),
				ConnectorSendUsernameProperty.getConnectorProperties().stream()
					.map(IConnectorStateParser::getClass)
					.collect(Collectors.toSet()));
	}
}
