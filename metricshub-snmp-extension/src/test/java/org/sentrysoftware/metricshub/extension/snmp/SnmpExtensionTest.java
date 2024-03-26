package org.sentrysoftware.metricshub.extension.snmp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType.HOST;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetNextCriterion;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

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

	private void initSnmp() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration
			.builder()
			.community("public")
			.version(SnmpConfiguration.SnmpVersion.V1)
			.port(161)
			.timeout(120L)
			.build();

		telemetryManager =
			TelemetryManager
				.builder()
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
			.executeSNMPGetNext(any(), any(), any(), eq(false));
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

		doReturn(null).when(snmpRequestExecutorMock).executeSNMPGetNext(any(), any(), any(), eq(false));
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

		doReturn("").when(snmpRequestExecutorMock).executeSNMPGetNext(any(), any(), any(), eq(false));
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

		doReturn(snmpGetNextResult).when(snmpRequestExecutorMock).executeSNMPGetNext(any(), any(), any(), eq(false));
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

		doReturn(SNMP_GET_NEXT_SECOND_RESULT)
			.when(snmpRequestExecutorMock)
			.executeSNMPGetNext(any(), any(), any(), eq(false));
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

		doReturn(SNMP_GET_NEXT_SECOND_RESULT)
			.when(snmpRequestExecutorMock)
			.executeSNMPGetNext(any(), any(), any(), eq(false));
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

		doReturn(SNMP_GET_NEXT_THIRD_RESULT)
			.when(snmpRequestExecutorMock)
			.executeSNMPGetNext(any(), any(), any(), eq(false));
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

		doReturn(SNMP_GET_NEXT_FOURTH_RESULT)
			.when(snmpRequestExecutorMock)
			.executeSNMPGetNext(any(), any(), any(), eq(false));
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

		doReturn(EXECUTE_SNMP_GET_RESULT).when(snmpRequestExecutorMock).executeSNMPGet(any(), any(), any(), eq(false));
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

		doReturn(EXECUTE_SNMP_GET_RESULT).when(snmpRequestExecutorMock).executeSNMPGet(any(), any(), any(), eq(false));
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

		doReturn(EXECUTE_SNMP_GET_RESULT).when(snmpRequestExecutorMock).executeSNMPGet(any(), any(), any(), eq(false));
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

		doReturn("").when(snmpRequestExecutorMock).executeSNMPGet(any(), any(), any(), eq(false));
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

		doReturn(null).when(snmpRequestExecutorMock).executeSNMPGet(any(), any(), any(), eq(false));
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
			.executeSNMPGet(any(), any(), any(), eq(false));
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

	/**
	 * Creates and returns a TelemetryManager instance with an SNMP configuration.
	 *
	 * @return A TelemetryManager instance configured with an SNMP configuration.
	 */
	private TelemetryManager createTelemetryManagerWithSnmpConfig() {
		final Monitor hostMonitor = Monitor.builder().type(HOST.getKey()).isEndpoint(true).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(HOST.getKey(), Map.of(HOST_NAME, hostMonitor))
		);
		// Create a telemetry manager
		return TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOST_NAME)
					.hostname(HOST_NAME)
					.configurations(Map.of(SnmpConfiguration.class, SnmpConfiguration.builder().build()))
					.build()
			)
			.build();
	}

	@Test
	void testCheckSnmpUpHealth() throws InterruptedException, ExecutionException, TimeoutException {
		// Create a telemetry manager using an SNMP HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSnmpConfig();

		// The time at which the collect of the protocol up metric is triggered.
		final long collectTime = System.currentTimeMillis();

		// Mock SNMP protocol health check response
		doReturn("success")
			.when(snmpRequestExecutorMock)
			.executeSNMPGetNext(eq(SnmpExtension.SNMP_OID), any(SnmpConfiguration.class), anyString(), anyBoolean());

		// Start the SNMP protocol check
		snmpExtension.checkProtocol(telemetryManager, collectTime);

		assertEquals(
			SnmpExtension.UP,
			telemetryManager.getEndpointHostMonitor().getMetric(SnmpExtension.SNMP_UP_METRIC).getValue()
		);
	}

	@Test
	void testCheckSnmpDownHealth() throws InterruptedException, ExecutionException, TimeoutException {
		// Create a telemetry manager using an SNMP HostConfiguration.
		final TelemetryManager telemetryManager = createTelemetryManagerWithSnmpConfig();

		// The time at which the collect of the protocol up metric is triggered.
		final long collectTime = System.currentTimeMillis();

		// Mock SNMP protocol health check response
		doReturn(null)
			.when(snmpRequestExecutorMock)
			.executeSNMPGetNext(eq(SnmpExtension.SNMP_OID), any(SnmpConfiguration.class), anyString(), anyBoolean());

		// Start the SNMP protocol check
		snmpExtension.checkProtocol(telemetryManager, collectTime);

		assertEquals(
			SnmpExtension.DOWN,
			telemetryManager.getEndpointHostMonitor().getMetric(SnmpExtension.SNMP_UP_METRIC).getValue()
		);
	}
}
