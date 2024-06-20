package org.sentrysoftware.metricshub.extension.snmp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetNextCriterion;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.snmp.SnmpConfiguration.SnmpVersion;

@ExtendWith(MockitoExtension.class)
class SnmpExtensionTest {

	private static final String CONNECTOR_ID = "connector";
	public static final String OID = "1.3.6.1.4.1.674.10893.1.20";
	private static final String HOST_NAME = "test-host" + UUID.randomUUID().toString();
	public static final String SNMP_GET_NEXT_SECOND_RESULT = "1.3.6.1.4.1.674.10893.1.20.1 ASN_INTEGER 1";
	public static final String SNMP_GET_NEXT_THIRD_RESULT = "1.3.6.1.4.1.674.10893.1.20.1 ASN_OCT 2.4.6";
	public static final String SNMP_GET_NEXT_FOURTH_RESULT = "1.3.6.1.4.1.674.10893.1.20.1 ASN_OCT";
	public static final String EXECUTE_SNMP_GET_RESULT = "CMC DELL";
	public static final String SNMP_VERSION = "2.4.6";

	@Mock
	private SnmpRequestExecutor snmpRequestExecutorMock;

	@InjectMocks
	private SnmpExtension snmpExtension;

	private TelemetryManager telemetryManager;

	/**
	 * Creates a TelemetryManager instance with an SNMP configuration.
	 */
	private void initSnmp() {
		final Monitor hostMonitor = Monitor.builder().type(HOST.getKey()).isEndpoint(true).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(HOST.getKey(), Map.of(HOST_NAME, hostMonitor))
		);

		final SnmpConfiguration snmpConfiguration = SnmpConfiguration
			.builder()
			.community("public".toCharArray())
			.version(SnmpConfiguration.SnmpVersion.V1)
			.hostname(HOST_NAME)
			.port(161)
			.timeout(120L)
			.build();

		telemetryManager =
			TelemetryManager
				.builder()
				.monitors(monitors)
				.hostConfiguration(
					HostConfiguration
						.builder()
						.hostname(HOST_NAME)
						.hostId(HOST_NAME)
						.hostType(DeviceKind.LINUX)
						.configurations(Map.of(SnmpConfiguration.class, snmpConfiguration))
						.build()
				)
				.build();
	}

	@Test
	void testProcessSnmpGetNextCriterionException() throws Exception {
		initSnmp();

		doThrow(new TimeoutException("SNMPGetNext timeout"))
			.when(snmpRequestExecutorMock)
			.executeSNMPGetNext(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetNextCriterion.builder().oid(OID).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - SNMP test failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 " +
					"was unsuccessful due to an exception. " +
					"Message: SNMPGetNext timeout. Connector ID: %s.",
					HOST_NAME,
					CONNECTOR_ID
				)
			)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetNextCriterionNullResult() throws Exception {
		initSnmp();

		doReturn(null).when(snmpRequestExecutorMock).executeSNMPGetNext(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetNextCriterion.builder().oid(OID).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - SNMP test failed - SNMP GetNext " +
					"of 1.3.6.1.4.1.674.10893.1.20 was unsuccessful " +
					"due to a null result.",
					HOST_NAME
				)
			)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetNextCriterionEmptyResult() throws Exception {
		initSnmp();

		doReturn("").when(snmpRequestExecutorMock).executeSNMPGetNext(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetNextCriterion.builder().oid(OID).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - SNMP test failed - SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 " +
					"was unsuccessful due to an empty result.",
					HOST_NAME
				)
			)
			.result("")
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetNextCriterionNotSameSubTreeOID() throws Exception {
		initSnmp();

		final String snmpGetNextResult = "1.3.6.1.4.1.674.99999.1.20.1 ASN_INTEGER 1";

		doReturn(snmpGetNextResult).when(snmpRequestExecutorMock).executeSNMPGetNext(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetNextCriterion.builder().oid(OID).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - SNMP test failed - SNMP " +
					"GetNext of 1.3.6.1.4.1.674.10893.1.20 was successful but the returned OID is" +
					" not under the same tree. Returned OID: 1.3.6.1.4.1.674.99999.1.20.1.",
					HOST_NAME
				)
			)
			.result(snmpGetNextResult)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetNextCriterionSuccessWithNoExpectedResult() throws Exception {
		initSnmp();

		doReturn(SNMP_GET_NEXT_SECOND_RESULT).when(snmpRequestExecutorMock).executeSNMPGetNext(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetNextCriterion.builder().oid(OID).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - Successful SNMP GetNext of " +
					"1.3.6.1.4.1.674.10893.1.20. Returned result: " +
					"1.3.6.1.4.1.674.10893.1.20.1 ASN_INTEGER 1.",
					HOST_NAME
				)
			)
			.result(SNMP_GET_NEXT_SECOND_RESULT)
			.success(true)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetNextCriterionExpectedResultNotMatches() throws Exception {
		initSnmp();

		doReturn(SNMP_GET_NEXT_SECOND_RESULT).when(snmpRequestExecutorMock).executeSNMPGetNext(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetNextCriterion.builder().oid(OID).expectedResult(SNMP_VERSION).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - SNMP test failed - " +
					"SNMP GetNext of 1.3.6.1.4.1.674.10893.1.20 was successful but the value of " +
					"the returned OID did not match with the expected result. Expected value: 2.4.6 - returned value 1.",
					HOST_NAME
				)
			)
			.result(SNMP_GET_NEXT_SECOND_RESULT)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetNextExpectedResultMatches() throws Exception {
		initSnmp();

		doReturn(SNMP_GET_NEXT_THIRD_RESULT).when(snmpRequestExecutorMock).executeSNMPGetNext(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetNextCriterion.builder().oid(OID).expectedResult(SNMP_VERSION).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - Successful SNMP GetNext of " +
					"1.3.6.1.4.1.674.10893.1.20. " +
					"Returned result: 1.3.6.1.4.1.674.10893.1.20.1 ASN_OCT 2.4.6.",
					HOST_NAME
				)
			)
			.result(SNMP_GET_NEXT_THIRD_RESULT)
			.success(true)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetNextCriterionExpectedResultCannotExtract() throws Exception {
		initSnmp();

		doReturn(SNMP_GET_NEXT_FOURTH_RESULT).when(snmpRequestExecutorMock).executeSNMPGetNext(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetNextCriterion.builder().oid(OID).expectedResult(SNMP_VERSION).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - SNMP test failed - SNMP GetNext " +
					"of 1.3.6.1.4.1.674.10893.1.20 was successful " +
					"but the value cannot be extracted. Returned result: 1.3.6.1.4.1.674.10893.1.20.1 ASN_OCT.",
					HOST_NAME
				)
			)
			.result(SNMP_GET_NEXT_FOURTH_RESULT)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetNextReturnsEmptyResult() {
		initSnmp();

		assertNull(
			snmpExtension
				.processCriterion(SnmpGetNextCriterion.builder().oid(OID).build(), CONNECTOR_ID, telemetryManager)
				.getResult()
		);
	}

	@Test
	void testProcessSnmpGetCriterionReturnsEmptyResult() {
		initSnmp();

		assertNull(
			snmpExtension
				.processCriterion(SnmpGetCriterion.builder().oid(OID).build(), CONNECTOR_ID, telemetryManager)
				.getResult()
		);
	}

	@Test
	void testProcessSnmpGetExpectedResultMatches() throws Exception {
		initSnmp();

		doReturn(EXECUTE_SNMP_GET_RESULT).when(snmpRequestExecutorMock).executeSNMPGet(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetCriterion.builder().oid(OID).expectedResult("CMC").build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - Successful SNMP Get of 1.3.6.1.4.1.674.10893.1.20. Returned result: CMC DELL",
					HOST_NAME
				)
			)
			.result(EXECUTE_SNMP_GET_RESULT)
			.success(true)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetExpectedResultNotMatches() throws Exception {
		initSnmp();

		doReturn(EXECUTE_SNMP_GET_RESULT).when(snmpRequestExecutorMock).executeSNMPGet(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetCriterion.builder().oid(OID).expectedResult(SNMP_VERSION).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - SNMP test failed - " +
					"SNMP Get of 1.3.6.1.4.1.674.10893.1.20 was successful but the value of the returned " +
					"OID did not match with the expected result. Expected value: 2.4.6 - returned value CMC DELL.",
					HOST_NAME
				)
			)
			.result(EXECUTE_SNMP_GET_RESULT)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetSuccessWithNoExpectedResult() throws Exception {
		initSnmp();

		doReturn(EXECUTE_SNMP_GET_RESULT).when(snmpRequestExecutorMock).executeSNMPGet(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetCriterion.builder().oid(OID).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - Successful SNMP Get of 1.3.6.1.4.1.674.10893.1.20. Returned result: CMC DELL.",
					HOST_NAME
				)
			)
			.result(EXECUTE_SNMP_GET_RESULT)
			.success(true)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetEmptyResult() throws Exception {
		initSnmp();

		doReturn("").when(snmpRequestExecutorMock).executeSNMPGet(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetCriterion.builder().oid(OID).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - SNMP test failed - SNMP Get " +
					"of 1.3.6.1.4.1.674.10893.1.20 was unsuccessful due to an empty result.",
					HOST_NAME
				)
			)
			.result("")
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetNullResult() throws Exception {
		initSnmp();

		doReturn(null).when(snmpRequestExecutorMock).executeSNMPGet(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetCriterion.builder().oid(OID).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - SNMP test failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 was " +
					"unsuccessful due to a null result",
					HOST_NAME
				)
			)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessSnmpGetException() throws Exception {
		initSnmp();

		doThrow(new TimeoutException("SNMPGet timeout"))
			.when(snmpRequestExecutorMock)
			.executeSNMPGet(any(), any(), eq(false));
		final CriterionTestResult actual = snmpExtension.processCriterion(
			SnmpGetCriterion.builder().oid(OID).build(),
			CONNECTOR_ID,
			telemetryManager
		);
		final CriterionTestResult expected = CriterionTestResult
			.builder()
			.message(
				String.format(
					"Hostname %s - SNMP test failed - SNMP Get of 1.3.6.1.4.1.674.10893.1.20 was unsuccessful " +
					"due to an exception. " +
					"Message: SNMPGet timeout. Connector ID: %s.",
					HOST_NAME,
					CONNECTOR_ID
				)
			)
			.build();
		assertEquals(expected, actual);
	}

	@Test
	void testCheckSnmpUpHealth() throws InterruptedException, ExecutionException, TimeoutException {
		// Create a telemetry manager using an SNMP HostConfiguration.
		initSnmp();

		// Mock SNMP protocol health check response
		doReturn("success")
			.when(snmpRequestExecutorMock)
			.executeSNMPGetNext(eq(SnmpExtension.SNMP_OID), any(SnmpConfiguration.class), anyBoolean());

		// Start the SNMP protocol check
		Optional<Boolean> result = snmpExtension.checkProtocol(telemetryManager);

		assertTrue(result.get());
	}

	@Test
	void testCheckSnmpDownHealth() throws InterruptedException, ExecutionException, TimeoutException {
		// Create a telemetry manager using an SNMP HostConfiguration.
		initSnmp();

		// Mock SNMP protocol health check response
		doReturn(null)
			.when(snmpRequestExecutorMock)
			.executeSNMPGetNext(eq(SnmpExtension.SNMP_OID), any(SnmpConfiguration.class), anyBoolean());

		// Start the SNMP protocol check
		Optional<Boolean> result = snmpExtension.checkProtocol(telemetryManager);

		assertFalse(result.get());
	}

	@Test
	void testIsValidConfiguration() {
		assertTrue(snmpExtension.isValidConfiguration(SnmpConfiguration.builder().build()));
		assertFalse(
			snmpExtension.isValidConfiguration(
				new IConfiguration() {
					@Override
					public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {}

					@Override
					public String getHostname() {
						return null;
					}

					@Override
					public void setHostname(String hostname) {}
				}
			)
		);
	}

	@Test
	void testGetSupportedSources() {
		assertFalse(snmpExtension.getSupportedSources().isEmpty());
	}

	@Test
	void testGetSupportedCriteria() {
		assertFalse(snmpExtension.getSupportedCriteria().isEmpty());
	}

	@Test
	void testGetConfigurationToSourceMapping() {
		assertFalse(snmpExtension.getConfigurationToSourceMapping().isEmpty());
	}

	@Test
	void testIsSupportedConfigurationType() {
		assertTrue(snmpExtension.isSupportedConfigurationType("snmp"));
		assertFalse(snmpExtension.isSupportedConfigurationType("http"));
	}

	@Test
	void testBuildConfiguration() throws InvalidConfigurationException {
		final ObjectNode configuration = JsonNodeFactory.instance.objectNode();
		configuration.set("version", new TextNode("1"));
		configuration.set("community", new TextNode("public"));
		configuration.set("port", new IntNode(161));
		configuration.set("timeout", new TextNode("2m"));

		assertEquals(
			SnmpConfiguration
				.builder()
				.community("public".toCharArray())
				.port(161)
				.timeout(120L)
				.version(SnmpVersion.V1)
				.build(),
			snmpExtension.buildConfiguration("snmp", configuration, value -> value)
		);
		assertEquals(
			SnmpConfiguration
				.builder()
				.community("public".toCharArray())
				.port(161)
				.timeout(120L)
				.version(SnmpVersion.V1)
				.build(),
			snmpExtension.buildConfiguration("snmp", configuration, null)
		);
	}
}
