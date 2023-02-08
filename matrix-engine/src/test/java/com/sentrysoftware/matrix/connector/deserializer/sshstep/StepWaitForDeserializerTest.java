package com.sentrysoftware.matrix.connector.deserializer.sshstep;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;
import com.sentrysoftware.matrix.connector.model.common.sshstep.WaitFor;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SshInteractiveCriterion;

class StepWaitForDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/sshInteractive/steps/waitFor/";
	}

	@Test
	/**
	 * Check that the waitFor step parsing works correctly
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveWaitFor() throws Exception { // NOSONAR compareCriterion performs assertion
		final String testResource = "criterionSshInteractiveWaitFor";
		final Connector sshInteractive = getConnector(testResource);
		List<Criterion> expected = new ArrayList<>();
		List<Step> steps = new ArrayList<>();
		steps.add(new WaitFor("waitFor", null, false, "success", null));
		expected.add(new SshInteractiveCriterion("sshInteractive", false, 22123, "Cisoc", steps));

		compareCriterion(testResource, sshInteractive, expected);
	}

	@Test
	/**
	 * Check that the missing text leads to parsing failure
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveWaitForTextMissing() throws Exception { // NOSONAR compareCriterion performs assertion
		try {
			getConnector("criterionSshInteractiveWaitForTextMissing");
			Assert.fail(MISMATCHED_EXCEPTION_MSG);
		} catch (MismatchedInputException e) {
			final String message = "Missing required creator property 'text' (index 3)";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Check that the null text leads to parsing failure
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveWaitForTextNull() throws Exception { // NOSONAR compareCriterion performs assertion
		try {
			getConnector("criterionSshInteractiveWaitForTextNull");
			Assert.fail(INVALID_NULL_EXCEPTION_MSG);
		} catch (InvalidNullException e) {
			final String message = "Invalid `null` value encountered for property \"text\"";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Check that the `Not-a-Number` timeout leads to a parsing failure
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveWaitForNanTimeout() throws Exception { // NOSONAR compareCriterion performs assertion

		try {
			getConnector("criterionSshInteractiveStepWaitForNanTimeout");
			Assert.fail(INVALID_FORMAT_EXCEPTION_MSG);
		} catch (InvalidFormatException e) {
			final String message = "Invalid value encountered for property 'timeout'.";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Check that the bad timeout (0 or negative) leads to a parsing failure
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveWaitForBadTimeout() throws Exception { // NOSONAR compareCriterion performs assertion

		try {
			getConnector("criterionSshInteractiveStepWaitForBadTimeout");
			Assert.fail(INVALID_FORMAT_EXCEPTION_MSG);
		} catch (InvalidFormatException e) {
			final String message = "Invalid negative or zero value encountered for property 'timeout'.";
			checkMessage(e, message);
		}
	}
}
