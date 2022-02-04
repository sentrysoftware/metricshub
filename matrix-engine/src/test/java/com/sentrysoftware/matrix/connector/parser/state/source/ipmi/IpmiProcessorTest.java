package com.sentrysoftware.matrix.connector.parser.state.source.ipmi;

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

class IpmiProcessorTest {

	@Test
	void testGetConnectorProperties() {
		assertEquals(
				Set.of(IpmiTypeProcessor.class,
						ForceSerializationProcessor.class,
						ExecuteForEachEntryOfProcessor.class,
						EntryConcatMethodProcessor.class,
						EntryConcatStartProcessor.class,
						EntryConcatEndProcessor.class),
				ConnectorIpmiProperty
					.getConnectorProperties()
					.stream()
					.map(IConnectorStateParser::getClass)
					.collect(Collectors.toSet()));
	}
}
