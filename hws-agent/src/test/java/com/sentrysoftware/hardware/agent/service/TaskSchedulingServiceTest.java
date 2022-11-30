package com.sentrysoftware.hardware.agent.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.sentrysoftware.hardware.agent.configuration.ConfigHelper;
import com.sentrysoftware.hardware.agent.dto.HostConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.opentelemetry.OtelCollectorConfigDto;
import com.sentrysoftware.hardware.agent.service.task.StrategyTask;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class TaskSchedulingServiceTest {

	private static final String HOST_ID = "357306c9-07e9-431b-bc71-b7712daabbbf-1";

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
	public TaskSchedulingService taskSchedulingService;

	@Test
	void testRemoveScheduledTaskNotFound() {
		doReturn(null).when(hostSchedules).get("host");
		assertDoesNotThrow(() -> taskSchedulingService.removeScheduledTask("host"));
		verify(hostSchedules, never()).remove(any());
	}

	@Test
	void testRemoveScheduledTask() {
		final ScheduledFuture<?> mock = spy(ScheduledFuture.class);
		doReturn(mock).when(hostSchedules).get("host");
		doReturn(true).when(mock).cancel(true);
		assertDoesNotThrow(() -> taskSchedulingService.removeScheduledTask("host"));
		verify(hostSchedules).remove("host");
	}

	@Test
	void testScheduleHostTaskNoHostMonitoring() {
		final MultiHostsConfigurationDto multiHostsConfigurationDto = ConfigHelper
				.readConfigurationSafe(configFile);

		// Get one from the test resources
		final HostConfigurationDto hostConfigDto = multiHostsConfigurationDto
			.getHosts()
			.stream()
			.filter(node -> HOST_ID.equals(node.getHost().getId()))
			.findFirst()
			.orElseThrow();

		doReturn(null).when(hostMonitoringMap).get(HOST_ID);

		taskSchedulingService.scheduleHostTask(hostConfigDto);

		verify(hostTaskScheduler, never()).schedule(any(StrategyTask.class), any(Trigger.class));

	}

	@Test
	void testScheduleHostTask() {
		final MultiHostsConfigurationDto multiHostsConfigurationDto = ConfigHelper
				.readConfigurationSafe(configFile);

		// Get one from the test resources
		final HostConfigurationDto hostConfigDto = multiHostsConfigurationDto
			.getHosts()
			.stream()
			.filter(node -> HOST_ID.equals(node.getHost().getId()))
			.findFirst()
			.orElseThrow();

		doReturn(new HostMonitoring()).when(hostMonitoringMap).get(HOST_ID);
		final ScheduledFuture<?> mock = spy(ScheduledFuture.class);
		doReturn(mock).when(hostTaskScheduler).schedule(any(StrategyTask.class), any(Trigger.class));

		taskSchedulingService.scheduleHostTask(hostConfigDto);

		verify(hostSchedules, times(1)).put(any(String.class), any());

	}

	@Test
	void testUpdateConfigurationSchedulesNewHosts() {
		// /data/hws-config.yaml has 3 hosts
		// let's say, we have nothing in the configuration
		// then verify that 3 hosts are fetched and 3 schedules are launched

		doReturn(new HashSet<>()).when(multiHostsConfigurationDto).getHosts();

		doReturn(MultiHostsConfigurationDto.DEFAULT_JOB_POOL_SIZE).when(multiHostsConfigurationDto).getJobPoolSize();

		final ScheduledFuture<?> mock = spy(ScheduledFuture.class);
		doReturn(mock).when(hostTaskScheduler).schedule(any(StrategyTask.class), any(Trigger.class));
		doReturn(new HostMonitoring()).when(hostMonitoringMap).get(any());
		doReturn(OtelCollectorConfigDto.builder().build()).when(multiHostsConfigurationDto).getOtelCollector();

		taskSchedulingService.updateConfiguration(configFile);

		verify(hostTaskScheduler, times(3)).schedule(any(StrategyTask.class), any(Trigger.class));
	}

}
