package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

class ConnectorOsCommandPropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(
				Set.of(
						TypeProcessor.class,
						ForceSerializationProcessor.class,
						CommandLineProcessor.class,
						ExecuteLocallyProcessor.class,
						ExcludeRegExpProcessor.class,
						KeepOnlyRegExpProcessor.class,
						RemoveFooterProcessor.class,
						RemoveHeaderProcessor.class,
						SelectColumnsProcessor.class,
						SeparatorsProcessor.class,
						TimeoutProcessor.class),
				ConnectorOsCommandProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));
	}
}
