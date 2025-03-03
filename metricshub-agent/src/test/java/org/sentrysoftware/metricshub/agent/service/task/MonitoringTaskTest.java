package org.sentrysoftware.metricshub.agent.service.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.COMPUTE_HOST_TYPE;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.HOSTNAME;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.HOST_TYPE_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.OS_LINUX;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.OS_TYPE_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOST_NAME;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.agent.config.ResourceConfig;
import org.sentrysoftware.metricshub.agent.config.StateSetMetricCompression;
import org.sentrysoftware.metricshub.agent.helper.OtelHelper;
import org.sentrysoftware.metricshub.agent.opentelemetry.MetricsExporter;
import org.sentrysoftware.metricshub.agent.opentelemetry.client.NoopClient;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.strategy.IStrategy;
import org.sentrysoftware.metricshub.engine.strategy.collect.CollectStrategy;
import org.sentrysoftware.metricshub.engine.strategy.collect.PrepareCollectStrategy;
import org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy;
import org.sentrysoftware.metricshub.engine.strategy.detection.DetectionStrategy;
import org.sentrysoftware.metricshub.engine.strategy.discovery.DiscoveryStrategy;
import org.sentrysoftware.metricshub.engine.strategy.simple.SimpleStrategy;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.MonitorFactory;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import org.sentrysoftware.metricshub.extension.snmp.SnmpConfiguration;
import org.sentrysoftware.metricshub.hardware.strategy.HardwarePostCollectStrategy;
import org.sentrysoftware.metricshub.hardware.strategy.HardwarePostDiscoveryStrategy;

@ExtendWith(MockitoExtension.class)
class MonitoringTaskTest {

	@Mock
	private MonitoringTaskInfo monitoringTaskInfoMock;

	@InjectMocks
	private MonitoringTask monitoringTask;

	private static HostConfiguration hostConfiguration;

	@BeforeAll
	static void beforeAll() {
		hostConfiguration =
			HostConfiguration
				.builder()
				.hostname(HOSTNAME)
				.hostId(HOSTNAME)
				.hostType(DeviceKind.LINUX)
				.configurations(Map.of(SnmpConfiguration.class, SnmpConfiguration.builder().build()))
				.build();
	}

	@Test
	void testConfigureLoggerContext() {
		doReturn(ResourceConfig.builder().outputDirectory(null).build()).when(monitoringTaskInfoMock).getResourceConfig();

		final String logId = "test";

		assertDoesNotThrow(() -> monitoringTask.configureLoggerContext(logId));

		doReturn(ResourceConfig.builder().outputDirectory("dir").build()).when(monitoringTaskInfoMock).getResourceConfig();

		assertDoesNotThrow(() -> monitoringTask.configureLoggerContext(logId));
	}

	@Test
	void testRun() {
		final Monitor host = Monitor.builder().id("id").build();
		host.addAttribute(HOST_NAME, HOSTNAME);
		host.addAttribute(HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE);
		host.addAttribute(OS_TYPE_ATTRIBUTE_KEY, OS_LINUX);

		final TelemetryManager telemetryManagerMock = spy(TelemetryManager.class);

		doReturn(telemetryManagerMock).when(monitoringTaskInfoMock).getTelemetryManager();
		doReturn(host).when(telemetryManagerMock).getEndpointHostMonitor();
		doReturn(
			ResourceConfig
				.builder()
				.loggerLevel("OFF")
				.attributes(Map.of(HOST_NAME, HOSTNAME, HOST_TYPE_ATTRIBUTE_KEY, OS_LINUX))
				.discoveryCycle(4)
				.resolveHostnameToFqdn(true)
				.stateSetCompression(StateSetMetricCompression.SUPPRESS_ZEROS)
				.build()
		)
			.when(monitoringTaskInfoMock)
			.getResourceConfig();
		doReturn(hostConfiguration).when(telemetryManagerMock).getHostConfiguration();

		doNothing().when(telemetryManagerMock).run(any(IStrategy[].class));

		doReturn(ExtensionManager.empty()).when(monitoringTaskInfoMock).getExtensionManager();

		doReturn(MetricsExporter.builder().withClient(new NoopClient()).build())
			.when(monitoringTaskInfoMock)
			.getMetricsExporter();

		monitoringTask.run(); // Discover + Collect
		monitoringTask.run(); // Collect
		monitoringTask.run(); // Collect
		monitoringTask.run(); // Collect

		verify(telemetryManagerMock, times(1))
			.run(
				any(DetectionStrategy.class),
				any(DiscoveryStrategy.class),
				any(SimpleStrategy.class),
				any(HardwarePostDiscoveryStrategy.class)
			);
		verify(telemetryManagerMock, times(4))
			.run(
				any(PrepareCollectStrategy.class),
				any(ProtocolHealthCheckStrategy.class),
				any(CollectStrategy.class),
				any(SimpleStrategy.class),
				any(HardwarePostCollectStrategy.class)
			);
	}

	@Test
	void testRegisterTelemetryManagerRecorders() {
		final Monitor host = Monitor.builder().id("id").build();
		host.addAttribute(HOST_NAME, HOSTNAME);
		host.addAttribute(HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE);
		host.addAttribute(OS_TYPE_ATTRIBUTE_KEY, OS_LINUX);

		TelemetryManager telemetryManager = new TelemetryManager();
		MonitorFactory factory = MonitorFactory
			.builder()
			.attributes(host.getAttributes())
			.connectorId("connector")
			.discoveryTime(System.currentTimeMillis())
			.monitorType(KnownMonitorType.HOST.getKey())
			.telemetryManager(telemetryManager)
			.build();
		factory.createOrUpdateMonitor(HOST_NAME);
	}
}
