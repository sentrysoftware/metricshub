package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.meta.parameter.state.DuplexMode;
import com.sentrysoftware.matrix.common.meta.parameter.state.IntrusionStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.LedIndicator;
import com.sentrysoftware.matrix.common.meta.parameter.state.LinkStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.NeedsCleaning;
import com.sentrysoftware.matrix.common.meta.parameter.state.PowerState;
import com.sentrysoftware.matrix.common.meta.parameter.state.PredictedFailure;
import com.sentrysoftware.matrix.common.meta.parameter.state.Present;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.common.meta.parameter.state.Up;

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
	void testNeedsCleaningPredicate() {

		assertFalse(CLEANING_NEEDED_PREDICATE.test(NeedsCleaning.OK));
		assertTrue(CLEANING_NEEDED_PREDICATE.test(NeedsCleaning.NEEDED));
		assertTrue(CLEANING_NEEDED_PREDICATE.test(NeedsCleaning.NEEDED_IMMEDIATELY));

	}

	@Test
	void testCreateStatusDescription() {
		assertThrows(IllegalArgumentException.class, () -> createStatusDescription(null, "state = ok"));
		assertThrows(IllegalArgumentException.class, () -> createStatusDescription("device", null));
		assertThrows(IllegalArgumentException.class, () -> createStatusDescription(null, "key", "val"));
		assertThrows(IllegalArgumentException.class, () -> createStatusDescription(null, "key", "val"));
		assertThrows(IllegalArgumentException.class, () -> createStatusDescription("physical disk", null, "val"));
		assertThrows(IllegalArgumentException.class, () -> createStatusDescription("physical disk", "key", (String[]) null));
		assertNotNull(createStatusDescription("physical disk", "key", "val"));
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
	void testPowerStatePredicates() {
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

	@Test
	void testLedIndicatorPredicates() {

		assertTrue(ON_LED_INDICATOR_PREDICATE.test(LedIndicator.ON));
		assertFalse(ON_LED_INDICATOR_PREDICATE.test(LedIndicator.BLINKING));
		assertFalse(ON_LED_INDICATOR_PREDICATE.test(LedIndicator.OFF));

		assertFalse(BLINKING_LED_INDICATOR_PREDICATE.test(LedIndicator.ON));
		assertTrue(BLINKING_LED_INDICATOR_PREDICATE.test(LedIndicator.BLINKING));
		assertFalse(BLINKING_LED_INDICATOR_PREDICATE.test(LedIndicator.OFF));

		assertFalse(OFF_LED_INDICATOR_PREDICATE.test(LedIndicator.ON));
		assertFalse(OFF_LED_INDICATOR_PREDICATE.test(LedIndicator.BLINKING));
		assertTrue(OFF_LED_INDICATOR_PREDICATE.test(LedIndicator.OFF));
	}

	@Test
	void testCreatePowerStateDescription() {
		assertThrows(IllegalArgumentException.class, () -> createPowerStateDescription(null, "key", "val"));
		assertThrows(IllegalArgumentException.class, () -> createPowerStateDescription("physical disk", null, "val"));
		assertThrows(IllegalArgumentException.class, () -> createPowerStateDescription("physical disk", "key", (String[]) null));
		assertNotNull(createPowerStateDescription("virtual machine", "key", "val"));
	}

	@Test
	void testFullDuplexPredicate() {
		assertTrue(FULL_DUPLEX_MODE_PREDICATE.test(DuplexMode.FULL));
		assertFalse(FULL_DUPLEX_MODE_PREDICATE.test(DuplexMode.HALF));
	}

	@Test
	void testLinkStatusPredicate() {
		assertTrue(PLUGGED_LINK_STATUS_PREDICATE.test(LinkStatus.PLUGGED));
		assertFalse(PLUGGED_LINK_STATUS_PREDICATE.test(LinkStatus.UNPLUGGED));
	}

	@Test
	void testBuildAttributeSection() {
		assertThrows(IllegalArgumentException.class, () -> buildAttributeSection("state", (String[])null));
		assertThrows(IllegalArgumentException.class, () -> buildAttributeSection(null, "value"));
		assertThrows(IllegalArgumentException.class, () -> buildAttributeSection("state", new String[] {}));
		assertNotNull(buildAttributeSection("state", "value"));
	}

	@Test
	void testCreateCustomDescriptionWithAttributes() {
		assertThrows(IllegalArgumentException.class, () -> createCustomDescriptionWithAttributes("text", "state", (String[]) null));
		assertThrows(IllegalArgumentException.class, () -> createCustomDescriptionWithAttributes("text", null, "value"));
		assertThrows(IllegalArgumentException.class, () -> createCustomDescriptionWithAttributes(null, "state", "value"));
	}

	@Test
	void testUpPredicate() {
		assertTrue(UP_PREDICATE.test(Up.UP));
		assertFalse(UP_PREDICATE.test(Up.DOWN));
	}
}
