package com.sentrysoftware.metricshub.engine.strategy.utils;

import static com.sentrysoftware.metricshub.engine.constants.Constants.EXPECTED_SNMP_TABLE_DATA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.strategy.source.ISourceProcessor;
import com.sentrysoftware.metricshub.engine.strategy.source.SourceProcessor;
import com.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.Test;

class ForceSerializationHelperTest {

	private static final String HOST_NAME = "host.test.force.serialization";
	private static final String SELECT_COLUMNS = "ID,1,3";
	private static final SourceTable EXPECTED_SOURCE_TABLE = SourceTable
		.builder()
		.table(EXPECTED_SNMP_TABLE_DATA)
		.headers(Arrays.asList(SELECT_COLUMNS.split(MetricsHubConstants.COMMA)))
		.build();
	private static final String DESCRIPTION = "source";
	private static final String CONNECTOR_NAME = "connector";

	@Test
	void testForceSerializationNullArguments() {
		final SourceTable emptySourceTable = SourceTable.empty();
		final TelemetryManager telemetryManager = new TelemetryManager();
		final Source snmpTableSource = SnmpTableSource.builder().oid("1.2.3.4").selectColumns(SELECT_COLUMNS).build();

		assertThrows(
			IllegalArgumentException.class,
			() ->
				ForceSerializationHelper.forceSerialization(
					null,
					telemetryManager,
					CONNECTOR_NAME,
					snmpTableSource,
					DESCRIPTION,
					emptySourceTable
				)
		);

		assertThrows(
			IllegalArgumentException.class,
			() ->
				ForceSerializationHelper.forceSerialization(
					() -> emptySourceTable,
					null,
					CONNECTOR_NAME,
					snmpTableSource,
					DESCRIPTION,
					emptySourceTable
				)
		);

		assertThrows(
			IllegalArgumentException.class,
			() ->
				ForceSerializationHelper.forceSerialization(
					() -> emptySourceTable,
					telemetryManager,
					null,
					snmpTableSource,
					DESCRIPTION,
					emptySourceTable
				)
		);

		assertThrows(
			IllegalArgumentException.class,
			() ->
				ForceSerializationHelper.forceSerialization(
					() -> emptySourceTable,
					telemetryManager,
					CONNECTOR_NAME,
					snmpTableSource,
					null,
					emptySourceTable
				)
		);

		assertThrows(
			IllegalArgumentException.class,
			() ->
				ForceSerializationHelper.forceSerialization(
					() -> emptySourceTable,
					telemetryManager,
					CONNECTOR_NAME,
					snmpTableSource,
					DESCRIPTION,
					null
				)
		);
	}

	@Test
	void testForceSerializationInterruptedException() throws InterruptedException {
		final ReentrantLock spyLock = spy(ReentrantLock.class);
		final SourceTable emptySourceTable = SourceTable.empty();
		final TelemetryManager telemetryManager = new TelemetryManager();
		final Source snmpTableSource = SnmpTableSource.builder().oid("1.2.3.4").selectColumns(SELECT_COLUMNS).build();

		telemetryManager.setHostConfiguration(HostConfiguration.builder().hostname(HOST_NAME).build());

		telemetryManager.getHostProperties().getConnectorNamespace(CONNECTOR_NAME).setForceSerializationLock(spyLock);

		doThrow(InterruptedException.class).when(spyLock).tryLock(anyLong(), any(TimeUnit.class));

		assertEquals(
			SourceTable.empty(),
			ForceSerializationHelper.forceSerialization(
				() -> SourceTable.builder().table(List.of(List.of("a", "b", "c"))),
				telemetryManager,
				CONNECTOR_NAME,
				snmpTableSource,
				DESCRIPTION,
				emptySourceTable
			)
		);
	}

	@Test
	void testForceSerializationCouldNotAcquireLock() throws InterruptedException {
		final ReentrantLock spyLock = spy(ReentrantLock.class);
		final SourceTable emptySourceTable = SourceTable.empty();
		final TelemetryManager telemetryManager = new TelemetryManager();
		final Source snmpTableSource = SnmpTableSource.builder().oid("1.2.3.4").selectColumns(SELECT_COLUMNS).build();

		telemetryManager.setHostConfiguration(HostConfiguration.builder().hostname(HOST_NAME).build());

		telemetryManager.getHostProperties().getConnectorNamespace(CONNECTOR_NAME).setForceSerializationLock(spyLock);

		doReturn(false).when(spyLock).tryLock(anyLong(), any(TimeUnit.class));

		assertEquals(
			SourceTable.empty(),
			ForceSerializationHelper.forceSerialization(
				() -> SourceTable.builder().table(List.of(List.of("a", "b", "c"))),
				telemetryManager,
				CONNECTOR_NAME,
				snmpTableSource,
				DESCRIPTION,
				emptySourceTable
			)
		);
	}

	@Test
	void testForceSerializationLockAcquired() throws Exception {
		final Source snmpTableSource = SnmpTableSource
			.builder()
			.oid("1.2.3.4")
			.selectColumns(SELECT_COLUMNS)
			.forceSerialization(true)
			.build();

		final MatsyaClientsExecutor matsyaClientsExecutor = spy(MatsyaClientsExecutor.class);
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(HOST_NAME)
					.configurations(Map.of(SnmpConfiguration.class, SnmpConfiguration.builder().build()))
					.build()
			)
			.build();

		telemetryManager.getHostProperties().getConnectorNamespace(CONNECTOR_NAME);

		doReturn(EXPECTED_SNMP_TABLE_DATA)
			.when(matsyaClientsExecutor)
			.executeSNMPTable(any(), any(), any(), any(), anyBoolean());

		final ISourceProcessor processor = SourceProcessor
			.builder()
			.connectorName(CONNECTOR_NAME)
			.matsyaClientsExecutor(matsyaClientsExecutor)
			.telemetryManager(telemetryManager)
			.build();

		assertEquals(
			EXPECTED_SOURCE_TABLE,
			ForceSerializationHelper.forceSerialization(
				() -> snmpTableSource.accept(processor),
				telemetryManager,
				CONNECTOR_NAME,
				snmpTableSource,
				DESCRIPTION,
				SourceTable.empty()
			)
		);
	}

	@Test
	void testForceSerializationMultiThreads() throws Exception {
		final Source snmpTableSource = SnmpTableSource
			.builder()
			.oid("1.2.3.4")
			.selectColumns(SELECT_COLUMNS)
			.forceSerialization(true)
			.build();

		final MatsyaClientsExecutor matsyaClientsExecutor = spy(MatsyaClientsExecutor.class);
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostname(HOST_NAME)
					.configurations(Map.of(SnmpConfiguration.class, SnmpConfiguration.builder().build()))
					.build()
			)
			.build();

		telemetryManager.getHostProperties().getConnectorNamespace(CONNECTOR_NAME);

		doReturn(EXPECTED_SNMP_TABLE_DATA)
			.when(matsyaClientsExecutor)
			.executeSNMPTable(any(), any(), any(), any(), anyBoolean());

		final ISourceProcessor processor = SourceProcessor
			.builder()
			.connectorName(CONNECTOR_NAME)
			.matsyaClientsExecutor(matsyaClientsExecutor)
			.telemetryManager(telemetryManager)
			.build();

		final ExecutorService threadsPool = Executors.newFixedThreadPool(2);

		final Callable<SourceTable> callable1 = () ->
			ForceSerializationHelper.forceSerialization(
				() -> snmpTableSource.accept(processor),
				telemetryManager,
				CONNECTOR_NAME,
				snmpTableSource,
				DESCRIPTION,
				SourceTable.empty()
			);

		final Callable<SourceTable> callable2 = () ->
			ForceSerializationHelper.forceSerialization(
				() -> snmpTableSource.accept(processor),
				telemetryManager,
				CONNECTOR_NAME,
				snmpTableSource,
				DESCRIPTION,
				SourceTable.empty()
			);

		// This only checks the behavior for two parallel threads to validate there is no crash.

		final Future<SourceTable> future1 = threadsPool.submit(callable1);
		final Future<SourceTable> future2 = threadsPool.submit(callable2);

		final SourceTable result2 = future2.get(120, TimeUnit.SECONDS);
		final SourceTable result1 = future1.get(120, TimeUnit.SECONDS);

		assertEquals(EXPECTED_SOURCE_TABLE, result1);
		assertEquals(EXPECTED_SOURCE_TABLE, result2);
	}
}
