package com.sentrysoftware.matrix.agent.service.task;

import static com.sentrysoftware.matrix.agent.helper.TestConstants.COMPUTE_HOST_TYPE;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.HOSTNAME;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.HOST_TYPE_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.OS_LINUX;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.OS_TYPE_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_NAME;
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

import com.sentrysoftware.matrix.agent.config.ResourceConfig;
import com.sentrysoftware.matrix.agent.helper.OtelHelper;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.strategy.IStrategy;
import com.sentrysoftware.matrix.strategy.collect.CollectStrategy;
import com.sentrysoftware.matrix.strategy.detection.DetectionStrategy;
import com.sentrysoftware.matrix.strategy.discovery.DiscoveryStrategy;
import com.sentrysoftware.matrix.strategy.simple.SimpleStrategy;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
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
		doReturn(host).when(telemetryManagerMock).getHostMonitor();
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
		doNothing().when(telemetryManagerMock).run(any(IStrategy.class), any(IStrategy.class), any(IStrategy.class));
		doNothing().when(telemetryManagerMock).run(any(IStrategy.class), any(IStrategy.class));

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
				.run(any(DetectionStrategy.class), any(DiscoveryStrategy.class), any(SimpleStrategy.class));
			verify(telemetryManagerMock, times(4)).run(any(CollectStrategy.class), any(SimpleStrategy.class));
		}
	}
}
