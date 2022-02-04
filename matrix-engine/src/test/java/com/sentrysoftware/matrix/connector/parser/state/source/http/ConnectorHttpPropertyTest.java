package com.sentrysoftware.matrix.connector.parser.state.source.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExecuteForEachEntryOfProcessor;
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
						ResultContentProcessor.class,
						ExecuteForEachEntryOfProcessor.class,
						EntryConcatMethodProcessor.class,
						EntryConcatStartProcessor.class,
						EntryConcatEndProcessor.class),
				ConnectorHttpProperty.getConnectorProperties()
					.stream()
					.map(IConnectorStateParser::getClass)
					.collect(Collectors.toSet()));
	}

}
