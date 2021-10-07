package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

class ConnectorSourceSshInteractiveStepPropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(
				Set.of(
						CaptureProcessor.class,
						TextProcessor.class,
						TimeoutProcessor.class,
						TypeProcessor.class,
						TelnetOnlyProcessor.class,
						DurationProcessor.class),
				ConnectorSourceSshInteractiveStepProperty.getConnectorProperties()
					.map(IConnectorStateParser::getClass)
					.collect(Collectors.toSet()));
	}
}
