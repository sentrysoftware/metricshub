package com.sentrysoftware.matrix.connector.parser.state.source.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

class ConnectorHttpPropertyTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(
				Set.of(
						TypeProcessor.class,
						ForceSerializationProcessor.class,
						MethodProcessor.class,
						UrlProcessor.class,
						HeaderProcessor.class,
						BodyProcessor.class,
						AuthenticationTokenProcessor.class,
						ExecuteForEachEntryOfProcessor.class,
						ResultContentProcessor.class,
						EntryConcatMethodProcessor.class,
						EntryConcatStartProcessor.class,
						EntryConcatEndProcessor.class),
				ConnectorHttpProperty.getConnectorProperties().stream().map(IConnectorStateParser::getClass).collect(Collectors.toSet()));
	}

}
