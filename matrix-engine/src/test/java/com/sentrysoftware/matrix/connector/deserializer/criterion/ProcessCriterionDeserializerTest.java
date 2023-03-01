package com.sentrysoftware.matrix.connector.deserializer.criterion;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProcessCriterion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ProcessCriterionDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/process/";
	}

	@Test
	/**
	 * Checks input properties for detection criteria
	 *
	 * @throws IOException
	 */
	void testDeserializeProcess() throws IOException { // NOSONAR compareCriterion performs assertion

		final Connector process = getConnector("processCriterion");

		List<Criterion> expected = new ArrayList<>();

		final String commandLine = "naviseccli -help";

		expected.add(new ProcessCriterion("process", true, commandLine));

		compareCriterion(process, expected);
	}

	@Test
	/**
	 * Checks that null commandline is rejected
	 *
	 * @throws IOException
	 */
	void testProcessNullCommandLine() throws IOException {
		// commandLine is null
		try {
			getConnector("processCriterionNullCommandLine");
			Assert.fail(INVALID_NULL_EXCEPTION_MSG);
		} catch (InvalidNullException e) {
			final String message = "Invalid `null` value encountered for property \"commandLine\"";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that blanks are rejected
	 *
	 * @throws IOException
	 */
	void testProcessBlankCommandLine() throws IOException {
		// commandLine is blank
		try {
			getConnector("processCriterionBlankCommandLine");
			Assert.fail(INVALID_FORMAT_EXCEPTION_MSG);
		} catch (InvalidFormatException e) {
			final String message = "Invalid blank value encountered for property 'commandLine'.";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that command line is declared
	 *
	 * @throws IOException
	 */
	void testProcessNoCommandLine() throws IOException {
		// no commandline defined
		try {
			getConnector("processCriterionNoCommandLine");
			Assert.fail(MISMATCHED_EXCEPTION_MSG);
		} catch (MismatchedInputException e) {
			checkMessage(e, "Missing required creator property 'commandLine' (index 2)");
		}
	}
}
