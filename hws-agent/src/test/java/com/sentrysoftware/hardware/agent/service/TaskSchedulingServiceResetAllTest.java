package com.sentrysoftware.hardware.agent.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.sentrysoftware.hardware.agent.configuration.ConfigHelper;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.service.task.StrategyTask;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class TaskSchedulingServiceResetAllTest {

	/**
	 * The reset all unit test requires a separate spring context as the code modifies the content of the internal 
	 * otelSdkConfiguration bean when the SDK configuration is updated and this causes an unexpected behavior on other unit tests.
	 */

	@Autowired
	private File configFile;

	@Mock
	private ThreadPoolTaskScheduler targetTaskScheduler;

	@Mock
	private MultiHostsConfigurationDto multiHostsConfigurationDto;

	@Mock
	private Map<String, IHostMonitoring> hostMonitoringMap;

	@Mock
	private Map<String, ScheduledFuture<?>> targetSchedules;

	@InjectMocks
	@Autowired
	public TaskSchedulingService taskSechedulingService;


	@Test
	void testUpdateConfigurationRestartAll() {

		// Current /data/hws-config.yaml has 3 targets
		// Let's say we have updated the SDK configuration
		final MultiHostsConfigurationDto previous = ConfigHelper.readConfigurationSafe(configFile);
		previous.getExporter().getOtlp().getHeaders().put("accept", "*/*".toCharArray());

		try (MockedStatic<ConfigHelper> configHelper = mockStatic(ConfigHelper.class)) {

			configHelper.when(() -> ConfigHelper.readConfigurationSafe(any())).thenReturn(previous);
			configHelper.when(() -> ConfigHelper.decrypt(any())).thenAnswer(invocation -> invocation.getArgument(0));
			configHelper.when(() -> ConfigHelper.fillHostMonitoringMap(any(), any(), any())).thenCallRealMethod();

			doReturn(previous.getTargets(),
					previous.getTargets().stream().collect(Collectors.toSet()),
					previous.getTargets().stream().collect(Collectors.toSet()))
			.when(multiHostsConfigurationDto).getTargets();
			doReturn(new HostMonitoring()).when(hostMonitoringMap).get(any());

			final ScheduledFuture<?> mock = spy(ScheduledFuture.class);
			doReturn(mock).when(targetSchedules).get(any());
			doReturn(true).when(mock).cancel(true);
			taskSechedulingService.updateConfiguration(configFile);

			verify(mock, times(3)).cancel(true);
			verify(targetTaskScheduler, times(3)).schedule(any(StrategyTask.class), any(Trigger.class));
		}

	}
}
