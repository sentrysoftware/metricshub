package com.sentrysoftware.matrix.connector.parser.state.detection.wbem;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.WbemNameSpaceProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.WbemQueryProcessor;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectorWbemPropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(
			Stream.of(
				TypeProcessor.class,
				ForceSerializationProcessor.class,
				ExpectedResultProcessor.class,
				WbemNameSpaceProcessor.class,
				WbemQueryProcessor.class,
				ErrorMessageProcessor.class)
			.collect(Collectors.toSet()),
			ConnectorWbemProperty
				.getConnectorProperties()
				.stream()
				.map(IConnectorStateParser::getClass)
				.collect(Collectors.toSet()));
	}
}