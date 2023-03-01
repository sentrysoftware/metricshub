package com.sentrysoftware.matrix.connector.deserializer.sshstep;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshstep.GetAvailable;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SshInteractiveCriterion;

class StepGetAvailableTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/sshInteractive/steps/getAvailable/";
	}

	@Test
	/**
	 * Check that the GetAvailable step parsing works correctly
	 * 
	 * @throws Exception
	 */
	void testSshInteractiveGetAvailable() throws Exception { // NOSONAR compareCriterion performs assertion

		final Connector sshInteractive = getConnector("criterionSshInteractiveStepGetAvailable");
		List<Criterion> expected = new ArrayList<>();
		List<Step> steps = new ArrayList<>();
		steps.add(new GetAvailable("getAvailable", null, false));
		expected.add(new SshInteractiveCriterion("sshInteractive", false, 22123, "Cisoc", steps));

		compareCriterion(sshInteractive, expected);
	}
}
