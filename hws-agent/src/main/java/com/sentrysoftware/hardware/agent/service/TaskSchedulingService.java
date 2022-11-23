package com.sentrysoftware.hardware.agent.service;

import static com.sentrysoftware.hardware.agent.configuration.AgentInfoConfig.AGENT_INFO_NAME_ATTRIBUTE_KEY;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.agent.configuration.ConfigHelper;
import com.sentrysoftware.hardware.agent.configuration.OtelCollectorProcessConfig;
import com.sentrysoftware.hardware.agent.configuration.OtelConfig;
import com.sentrysoftware.hardware.agent.dto.HostConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.UserConfiguration;
import com.sentrysoftware.hardware.agent.dto.opentelemetry.OtelCollectorConfigDto;
import com.sentrysoftware.hardware.agent.dto.opentelemetry.OtelCollectorOutput;
import com.sentrysoftware.hardware.agent.process.config.ProcessConfig;
import com.sentrysoftware.hardware.agent.service.opentelemetry.process.OtelCollectorProcess;
import com.sentrysoftware.hardware.agent.service.opentelemetry.signal.OtelHelper;
import com.sentrysoftware.hardware.agent.service.opentelemetry.signal.OtelSelfObserver;
import com.sentrysoftware.hardware.agent.service.task.FileWatcherTask;
import com.sentrysoftware.hardware.agent.service.task.StrategyTask;
import com.sentrysoftware.hardware.agent.service.task.StrategyTaskInfo;
import com.sentrysoftware.matrix.common.helpers.MapHelper;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
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
	private MultiHostsConfigurationDto multiHostsConfigurationDto;

	@Autowired
	private Map<String, IHostMonitoring> hostMonitoringMap;

	@Autowired
	private Map<String, ScheduledFuture<?>> hostSchedules;

	@Autowired
	private Map<String, String> agentInfo;

	@Autowired
	private Map<String, String> otelSdkConfiguration;

	@Autowired
	private OtelCollectorProcess otelCollectorProcess;

	@Autowired
	private ProcessConfig processConfig;

	@PostConstruct
	public void startScheduling() {

		configureGlobalLogger(multiHostsConfigurationDto, serverPort);

		startOtelCollector();

		// Schedule self observer
		scheduleAgentSelfObserver();

		// Loop over each host and get its id then schedule the host monitoring strategy task
		multiHostsConfigurationDto
			.getResolvedHosts()
			.forEach(this::scheduleHostTask);

		FileWatcherTask.builder()
			.file(configFile)
			.filter(event -> event.context() != null && configFile.getName().equals(event.context().toString()))
			.await(500)
			.onChange(() -> updateConfiguration(configFile))
			.build()
			.start();
	}

	/**
	 * Start the OpenTelemetry Collector process. If the configuration specifies
	 * that the collector must be disabled, this method skips the collector startup.
	 */
	void startOtelCollector() {

		if (multiHostsConfigurationDto.getOtelCollector().isDisabled()) {
			log.info("The Hardware Sentry Agent is configured to not start the OpenTelemetry Collector.");
			return;
		}

		// The subprocess must be ran using a separate thread so that it creates a log context for himself only.
		// The process output will be redirected to a dedicated file, see how log4j2.xml is configured using the 
		// thread context pattern.
		final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

		// Run the execution
		singleThreadExecutor.submit(() -> {
			// Configure logger 
			ThreadContext.put("logId", OtelCollectorConfigDto.EXECUTABLE_OUTPUT_ID);

			// Get the global application level
			final Level globalLevel = getLoggerLevel(multiHostsConfigurationDto.getLoggerLevel());

			String loggerLevel = globalLevel.name();
			// User might want to disable logging for the process
			if (multiHostsConfigurationDto.getOtelCollector().getOutput() != OtelCollectorOutput.LOG) {
				loggerLevel = Level.OFF.name();
			}

			ThreadContext.put("loggerLevel", loggerLevel);

			String outputDirectory = multiHostsConfigurationDto.getOutputDirectory();
			if (outputDirectory  != null) {
				ThreadContext.put("outputDirectory", outputDirectory);
			}

			// Start the executable
			try {
				otelCollectorProcess.start();
			} catch (Exception e) {
				log.error("Could not start process using command line: {}.", otelCollectorProcess.getProcessConfig().getCommandLine());
				log.debug("Error: ", e);
			}
		});

		try {
			singleThreadExecutor.awaitTermination(multiHostsConfigurationDto.getOtelCollector().getStartupDelay(), TimeUnit.SECONDS);
		} catch (Exception e) { // NOSONAR
			log.error(
				"Startup process has been interrupted after {} seconds. Command line: {}.",
				multiHostsConfigurationDto.getOtelCollector().getStartupDelay(),
				otelCollectorProcess.getProcessConfig().getCommandLine()
			);
			log.debug("Error: ", e);
		}

	}

	/**
	 * Initialize the {@link OtelSelfObserver} and trigger a periodic task to flush
	 * the metrics
	 */
	void scheduleAgentSelfObserver() {

		// Create the service resource
		final Resource resource = OtelHelper
			.createServiceResource(
				agentInfo.get(AGENT_INFO_NAME_ATTRIBUTE_KEY),
				multiHostsConfigurationDto.getExtraLabels()
			);

		final AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk = OtelHelper.initOpenTelemetrySdk(resource, otelSdkConfiguration);

		// Need a periodic trigger because we need the job to be scheduled based on the configured collect period
		final PeriodicTrigger trigger = new PeriodicTrigger(multiHostsConfigurationDto.getCollectPeriod(), TimeUnit.SECONDS);

		// Get the SDK Meter provider
		final SdkMeterProvider meterProvider = autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().getSdkMeterProvider();

		// Init the observer
		OtelSelfObserver
			.builder()
			.agentInfo(agentInfo)
			.sdkMeterProvider(meterProvider)
			.multiHostsConfigurationDto(multiHostsConfigurationDto)
			.build()
			.init();

		// Here we go
		taskScheduler.schedule(meterProvider::forceFlush, trigger);
	}

	/**
	 * Configure the 'com.sentrysoftware' logger based on the user's command.<br>
	 * See src/main/resources/log4j2.xml
	 * 
	 * @param multiHostsConfigDto User's configuration
	 * @param serverPort          Application port number
	 */
	public static void configureGlobalLogger(final MultiHostsConfigurationDto multiHostsConfigDto, final int serverPort) {

		final Level loggerLevel = getLoggerLevel(multiHostsConfigDto.getLoggerLevel());

		ThreadContext.put("logId", String.format("hws-agent-%d-global", serverPort));
		ThreadContext.put("loggerLevel", loggerLevel.toString());

		final String outputDirectory = multiHostsConfigDto.getOutputDirectory();
		if (outputDirectory  != null) {
			ThreadContext.put("outputDirectory", outputDirectory);
		}
	}

	/**
	 * Get the Log4j log level from the configured logLevel string
	 *
	 * @param loggerLevel string value from the configuration (e.g. off, debug, info, warn, error, trace, all)
	 * @return log4j {@link Level} instance
	 */
	static Level getLoggerLevel(final String loggerLevel) {

		final Level level = loggerLevel != null ? Level.getLevel(loggerLevel.toUpperCase()) : null;

		return level != null ? level : Level.OFF;
	}

	/**
	 * Update the configuration:
	 * <ol>
	 * <li>Remove the obsolete hosts</li>
	 * <li>Cancel the scheduling of the obsolete hosts</li>
	 * <li>Schedule the new hosts</li>
	 * </ol>
	 *
	 * @param configFile the host configuration file (YAML file: hws-config.yaml)
	 */
	synchronized void updateConfiguration(final File configFile) {

		final MultiHostsConfigurationDto newMultiHostsConfigurationDto = ConfigHelper
				.readConfigurationSafe(configFile);

		// Update global settings
		multiHostsConfigurationDto.setCollectPeriod(newMultiHostsConfigurationDto.getCollectPeriod());
		multiHostsConfigurationDto.setDiscoveryCycle(newMultiHostsConfigurationDto.getDiscoveryCycle());
		multiHostsConfigurationDto.setExportTimestamps(newMultiHostsConfigurationDto.isExportTimestamps());
		multiHostsConfigurationDto.setOutputDirectory(newMultiHostsConfigurationDto.getOutputDirectory());
		multiHostsConfigurationDto.setLoggerLevel(newMultiHostsConfigurationDto.getLoggerLevel());
		multiHostsConfigurationDto.setExtraLabels(newMultiHostsConfigurationDto.getExtraLabels());
		multiHostsConfigurationDto.setExtraMetrics(newMultiHostsConfigurationDto.getExtraMetrics());
		multiHostsConfigurationDto.setSequential(newMultiHostsConfigurationDto.isSequential());
		multiHostsConfigurationDto.setResolveHostnameToFqdn(newMultiHostsConfigurationDto.isResolveHostnameToFqdn());
		multiHostsConfigurationDto.setExporter(newMultiHostsConfigurationDto.getExporter());
		multiHostsConfigurationDto.setHardwareProblemTemplate(newMultiHostsConfigurationDto.getHardwareProblemTemplate());
		multiHostsConfigurationDto.setDisableAlerts(newMultiHostsConfigurationDto.isDisableAlerts());

		// Make sure the logger is configured correctly
		configureGlobalLogger(multiHostsConfigurationDto, serverPort);

		// Configuration updated? if yes stop then start the collector
		if (!newMultiHostsConfigurationDto.getOtelCollector().equals(multiHostsConfigurationDto.getOtelCollector())) {

			// Stop the collector
			otelCollectorProcess.stop();

			// Update the global configuration
			multiHostsConfigurationDto.setOtelCollector(newMultiHostsConfigurationDto.getOtelCollector());

			final OtelCollectorProcessConfig config = new  OtelCollectorProcessConfig();

			// Get a new ProcessConfig based on what the user has configured
			final ProcessConfig newProcessConfig = config.processConfig(newMultiHostsConfigurationDto);

			// Update the process configuration
			processConfig.update(newProcessConfig);

			// Start the OpenTelemetry Collector
			startOtelCollector();
		}

		final Map<String, String> newOtelSdkConfiguration = new OtelConfig().otelSdkConfiguration(newMultiHostsConfigurationDto);

		// The SDK configuration has been updated? reschedule everything
		if (!MapHelper.areEqual(otelSdkConfiguration, newOtelSdkConfiguration)) {
			restartAll(newOtelSdkConfiguration, newMultiHostsConfigurationDto);
			return;
		}

		// Do we have new hosts?
		final Set<HostConfigurationDto> newHosts = newMultiHostsConfigurationDto
				.getHosts()
				.stream()
				.filter(host -> multiHostsConfigurationDto
						.getHosts()
						.stream()
						.noneMatch(host::equals))
				.collect(Collectors.toSet());

		// Any host to remove?
		final Set<HostConfigurationDto> hostsToRemove = multiHostsConfigurationDto
				.getHosts()
				.stream()
				.filter(existing -> newMultiHostsConfigurationDto
						.getHosts()
						.stream()
						.noneMatch(existing::equals))
				.collect(Collectors.toSet());

		// Clean the scheduling, host monitoring and the internal configuration for the hosts that need to be removed
		rescheduleNewHosts(newMultiHostsConfigurationDto, newHosts, hostsToRemove);
	}

	/**
	 * Restart all the hosts scheduling
	 *
	 * @param newOtelSdkConfiguration       The new SDK configuration
	 * @param newMultiHostsConfigurationDto The new configuration
	 */
	void restartAll(final Map<String, String> newOtelSdkConfiguration, final MultiHostsConfigurationDto newMultiHostsConfigurationDto) {

		// Update the SDK configuration
		otelSdkConfiguration.putAll(newOtelSdkConfiguration);

		// All the hosts are considered as new
		final Set<HostConfigurationDto> newHosts = newMultiHostsConfigurationDto.getHosts();

		// All the existing hosts are considered as old
		final Set<HostConfigurationDto> hostsToRemove = multiHostsConfigurationDto.getHosts();

		// Now reschedule the new hosts
		rescheduleNewHosts(newMultiHostsConfigurationDto, newHosts, hostsToRemove);
	}

	/**
	 * Reschedule the given new hosts
	 *
	 * @param newMultiHostsConfigurationDto The new configuration
	 * @param newHosts                      The new configured hosts
	 * @param hostsToRemove                 The hosts to remove from the scheduler
	 */
	private void rescheduleNewHosts(final MultiHostsConfigurationDto newMultiHostsConfigurationDto,
			final Set<HostConfigurationDto> newHosts, final Set<HostConfigurationDto> hostsToRemove) {

		hostsToRemove
			.stream()
			.flatMap(host ->
				host.isHostGroup() ? host.resolveHostGroups().stream() : Stream.of(host)
			)
			.map(host -> host.getHost().getId())
			.forEach(hostId -> {
				// Remove the scheduled task
				removeScheduledTask(hostId);

				// Remove the host monitoring
				hostMonitoringMap.remove(hostId);
			});

		// First create new HostMonitoring instances for the new hosts
		newHosts
		.stream()
		.flatMap(newHost ->
			newHost.isHostGroup() ? newHost.resolveHostGroups().stream() : Stream.of(newHost)
		)
		.forEach(newHost ->
			ConfigHelper.fillHostMonitoringMap(
				hostMonitoringMap,
				ConnectorStore.getInstance().getConnectors().keySet(),
				newHost
			)
		);

		// Remove hosts from the DTO
		multiHostsConfigurationDto.getHosts().removeAll(hostsToRemove);

		// Then update the existing multi-hosts configuration DTO for the next schedules
		multiHostsConfigurationDto.getHosts().addAll(newHosts);

		// Update the Scheduler job pool size
		// Just before the last step so that we are sure we are not going to schedule canceled tasks
		if (newMultiHostsConfigurationDto.getJobPoolSize() != multiHostsConfigurationDto.getJobPoolSize()) {
			multiHostsConfigurationDto.setJobPoolSize(newMultiHostsConfigurationDto.getJobPoolSize());

			// The scheduler job pool size can be increased or decreased dynamically
			taskScheduler.setPoolSize(multiHostsConfigurationDto.getJobPoolSize());
		}

		// Finally schedule tasks for the new added or updated hosts
		newHosts
			.stream()
			.flatMap(host ->
				host.isHostGroup() ? host.resolveHostGroups().stream() : Stream.of(host)
			)
			.forEach(this::scheduleHostTask);
	}

	/**
	 * Schedule a task for the given {@link HostConfigurationDto}
	 *
	 * @param hostConfigDto The user's host configuration
	 */
	void scheduleHostTask(final HostConfigurationDto hostConfigDto) {
		final String hostId = hostConfigDto.getHost().getId();
		final IHostMonitoring hostMonitoring = hostMonitoringMap.get(hostId);

		// No host monitoring no schedule
		if (hostMonitoring == null) {
			log.warn("The host {} has been removed from the configuration. Nothing to monitor and no tasks to schedule.", hostId);
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
				otelSdkConfiguration
		);

		// Need a periodic trigger because we need the job to be scheduled based on the configured collect period
		final PeriodicTrigger trigger = new PeriodicTrigger(hostConfigDto.getCollectPeriod(), TimeUnit.SECONDS);

		// Here we go
		final ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(strategyTask, trigger);

		// Don't forget to store the scheduled task in case we want to cancel it due to a configuration change
		hostSchedules.put(hostId, scheduledFuture);

		log.info("Scheduled job for host id {}", hostId);
	}

	/**
	 * Remove a specific scheduled task
	 *
	 * @param hostId unique identifier of the host
	 */
	void removeScheduledTask(String hostId) {
		final ScheduledFuture<?> scheduledFuture = hostSchedules.get(hostId);
		if (scheduledFuture != null) {
			scheduledFuture.cancel(true);
			hostSchedules.remove(hostId);
		}
	}

}
