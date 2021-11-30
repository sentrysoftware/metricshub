package com.sentrysoftware.hardware.agent.service.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.agent.service.opentelemetry.OtelHelper;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import io.opentelemetry.sdk.resources.Resource;

@ExtendWith(MockitoExtension.class)
class StrategyTaskTest {

	@Mock
	private StrategyTaskInfo strategyTaskInfo;

	@Mock
	private MultiHostsConfigurationDTO multiHostsConfigurationDTO;

	@Mock
	private MetricReaderFactory periodicReaderFactory;

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
				.protocolConfigurations(Map.of(SNMPProtocol.class, SNMPProtocol.builder().build()))
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

		doReturn(4).when(strategyTaskInfo).getDiscoveryCycle();

		try(MockedStatic<OtelHelper> otelHelper = mockStatic(OtelHelper.class)){
			otelHelper.when(() -> OtelHelper.createHostResource(any(Monitor.class), anyString()))
				.thenCallRealMethod();

			// Build the SdkMeterProvider using InMemoryMetricReader, it's not required to
			// build PeriodicMetricReaderFactory using the gRPC exporter in this test.
			otelHelper.when(() -> OtelHelper.initOpenTelemetryMetrics(any(Resource.class), any(MetricReaderFactory.class)))
				.thenAnswer(answer -> SdkMeterProvider.builder()
						.setResource(answer.getArgument(0))
						.registerMetricReader(InMemoryMetricReader.create())
						.buildAndRegisterGlobal());

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
