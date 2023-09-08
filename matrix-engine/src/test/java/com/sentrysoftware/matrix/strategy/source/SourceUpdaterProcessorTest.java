package com.sentrysoftware.matrix.strategy.source;

import static com.sentrysoftware.matrix.constants.Constants.ENCLOSURE_COLLECT_SOURCE_1;
import static com.sentrysoftware.matrix.constants.Constants.EXPECTED_RESULT;
import static com.sentrysoftware.matrix.constants.Constants.EXPECTED_SNMP_TABLE_DATA;
import static com.sentrysoftware.matrix.constants.Constants.EXPECTED_VAL_1;
import static com.sentrysoftware.matrix.constants.Constants.EXPECTED_VAL_1_AND_2;
import static com.sentrysoftware.matrix.constants.Constants.EXPECTED_VAL_1_AND_2_ARRAY;
import static com.sentrysoftware.matrix.constants.Constants.EXPECTED_VAL_2;
import static com.sentrysoftware.matrix.constants.Constants.LOCALHOST;
import static com.sentrysoftware.matrix.constants.Constants.MONITOR_ID_ATTRIBUTE_VALUE;
import static com.sentrysoftware.matrix.constants.Constants.MY_CONNECTOR_1_NAME;
import static com.sentrysoftware.matrix.constants.Constants.OID;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_SELECTED_COLUMNS;
import static com.sentrysoftware.matrix.constants.Constants.SNMP_SELECTED_COLUMNS_LIST;
import static com.sentrysoftware.matrix.constants.Constants.TAB1_REF;
import static com.sentrysoftware.matrix.constants.Constants.URL;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_A1;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_B1;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_C1;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL1;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL2;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.common.EntryConcatMethod;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.CopySource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableUnionSource;
import com.sentrysoftware.matrix.telemetry.HostProperties;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class SourceUpdaterProcessorTest {

	@Mock
	private ISourceProcessor sourceProcessor;

	@Test
	void testProcessHTTPSource() {
		final HttpConfiguration httpConfiguration = HttpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(LOCALHOST)
			.hostId(LOCALHOST)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(HttpConfiguration.class, httpConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder()
			.hostConfiguration(hostConfiguration)
			.build();

		doReturn(SourceTable.empty()).when(sourceProcessor).process(any(HttpSource.class));
		assertEquals(SourceTable.empty(),
			new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE)
				.process(HttpSource.builder().url(URL).method(HttpMethod.GET).build()));

		final HttpSource httpSource = HttpSource.builder().url(URL).build();
		httpSource.setExecuteForEachEntryOf(ExecuteForEachEntryOf.builder().source(ENCLOSURE_COLLECT_SOURCE_1).build());

		final SourceTable sourceTable = SourceTable.builder()
			.table(Arrays.asList(
				Arrays.asList(VALUE_VAL1, VALUE_VAL2, VALUE_VAL3),
				Arrays.asList(VALUE_A1, VALUE_B1, VALUE_C1)))
			.build();

		final HostProperties hostProperties = HostProperties
			.builder()
			.build();

		hostProperties
			.getConnectorNamespace(MY_CONNECTOR_1_NAME)
			.addSourceTable(ENCLOSURE_COLLECT_SOURCE_1, sourceTable);

		telemetryManager.setHostProperties(hostProperties);

		final SourceTable expected1 = SourceTable.builder().rawData(EXPECTED_VAL_1).build();
		final SourceTable expected2 = SourceTable.builder().rawData(EXPECTED_VAL_2).build();
		doReturn(expected1, expected2).when(sourceProcessor).process(any(HttpSource.class));
		SourceTable result = new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE).process(httpSource);
		assertEquals(EXPECTED_VAL_1_AND_2, result.getRawData());

		httpSource.setExecuteForEachEntryOf(ExecuteForEachEntryOf.builder().source(ENCLOSURE_COLLECT_SOURCE_1).concatMethod(EntryConcatMethod.LIST).build());
		doReturn(expected1, expected2).when(sourceProcessor).process(any(HttpSource.class));
		result = new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE).process(httpSource);
		assertEquals(EXPECTED_VAL_1_AND_2, result.getRawData());

		httpSource.setExecuteForEachEntryOf(ExecuteForEachEntryOf.builder().source(ENCLOSURE_COLLECT_SOURCE_1).concatMethod(EntryConcatMethod.JSON_ARRAY).build());
		doReturn(expected1, expected2).when(sourceProcessor).process(any(HttpSource.class));
		result = new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE).process(httpSource);
		assertEquals(EXPECTED_VAL_1_AND_2_ARRAY, result.getRawData());

		httpSource.setExecuteForEachEntryOf(ExecuteForEachEntryOf.builder().source(ENCLOSURE_COLLECT_SOURCE_1).concatMethod(EntryConcatMethod.JSON_ARRAY_EXTENDED).build());
		doReturn(expected1, expected2).when(sourceProcessor).process(any(HttpSource.class));
		result = new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE).process(httpSource);

		assertEquals(EXPECTED_RESULT, result.getRawData());
	}

	@Test
	void testProcessSNMPGetSource() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(LOCALHOST)
			.hostId(LOCALHOST)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder()
			.hostConfiguration(hostConfiguration)
			.build();

		doReturn(SourceTable.empty()).when(sourceProcessor).process(any(SnmpGetSource.class));
		assertEquals(SourceTable.empty(),
			new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE)
				.process(SnmpGetSource.builder().oid(OID).build()));

		final SourceTable expected1 = SourceTable.builder().rawData(EXPECTED_VAL_1).build();

		doReturn(expected1).when(sourceProcessor).process(any(SnmpGetSource.class));
		assertEquals(expected1,
			new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE)
				.process(SnmpGetSource.builder().oid(OID).build()));
	}

	@Test
	void testProcessSNMPGetTableSource() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(LOCALHOST)
			.hostId(LOCALHOST)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder()
			.hostConfiguration(hostConfiguration)
			.build();
		doReturn(SourceTable.empty()).when(sourceProcessor).process(any(SnmpTableSource.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE)
			.process(SnmpTableSource.builder().oid(OID).selectColumns(SNMP_SELECTED_COLUMNS).build()));

		SourceTable expected = SourceTable.builder().table(EXPECTED_SNMP_TABLE_DATA)
			.headers(SNMP_SELECTED_COLUMNS_LIST).build();
		doReturn(expected).when(sourceProcessor).process(any(SnmpTableSource.class));
		assertEquals(expected, new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE)
			.process(SnmpTableSource.builder().oid(OID).selectColumns(SNMP_SELECTED_COLUMNS).build()));
	}

	@Test
	void testProcessTableJoinSource() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(LOCALHOST)
			.hostId(LOCALHOST)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder()
			.hostConfiguration(hostConfiguration)
			.build();
		doReturn(SourceTable.empty()).when(sourceProcessor).process(any(TableJoinSource.class));
		assertEquals(SourceTable.empty(),
			new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE)
				.process(TableJoinSource.builder().build()));
	}

	@Test
	void testProcessTableUnionSource() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(LOCALHOST)
			.hostId(LOCALHOST)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder()
			.hostConfiguration(hostConfiguration)
			.build();
		doReturn(SourceTable.empty()).when(sourceProcessor).process(any(TableUnionSource.class));
		assertEquals(SourceTable.empty(),
			new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE)
				.process(TableUnionSource.builder().tables(new ArrayList<>()).build()));
	}

	@Test
	void testProcessCopySource() {
		final SnmpConfiguration snmpConfiguration = SnmpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(LOCALHOST)
			.hostId(LOCALHOST)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(SnmpConfiguration.class, snmpConfiguration))
			.build();
		final TelemetryManager telemetryManager = TelemetryManager.builder()
			.hostConfiguration(hostConfiguration)
			.build();
		doReturn(SourceTable.empty()).when(sourceProcessor).process(any(CopySource.class));
		assertEquals(SourceTable.empty(),
			new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE)
				.process(CopySource.builder().from(TAB1_REF).build()));

		CopySource copySource = CopySource.builder().from(TAB1_REF).build();
		final List<List<String>> result = Collections.singletonList(Collections.singletonList(VALUE_VAL1));
		final SourceTable expected = SourceTable.builder().table(result).build();

		final SnmpGetSource snmpGetSource = SnmpGetSource.builder().oid(OID).build();

		final List<List<String>> resultSnmp = Collections.singletonList(Collections.singletonList(VALUE_VAL1));
		final SourceTable expectedSnmp = SourceTable.builder().table(resultSnmp).build();
		doReturn(expectedSnmp).when(sourceProcessor).process(any(SnmpGetSource.class));
		assertEquals(expectedSnmp,
			new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE)
				.process(snmpGetSource));

		doReturn(expected).when(sourceProcessor).process(any(CopySource.class));
		assertEquals(expected,
			new SourceUpdaterProcessor(sourceProcessor, telemetryManager, MY_CONNECTOR_1_NAME, MONITOR_ID_ATTRIBUTE_VALUE)
				.process(copySource));
	}
}
