package com.sentrysoftware.hardware.agent.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import org.apache.logging.log4j.Level;
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
import com.sentrysoftware.hardware.agent.dto.HardwareTargetDto;
import com.sentrysoftware.hardware.agent.dto.HostConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.protocol.SnmpProtocolDto;
import com.sentrysoftware.hardware.agent.service.task.StrategyTask;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class TaskSchedulingServiceTest {

	private static final String TARGET_ID = "357306c9-07e9-431b-bc71-b7712daabbbf-1";

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
	void testRemoveScheduledTaskNotFound() {
		doReturn(null).when(targetSchedules).get("target");
		assertDoesNotThrow(() -> taskSechedulingService.removeScheduledTask("target"));
		verify(targetSchedules, never()).remove(any());
	}

	@Test
	void testRemoveScheduledTask() {
		final ScheduledFuture<?> mock = spy(ScheduledFuture.class);
		doReturn(mock).when(targetSchedules).get("target");
		doReturn(true).when(mock).cancel(true);
		assertDoesNotThrow(() -> taskSechedulingService.removeScheduledTask("target"));
		verify(targetSchedules).remove("target");
	}

	@Test
	void testScheduleTargetTaskNoHostMonitoring() {
		final MultiHostsConfigurationDto multiHostsConfigurationDto = ConfigHelper
				.readConfigurationSafe(configFile);

		// Get one from the test resources
		final HostConfigurationDto hostConfigDto = multiHostsConfigurationDto
			.getTargets()
			.stream()
			.filter(node -> TARGET_ID.equals(node.getTarget().getId()))
			.findFirst()
			.orElseThrow();

		doReturn(null).when(hostMonitoringMap).get(TARGET_ID);

		taskSechedulingService.scheduleTargetTask(hostConfigDto);

		verify(targetTaskScheduler, never()).schedule(any(StrategyTask.class), any(Trigger.class));

	}

	@Test
	void testScheduleTargetTask() {
		final MultiHostsConfigurationDto multiHostsConfigurationDto = ConfigHelper
				.readConfigurationSafe(configFile);

		// Get one from the test resources
		final HostConfigurationDto hostConfigDto = multiHostsConfigurationDto
			.getTargets()
			.stream()
			.filter(node -> TARGET_ID.equals(node.getTarget().getId()))
			.findFirst()
			.orElseThrow();

		doReturn(new HostMonitoring()).when(hostMonitoringMap).get(TARGET_ID);
		final ScheduledFuture<?> mock = spy(ScheduledFuture.class);
		doReturn(mock).when(targetTaskScheduler).schedule(any(StrategyTask.class), any(Trigger.class));

		taskSechedulingService.scheduleTargetTask(hostConfigDto);

		verify(targetSchedules, times(1)).put(any(String.class), any());

	}

	@Test
	void testUpdateConfigurationRemoveObsoleteSchedules() {

		// Current /data/hws-config.yaml has 3 targets
		// Let's say we have 4 targets from the previous configuration but the current contains only 3 targets
		// Let's check that 1 target is unscheduled and the exsiting targets are never re-scheduled
		final MultiHostsConfigurationDto previous = ConfigHelper.readConfigurationSafe(configFile);
		previous.getTargets().add(HostConfigurationDto.builder()
				.collectPeriod(MultiHostsConfigurationDto.DEFAULT_COLLECT_PERIOD)
				.discoveryCycle(MultiHostsConfigurationDto.DEFAULT_DISCOVERY_CYCLE)
				.target(HardwareTargetDto
						.builder()
						.hostname("host1")
						.id("host1")
						.type(TargetType.LINUX)
						.build())
				.snmp(SnmpProtocolDto.builder().community("public1".toCharArray()).build())
				.build());

		doReturn(previous.getTargets()).when(multiHostsConfigurationDto).getTargets();
		final ScheduledFuture<?> mock = spy(ScheduledFuture.class);
		doReturn(mock).when(targetSchedules).get(any());
		doReturn(true).when(mock).cancel(true);

		taskSechedulingService.updateConfiguration(configFile);

		verify(mock, times(1)).cancel(true);
		verify(targetTaskScheduler, never()).schedule(any(StrategyTask.class), any(Trigger.class));

	}

	@Test
	void testUpdateConfigurationSchedulesNewTargets() {
		// /data/hws-config.yaml has 3 targets
		// let's say, we have nothing in the configuration
		// then verify that 3 targets are fetched and 3 schedules are launched

		doReturn(new HashSet<>()).when(multiHostsConfigurationDto).getTargets();

		doReturn(MultiHostsConfigurationDto.DEFAULT_JOB_POOL_SIZE).when(multiHostsConfigurationDto).getJobPoolSize();

		final ScheduledFuture<?> mock = spy(ScheduledFuture.class);
		doReturn(mock).when(targetTaskScheduler).schedule(any(StrategyTask.class), any(Trigger.class));
		doReturn(new HostMonitoring()).when(hostMonitoringMap).get(any());

		taskSechedulingService.updateConfiguration(configFile);

		verify(targetTaskScheduler, times(3)).schedule(any(StrategyTask.class), any(Trigger.class));
	}

	@Test
	void testGetLoggerLevel() {
		assertEquals(Level.OFF, taskSechedulingService.getLoggerLevel(null));
		assertEquals(Level.DEBUG, taskSechedulingService.getLoggerLevel("debug"));
		assertEquals(Level.OFF, taskSechedulingService.getLoggerLevel("unknown"));
	}
}
