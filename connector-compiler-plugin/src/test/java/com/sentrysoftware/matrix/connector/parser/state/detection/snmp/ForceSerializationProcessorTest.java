package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import org.junit.jupiter.api.Test;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.ONE;
import static org.junit.jupiter.api.Assertions.*;

class ForceSerializationProcessorTest {

	private final ForceSerializationProcessor forceSerializationProcessor = new ForceSerializationProcessor();

	private final Connector connector = new Connector();

	private static final String CRITERION_FORCE_SERIALIZATION_KEY = "detection.criteria(1).forceserialization";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		// knownCriterion is null
		assertThrows(
				IllegalArgumentException.class,
				() -> forceSerializationProcessor.parse(CRITERION_FORCE_SERIALIZATION_KEY, FOO, connector)
		);

		// knownCriterion is not null
		forceSerializationProcessor.knownCriterion = new SNMPGetNext();

		forceSerializationProcessor.parse(CRITERION_FORCE_SERIALIZATION_KEY, FOO, connector);
		assertFalse(forceSerializationProcessor.knownCriterion.isForceSerialization());

		forceSerializationProcessor.parse(CRITERION_FORCE_SERIALIZATION_KEY, ONE, connector);
		assertTrue(forceSerializationProcessor.knownCriterion.isForceSerialization());
	}
}