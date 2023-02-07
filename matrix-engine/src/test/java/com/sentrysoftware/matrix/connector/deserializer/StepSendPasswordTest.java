package com.sentrysoftware.matrix.connector.deserializer;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshstep.SendPassword;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SshInteractive;

class StepSendPasswordTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/sshInteractive/steps/sendPassword/";
	}

	@Test
	/**
	 * Check that the SendPassord step parsing works correctly
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveSendPassword() throws Exception { // NOSONAR compareCriterion performs assertion
		final String testResource = "criterionSshInteractiveStepSendPassword";
		final Connector sshInteractive = getConnector(testResource);
		List<Criterion> expected = new ArrayList<>();
		List<Step> steps = new ArrayList<>();
		steps.add(new SendPassword("sendPassword", null, false));
		expected.add(new SshInteractive("sshInteractive", false, 22123, "Cisoc", steps));

		compareCriterion(testResource, sshInteractive, expected);
	}
}
