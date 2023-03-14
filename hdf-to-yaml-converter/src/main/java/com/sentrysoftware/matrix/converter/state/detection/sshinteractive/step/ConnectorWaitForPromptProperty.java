package com.sentrysoftware.matrix.converter.state.detection.sshinteractive.step;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.sshstep.WaitForPrompt;
import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorWaitForPromptProperty {

	private static final Class<WaitForPrompt> TYPE = WaitForPrompt.class;

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
				new TypeProcessor(TYPE),
				new CaptureProcessor(TYPE),
				new TimeoutProcessor(TYPE),
				new TelnetOnlyProcessor(TYPE));
	}
}
