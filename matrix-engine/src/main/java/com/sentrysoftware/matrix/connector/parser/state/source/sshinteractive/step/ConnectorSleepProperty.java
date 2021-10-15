package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Sleep;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSleepProperty {

	static final Class<Sleep> TYPE = Sleep.class;

	static final String TYPE_VALUE = "Sleep";

	public static Set<IConnectorStateParser> getConnectorProperties() {
		return Set.of(
				new TypeProcessor(TYPE, TYPE_VALUE),
				new CaptureProcessor(TYPE, TYPE_VALUE),
				new TelnetOnlyProcessor(TYPE, TYPE_VALUE),
				new DurationProcessor());
	}
}
