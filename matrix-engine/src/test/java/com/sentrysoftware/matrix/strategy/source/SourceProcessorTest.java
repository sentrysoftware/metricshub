package com.sentrysoftware.matrix.strategy.source;

import static com.sentrysoftware.matrix.constants.Constants.ECS1_01;
import static com.sentrysoftware.matrix.constants.Constants.EXPECTED_SNMP_TABLE_DATA;
import static com.sentrysoftware.matrix.constants.Constants.OID;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_SELECTED_COLUMNS;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_SELECTED_COLUMNS_LIST;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_WRONG_COLUMNS;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_WRONG_COLUMNS_LIST;
import static com.sentrysoftware.matrix.constants.Constants.URL;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpTableSource;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class SourceProcessorTest {

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutorMock;

	@Test
	void testProcessHttpSourceOK() {
		final HttpConfiguration httpConfiguration = HttpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(HttpConfiguration.class, httpConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.build();

		doReturn(ECS1_01).when(matsyaClientsExecutorMock).executeHttp(any(), eq(true));
		final SourceTable actual = sourceProcessor.process(HttpSource.builder()
			.url(URL)
			.method(HttpMethod.GET)
			.build());

		final SourceTable expected = SourceTable.builder().rawData(ECS1_01).build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessHttpSourceNoHttpConfiguration() {
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder().telemetryManager(telemetryManager).build();

		assertEquals(SourceTable.empty(), sourceProcessor.process(HttpSource.builder()
			.url("my/url")
			.method(HttpMethod.GET)
			.build()));
	}

	@Test
	void testVisitSnmpGetSource() throws Exception {
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(HostConfiguration.builder().build()).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.build();

		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));
		assertEquals(SourceTable.empty(), sourceProcessor.process(new SnmpGetSource()));

		// no snmp protocol
		HostConfiguration hostConfigurationNoProtocol = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.build();
		telemetryManager.setHostConfiguration(hostConfigurationNoProtocol);
		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));

		// classic case
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		telemetryManager.setHostConfiguration(hostConfiguration);
		doReturn(ECS1_01).when(matsyaClientsExecutorMock).executeSNMPGet(any(), any(), any(), eq(true));
		final SourceTable actual = sourceProcessor.process(SnmpGetSource.builder().oid(OID).build());
		final SourceTable expected = SourceTable.builder().table(Arrays.asList(Arrays.asList(ECS1_01))).build();
		assertEquals(expected, actual);

		// test that the exception is correctly caught and still returns a result
		when(matsyaClientsExecutorMock.executeSNMPGet(any(), any(), any(), eq(true))).thenThrow(TimeoutException.class);
		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));
	}

	@Test
	void testVisitSnmpGetTableExpectedResultNotMatches() throws Exception {
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(HostConfiguration.builder().build()).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.build();
		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpTableSource.builder().oid(OID).selectColumns(SNMP_WRONG_COLUMNS).build()));
		assertEquals(SourceTable.empty(), sourceProcessor.process(new SnmpTableSource()));

		// no snmp protocol
		HostConfiguration hostConfigurationNoProtocol = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.build();
		telemetryManager.setHostConfiguration(hostConfigurationNoProtocol);
		assertEquals(SourceTable.empty(), sourceProcessor.process(SnmpGetSource.builder().oid(OID).build()));

		// no matches
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		telemetryManager.setHostConfiguration(hostConfiguration);
		doReturn(new ArrayList<>()).when(matsyaClientsExecutorMock).executeSNMPTable(any(), any(), any(), any(), eq(true));
		final SourceTable actual = sourceProcessor.process(SnmpTableSource.builder().oid(OID).selectColumns(SNMP_WRONG_COLUMNS).build());
		final SourceTable expected = SourceTable.builder().table(new ArrayList<>()).headers(SNMP_WRONG_COLUMNS_LIST).build();
		assertEquals(expected, actual);
	}

	@Test
	void testVisitSbmpGetTableExpectedResultMatches() throws Exception {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.build();
		doReturn(EXPECTED_SNMP_TABLE_DATA).when(matsyaClientsExecutorMock).executeSNMPTable(any(), any(), any(), any(), eq(true));
		final SourceTable actual = sourceProcessor.process(SnmpTableSource.builder().oid(OID).selectColumns(SNMP_SELECTED_COLUMNS).build());
		final SourceTable expected = SourceTable.builder().table(EXPECTED_SNMP_TABLE_DATA)
			.headers(SNMP_SELECTED_COLUMNS_LIST).build();
		assertEquals(expected, actual);
	}
}
