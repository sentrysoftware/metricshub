package com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.CaptureProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.DurationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.TelnetOnlyProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.TextProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.TimeoutProcessor;

class ConnectorSshInteractivePropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(
				Set.of(
						TypeProcessor.class,
						ForceSerializationProcessor.class,
						ExpectedResultProcessor.class,
						PortProcessor.class,
						com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.TypeProcessor.class,
						CaptureProcessor.class,
						TextProcessor.class,
						TimeoutProcessor.class,
						DurationProcessor.class,
						TelnetOnlyProcessor.class),
				ConnectorSshInteractiveProperty.getConnectorProperties().stream()
					.map(IConnectorStateParser::getClass)
					.collect(Collectors.toSet()));
	}
}
