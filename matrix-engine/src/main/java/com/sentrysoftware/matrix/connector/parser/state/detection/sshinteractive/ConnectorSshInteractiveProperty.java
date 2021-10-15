package com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive.SshInteractive;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step.ConnectorDetectionSshInteractiveStepProperty;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSshInteractiveProperty {

	static final Class<SshInteractive> TYPE = SshInteractive.class;

	static final String TYPE_VALUE = "TelnetInteractive";

	public static Set<IConnectorStateParser> getConnectorProperties() {
		final Set<IConnectorStateParser> sshInteractive = Set.of(
				new TypeProcessor(TYPE, TYPE_VALUE),
				new ForceSerializationProcessor(TYPE, TYPE_VALUE),
				new ExpectedResultProcessor(TYPE, TYPE_VALUE),
				new PortProcessor());
		
		return Stream.concat(sshInteractive.stream(), ConnectorDetectionSshInteractiveStepProperty.getConnectorProperties())
				.collect(Collectors.toSet());
	}
}
