package com.sentrysoftware.matrix.converter.state.detection.sshinteractive.step;

import java.util.Set;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorDetectionSshInteractiveStepProperty {

	public static Stream<IConnectorStateConverter> getConnectorProperties() {
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
