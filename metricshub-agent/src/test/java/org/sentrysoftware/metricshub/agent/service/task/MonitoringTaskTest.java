package org.sentrysoftware.metricshub.agent.service.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
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

		try (MockedStatic<OtelHelper> otelHelperMockedStatic = mockStatic(OtelHelper.class)) {
			otelHelperMockedStatic
				.when(() -> OtelHelper.createHostResource(anyMap(), anyMap(), anyBoolean()))
				.thenCallRealMethod();
			otelHelperMockedStatic.when(() -> OtelHelper.createOpenTelemetryResource(anyMap())).thenCallRealMethod();
			otelHelperMockedStatic.when(() -> OtelHelper.buildOtelAttributesFromMap(anyMap())).thenCallRealMethod();

			// Build the SdkMeterProvider using InMemoryMetricReader, it's not required to
			// build PeriodicMetricReaderFactory using the gRPC exporter in this test.
			otelHelperMockedStatic
				.when(() -> OtelHelper.initOpenTelemetrySdk(any(Resource.class), any()))
				.thenAnswer(answer -> {
					final var sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

					sdkBuilder.addMeterProviderCustomizer((builder, u) -> {
						return builder.registerMetricReader(InMemoryMetricReader.create()).setResource(answer.getArgument(0));
					});

					return sdkBuilder.build();
				});

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
	}

	@Test
	void testInitMetricObserver() {
		// Create a new MonitoringTask using the mocked monitoringTaskInfo instance
		final MonitoringTask newMonitoringTask = new MonitoringTask(monitoringTaskInfoMock);

		doReturn(ResourceConfig.builder().stateSetCompression(StateSetMetricCompression.SUPPRESS_ZEROS).build())
			.when(monitoringTaskInfoMock)
			.getResourceConfig();

		// Create an in-memory metric reader for testing, this instance will be used later to collect
		// and get the metric value
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();

		// Use a try-with-resources block to mock static methods in OtelHelper
		try (MockedStatic<OtelHelper> otelHelperMockedStatic = mockStatic(OtelHelper.class)) {
			// Mock the initialization of the OpenTelemetry SDK
			otelHelperMockedStatic
				.when(() -> OtelHelper.initOpenTelemetrySdk(any(Resource.class), any()))
				.thenAnswer(answer -> {
					final var sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

					// Customize the meter provider to use the in-memory metric reader and set the resource
					sdkBuilder.addMeterProviderCustomizer((builder, u) -> {
						return builder.registerMetricReader(inMemoryReader).setResource(answer.getArgument(0));
					});

					return sdkBuilder.build();
				});

			// Mock other OtelHelper methods to ensure that the behavior correctly initializes intermediate objects
			otelHelperMockedStatic.when(() -> OtelHelper.createOpenTelemetryResource(anyMap())).thenCallRealMethod();
			otelHelperMockedStatic
				.when(() -> OtelHelper.createHostResource(anyMap(), anyMap(), anyBoolean()))
				.thenReturn(Resource.create(Attributes.of(AttributeKey.stringKey(OS_TYPE_ATTRIBUTE_KEY), OS_LINUX)));
			otelHelperMockedStatic
				.when(() -> OtelHelper.resolveResourceHostname(anyString(), anyString(), anyBoolean(), anyString()))
				.thenReturn(HOST_NAME);
			otelHelperMockedStatic
				.when(() -> OtelHelper.mergeOtelAttributes(any(Attributes.class), any(Attributes.class)))
				.thenCallRealMethod();
			otelHelperMockedStatic.when(() -> OtelHelper.buildOtelAttributesFromMap(anyMap())).thenCallRealMethod();
			otelHelperMockedStatic.when(() -> OtelHelper.isAcceptedKey(anyString())).thenCallRealMethod();

			// Create a telemetry manager mock and set behavior for getting the host monitor
			final TelemetryManager telemetryManagerMock = spy(TelemetryManager.class);
			doReturn(new Monitor()).when(telemetryManagerMock).getEndpointHostMonitor();

			// Initialize the OpenTelemetry SDK with specific resource configurations
			newMonitoringTask.initOtelSdk(
				telemetryManagerMock,
				ResourceConfig
					.builder()
					.loggerLevel("OFF")
					.attributes(Map.of(HOST_NAME, HOSTNAME, HOST_TYPE_ATTRIBUTE_KEY, OS_LINUX))
					.discoveryCycle(4)
					.resolveHostnameToFqdn(true)
					.build()
			);

			// Create metric and monitor data for testing
			final Monitor monitor = Monitor.builder().id("enclosure-1").type("enclosure").build();
			final String expectedUnit = "Cel";
			final String metricName = "hw.temperature.limit";
			final double expectedMetricValue = 70D;
			final String metricKey = "hw.temperature.limit{limit_type=\"high.critical\"}";
			final String expectedDescription = "description";

			// Create metric definition map that defines the expected unit and description for the hw.temperature.limit metric
			final Map<String, MetricDefinition> metricDefinitionMap = Map.of(
				metricName,
				MetricDefinition.builder().unit(expectedUnit).description(expectedDescription).build()
			);

			// Create a mock metric entry
			final Entry<String, AbstractMetric> metricEntry = Map.entry(
				metricKey,
				NumberMetric
					.builder()
					.name(metricKey)
					.value(expectedMetricValue)
					.attributes(Map.of("limit_type", "high.critical"))
					.collectTime(System.currentTimeMillis())
					.build()
			);

			// Initialize the metric observer
			newMonitoringTask.initMetricObserver(monitor, metricDefinitionMap, metricEntry);

			// Collect metrics from the in-memory reader and perform assertions
			final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();
			assertFalse(metrics.isEmpty());

			// Filter the collected metrics for the specific metric name
			final List<MetricData> metricList = metrics
				.stream()
				.filter(metricData -> metricName.equals(metricData.getName()))
				.toList();

			// Assert that there is one metric with the expected name
			assertEquals(1, metricList.size());
			final MetricData hwTemperatureLimitData = metricList.get(0);
			assertNotNull(hwTemperatureLimitData);

			// Assert that the description and unit of the metric match the expected values
			assertEquals(expectedDescription, hwTemperatureLimitData.getDescription());
			assertEquals(expectedUnit, hwTemperatureLimitData.getUnit());

			// Assert the value of the metric
			final GaugeData<DoublePointData> doubleData = hwTemperatureLimitData.getDoubleGaugeData();
			final DoublePointData dataPoint = doubleData.getPoints().stream().findAny().orElse(null);
			assertNotNull(dataPoint);
			assertEquals(expectedMetricValue, dataPoint.getValue());
		}
	}
}
