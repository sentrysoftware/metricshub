package com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step;

import java.util.Set;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorDetectionSshInteractiveStepProperty {

	public static Stream<IConnectorStateParser> getConnectorProperties() {
		return Stream
				.of(
						ConnectorSleepProperty.getConnectorProperties(),
						ConnectorSendTextProperty.getConnectorProperties(),
						ConnectorGetAvailableProperty.getConnectorProperties(),
						ConnectorWaitForProperty.getConnectorProperties(),
						ConnectorWaitForPromptProperty.getConnectorProperties(),
						ConnectorGetUntilPromptProperty.getConnectorProperties(),
						ConnectorSendUsernameProperty.getConnectorProperties(),
						ConnectorSendPasswordProperty.getConnectorProperties())
				.flatMap(Set::stream);
	}
}
