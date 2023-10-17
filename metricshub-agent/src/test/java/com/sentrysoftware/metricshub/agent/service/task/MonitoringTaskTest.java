package com.sentrysoftware.metricshub.agent.service.task;

import static com.sentrysoftware.metricshub.agent.helper.TestConstants.COMPUTE_HOST_TYPE;
import static com.sentrysoftware.metricshub.agent.helper.TestConstants.HOSTNAME;
import static com.sentrysoftware.metricshub.agent.helper.TestConstants.HOST_TYPE_ATTRIBUTE_KEY;
import static com.sentrysoftware.metricshub.agent.helper.TestConstants.OS_LINUX;
import static com.sentrysoftware.metricshub.agent.helper.TestConstants.OS_TYPE_ATTRIBUTE_KEY;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOST_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sentrysoftware.metricshub.agent.config.ResourceConfig;
import com.sentrysoftware.metricshub.agent.helper.OtelHelper;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import com.sentrysoftware.metricshub.engine.strategy.IStrategy;
import com.sentrysoftware.metricshub.engine.strategy.collect.CollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.collect.PostCollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.collect.PrepareCollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.detection.DetectionStrategy;
import com.sentrysoftware.metricshub.engine.strategy.discovery.DiscoveryStrategy;
import com.sentrysoftware.metricshub.engine.strategy.discovery.PostDiscoveryStrategy;
import com.sentrysoftware.metricshub.engine.strategy.simple.SimpleStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonitoringTaskTest {

	@Mock
	private MonitoringTaskInfo monitoringTaskInfo;

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
		doReturn(ResourceConfig.builder().outputDirectory(null).build()).when(monitoringTaskInfo).getResourceConfig();

		final String logId = "test";

		assertDoesNotThrow(() -> monitoringTask.configureLoggerContext(logId));

		doReturn(ResourceConfig.builder().outputDirectory("dir").build()).when(monitoringTaskInfo).getResourceConfig();

		assertDoesNotThrow(() -> monitoringTask.configureLoggerContext(logId));
	}

	@Test
	void testRun() {
		final Monitor host = Monitor.builder().id("id").build();
		host.addAttribute(HOST_NAME, HOSTNAME);
		host.addAttribute(HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE);
		host.addAttribute(OS_TYPE_ATTRIBUTE_KEY, OS_LINUX);

		final TelemetryManager telemetryManagerMock = spy(TelemetryManager.class);

		doReturn(telemetryManagerMock).when(monitoringTaskInfo).getTelemetryManager();
		doReturn(host).when(telemetryManagerMock).getEndpointHostMonitor();
		doReturn(
			ResourceConfig
				.builder()
				.loggerLevel("OFF")
				.attributes(Map.of(HOST_NAME, HOSTNAME, HOST_TYPE_ATTRIBUTE_KEY, OS_LINUX))
				.discoveryCycle(4)
				.resolveHostnameToFqdn(true)
				.build()
		)
			.when(monitoringTaskInfo)
			.getResourceConfig();
		doReturn(hostConfiguration).when(telemetryManagerMock).getHostConfiguration();
		doNothing()
			.when(telemetryManagerMock)
			.run(any(IStrategy.class), any(IStrategy.class), any(IStrategy.class), any(IStrategy.class));

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
					any(PostDiscoveryStrategy.class)
				);
			verify(telemetryManagerMock, times(4))
				.run(
					any(PrepareCollectStrategy.class),
					any(CollectStrategy.class),
					any(SimpleStrategy.class),
					any(PostCollectStrategy.class)
				);
		}
	}
}
