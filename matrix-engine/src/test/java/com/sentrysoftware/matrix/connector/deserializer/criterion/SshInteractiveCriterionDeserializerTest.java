package com.sentrysoftware.matrix.connector.deserializer.criterion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshstep.GetAvailable;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SshInteractiveCriterion;

class SshInteractiveCriterionDeserializerTest extends DeserializerTest {

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
	void testDeserializeSshInteractive() throws Exception { // NOSONAR compareCriterion performs assertion

		final Connector sshInteractive = getConnector("criterionSshInteractive");

		List<Criterion> expected = new ArrayList<>();
		List<Step> steps = new ArrayList<>();
		steps.add(new GetAvailable("getAvailable", true, false));

		expected.add(new SshInteractiveCriterion("sshInteractive", false, 22123, "Cisoc", steps));
		compareCriterion(sshInteractive, expected);
	}

	@Test
	/**
	 * Checks that steps is not null
	 */
	void testSshInteractiveStepsNonNull() throws Exception {
		// fail on null steps
		{
			try {
				getConnector("criterionSshInteractiveNoSteps");
				Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
			} catch (JsonMappingException e) {
				String message = "Missing required creator property 'steps' (index 4)";
				checkMessage(e, message);
			}
		}
		{
			try {
				getConnector("criterionSshInteractiveNullSteps");
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
	 * Checks that null port is accepted
	 */
	void testSshInteractivePortNull() throws Exception {
		// doesn't fail on null port
		{
			assertDoesNotThrow(() -> getConnector("criterionSshInteractiveNullPort"));
		}
		{
			assertDoesNotThrow(() -> getConnector("criterionSshInteractiveNoPort"));
		}
	}

	@Test
	/**
	 * Checks that port string is rejected
	 */
	void testSshInteractivePortInteger() throws Exception {
		try {
			getConnector("criterionSshInteractiveStringPort");
			Assert.fail(INVALID_FORMAT_EXCEPTION_MSG);
		} catch (JsonMappingException e) {
			String message = "Invalid value encountered for property 'port'.";
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

}
