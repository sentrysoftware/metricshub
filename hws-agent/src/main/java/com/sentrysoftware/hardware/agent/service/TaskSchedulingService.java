package com.sentrysoftware.hardware.agent.service;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.agent.configuration.ConfigHelper;
import com.sentrysoftware.hardware.agent.dto.HostConfigurationDTO;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.agent.dto.UserConfiguration;
import com.sentrysoftware.hardware.agent.service.opentelemetry.OtelHelper;
import com.sentrysoftware.hardware.agent.service.opentelemetry.OtelSelfObserver;
import com.sentrysoftware.hardware.agent.service.task.FileWatcherTask;
import com.sentrysoftware.hardware.agent.service.task.StrategyTask;
import com.sentrysoftware.hardware.agent.service.task.StrategyTaskInfo;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.resources.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TaskSchedulingService {

	@Value("${server.port:8080}")
	private int serverPort;

	@Autowired
	private File configFile;

	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	@Autowired
	private MultiHostsConfigurationDTO multiHostsConfigurationDto;

	@Autowired
	private Map<String, IHostMonitoring> hostMonitoringMap;

	@Autowired
	private Map<String, ScheduledFuture<?>> targetSchedules;

	@Autowired
	private MetricReaderFactory periodicReaderFactory;

	@Autowired
	private Map<String, String> agentInfo;

	@PostConstruct
	public void startScheduling() {

		configureLoggerLevel();

		// Schedule self observer
		scheduleAgentSelfObserver();

		// Loop over each target and get its id then schedule the host monitoring strategy task
		multiHostsConfigurationDto
			.getTargets()
			.forEach(this::scheduleTargetTask);

		FileWatcherTask.builder()
			.file(configFile)
			.filter(event -> event.context() != null && configFile.getName().equals(event.context().toString()))
			.await(500)
			.onChange(() -> updateConfiguration(configFile))
			.build()
			.start();
	}

	/**
	 * Initialize the {@link OtelSelfObserver} and trigger a periodic task to flush
	 * the metrics
	 */
	void scheduleAgentSelfObserver() {

		final Resource resource = OtelHelper.createServiceResource(agentInfo.get("project_name"));
		final SdkMeterProvider sdkMeterProvider = OtelHelper.initOpenTelemetryMetrics(resource, periodicReaderFactory);

		// Need a periodic trigger because we need the job to be scheduled based on the configured collect period
		final PeriodicTrigger trigger = new PeriodicTrigger(multiHostsConfigurationDto.getCollectPeriod(), TimeUnit.SECONDS);

		// Init the observer
		OtelSelfObserver
			.builder()
			.agentInfo(agentInfo)
			.sdkMeterProvider(sdkMeterProvider)
			.multiHostsConfigurationDTO(multiHostsConfigurationDto)
			.build()
			.init();

		// Here we go
		taskScheduler.schedule(sdkMeterProvider::forceFlush, trigger);
	}

	/**
	 * Configure the 'com.sentrysoftware' logger based on the user's command. See
	 * log4j2.xml
	 */
	void configureLoggerLevel() {

		final Logger logger = LogManager.getLogger("com.sentrysoftware");

		Configurator.setAllLevels(logger.getName(), getLoggerLevel(multiHostsConfigurationDto.getLoggerLevel()));
	}

	/**
	 * Get the Log4j log level from the configured logLevel string
	 *
	 * @param loggerLevel string value from the configuration (e.g. off, debug, info, warn, error, trace, all)
	 * @return log4j {@link Level} instance
	 */
	Level getLoggerLevel(final String loggerLevel) {

		final Level level = loggerLevel != null ? Level.getLevel(loggerLevel.toUpperCase()) : null;

		return level != null ? level : Level.OFF;
	}

	/**
	 * Update the configuration:
	 * <ol>
	 * <li>Remove the obsolete targets</li>
	 * <li>Cancel the scheduling of the obsolete targets</li>
	 * <li>Schedule the new targets</li>
	 * </ol>
	 *
	 * @param configFile the target configuration file (YAML file: hws-config.yaml)
	 */
	void updateConfiguration(final File configFile) {

		final MultiHostsConfigurationDTO newMultiHostsConfigurationDto = ConfigHelper
				.readConfigurationSafe(configFile);

		// Update global settings
		multiHostsConfigurationDto.setCollectPeriod(newMultiHostsConfigurationDto.getCollectPeriod());
		multiHostsConfigurationDto.setDiscoveryCycle(newMultiHostsConfigurationDto.getDiscoveryCycle());
		multiHostsConfigurationDto.setExportTimestamps(newMultiHostsConfigurationDto.isExportTimestamps());
		multiHostsConfigurationDto.setOutputDirectory(newMultiHostsConfigurationDto.getOutputDirectory());
		multiHostsConfigurationDto.setLoggerLevel(newMultiHostsConfigurationDto.getLoggerLevel());
		multiHostsConfigurationDto.setExtraLabels(newMultiHostsConfigurationDto.getExtraLabels());
		multiHostsConfigurationDto.setExtraMetrics(newMultiHostsConfigurationDto.getExtraMetrics());

		// Make sure the logger is configured correctly
		configureLoggerLevel();

		// Do we have new targets?
		final Set<HostConfigurationDTO> newTargets = newMultiHostsConfigurationDto
				.getTargets()
				.stream()
				.filter(target -> multiHostsConfigurationDto
						.getTargets()
						.stream()
						.noneMatch(target::equals))
				.collect(Collectors.toSet());

		// Any target to remove?
		final Set<HostConfigurationDTO> targetsToRemove = multiHostsConfigurationDto
				.getTargets()
				.stream()
				.filter(existing -> newMultiHostsConfigurationDto
						.getTargets()
						.stream()
						.noneMatch(existing::equals))
				.collect(Collectors.toSet());

		// Clean the scheduling, host monitoring and the internal configuration for the targets that need to be removed
		targetsToRemove
			.stream()
			.map(target -> target.getTarget().getId())
			.forEach(targetId -> {
				// Remove the scheduled task
				removeScheduledTask(targetId);

				// Remove the host monitoring
				hostMonitoringMap.remove(targetId);

				// Remove targets from the DTO
				multiHostsConfigurationDto.getTargets().removeAll(targetsToRemove);
			});

		// Now reschedule the new targets

		// First create new HostMonitoring instances for the new targets
		newTargets.forEach(newTarget ->
			ConfigHelper.fillHostMonitoringMap(
					hostMonitoringMap,
					ConnectorStore.getInstance().getConnectors().keySet(),
					newTarget
			)
		);

		// Then update the existing multi-hosts configuration DTO for the next schedules
		multiHostsConfigurationDto.getTargets().addAll(newTargets);

		// Update the Scheduler job pool size
		// Just before the last step so that we are sure we are not going to schedule canceled tasks
		if (newMultiHostsConfigurationDto.getJobPoolSize() != multiHostsConfigurationDto.getJobPoolSize()) {
			multiHostsConfigurationDto.setJobPoolSize(newMultiHostsConfigurationDto.getJobPoolSize());

			// The scheduler job pool size can be increased or decreased dynamically
			taskScheduler.setPoolSize(multiHostsConfigurationDto.getJobPoolSize());
		}

		// Finally schedule tasks for the new added or updated targets
		newTargets.forEach(this::scheduleTargetTask);
	}

	/**
	 * Schedule a task for the given {@link HostConfigurationDTO}
	 *
	 * @param hostConfigDto The user's host configuration
	 */
	void scheduleTargetTask(final HostConfigurationDTO hostConfigDto) {
		final String targetId = hostConfigDto.getTarget().getId();
		final IHostMonitoring hostMonitoring = hostMonitoringMap.get(targetId);

		// No host monitoring no schedule
		if (hostMonitoring == null) {
			log.warn("There is no HostMonitoring for the target id: {}. Skip task schedule.", targetId);
			return;
		}

		// Create the runnable task
		final StrategyTask strategyTask = new StrategyTask(
				StrategyTaskInfo
					.builder()
					.hostMonitoring(hostMonitoring)
					.discoveryCycle(hostConfigDto.getDiscoveryCycle())
					.loggerLevel(getLoggerLevel(hostConfigDto.getLoggerLevel()).name())
					.outputDirectory(hostConfigDto.getOutputDirectory())
					.serverPort(serverPort)
					.build(),
				new UserConfiguration(multiHostsConfigurationDto, hostConfigDto),
				periodicReaderFactory
		);

		// Need a periodic trigger because we need the job to be scheduled based on the configured collect period
		final PeriodicTrigger trigger = new PeriodicTrigger(hostConfigDto.getCollectPeriod(), TimeUnit.SECONDS);

		// Here we go
		final ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(strategyTask, trigger);

		// Don't forget to store the scheduled task in case we want to cancel it due to a configuration change
		targetSchedules.put(targetId, scheduledFuture);

		log.info("Scheduled Job for target id {}", targetId);
	}

	/**
	 * Remove a specific scheduled task
	 *
	 * @param targetId unique identifier of the target
	 */
	void removeScheduledTask(String targetId) {
		final ScheduledFuture<?> scheduledFuture = targetSchedules.get(targetId);
		if (scheduledFuture != null) {
			scheduledFuture.cancel(true);
			targetSchedules.remove(targetId);
		}
	}

}
