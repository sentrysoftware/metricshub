package com.sentrysoftware.hardware.agent.service.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.hardware.agent.dto.HardwareTargetDto;
import com.sentrysoftware.hardware.agent.dto.HostConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.UserConfiguration;
import com.sentrysoftware.hardware.agent.service.opentelemetry.OtelHelper;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;

@ExtendWith(MockitoExtension.class)
class StrategyTaskTest {

	@Mock
	private StrategyTaskInfo strategyTaskInfo;

	@Mock
	private UserConfiguration userConfiguration;

	@Mock
	private Map<String, String> otelSdkConfiguration;

	@InjectMocks
	private StrategyTask strategyTask;

	private static EngineConfiguration engineConfiguration;

	@BeforeAll
	static void beforeAll() {
		engineConfiguration = EngineConfiguration
				.builder()
				.target(HardwareTarget
						.builder()
						.hostname("host")
						.id("host")
						.type(TargetType.LINUX)
						.build())
				.protocolConfigurations(Map.of(SnmpProtocol.class, SnmpProtocol.builder().build()))
				.build();
	}

	@Test
	void testConfigureLoggerContext() {
		doReturn(null).when(strategyTaskInfo).getOutputDirectory();

		assertDoesNotThrow(() -> strategyTask.configureLoggerContext("test"));

		doReturn("dir").when(strategyTaskInfo).getOutputDirectory();

		assertDoesNotThrow(() -> strategyTask.configureLoggerContext("test"));
	}

	@Test
	void testRun() {
		final Monitor target = Monitor
				.builder()
				.id("id")
				.name("target")
				.build();
		target.addMetadata("fqdn", "host.my.domain.net");

		final HostMonitoring hostMonitoring = spy(HostMonitoring.class);

		doReturn(hostMonitoring).when(strategyTaskInfo).getHostMonitoring();
		doReturn(target).when(hostMonitoring).getTargetMonitor();
		doReturn("OFF").when(strategyTaskInfo).getLoggerLevel();
		doReturn(engineConfiguration).when(hostMonitoring).getEngineConfiguration();
		doReturn(EngineResult.builder().build()).when(hostMonitoring).run(any());
		doReturn(MultiHostsConfigurationDto.builder().build()).when(userConfiguration).getMultiHostsConfigurationDto();
		doReturn(HostConfigurationDto
				.builder()
				.target(HardwareTargetDto
						.builder()
						.hostname("target")
						.id("id")
						.type(TargetType.LINUX)
						.build())
				.build())
				.when(userConfiguration).getHostConfigurationDto();

		doReturn(4).when(strategyTaskInfo).getDiscoveryCycle();

		try(MockedStatic<OtelHelper> otelHelper = mockStatic(OtelHelper.class)){
			otelHelper.when(() -> OtelHelper.createHostResource(
					anyString(), anyString(), any(TargetType.class), anyString(), anyBoolean(), any(), any()))
				.thenCallRealMethod();

			// Build the SdkMeterProvider using InMemoryMetricReader, it's not required to
			// build PeriodicMetricReaderFactory using the gRPC exporter in this test.
			otelHelper.when(() -> OtelHelper.initOpenTelemetrySdk(any(Resource.class), any())).thenAnswer(answer -> {

				final var sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

				sdkBuilder
						.addMeterProviderCustomizer((builder, u) -> { 
							return builder
									.registerMetricReader(InMemoryMetricReader.create())
									.setResource(answer.getArgument(0));
						});
				sdkBuilder.registerShutdownHook(false);

				return sdkBuilder.build();

			});

			strategyTask.run(); // Discover + Collect
			strategyTask.run(); // Collect
			strategyTask.run(); // Collect
			strategyTask.run(); // Collect

			verify(hostMonitoring, times(1)).run(any(DetectionOperation.class), any(DiscoveryOperation.class));
			verify(hostMonitoring, times(4)).run(any(CollectOperation.class));
		}
		
	}

	@Test
	void testRequiredArgumentsOnConstructor() {
		assertThrows(IllegalArgumentException.class, () -> new StrategyTask(null, null, null));
	}
}
