package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
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
	 * Checks that the invalid duration is rejected
	 */
	void testSshInteractiveSleepBadLong() throws Exception {
		try {
			getConnector("criterionSshInteractiveSleepBadLong");
			fail(JSON_MAPPING_EXCEPTION_MSG);
		} catch (JsonMappingException e) {
			String message = "Cannot deserialize value of type `java.lang.Long`";
			checkMessage(e, message);
		}
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

}
