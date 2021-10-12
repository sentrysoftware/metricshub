package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectorHttpPropertyTest {

	@Test
	void testGetConnectorProperties() {

		assertEquals(
			Stream.of(
				TypeProcessor.class,
				ForceSerializationProcessor.class,
				ExpectedResultProcessor.class,
				ErrorMessageProcessor.class,
				MethodProcessor.class,
				UrlProcessor.class,
				HeaderProcessor.class,
				BodyProcessor.class,
				ResultContentProcessor.class)
				.collect(Collectors.toSet()),
			ConnectorHttpProperty
				.getConnectorProperties()
				.stream()
				.map(IConnectorStateParser::getClass)
				.collect(Collectors.toSet()));
	}
}