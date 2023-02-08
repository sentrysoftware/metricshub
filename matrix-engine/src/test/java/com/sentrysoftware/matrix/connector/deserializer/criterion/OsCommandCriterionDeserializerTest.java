package com.sentrysoftware.matrix.connector.deserializer.criterion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.OsCommandCriterion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class OsCommandCriterionDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/osCommand/";
	}

	@Test
	/**
	 * Checks input properties for os command detection criteria
	 *
	 * @throws IOException
	 */
	void testDeserializeOsCommand() throws IOException {
		final String testResource = "osCommandCriterion";
		final Connector osCommand = getConnector(testResource);

		List<Criterion> expected = new ArrayList<>();

		final String commandLine = "naviseccli -help";
		final String errorMessage = "Not a Navisphere system";
		final String expectedResult = "Navisphere";
		final boolean executeLocally = true;
		final Long timeout = 1234L;

		expected.add(
				new OsCommandCriterion("osCommand", true, commandLine, errorMessage, expectedResult, executeLocally, timeout));

		assertNotNull(osCommand);
		assertEquals(testResource, osCommand.getConnectorIdentity().getCompiledFilename());

		assertNotNull(osCommand.getConnectorIdentity().getDetection());
		assertEquals(expected, osCommand.getConnectorIdentity().getDetection().getCriteria());
	}

	@Test
	/**
	 * Checks that if commandLine is declared it is not null
	 *
	 * @throws IOException
	 */
	void testDeserializeOsCommandNoCommand() throws IOException {
		// commandLine is null
		try {
			getConnector("osCommandCriterionCommandLineNo");
			Assert.fail(MISMATCHED_EXCEPTION_MSG);
		} catch (MismatchedInputException e) {
			final String message = "Missing required creator property 'commandLine' (index 2)";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that if commandLine is not whitespace
	 *
	 * @throws IOException
	 */
	void testDeserializeOsCommandBlankCommand() throws IOException {
		try {
			getConnector("osCommandCriterionCommandLineBlank");
			Assert.fail(IO_EXCEPTION_MSG);
		} catch (IOException e) {
			String message = "Invalid blank value encountered for property 'commandLine'.";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that if commandLine is not declared we throw an exception
	 *
	 * @throws IOException
	 */
	void testDeserializeOsCommandNullCommand() throws IOException {
		// commandLine is null
		try {
			getConnector("osCommandCriterionCommandLineNull");
			Assert.fail(INVALID_NULL_EXCEPTION_MSG);
		} catch (InvalidNullException e) {
			final String message = "Invalid `null` value encountered for property \"commandLine\"";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that negative timeout throws exception
	 */
	void testDeserializeOsCommandNegativeTimeout() throws IOException {
		try {
			getConnector("osCommandCriterionNegativeTimeout");
			Assert.fail(INVALID_FORMAT_EXCEPTION_MSG);
		} catch (InvalidFormatException e) {
			final String message = "Invalid negative or zero value encountered for property 'timeout'";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that zero timeout throws exception
	 */
	void testDeserializeOsCommandZeroTimeout() throws IOException {
		try {
			getConnector("osCommandCriterionZeroTimeout");
			Assert.fail(INVALID_FORMAT_EXCEPTION_MSG);
		} catch (InvalidFormatException e) {
			final String message = "Invalid negative or zero value encountered for property 'timeout'";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that string timeout throws exception
	 */
	void testDeserializeOsCommandStringTimeout() throws IOException {
		try {
			getConnector("osCommandCriterionStringTimeout");
			Assert.fail(INVALID_FORMAT_EXCEPTION_MSG);
		} catch (InvalidFormatException e) {
			final String message = "Invalid value encountered for property 'timeout'";
			checkMessage(e, message);
		}
	}
}