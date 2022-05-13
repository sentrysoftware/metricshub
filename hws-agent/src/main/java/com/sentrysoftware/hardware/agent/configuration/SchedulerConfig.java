package com.sentrysoftware.hardware.agent.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;

@Configuration
public class SchedulerConfig {

	@Bean
	public ThreadPoolTaskScheduler taskScheduler(final MultiHostsConfigurationDto multiHostsConfigurationDto) {

		return createScheduler(multiHostsConfigurationDto.getJobPoolSize(), "hws-agent-task-");
	}

	@Bean
	public <T> Map<String, ScheduledFuture<T>> targetSchedules() {
		return new HashMap<>();
	}

	/**
	 * Create and initialize a scheduler instance
	 * 
	 * @param poolSize         maximum number of threads that the scheduler is able
	 *                         to run in parallel
	 * @param threadNamePrefix the prefix of all the thread names
	 * @return new instance of {@link ThreadPoolTaskScheduler}
	 */
	private ThreadPoolTaskScheduler createScheduler(final int poolSize, final String threadNamePrefix) {
		// Create the TaskScheduler
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

		// Set the maximum pool size.
		threadPoolTaskScheduler.setPoolSize(poolSize);

		// Set the thread name prefix to hws-agent-task-
		threadPoolTaskScheduler.setThreadNamePrefix(threadNamePrefix);

		// Initialization
		threadPoolTaskScheduler.initialize();

		return threadPoolTaskScheduler;
	}

}
