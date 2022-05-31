package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static org.junit.jupiter.api.Assertions.*;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.*;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.meta.parameter.state.IntrusionStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.NeedsCleaning;
import com.sentrysoftware.matrix.common.meta.parameter.state.PowerState;
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

	@Test
	void testNeedsCleaningPredicates() {
		assertTrue(NO_NEEDS_CLEANING_PREDICATE.test(NeedsCleaning.OK));
		assertFalse(CLEANING_NEEDED_PREDICATE.test(NeedsCleaning.OK));
		assertFalse(IMMEDIATEL_CLEANING_NEEDED_PREDICATE.test(NeedsCleaning.OK));

		assertFalse(NO_NEEDS_CLEANING_PREDICATE.test(NeedsCleaning.NEEDED));
		assertTrue(CLEANING_NEEDED_PREDICATE.test(NeedsCleaning.NEEDED));
		assertFalse(IMMEDIATEL_CLEANING_NEEDED_PREDICATE.test(NeedsCleaning.NEEDED));

		assertFalse(NO_NEEDS_CLEANING_PREDICATE.test(NeedsCleaning.NEEDED_IMMEDIATELY));
		assertFalse(CLEANING_NEEDED_PREDICATE.test(NeedsCleaning.NEEDED_IMMEDIATELY));
		assertTrue(IMMEDIATEL_CLEANING_NEEDED_PREDICATE.test(NeedsCleaning.NEEDED_IMMEDIATELY));
	}

	@Test
	void testCreateStatusDescription() {
		assertThrows(IllegalArgumentException.class, () -> createStatusDescription(null, "ok"));
		assertThrows(IllegalArgumentException.class, () -> createStatusDescription("physical disk", null));
		assertNotNull(createStatusDescription("physical disk", "ok"));
	}

	@Test
	void testCreatePresentDescription() {
		assertThrows(IllegalArgumentException.class, () -> createPresentDescription(null));
		assertNotNull(createPresentDescription("physical disk"));
	}

	@Test
	void testCreateEnergyDescription() {
		assertThrows(IllegalArgumentException.class, () -> createEnergyDescription(null));
		assertNotNull(createEnergyDescription("physical disk"));
	}

	@Test
	void testCreatePowerConsumptionDescription() {
		assertThrows(IllegalArgumentException.class, () -> createPowerConsumptionDescription(null));
		assertNotNull(createPowerConsumptionDescription("physical disk"));
	}

	@Test
	void testPowerStatePredicate() {
		assertTrue(ON_POWER_STATE_PREDICATE.test(PowerState.ON));
		assertFalse(ON_POWER_STATE_PREDICATE.test(PowerState.OFF));
		assertFalse(ON_POWER_STATE_PREDICATE.test(PowerState.SUSPENDED));

		assertFalse(OFF_POWER_STATE_PREDICATE.test(PowerState.ON));
		assertTrue(OFF_POWER_STATE_PREDICATE.test(PowerState.OFF));
		assertFalse(OFF_POWER_STATE_PREDICATE.test(PowerState.SUSPENDED));

		assertFalse(SUSPENDED_POWER_STATE_PREDICATE.test(PowerState.ON));
		assertFalse(SUSPENDED_POWER_STATE_PREDICATE.test(PowerState.OFF));
		assertTrue(SUSPENDED_POWER_STATE_PREDICATE.test(PowerState.SUSPENDED));
	}
}
