package com.sentrysoftware.matrix.common.meta.parameter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class PredictedFailureTest {

	@Test
	void testInterpretNull() {
		assertEquals(Optional.empty(), PredictedFailure.interpret(null));
		assertEquals(Optional.empty(), PredictedFailure.interpret(""));
		assertEquals(Optional.empty(), PredictedFailure.interpret("unknown"));
	}

	@Test
	void testInterpretOK() {
		assertEquals(Optional.of(PredictedFailure.OK), PredictedFailure.interpret("0"));
		assertEquals(Optional.of(PredictedFailure.OK), PredictedFailure.interpret("0.0"));
		assertEquals(Optional.of(PredictedFailure.OK), PredictedFailure.interpret("ok"));
		assertEquals(Optional.of(PredictedFailure.OK), PredictedFailure.interpret(" ok "));
		assertEquals(Optional.of(PredictedFailure.OK), PredictedFailure.interpret(" Ok "));
		assertEquals(Optional.of(PredictedFailure.OK), PredictedFailure.interpret(" OK "));
		assertEquals(Optional.of(PredictedFailure.OK), PredictedFailure.interpret("OK"));
	}

	@Test
	void testInterpretPredictedFailureFromWarn() {
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret("1"));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret("1.0"));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret("warn"));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret(" warn "));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret(" WARN "));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret("WARN"));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret("warning"));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret(" warning "));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret(" WARNING "));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret("WARNING"));
	}

	@Test
	void testInterpretPredictedFailureFromAlarm() {
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret("2.0"));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret("2"));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret("2 "));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret("alarm"));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret(" alarm "));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret(" Alarm "));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret(" ALARM "));
		assertEquals(Optional.of(PredictedFailure.FAILURE_PREDICTED), PredictedFailure.interpret("ALARM"));
	}


}
