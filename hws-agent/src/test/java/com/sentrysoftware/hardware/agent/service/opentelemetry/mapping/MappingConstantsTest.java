package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static org.junit.jupiter.api.Assertions.*;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.meta.parameter.state.IntrusionStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.PredictedFailure;
import com.sentrysoftware.matrix.common.meta.parameter.state.Present;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;

class MappingConstantsTest {

	@Test
	void testStatusPredicates() {

		assertTrue(OK_STATUS_PREDICATE.test(Status.OK));
		assertFalse(OK_STATUS_PREDICATE.test(Status.DEGRADED));
		assertFalse(OK_STATUS_PREDICATE.test(Status.FAILED));

		assertFalse(DEGRADED_STATUS_PREDICATE.test(Status.OK));
		assertTrue(DEGRADED_STATUS_PREDICATE.test(Status.DEGRADED));
		assertFalse(DEGRADED_STATUS_PREDICATE.test(Status.FAILED));

		assertFalse(FAILED_STATUS_PREDICATE.test(Status.OK));
		assertFalse(FAILED_STATUS_PREDICATE.test(Status.DEGRADED));
		assertTrue(FAILED_STATUS_PREDICATE.test(Status.FAILED));
	}

	@Test
	void testPresentPredicate() {
		assertTrue(PRESENT_PREDICATE.test(Present.PRESENT));
		assertFalse(PRESENT_PREDICATE.test(Present.MISSING));
	}

	@Test
	void testIntrusionStatusPredicate() {
		assertTrue(INTRUSION_STATUS_PREDICATE.test(IntrusionStatus.OPEN));
		assertFalse(INTRUSION_STATUS_PREDICATE.test(IntrusionStatus.CLOSED));
	}

	@Test
	void testPredictedFailureStatusPredicate() {
		assertTrue(PREDICTED_FAILURE_PREDICATE.test(PredictedFailure.FAILURE_PREDICTED));
		assertFalse(PREDICTED_FAILURE_PREDICATE.test(PredictedFailure.OK));
	}

}
