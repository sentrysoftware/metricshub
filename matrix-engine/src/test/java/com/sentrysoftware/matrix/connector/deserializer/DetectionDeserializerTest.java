package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.identity.ConnectionType;
import com.sentrysoftware.matrix.connector.model.identity.Detection;

class DetectionDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/";
	}

	@Test
	/**
	 * Checks detection input properties
	 *
	 * @throws IOException
	 */
	void testDeserializeDetectionValues() throws IOException {

		final Connector detection = getConnector("detection");

		// Initialization tests
		assertNotNull(detection);
		assertEquals("detection", detection.getConnectorIdentity().getCompiledFilename());
		assertNotNull(detection.getConnectorIdentity().getDetection());

		// appliesTo
		final Detection expectedAppliesTo = Detection.builder().appliesTo(Set.of(DeviceKind.values())).build();
		assertEquals(expectedAppliesTo.getAppliesTo(), detection.getConnectorIdentity().getDetection().getAppliesTo());
		
		// connectionTypes
		final Detection expectedConnectionType = Detection.builder().connectionTypes(Set.of(ConnectionType.values())).build();
		assertEquals(expectedConnectionType.getConnectionTypes(), detection.getConnectorIdentity().getDetection().getConnectionTypes());

		// disableAutoDetection
		assertEquals(true, detection.getConnectorIdentity().getDetection().isDisableAutoDetection());

		// onLastResort
		assertEquals("enclosure", detection.getConnectorIdentity().getDetection().getOnLastResort());

		// supersedes
		var expectedSupersedes = detection.getConnectorIdentity().getDetection().getSupersedes();
		assertTrue(expectedSupersedes instanceof HashSet, "supersedes are expected to be a HashSet.");
		assertEquals(new HashSet<>(List.of("Connector1", "Connector2")), expectedSupersedes);
	}

	/*
	* appliesTo
	*/

	@Test
	/**
	 * appliesTo: checks that an empty value is rejected
	 *
	 * @throws Exception
	 */
	void testDeserializeAppliesToEmpty() throws Exception {

		try {
			getConnector("appliesToEmpty");
			Assert.fail("Expected an InvalidFormatException to be thrown.");
		} catch (InvalidFormatException e) {
			final String message = "Invalid blank value encountered for property \"appliesTo\".";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * appliesTo: check a null value is rejected
	 *
	 * @throws Exception
	 */
	void testDeserializeAppliesToNull() throws Exception {

		try {
			getConnector("appliesToNull");
			Assert.fail("Expected an InvalidNullException to be thrown.");
		} catch (InvalidNullException e) {
			final String message = "Invalid `null` value encountered for property \"appliesTo\"";
			checkMessage(e, message);
		}
	}

	@Test
	/**
	 * appliesTo: check an invalid value is rejected
	 *
	 * @throws Exception
	 */
	void testDeserializeAppliesToInvalid() throws Exception {

		try {
			getConnector("appliesToInvalid");
			Assert.fail(JSON_MAPPING_EXCEPTION_MSG);
		} catch (JsonMappingException e) {
			checkMessage(e, "\"unknownValue\" is not a supported device kind.");
		}
	}

	@Test
	/**
	 * appliesTo: check a commented-out line is rejected
	 *
	 * @throws Exception
	 */
	void testDeserializeAppliesToCommentedOut() throws IOException {
		try {
			getConnector("appliesToCommentedOut");
			Assert.fail("Expected an InvalidNullException to be thrown.");
		} catch (InvalidNullException e) {
			final String message = "Invalid `null` value encountered for property \"appliesTo\"";
			checkMessage(e, message);
		}
	}

	/*
	* connectionTypes
	*/
	
	@Test
	/**
	 * connectionTypes: check that an empty value is defaulted to local
	 *
	 * @throws Exception
	 */
	void testDeserializeConnectionTypeEmpty() throws IOException {
		final Connector detection = getConnector("connectionTypesEmpty");

		assertEquals(
			Collections.singleton(ConnectionType.LOCAL),
			detection.getConnectorIdentity().getDetection().getConnectionTypes()
		);
	}

	@Test
	/**
	 * connectionTypes: check that a null value is defaulted to local
	 *
	 * @throws Exception
	 */
	void testDeserializeConnectionTypeNull() throws IOException {
		final Connector detection = getConnector("connectionTypesNull");

		assertEquals(
			Collections.singleton(ConnectionType.LOCAL),
			detection.getConnectorIdentity().getDetection().getConnectionTypes()
		);
	}

	/**
	 * connectionTypes: check that an commented-out value is defaulted to local
	 * @throws IOException 
	 *
	 */
	void testDeserializeConnectionTypeCommentedOut() throws IOException {
		final Connector detection = getConnector("connectionTypesCommentedOut");

		assertEquals(
			Collections.singleton(ConnectionType.LOCAL),
			detection.getConnectorIdentity().getDetection().getConnectionTypes()
		);
	}

	@Test
	/**
	 * connectionTypes: check an invalid value is rejected
	 *
	 * @throws Exception
	 */
	void testDeserializeConnectionTypeInvalid() throws Exception {

		try {
			getConnector("connectionTypesInvalid");
			Assert.fail("Expected an IOException to be thrown.");
		} catch (IOException e) {
			final String message = "connectionType must be a known connection type (local, remote)";
			assertTrue(
					e.getMessage().contains(message),
					() -> "Expected exception should contain \"" + message + "\" but got the following error instead: " + e.getMessage());
		}
	}

	//
	// disableAutoDetection
	//
	
	/**
	 * disableAutoDetection: check that an empty value is defaulted to false
	 * 
	 * @throws IOException 
	 *
	 */
	void testDeserializeDisableAutoDetectionEmpty() throws IOException {
		final Connector detection = getConnector("disableAutoDetectionEmpty");

		assertEquals(false, detection.getConnectorIdentity().getDetection().isDisableAutoDetection());
	}
	
	/**
	 * disableAutoDetection: check that a null value is defaulted to false
	 * 
	 * @throws IOException 
	 *
	 */
	void testDeserializeDisableAutoDetectionNull() throws IOException {
		final Connector detection = getConnector("disableAutoDetectionNull");

		assertEquals(false, detection.getConnectorIdentity().getDetection().isDisableAutoDetection());
	}

	/**
	 * disableAutoDetection: check that an commented-out value is defaulted to false
	 * 
	 * @throws IOException 
	 *
	 */
	void testDeserializeDisableAutoDetectionCommentedOut() throws IOException {
		final Connector detection = getConnector("disableAutoDetectionCommentedOut");

		assertEquals(false, detection.getConnectorIdentity().getDetection().isDisableAutoDetection());
	}

	@Test
	/**
	 * disableAutoDetection: check an invalid value is rejected
	 * 
	 * @throws Exception
	 */
	void testDeserializeDisableAutodetectionInvalid() throws Exception {

		try {
			getConnector("disableAutoDetectionInvalid");
			Assert.fail("Expected an IOException to be thrown.");
		} catch (IOException e) {
			final String message = String.format("Value is invalid");
			assertTrue(
					e.getMessage().contains(message),
					() -> "Expected exception should contain \"" + message + "\" but got the following error instead: " + e.getMessage());
		}
	}

	//
	// supercedes
	//
	
	@Test
	/**
	 * supercedes: check an empty value is rejected
	 * 
	 * @throws Exception
	 */
	void testDeserializeSupercedesEmpty() {

		try {
			getConnector("supercedesEmpty");
			Assert.fail("Expected an IOException to be thrown.");
		} catch (IOException e) {
			String message = "The connector referenced by \"supercedes\" cannot be empty.";
			assertTrue(
				e.getMessage().contains(message),
				() -> "Expected exception should contain: " + message + " but got the following error instead: " + e.getMessage()
			);
		}
	}

	@Test
	/**
	 * supercedes: check a null value is just ignored and returns an empty value
	 * 
	 * @throws Exception
	 */
	void testDeserializeSupercedesNull() throws IOException {
		final Connector detection = getConnector("supercedesNull");

		var supercedes = detection.getConnectorIdentity().getDetection().getSupersedes();

		assertTrue(supercedes instanceof HashSet, "supercedes are expected to be a HashSet.");
		assertEquals(Collections.emptySet(), supercedes);
	}

	@Test
	/**
	 * supercedes: check a commented-out line is just ignored and returns an empty value
	 * 
	 * @throws Exception
	 */
	void testDeserializeSupercedesCommentedOut() throws IOException {

		final Connector detection = getConnector("supercedesCommentedOut");

		var supercedes = detection.getConnectorIdentity().getDetection().getSupersedes();

		assertTrue(supercedes instanceof HashSet, "supercedes are expected to be a HashSet.");
		assertEquals(Collections.emptySet(), supercedes);
	}
	
	@Test
	/**
	 * supercedes: check a null value in a list is rejected
	 *
	 * @throws Exception
	 */
	void testDeserializeSupercedesNullList() {
		try {
			getConnector("supercedesNullList");
			Assert.fail("Expected an IOException to be thrown.");
		} catch (IOException e) {
			String message = "A connector referenced under 'supercedes' cannot be null.";
			assertTrue(
				e.getMessage().contains(message),
				() -> "Expected exception should contain: " + message + " but got the following error instead: " + e.getMessage()
			);
		}
	}
	
	@Test
	/**
	 * supercedes: check a empty value in a list is rejected
	 *
	 * @throws Exception
	 */
	void testDeserializeSupercedesEmptyList() {
		try {
			getConnector("supercedesEmptyList");
			Assert.fail("Expected an IOException to be thrown.");
		} catch (IOException e) {
			String message = "A connector referenced under 'supercedes' cannot be empty.";
			assertTrue(
				e.getMessage().contains(message),
				() -> "Expected exception should contain: " + message + " but got the following error instead: " + e.getMessage()
			);
		}
	}
	
	//
	// onLastResort
	//
	/**
	 * onLastResort: check a commented-out line is ignored and returns a null value
	 * 
	 * @throws IOException 
	 *
	 */
	void testDeserializeonLastResortCommentedOut() throws IOException {
		final Connector detection = getConnector("onLastResortCommentedOut");

		assertNull(detection.getConnectorIdentity().getDetection().getOnLastResort());
	}

	/**
	 * onLastResort: check an empty value triggers an error
	 * 
	 * @throws IOException 
	 *
	 */
	void testDeserializeonLastResortEmpty() throws IOException {
		try {
			getConnector("onLastResortEmpty");

			Assert.fail("Expected an InvalidFormatException to be thrown.");
		} catch (InvalidFormatException e) {
			String message = "Invalid blank value encountered for property \"onLastResort\".";
			checkMessage(e, message);
		}
	}
	
	/**
	 * onLastResort: check a null value returns a null value
	 * 
	 * @throws IOException 
	 *
	 */
	void testDeserializeonLastResortNull() throws IOException {
		final Connector detection = getConnector("onLastResortNull");

		assertNull(detection.getConnectorIdentity().getDetection().getOnLastResort());
	}

}