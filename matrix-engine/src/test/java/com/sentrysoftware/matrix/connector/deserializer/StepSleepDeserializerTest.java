package com.sentrysoftware.matrix.connector.deserializer;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Sleep;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SshInteractive;

class StepSleepDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/sshInteractive/steps/sleep/";
	}

	@Test
	/**
	 * Check that the Sleep step parsing works correctly
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveSleep() throws Exception { // NOSONAR compareCriterion performs assertion
		final String testResource = "criterionSshInteractiveSleep";
		final Connector sshInteractive = getConnector(testResource);
		List<Criterion> expected = new ArrayList<>();
		List<Step> steps = new ArrayList<>();
		steps.add(new Sleep("sleep", null, false, 1L));
		expected.add(new SshInteractive("sshInteractive", false, 22123, "Cisoc", steps));

		compareCriterion(testResource, sshInteractive, expected);
	}

	@Test
	/**
	 * Checks that yes is accepted as capture input
	 */
	void testSshInteractiveSleepCaptureYes() throws Exception { // NOSONAR compareCriterion performs assertion
		final String testResource = "criterionSshInteractiveSleepCaptureBooleanYes";
		final Connector sshInteractive = getConnector(testResource);
		List<Criterion> expected = new ArrayList<>();
		List<Step> steps = new ArrayList<>();
		steps.add(new Sleep("sleep", Boolean.TRUE, false, 10L));
		expected.add(new SshInteractive("sshInteractive", false, 22123, "Cisoc", steps));

		compareCriterion(testResource, sshInteractive, expected);
	}

	@Test
	/**
	 * Checks that 1 is accepted as capture input
	 */
	void testSshInteractiveSleepCapture1() throws Exception { // NOSONAR compareCriterion performs assertion
		final String testResource = "criterionSshInteractiveSleepCaptureBoolean1";
		final Connector sshInteractive = getConnector(testResource);

		List<Criterion> expected = new ArrayList<>();
		List<Step> steps = new ArrayList<>();
		steps.add(new Sleep("sleep", Boolean.TRUE, false, 10L));
		expected.add(new SshInteractive("sshInteractive", false, 22123, "Cisoc", steps));

		compareCriterion(testResource, sshInteractive, expected);
	}

	@Test
	/**
	 * Checks that bad capture input is rejected
	 */
	void testSshInteractiveSleepBadCapture() throws Exception {
		try {
			getConnector("criterionSshInteractiveSleepBadCapture");
			Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
		} catch (JsonMappingException e) {
			String message = "Cannot deserialize value of type `java.lang.Boolean` from String ";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that null duration leads to a parsing failure
	 */
	void testSshInteractiveNullDuration() throws Exception {
		// fail on null port
		try {
			getConnector("criterionSshInteractiveSleepNullDuration");
			Assert.fail(INVALID_NULL_EXCEPTION_MSG);
		} catch (InvalidNullException e) {
			String message = "Invalid `null` value encountered for property \"duration\"";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that missing duration leads to a parsing failure
	 */
	void testSshInteractiveMissingDuration() throws Exception {
		try {
			getConnector("criterionSshInteractiveSleepNoDuration");
			Assert.fail(MISMATCHED_EXCEPTION_MSG);
		} catch (MismatchedInputException e) {
			String message = "Missing required creator property 'duration' (index 3)";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that `String` duration leads to a parsing failure
	 */
	void testSshInteractiveStringDuration() throws Exception {
		try {
			getConnector("criterionSshInteractiveSleepStringDuration");
			Assert.fail(INVALID_FORMAT_EXCEPTION_MSG);
		} catch (InvalidFormatException e) {
			String message = "Invalid value encountered for property 'duration'.";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that negative duration is rejected
	 */
	void testSshInteractiveNegativeDuration() throws Exception {
		try {
			getConnector("criterionSshInteractiveSleepNegativeDuration");
			Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
		} catch (JsonMappingException e) {
			String message = "Invalid negative or zero value encountered for property 'duration'.";
			checkMessage(e, message);
		}
	}
}
