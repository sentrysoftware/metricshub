package com.sentrysoftware.matrix.converter.state.detection.sshinteractive.step;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.sshstep.Sleep;
import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSleepProperty {

	static final Class<Sleep> TYPE = Sleep.class;

	static final String TYPE_VALUE = "Sleep";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
				new TypeProcessor(TYPE),
				new CaptureProcessor(TYPE),
				new DurationProcessor(),
				new TelnetOnlyProcessor(TYPE));
	}
}
