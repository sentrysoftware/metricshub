package com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.CaptureProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.ConnectorWaitForProperty;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.TextProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.TimeoutProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.TypeProcessor;

class ConnectorWaitForPropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(
				Set.of(
						TypeProcessor.class,
						CaptureProcessor.class,
						TextProcessor.class,
						TimeoutProcessor.class,
						TelnetOnlyProcessor.class),
				ConnectorWaitForProperty.getConnectorProperties().stream()
					.map(IConnectorStateParser::getClass)
					.collect(Collectors.toSet()));
	}
}
