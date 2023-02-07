package com.sentrysoftware.matrix.connector.deserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SnmpGet;

class CriteriaSnmpGetDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/snmpGet/";
	}

	@Test
	void testDeserializeSnmpGet() throws IOException {
		final String testResource = "snmpGetCriterion";
		final Connector snmpGet = getConnector(testResource);

		final String oid = "1.3.6.1.4.1.674.10892.5.5.1.20.130.4";
		final String expectedResult = "OK";

		List<Criterion> expected = new ArrayList<>();
		expected.add(new SnmpGet("snmpGet", false, oid, expectedResult));

		compareCriterion(testResource, snmpGet, expected);
	}

	@Test
	/**
	 * Checks that oid is not null
	 *
	 * @throws IOException
	 */
	void testSnmpGetNullOid() throws IOException {
		// oid is null
		try {
			getConnector("snmpGetCriterionNullOid");
			Assert.fail("Expected an InvalidNullException to be thrown.");
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
	void testSnmpGetBlankOid() throws IOException {
		// oid is null
		try {
			getConnector("snmpGetCriterionBlankOid");
			Assert.fail("Expected an IOException to be thrown.");
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
	void testSnmpGetNoOid() throws IOException {
		// oid is defined
		try {
			getConnector("snmpGetCriterionNoOid");
			Assert.fail("Expected an MismatchedInputException to be thrown.");
		} catch (MismatchedInputException e) {
			final String message = "Missing required creator property 'oid' (index 2)";
			checkMessage(e, message);
		}
	}
}
