package com.sentrysoftware.hardware.prometheus.service.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

@ExtendWith(MockitoExtension.class)
class StrategyTaskTest {

	@Mock
	private StrategyTaskInfo strategyTaskInfo;

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
				.unknownStatus(Optional.of(ParameterState.OK))
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
		final HostMonitoring hostMonitoring = spy(HostMonitoring.class);

		doReturn(hostMonitoring).when(strategyTaskInfo).getHostMonitoring();
		doReturn("OFF").when(strategyTaskInfo).getLoggerLevel();
		doReturn(engineConfiguration).when(hostMonitoring).getEngineConfiguration();
		doReturn(EngineResult.builder().build()).when(hostMonitoring).run(any());

		doReturn(4).when(strategyTaskInfo).getDiscoveryCycle();

		strategyTask.run(); // Discover + Collect
		strategyTask.run(); // Collect
		strategyTask.run(); // Collect
		strategyTask.run(); // Collect

		verify(hostMonitoring, times(1)).run(any(DetectionOperation.class), any(DiscoveryOperation.class),
				any(CollectOperation.class));
		verify(hostMonitoring, times(3)).run(any(CollectOperation.class));
	}

	@Test
	void testRequiredArgumentsOnConstructor() {
		assertThrows(IllegalArgumentException.class, () -> new StrategyTask(null));
	}
}
