package com.sentrysoftware.matrix.connector.deserializer.sshstep;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshstep.GetUntilPrompt;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SshInteractiveCriterion;

class StepGetUntilPromptTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/sshInteractive/steps/getUntilPrompt/";
	}

	@Test
	/**
	 * Check that the GetUntilPrompt step parsing works correctly
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveGetUntilPrompt() throws Exception { // NOSONAR compareCriterion performs assertion
		final String testResource = "criterionSshInteractiveStepGetUntilPrompt";
		final Connector sshInteractive = getConnector(testResource);
		List<Criterion> expected = new ArrayList<>();
		List<Step> steps = new ArrayList<>();
		steps.add(new GetUntilPrompt("getUntilPrompt", true, false, 10L));
		expected.add(new SshInteractiveCriterion("sshInteractive", false, 22123, "Cisoc", steps));

		compareCriterion(testResource, sshInteractive, expected);
	}

	@Test
	/**
	 * Check that the `Not-a-Number` timeout leads to a parsing failure
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveGetUntilPromptNanTimeout() throws Exception {

		try {
			getConnector("criterionSshInteractiveStepGetUntilPromptNanTimeout");
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
	void testSshInteractiveGetUntilPromptBadTimeout() throws Exception {

		try {
			getConnector("criterionSshInteractiveStepGetUntilPromptBadTimeout");
			Assert.fail(INVALID_FORMAT_EXCEPTION_MSG);
		} catch (InvalidFormatException e) {
			final String message = "Invalid negative or zero value encountered for property 'timeout'.";
			checkMessage(e, message);
		}
	}
}
