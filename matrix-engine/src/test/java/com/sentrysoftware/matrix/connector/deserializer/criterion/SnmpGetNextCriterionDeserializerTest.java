package com.sentrysoftware.matrix.connector.deserializer.criterion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SnmpGetNextCriterion;

class SnmpGetNextCriterionDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/snmpGetNext/";
	}

	@Test
	void testDeserializeSnmpGetNext() throws IOException { // NOSONAR compareCriterion performs assertion

		final Connector snmpGetNext = getConnector("snmpGetNextCriterion");

		final String oid = "1.3.6.1.4.1.674.10892.5.5.1.20.130.4";
		final String expectedResult = "OK";

		List<Criterion> expected = new ArrayList<>();
		expected.add(new SnmpGetNextCriterion("snmpGetNext", false, oid, expectedResult));

		compareCriterion(snmpGetNext, expected);
	}

	@Test
	/**
	 * Checks that oid is not null
	 *
	 * @throws IOException
	 */
	void testSnmpGetNextNullOid() throws IOException {
		// oid is null
		try {
			getConnector("snmpGetNextCriterionNullOid");
			Assert.fail(INVALID_NULL_EXCEPTION_MSG);
		} catch (InvalidNullException e) {
			final String message = "Invalid `null` value encountered for property \"oid\"";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * Checks that oid is not blank
	 *
	 * @throws IOException
	 */
	void testSnmpGetNextBlankOid() throws IOException {
		// oid is null
		try {
			getConnector("snmpGetNextCriterionBlankOid");
			Assert.fail(IO_EXCEPTION_MSG);
		} catch (IOException e) {
			final String message = "Invalid blank value encountered for property 'oid'.";
			checkMessage(e, message);
		}
	}


	@Test
	/**
	 * Checks that oid is defined
	 *
	 * @throws IOException
	 */
	void testSnmpGetNextNoOid() throws IOException {
		// oid is defined
		try {
			getConnector("snmpGetNextCriterionNoOid");
			Assert.fail(MISMATCHED_EXCEPTION_MSG);
		} catch (MismatchedInputException e) {
			final String message = "Missing required creator property 'oid' (index 2)";
			checkMessage(e, message);
		}
	}
}
