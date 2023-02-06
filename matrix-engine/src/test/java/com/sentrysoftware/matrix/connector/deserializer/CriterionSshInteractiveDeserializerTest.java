package com.sentrysoftware.matrix.connector.deserializer;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshstep.GetAvailable;
import com.sentrysoftware.matrix.connector.model.common.sshstep.GetUntilPrompt;
import com.sentrysoftware.matrix.connector.model.common.sshstep.SendPassword;
import com.sentrysoftware.matrix.connector.model.common.sshstep.SendText;
import com.sentrysoftware.matrix.connector.model.common.sshstep.SendUsername;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Sleep;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;
import com.sentrysoftware.matrix.connector.model.common.sshstep.WaitFor;
import com.sentrysoftware.matrix.connector.model.common.sshstep.WaitForPrompt;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SshInteractive;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class CriterionSshInteractiveDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/sshInteractive/";
	}

	@Test
	/**
	 * Checks input properties for detection criteria
	 *
	 * @throws Exception
	 */
	void testDeserializeSshInteractive() throws Exception {
		final String testResource = "criterionSshInteractive";
		final Connector sshInteractive = getConnector(testResource);

		List<Criterion> expected = new ArrayList<>();
		List<Step> steps = new ArrayList<>();
		steps.add(new GetAvailable("getAvailable", true, false));
		steps.add(new GetUntilPrompt("getUntilPrompt", false, false, 1234L));
		steps.add(new SendPassword("sendPassword", true, false));
		steps.add(new SendText("sendText", null, false, "testSendText"));
		steps.add(new SendUsername("sendUsername", null, false));
		steps.add(new Sleep("sleep", null, true, 1L));
		steps.add(new WaitFor("waitFor", null, false, "ogin:", 60L));
		steps.add(new WaitForPrompt("waitForPrompt", null, false, 60L));

		expected.add(new SshInteractive("sshInteractive", false, 22123, "Cisoc", steps));
		compareCriterion(testResource, sshInteractive, expected);
	}

	@Test
	/**
	 * Checks that steps is not null
	 */
	void testSshInteractiveStepsNonNull() throws Exception {
		// fail on null steps
		{
			try {
				getConnector("criterionSshInteractiveNullSteps");
				Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
			} catch (JsonMappingException e) {
				String message = "Missing required creator property 'steps' (index 4)";
				checkMessage(e, message);
			}
		}
		{
			try {
				getConnector("criterionSshInteractiveNoSteps");
				Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
			} catch (JsonMappingException e) {
				String message = "Invalid `null` value encountered for property \"steps\"";
				checkMessage(e, message);
			}
		}
	}

	@Test
	/**
	 * Checks that a step that does not exist is not processed
	 * 
	 * @throws Exception
	 */
	void testSshIneractiveBadSteps() throws Exception {
		try {
			getConnector("criterionSshInteractiveBadSteps");
			Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
		} catch (JsonMappingException e) {
			String message = "Could not resolve type id 'gotAvailable' as a subtype of `com.sentrysoftware.matrix.connector.model.common.sshstep.Step`";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that port is not null
	 */
	void testSshInteractivePortNonNull() throws Exception {
		// fail on null port
		{
			try {
				getConnector("criterionSshInteractiveNullPort");
				Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
			} catch (JsonMappingException e) {
				String message = "Missing required creator property 'port' (index 2)";
				checkMessage(e, message);
			}
		}
		{
			try {
				getConnector("criterionSshInteractiveNoPort");
				Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
			} catch (JsonMappingException e) {
				String message = "Invalid `null` value encountered for property \"port\"";
				checkMessage(e, message);
			}
		}
	}

	@Test
	/**
	 * Checks that port string is rejected
	 */
	void testSshInteractivePortInteger() throws Exception {
		try {
			getConnector("criterionSshInteractiveStringPort");
			Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
		} catch (JsonMappingException e) {
			String message = "Current token (VALUE_STRING) not numeric, can not use numeric value accessors";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that negative port is rejected
	 */
	void testSshInteractiveNegativePort() throws Exception {
		try {
			getConnector("criterionSshInteractiveNegativePort");
			Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
		} catch (JsonMappingException e) {
			String message = "Invalid negative or zero value encountered for property 'port'.";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that an invalid timeout is rejected
	 */
	void testSshInteractiveBadLong() throws Exception {
		try {
			getConnector("criterionSshInteractiveBadLong");
			Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
		} catch (JsonMappingException e) {
			String message = "Cannot deserialize value of type `java.lang.Long`";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that yes is accepted as capture input
	 */
	void testSshInteractiveCaptureYes() throws Exception {
		final String testResource = "criterionSshInteractiveCaptureBooleanYes";
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
	void testSshInteractiveCapture1() throws Exception {
		final String testResource = "criterionSshInteractiveCaptureBoolean1";
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
	void testSshInteractiveBadCapture() throws Exception {
		try {
			getConnector("criterionSshInteractiveBadCapture");
			Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
		} catch (JsonMappingException e) {
			String message = "Cannot deserialize value of type `java.lang.Boolean` from String ";
			checkMessage(e, message);
		}
	}
}
