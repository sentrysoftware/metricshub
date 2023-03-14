package com.sentrysoftware.matrix.converter.state.detection.sshinteractive.step;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.sshstep.WaitFor;
import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorWaitForProperty {

	private static final Class<WaitFor> TYPE = WaitFor.class;

	private static final String TYPE_VALUE = "WaitFor";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
				new TypeProcessor(TYPE),
				new CaptureProcessor(TYPE),
				new TextProcessor(TYPE),
				new TimeoutProcessor(TYPE),
				new TelnetOnlyProcessor(TYPE));
	}
}
