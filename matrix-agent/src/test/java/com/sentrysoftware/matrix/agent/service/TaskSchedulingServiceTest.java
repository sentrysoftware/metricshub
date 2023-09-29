package com.sentrysoftware.matrix.agent.service;

import static com.sentrysoftware.matrix.agent.helper.TestConstants.COMPANY_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.COMPANY_ATTRIBUTE_VALUE;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SENTRY_OTTAWA_RESOURCE_GROUP_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SENTRY_OTTAWA_SITE_VALUE;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SENTRY_PARIS_RESOURCE_GROUP_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SENTRY_PARIS_SITE_VALUE;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SITE_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.service.TaskSchedulingService.METRICSHUB_OVERALL_SELF_TASK_KEY;
import static com.sentrysoftware.matrix.agent.service.TaskSchedulingService.METRICSHUB_RESOURCE_GROUP_KEY_FORMAT;
import static com.sentrysoftware.matrix.agent.service.signal.ResourceGroupMetricsObserver.HW_SITE_PUE_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sentrysoftware.matrix.agent.config.AgentConfig;
import com.sentrysoftware.matrix.agent.config.ResourceGroupConfig;
import com.sentrysoftware.matrix.agent.context.AgentInfo;
import com.sentrysoftware.matrix.agent.helper.OtelConfigHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

class TaskSchedulingServiceTest {

	private static final String NO_CONFIG_RESOURCE_GROUP_KEY = "no-config";

	@Test
	void testScheduleSelfObserver() {
		final AgentConfig agentConfig = AgentConfig
			.builder()
			.attributes(Map.of(COMPANY_ATTRIBUTE_KEY, COMPANY_ATTRIBUTE_VALUE))
			.build();

		final AgentInfo agentInfo = new AgentInfo();

		final ThreadPoolTaskScheduler taskSchedulerMock = spy(ThreadPoolTaskScheduler.class);
		final ScheduledFuture<?> scheduledFutureMock = spy(ScheduledFuture.class);

		doReturn(scheduledFutureMock).when(taskSchedulerMock).schedule(any(Runnable.class), any(Trigger.class));

		final TaskSchedulingService taskSchedulingService = TaskSchedulingService
			.builder()
			.withAgentConfig(agentConfig)
			.withAgentInfo(agentInfo)
			.withOtelSdkConfiguration(OtelConfigHelper.buildOtelSdkConfiguration(agentConfig))
			.withResourceSchedules(new HashMap<>())
			.withTaskScheduler(taskSchedulerMock)
			.build();

		taskSchedulingService.scheduleSelfObserver();

		verify(taskSchedulerMock, times(1)).schedule(any(Runnable.class), any(Trigger.class));

		assertEquals(
			scheduledFutureMock,
			taskSchedulingService.getResourceSchedules().get(METRICSHUB_OVERALL_SELF_TASK_KEY)
		);
	}

	@Test
	void testScheduleResourceGroupObservers() {
		final Map<String, ResourceGroupConfig> resourceGroups = new HashMap<>();
		resourceGroups.put(
			SENTRY_PARIS_RESOURCE_GROUP_KEY,
			ResourceGroupConfig
				.builder()
				.attributes(Map.of(SITE_ATTRIBUTE_KEY, SENTRY_PARIS_SITE_VALUE))
				.metrics(Map.of(HW_SITE_PUE_METRIC, 1D))
				.build()
		);
		resourceGroups.put(
			SENTRY_OTTAWA_RESOURCE_GROUP_KEY,
			ResourceGroupConfig
				.builder()
				.attributes(Map.of(SITE_ATTRIBUTE_KEY, SENTRY_OTTAWA_SITE_VALUE))
				.metrics(Map.of(HW_SITE_PUE_METRIC, 1D))
				.build()
		);
		resourceGroups.put(NO_CONFIG_RESOURCE_GROUP_KEY, null);

		final AgentConfig agentConfig = AgentConfig.builder().resourceGroups(resourceGroups).build();

		final ThreadPoolTaskScheduler taskSchedulerMock = spy(ThreadPoolTaskScheduler.class);
		final ScheduledFuture<?> scheduledFutureMock = spy(ScheduledFuture.class);

		doReturn(scheduledFutureMock).when(taskSchedulerMock).schedule(any(Runnable.class), any(Trigger.class));

		final TaskSchedulingService taskSchedulingService = TaskSchedulingService
			.builder()
			.withAgentConfig(agentConfig)
			.withOtelSdkConfiguration(OtelConfigHelper.buildOtelSdkConfiguration(agentConfig))
			.withResourceSchedules(new HashMap<>())
			.withTaskScheduler(taskSchedulerMock)
			.build();

		taskSchedulingService.scheduleResourceGroupObservers();

		verify(taskSchedulerMock, times(2)).schedule(any(Runnable.class), any(Trigger.class));

		assertEquals(
			scheduledFutureMock,
			taskSchedulingService
				.getResourceSchedules()
				.get(String.format(METRICSHUB_RESOURCE_GROUP_KEY_FORMAT, SENTRY_PARIS_RESOURCE_GROUP_KEY))
		);

		assertEquals(
			scheduledFutureMock,
			taskSchedulingService
				.getResourceSchedules()
				.get(String.format(METRICSHUB_RESOURCE_GROUP_KEY_FORMAT, SENTRY_OTTAWA_RESOURCE_GROUP_KEY))
		);

		assertNull(
			taskSchedulingService
				.getResourceSchedules()
				.get(String.format(METRICSHUB_RESOURCE_GROUP_KEY_FORMAT, NO_CONFIG_RESOURCE_GROUP_KEY))
		);
	}
}
