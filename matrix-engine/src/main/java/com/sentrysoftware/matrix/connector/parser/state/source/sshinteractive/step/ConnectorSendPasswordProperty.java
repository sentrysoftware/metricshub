package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendPassword;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSendPasswordProperty {

	private static final Class<SendPassword> TYPE = SendPassword.class;

	private static final String TYPE_VALUE = "SendPassword";

	public static Set<IConnectorStateParser> getConnectorProperties() {
		return Set.of(
				new TypeProcessor(TYPE, TYPE_VALUE),
				new CaptureProcessor(TYPE, TYPE_VALUE),
				new TelnetOnlyProcessor(TYPE, TYPE_VALUE));
	}
}
