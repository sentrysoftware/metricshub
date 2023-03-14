package com.sentrysoftware.matrix.converter.state.detection.sshinteractive.step;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.sshstep.SendPassword;
import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSendPasswordProperty {

	private static final Class<SendPassword> TYPE = SendPassword.class;

	private static final String TYPE_VALUE = "SendPassword";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
				new TypeProcessor(TYPE),
				new CaptureProcessor(TYPE),
				new TelnetOnlyProcessor(TYPE));
	}
}
