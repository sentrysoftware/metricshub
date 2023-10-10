package com.sentrysoftware.matrix.agent.service.scheduling;

import static com.sentrysoftware.matrix.agent.helper.TestConstants.HOSTNAME;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.HOST_TYPE_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.OS_LINUX;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SENTRY_PARIS_RESOURCE_GROUP_KEY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sentrysoftware.matrix.agent.config.ResourceConfig;
import com.sentrysoftware.matrix.agent.context.AgentContext;
import com.sentrysoftware.matrix.agent.service.task.MonitoringTask;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

class ResourceSchedulingTest {

	@Test
	void testSchedule() throws IOException {
		final ResourceConfig resourceConfig = ResourceConfig
			.builder()
			.loggerLevel("OFF")
			.attributes(Map.of(HOST_NAME, HOSTNAME, HOST_TYPE_ATTRIBUTE_KEY, OS_LINUX))
			.discoveryCycle(4)
			.collectPeriod(2L)
			.resolveHostnameToFqdn(true)
			.build();
		final ThreadPoolTaskScheduler taskSchedulerMock = spy(ThreadPoolTaskScheduler.class);
		final ScheduledFuture<?> scheduledFutureMock = spy(ScheduledFuture.class);

		doReturn(scheduledFutureMock).when(taskSchedulerMock).schedule(any(Runnable.class), any(Trigger.class));
		final ResourceScheduling resourceScheduling = ResourceScheduling
			.builder()
			.withHostMetricDefinitions(AgentContext.readHostMetricDefinitions())
			.withOtelSdkConfiguration(new HashMap<>())
			.withResourceConfig(resourceConfig)
			.withTelemetryManager(new TelemetryManager())
			.withTaskScheduler(taskSchedulerMock)
			.withResourceGroupKey(SENTRY_PARIS_RESOURCE_GROUP_KEY)
			.withResourceKey(HOSTNAME)
			.withSchedules(new HashMap<>())
			.withTaskScheduler(taskSchedulerMock)
			.build();

		resourceScheduling.schedule();

		verify(taskSchedulerMock, times(1)).schedule(any(MonitoringTask.class), any(PeriodicTrigger.class));
	}
}
