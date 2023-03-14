package com.sentrysoftware.matrix.converter.state.detection.sshinteractive;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;
import com.sentrysoftware.matrix.converter.state.detection.sshinteractive.step.ConnectorDetectionSshInteractiveStepProperty;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSshInteractiveProperty {

	static final String SSH_INTERACTIVE_TYPE_VALUE = "SshInteractive";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		final Set<IConnectorStateConverter> sshInteractive = Set.of(
				new TypeProcessor(SSH_INTERACTIVE_TYPE_VALUE),
				new ForceSerializationProcessor(SSH_INTERACTIVE_TYPE_VALUE),
				new ExpectedResultProcessor(SSH_INTERACTIVE_TYPE_VALUE),
				new PortProcessor());
		
		return Stream.concat(sshInteractive.stream(), ConnectorDetectionSshInteractiveStepProperty.getConnectorProperties())
				.collect(Collectors.toSet());
	}
}
