package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExcludeRegExpProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.KeepOnlyRegExpProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.RemoveFooterProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.RemoveHeaderProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.SelectColumnsProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.SeparatorsProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step.CaptureProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step.DurationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step.TelnetOnlyProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step.TextProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step.TimeoutProcessor;

class ConnectorSshInteractivePropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(
				Set.of(
						TypeProcessor.class,
						ForceSerializationProcessor.class,
						ExcludeRegExpProcessor.class,
						KeepOnlyRegExpProcessor.class,
						RemoveFooterProcessor.class,
						RemoveHeaderProcessor.class,
						SelectColumnsProcessor.class,
						SeparatorsProcessor.class,
						com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step.TypeProcessor.class,
						CaptureProcessor.class,
						TextProcessor.class,
						TimeoutProcessor.class,
						TelnetOnlyProcessor.class,
						DurationProcessor.class),
				ConnectorSshInteractiveProperty.getConnectorProperties().stream()
					.map(IConnectorStateParser::getClass)
					.collect(Collectors.toSet()));
	}
}
