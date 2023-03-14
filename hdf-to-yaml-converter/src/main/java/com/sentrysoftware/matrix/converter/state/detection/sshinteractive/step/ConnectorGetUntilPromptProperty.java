package com.sentrysoftware.matrix.converter.state.detection.sshinteractive.step;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.sshstep.GetUntilPrompt;
import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorGetUntilPromptProperty {

	private static final Class<GetUntilPrompt> TYPE = GetUntilPrompt.class;

	private static final String TYPE_VALUE = "GetUntilPrompt";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
				new TypeProcessor(TYPE),
				new CaptureProcessor(TYPE),
				new TimeoutProcessor(TYPE),
				new TelnetOnlyProcessor(TYPE));
	}
}
