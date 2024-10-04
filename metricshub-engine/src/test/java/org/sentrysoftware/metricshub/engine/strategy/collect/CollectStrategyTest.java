package org.sentrysoftware.metricshub.engine.strategy.collect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.DEFAULT_KEYS;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.IS_ENDPOINT;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_ID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.CONNECTOR;
import static org.sentrysoftware.metricshub.engine.constants.Constants.DISK_CONTROLLER;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ENCLOSURE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HEALTHY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST_ID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST_NAME;
import static org.sentrysoftware.metricshub.engine.constants.Constants.ID;
import static org.sentrysoftware.metricshub.engine.constants.Constants.MONITOR_ID_ATTRIBUTE_VALUE;
import static org.sentrysoftware.metricshub.engine.constants.Constants.STATUS_INFORMATION;
import static org.sentrysoftware.metricshub.engine.constants.Constants.TEST_CONNECTOR_ID;
import static org.sentrysoftware.metricshub.engine.strategy.AbstractStrategy.CONNECTOR_ID_FORMAT;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.SnmpGetNextCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.extension.TestConfiguration;
import org.sentrysoftware.metricshub.engine.strategy.IStrategy;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.MonitorFactory;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

@ExtendWith(MockitoExtension.class)
@Disabled
class CollectStrategyTest {

	// Connector path
	public static final Path TEST_CONNECTOR_PATH = Paths.get(
		"src",
		"test",
		"resources",
		"test-files",
		"strategy",
		"collect"
	);

	@Mock
	private ClientsExecutor clientsExecutorMock;

	@Mock
	private IProtocolExtension protocolExtensionMock;

	private IStrategy collectStrategy;

	static Long strategyTime = new Date().getTime();

	@Test
	void testRun() throws Exception {
		// Create host and connector monitors and set them in the telemetry manager
		final Monitor hostMonitor = Monitor.builder().type(KnownMonitorType.HOST.getKey()).build();
		final Monitor connectorMonitor = Monitor.builder().type(KnownMonitorType.CONNECTOR.getKey()).build();
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>(
			Map.of(
				HOST,
				Map.of(MONITOR_ID_ATTRIBUTE_VALUE, hostMonitor),
				CONNECTOR,
				Map.of(
					String.format(CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), TEST_CONNECTOR_ID),
					connectorMonitor
				)
			)
		);

		final TestConfiguration snmpConfig = TestConfiguration.builder().build();

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.monitors(monitors)
			.hostConfiguration(
				HostConfiguration
					.builder()
					.hostId(HOST_ID)
					.hostname(HOST_NAME)
					.sequential(false)
					.configurations(Map.of(TestConfiguration.class, snmpConfig))
					.build()
			)
			.build();

		MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.monitorType(ENCLOSURE)
			.telemetryManager(telemetryManager)
			.connectorId(TEST_CONNECTOR_ID)
			.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "enclosure-1")))
			.discoveryTime(strategyTime - 30 * 60 * 1000)
			.keys(DEFAULT_KEYS)
			.build();
		final Monitor enclosure = monitorFactory.createOrUpdateMonitor();

		monitorFactory =
			MonitorFactory
				.builder()
				.monitorType(DISK_CONTROLLER)
				.telemetryManager(telemetryManager)
				.connectorId(TEST_CONNECTOR_ID)
				.attributes(new HashMap<>(Map.of(MONITOR_ATTRIBUTE_ID, "1")))
				.discoveryTime(strategyTime - 30 * 60 * 1000)
				.keys(DEFAULT_KEYS)
				.build();
		final Monitor diskController = monitorFactory.createOrUpdateMonitor();

		hostMonitor.addAttribute(IS_ENDPOINT, "true");

		connectorMonitor.addAttribute(ID, TEST_CONNECTOR_ID);

		// Create the connector store
		final ConnectorStore connectorStore = new ConnectorStore(TEST_CONNECTOR_PATH);
		telemetryManager.setConnectorStore(connectorStore);

		final ExtensionManager extensionManager = ExtensionManager
			.builder()
			.withProtocolExtensions(List.of(protocolExtensionMock))
			.build();

		doReturn(true).when(protocolExtensionMock).isValidConfiguration(snmpConfig);
		doReturn(Set.of(SnmpGetSource.class, SnmpTableSource.class)).when(protocolExtensionMock).getSupportedSources();
		doReturn(Set.of(SnmpGetNextCriterion.class, SnmpGetCriterion.class))
			.when(protocolExtensionMock)
			.getSupportedCriteria();

		collectStrategy =
			CollectStrategy
				.builder()
				.clientsExecutor(clientsExecutorMock)
				.strategyTime(strategyTime)
				.telemetryManager(telemetryManager)
				.extensionManager(extensionManager)
				.build();

		// Mock detection criteria result
		final SnmpGetNextCriterion snmpGetNextCriterion = SnmpGetNextCriterion
			.builder()
			.oid("1.3.6.1.4.1.795.10.1.1.3.1.1")
			.type("snmpGetNext")
			.build();
		doReturn(CriterionTestResult.success(snmpGetNextCriterion, "1.3.6.1.4.1.795.10.1.1.3.1.1.0	ASN_OCTET_STR	Test"))
			.when(protocolExtensionMock)
			.processCriterion(eq(snmpGetNextCriterion), anyString(), any(TelemetryManager.class));

		// Mock source table information for enclosure
		final SnmpTableSource enclosureSource = SnmpTableSource
			.builder()
			.oid("1.3.6.1.4.1.795.10.1.1.30.1")
			.selectColumns("ID,1,2")
			.type("snmpTable")
			.key("${source::monitors.enclosure.collect.sources.source(1)}")
			.build();
		doReturn(
			SourceTable
				.builder()
				.table(SourceTable.csvToTable("enclosure-1;1;healthy", MetricsHubConstants.TABLE_SEP))
				.build()
		)
			.when(protocolExtensionMock)
			.processSource(eq(enclosureSource), anyString(), any(TelemetryManager.class));

		// Mock source table information for disk_controller
		final SnmpTableSource diskControllerSource = SnmpTableSource
			.builder()
			.oid("1.3.6.1.4.1.795.10.1.1.31.1")
			.selectColumns("ID,1,2")
			.type("snmpTable")
			.key("${source::monitors.disk_controller.collect.sources.source(1)}")
			.build();
		doReturn(SourceTable.builder().table(SourceTable.csvToTable("1;1;healthy", MetricsHubConstants.TABLE_SEP)).build())
			.when(protocolExtensionMock)
			.processSource(eq(diskControllerSource), anyString(), any(TelemetryManager.class));

		collectStrategy.run();

		// Check metrics
		assertEquals(
			1.0,
			diskController.getMetric("hw.status{hw.type=\"disk_controller\"}", NumberMetric.class).getValue()
		);
		assertEquals(HEALTHY, diskController.getLegacyTextParameters().get(STATUS_INFORMATION));
		assertEquals(1.0, enclosure.getMetric("hw.status{hw.type=\"enclosure\"}", NumberMetric.class).getValue());
		assertEquals(HEALTHY, enclosure.getLegacyTextParameters().get(STATUS_INFORMATION));

		// Check that StatusInformation is collected on the connector monitor
		assertEquals(
			"\nMessage: SnmpGetNextCriterion test succeeded:\n" +
			"SnmpGetNextCriterion(super=- OID: 1.3.6.1.4.1.795.10.1.1.3.1.1)\n" +
			"\n" +
			"Result: 1.3.6.1.4.1.795.10.1.1.3.1.1.0\tASN_OCTET_STR\tTest\n" +
			"Received Result: 1.3.6.1.4.1.795.10.1.1.3.1.1.0\tASN_OCTET_STR\tTest\n" +
			"Conclusion: Test on host.name SUCCEEDED",
			connectorMonitor.getLegacyTextParameters().get(STATUS_INFORMATION)
		);

		// Mock detection criteria result to switch to a failing criterion processing case
		doReturn(CriterionTestResult.failure(snmpGetNextCriterion, "1.3.6.1.4.1.795.10.1.1.3.1.1.0	ASN_OCTET_STR	Test"))
			.when(protocolExtensionMock)
			.processCriterion(eq(snmpGetNextCriterion), anyString(), any(TelemetryManager.class));

		// Call DiscoveryStrategy to discover the monitors
		collectStrategy.run();

		// Check that StatusInformation is collected on the connector monitor (criterion processing failure case)
		assertEquals(
			"\nMessage: SnmpGetNextCriterion test ran but failed:\n" +
			"SnmpGetNextCriterion(super=- OID: 1.3.6.1.4.1.795.10.1.1.3.1.1)\n" +
			"\n" +
			"Actual result:\n" +
			"1.3.6.1.4.1.795.10.1.1.3.1.1.0\tASN_OCTET_STR\tTest\n" +
			"Received Result: 1.3.6.1.4.1.795.10.1.1.3.1.1.0\tASN_OCTET_STR\tTest\n" +
			"Conclusion: Test on host.name FAILED",
			connectorMonitor.getLegacyTextParameters().get(STATUS_INFORMATION)
		);
	}
}
