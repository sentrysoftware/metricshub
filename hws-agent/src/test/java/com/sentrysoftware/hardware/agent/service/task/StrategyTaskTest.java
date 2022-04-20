package com.sentrysoftware.hardware.agent.service.task;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.hardware.agent.dto.HardwareTargetDTO;
import com.sentrysoftware.hardware.agent.dto.HostConfigurationDTO;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.agent.dto.UserConfiguration;
import com.sentrysoftware.hardware.agent.service.opentelemetry.OtelAlertHelperTest;
import com.sentrysoftware.hardware.agent.service.opentelemetry.OtelHelper;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectHelper;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.alert.AlertInfo;
import com.sentrysoftware.matrix.model.alert.Severity;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.logs.data.LogData;
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
	private StrategyTask strategyTask1;

	@InjectMocks 
	private StrategyTask strategyTask2;

	private static EngineConfiguration engineConfiguration;
	private static EngineConfiguration engineConfigWithAlertConfig;

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

		engineConfigWithAlertConfig = EngineConfiguration
				.builder()
				.target(HardwareTarget
						.builder()
						.hostname("host")
						.id("host")
						.type(TargetType.LINUX)
						.build())
				.protocolConfigurations(Map.of(SNMPProtocol.class, SNMPProtocol.builder().build()))
				.alertTrigger(alertInfo -> {})
				.build();
	}

	@Test
	void testConfigureLoggerContext() {
		doReturn(null).when(strategyTaskInfo).getOutputDirectory();

		assertDoesNotThrow(() -> strategyTask1.configureLoggerContext("test"));

		doReturn("dir").when(strategyTaskInfo).getOutputDirectory();

		assertDoesNotThrow(() -> strategyTask1.configureLoggerContext("test"));
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
		doReturn(MultiHostsConfigurationDTO.builder().build()).when(userConfiguration).getMultiHostsConfigurationDTO();
		doReturn(HostConfigurationDTO
				.builder()
				.target(HardwareTargetDTO
						.builder()
						.hostname("target")
						.id("id")
						.type(TargetType.LINUX)
						.build())
				.build())
				.when(userConfiguration).getHostConfigurationDTO();

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
				sdkBuilder.setResultAsGlobal(false);

				return sdkBuilder.build();

			});

			strategyTask1.run(); // Discover + Collect
			strategyTask1.run(); // Collect
			strategyTask1.run(); // Collect
			strategyTask1.run(); // Collect

			verify(hostMonitoring, times(1)).run(any(DetectionOperation.class), any(DiscoveryOperation.class));
			verify(hostMonitoring, times(4)).run(any(CollectOperation.class));
		}
		
	}

	@Test
	void testRequiredArgumentsOnConstructor() {
		assertThrows(IllegalArgumentException.class, () -> new StrategyTask(null, null, null));
	}

	@Test
	void testTriggerAlertAsOtelLog() {

		final List<LogData> actualLogs = triggerAlertAsOtelLog(false);
		assertEquals("Hardware problem on localhost with SAS Flash Module - CH0.BAY9.",
				actualLogs.stream().findFirst().orElseThrow().getBody().asString());

		assertEquals(Collections.emptyList(), triggerAlertAsOtelLog(true));

		assertThrows(IllegalArgumentException.class, () -> strategyTask2.triggerAlertAsOtelLog(null));
	}

	/**
	 * Trigger alert as log, capture the OpenTelemetry logs then return the list of
	 * captured {@link LogData}
	 * 
	 * @param disableAlerting whether the alerting is disabled or not
	 * @return List of captured {@link LogData}
	 */
	List<LogData> triggerAlertAsOtelLog(final boolean disableAlerting) {
		final List<LogData> actualLogs = new ArrayList<>();

		try (MockedStatic<OtelHelper> otelHelper = mockStatic(OtelHelper.class)) {

			otelHelper.when(() -> OtelHelper.createHostResource(anyString(), anyString(), any(TargetType.class),
					anyString(), anyBoolean(), any(), any())).thenCallRealMethod();

			// Build the SdkMeterProvider using InMemoryMetricReader, it's not required to
			// build PeriodicMetricReaderFactory using the gRPC exporter in this test.
			otelHelper.when(() -> OtelHelper.initOpenTelemetrySdk(any(Resource.class), any())).thenAnswer(answer -> {

				final var sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

				sdkBuilder.addMeterProviderCustomizer((builder, p) -> builder
								.registerMetricReader(InMemoryMetricReader.create())
								.setResource(answer.getArgument(0))
				);

				sdkBuilder.addLogEmitterProviderCustomizer((builder, p) -> builder
								// Capture the logs
								.addLogProcessor(logData -> actualLogs.add(logData))
				);

				sdkBuilder.registerShutdownHook(false);
				sdkBuilder.setResultAsGlobal(false);

				return sdkBuilder.build();

			});

			final IHostMonitoring hostMonitoring = new HostMonitoring();

			OtelAlertHelperTest.initMonitorsForAlert(hostMonitoring);

			hostMonitoring.setEngineConfiguration(engineConfigWithAlertConfig);

			lenient().doReturn(MultiHostsConfigurationDTO.builder().build()).when(userConfiguration)
					.getMultiHostsConfigurationDTO();
			HostConfigurationDTO hostConfig = HostConfigurationDTO
					.builder()
					.target(HardwareTargetDTO.builder().hostname("target").id("id").type(TargetType.LINUX).build())
					.hardwareProblemTemplate("Hardware problem on ${FQDN} with ${MONITOR_NAME}.")
					.build();
			hostConfig.setDisableAlerting(disableAlerting);
			doReturn(hostConfig).when(userConfiguration).getHostConfigurationDTO();

			strategyTask2.initOtelSdk(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");
			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, System.currentTimeMillis(),
					Status.FAILED);

			final AlertInfo alertInfo = AlertInfo.builder()
					.alertRule(physicalDisk.getAlertRules().get(STATUS_PARAMETER).stream()
							.filter(rule -> Severity.ALARM.equals(rule.getSeverity())).findFirst().orElseThrow())
					.monitor(physicalDisk).parameterName(STATUS_PARAMETER)
					.hardwareTarget(engineConfigWithAlertConfig.getTarget()).hostMonitoring(hostMonitoring).build();

			strategyTask2.triggerAlertAsOtelLog(alertInfo);

		}

		return actualLogs;
	}
}
