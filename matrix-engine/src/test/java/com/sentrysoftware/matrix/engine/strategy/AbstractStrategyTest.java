package com.sentrysoftware.matrix.engine.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SnmpGetNext;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol.SnmpVersion;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.source.SourceVisitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;

@ExtendWith(MockitoExtension.class)
class AbstractStrategyTest {

	private static final String ENCLOSURE_DISCOVERY_SOURCE_1_KEY = "Enclosure.Discovery.Source(1)";

	private static final List<String> SNMP_TABLE_SELECTED_COLUMNS = List.of("1", "2", "3", "4");

	private static final String MY_CONNECTOR_NAME = "myConnector";

	private static final List<List<String>> EXPECTED_SNMP_TABLE_DATA = Arrays.asList(Arrays.asList("1", "PowerEdge R630", "FSJR3N2", "34377965102"));

	private static final SourceTable EXPECTED_SOURCE_TABLE = SourceTable
				.builder()
				.table(EXPECTED_SNMP_TABLE_DATA)
				.headers(SNMP_TABLE_SELECTED_COLUMNS)
				.build();

	private static final String HOSTNAME = "hostname";

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private ConnectorStore store;

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@InjectMocks
	private DiscoveryOperation discoveryOperation;

	@InjectMocks
	private DetectionOperation detectionOperation;

	private static EngineConfiguration engineConfiguration;
	private static IHostMonitoring hostMonitoring;
	private static Connector connector;
	private static SnmpGetTableSource snmpGetTableSource;
	private static SnmpGetNext snmpGetNext;

	@BeforeAll
	static void setUp() {
		final SnmpProtocol protocol = SnmpProtocol.builder().community("public").version(SnmpVersion.V1).port(161)
				.timeout(120L).build();
		engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname(HOSTNAME).id(HOSTNAME).type(HostType.LINUX).build())
				.protocolConfigurations(Map.of(SnmpProtocol.class, protocol)).build();

		connector = Connector.builder().compiledFilename(MY_CONNECTOR_NAME).build();

		snmpGetTableSource = SnmpGetTableSource
					.builder()
					.oid("1.2.3.4.5.6")
					.snmpTableSelectColumns(SNMP_TABLE_SELECTED_COLUMNS)
					.forceSerialization(true)
					.key(ENCLOSURE_DISCOVERY_SOURCE_1_KEY)
					.build();

		snmpGetNext = SnmpGetNext.builder().oid("1.3.6.1.4.1.674.10893.1.20").forceSerialization(true).build();
	}

	@BeforeEach
	void beforeEach() {
		hostMonitoring = new HostMonitoring();
		lenient().doReturn(engineConfiguration).when(strategyConfig).getEngineConfiguration();
		lenient().doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
	}

	@Test
	void testForceSerializationNullArguments() {

		final SourceTable emptySourceTable = SourceTable.empty();

		assertThrows(IllegalArgumentException.class,
				() -> discoveryOperation.forceSerialization(null, connector, snmpGetTableSource, "source", emptySourceTable));

		assertThrows(IllegalArgumentException.class,
				() -> discoveryOperation.forceSerialization(() -> emptySourceTable, null, snmpGetTableSource, "source", emptySourceTable));

		assertThrows(IllegalArgumentException.class,
				() -> discoveryOperation.forceSerialization(() -> emptySourceTable, connector, snmpGetTableSource, null, emptySourceTable));

		assertThrows(IllegalArgumentException.class,
				() -> discoveryOperation.forceSerialization(() -> emptySourceTable, connector, snmpGetTableSource, "source", null));
	}

	@Test
	void testForceSerializationInterruptedException() throws InterruptedException {
		final ReentrantLock spyLock = spy(ReentrantLock.class);
		hostMonitoring.getConnectorNamespace(connector).setForceSerializationLock(spyLock);

		doThrow(InterruptedException.class).when(spyLock).tryLock(anyLong(), any(TimeUnit.class));

		assertEquals(SourceTable.empty(), discoveryOperation.forceSerialization(
				() -> snmpGetTableSource.accept(new SourceVisitor(strategyConfig, matsyaClientsExecutor, connector)),
				connector,
				snmpGetTableSource,
				"source",
				SourceTable.empty()));
	}

	@Test
	void testForceSerializationCouldNotAcquireLock() throws InterruptedException {
		final ReentrantLock spyLock = spy(ReentrantLock.class);
		hostMonitoring.getConnectorNamespace(connector).setForceSerializationLock(spyLock);

		doReturn(false).when(spyLock).tryLock(anyLong(), any(TimeUnit.class));

		assertEquals(SourceTable.empty(), discoveryOperation.forceSerialization(
				() -> snmpGetTableSource.accept(new SourceVisitor(strategyConfig, matsyaClientsExecutor, connector)),
				connector,
				snmpGetTableSource,
				"source",
				SourceTable.empty()));
	}

	@Test
	void testForceSerializationLockAcquired() throws Exception {

		doReturn(EXPECTED_SNMP_TABLE_DATA).when(matsyaClientsExecutor).executeSNMPTable(any(), any(), any(), any(), anyBoolean());

		assertEquals(EXPECTED_SOURCE_TABLE, 
					discoveryOperation.forceSerialization(
						() -> snmpGetTableSource.accept(new SourceVisitor(strategyConfig, matsyaClientsExecutor, connector)),
						connector,
						snmpGetTableSource,
						"source",
						SourceTable.empty()));
	}

	@Test
	void testForceSerializationMultiThreads() throws Exception {

		doReturn(EXPECTED_SNMP_TABLE_DATA).when(matsyaClientsExecutor).executeSNMPTable(any(), any(), any(), any(), anyBoolean());

		final ExecutorService threadsPool = Executors.newFixedThreadPool(2);

		final Callable<SourceTable> callable1 = () -> discoveryOperation.forceSerialization(
				() -> snmpGetTableSource.accept(new SourceVisitor(strategyConfig, matsyaClientsExecutor, connector)),
				connector,
				snmpGetTableSource,
				"source",
				SourceTable.empty());

		final Callable<SourceTable> callable2 = () -> discoveryOperation.forceSerialization(
				() -> snmpGetTableSource.accept(new SourceVisitor(strategyConfig, matsyaClientsExecutor, connector)),
				connector,
				snmpGetTableSource,
				"source",
				SourceTable.empty());

		// This only checks the behavior for two parallel threads to validate there is no crash.

		final Future<SourceTable> future1 = threadsPool.submit(callable1);
		final Future<SourceTable> future2 = threadsPool.submit(callable2);

		final SourceTable result2 = future2.get(120, TimeUnit.SECONDS);
		final SourceTable result1 = future1.get(120, TimeUnit.SECONDS);

		assertEquals(EXPECTED_SOURCE_TABLE, result1);
		assertEquals(EXPECTED_SOURCE_TABLE, result2);
	}

	@Test
	void testProcessCriterionForceSerialization() throws Exception {
		doReturn("1.3.6.1.4.1.674.10893.1.20.1 ASN_OCT 2.4.6").when(matsyaClientsExecutor).executeSNMPGetNext(
				anyString(),
				any(SnmpProtocol.class),
				anyString(),
				anyBoolean());

		// The criteria are already tested in the DetectionOperationTest, check
		// everything is OK using a force serialization criterion
		assertTrue(detectionOperation.processCriterion(snmpGetNext, connector).isSuccess());

	}

	@Test
	void testProcessSourceAndComputesForceSerialization() throws Exception {

		doReturn(EXPECTED_SNMP_TABLE_DATA).when(matsyaClientsExecutor).executeSNMPTable(any(), any(), any(), any(), anyBoolean());

		discoveryOperation.processSourcesAndComputes(Collections.singletonList(snmpGetTableSource), hostMonitoring, connector, null, HOSTNAME);

		assertEquals(EXPECTED_SOURCE_TABLE, hostMonitoring
				.getConnectorNamespace(connector)
				.getSourceTable(ENCLOSURE_DISCOVERY_SOURCE_1_KEY));

	}
}
