package com.sentrysoftware.matrix.connector.deserializer;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshstep.SendText;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SshInteractive;

class StepSendTextTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/sshInteractive/steps/sendText/";
	}

	@Test
	/**
	 * Check that the SendText step parsing works correctly
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveSendText() throws Exception { // NOSONAR compareCriterion performs assertion
		final String testResource = "criterionSshInteractiveStepSendText";
		final Connector sshInteractive = getConnector(testResource);
		List<Criterion> expected = new ArrayList<>();
		List<Step> steps = new ArrayList<>();
		steps.add(new SendText("sendText", null, false, "start"));
		expected.add(new SshInteractive("sshInteractive", false, 22123, "Cisoc", steps));

		compareCriterion(testResource, sshInteractive, expected);
	}

	@Test
	/**
	 * Check that the missing text leads to parsing failure
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveSendTextMissing() throws Exception {

		try {
			getConnector("criterionSshInteractiveStepSendTextMissing");
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
	void testSshInteractiveSendTextNull() throws Exception {

		try {
			getConnector("criterionSshInteractiveStepSendTextNull");
			Assert.fail(MISMATCHED_EXCEPTION_MSG);
		} catch (InvalidNullException e) {
			final String message = "Invalid `null` value encountered for property \"text\"";
			checkMessage(e, message);
		}
	}
}
