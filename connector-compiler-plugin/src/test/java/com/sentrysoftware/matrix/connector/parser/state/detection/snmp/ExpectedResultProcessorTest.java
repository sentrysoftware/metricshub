package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpectedResultProcessorTest {

	private final ExpectedResultProcessor expectedResultProcessor = new ExpectedResultProcessor();

	private final Connector connector = new Connector();

	private static final String CRITERION_EXPECTED_RESULT_KEY = "detection.criteria(1).expectedresult";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		// knownCriterion is null
		assertThrows(
				IllegalArgumentException.class,
				() -> expectedResultProcessor.parse(CRITERION_EXPECTED_RESULT_KEY, FOO, connector)
		);

		// knownCriterion is not null
		expectedResultProcessor.knownCriterion = new SNMPGetNext();

		expectedResultProcessor.parse(CRITERION_EXPECTED_RESULT_KEY, FOO, connector);
		assertEquals(FOO, ((SNMPGetNext) expectedResultProcessor.knownCriterion).getExpectedResult());
	}
}