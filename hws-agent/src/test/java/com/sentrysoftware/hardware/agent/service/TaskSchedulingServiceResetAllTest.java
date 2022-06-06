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
	private ThreadPoolTaskScheduler hostTaskScheduler;

	@Mock
	private MultiHostsConfigurationDto multiHostsConfigurationDto;

	@Mock
	private Map<String, IHostMonitoring> hostMonitoringMap;

	@Mock
	private Map<String, ScheduledFuture<?>> hostSchedules;

	@InjectMocks
	@Autowired
	public TaskSchedulingService taskSechedulingService;


	@Test
	void testUpdateConfigurationRestartAll() {

		// Current /data/hws-config.yaml has 3 hosts
		// Let's say we have updated the SDK configuration
		final MultiHostsConfigurationDto previous = ConfigHelper.readConfigurationSafe(configFile);
		previous.getExporter().getOtlp().getHeaders().put("accept", "*/*".toCharArray());

		try (MockedStatic<ConfigHelper> configHelper = mockStatic(ConfigHelper.class)) {

			configHelper.when(() -> ConfigHelper.readConfigurationSafe(any())).thenReturn(previous);
			configHelper.when(() -> ConfigHelper.decrypt(any())).thenAnswer(invocation -> invocation.getArgument(0));
			configHelper.when(() -> ConfigHelper.fillHostMonitoringMap(any(), any(), any())).thenCallRealMethod();

			doReturn(previous.getHosts(),
					previous.getHosts().stream().collect(Collectors.toSet()),
					previous.getHosts().stream().collect(Collectors.toSet()))
			.when(multiHostsConfigurationDto).getHosts();
			doReturn(new HostMonitoring()).when(hostMonitoringMap).get(any());

			final ScheduledFuture<?> mock = spy(ScheduledFuture.class);
			doReturn(mock).when(hostSchedules).get(any());
			doReturn(true).when(mock).cancel(true);
			taskSechedulingService.updateConfiguration(configFile);

			verify(mock, times(3)).cancel(true);
			verify(hostTaskScheduler, times(3)).schedule(any(StrategyTask.class), any(Trigger.class));
		}

	}
}
