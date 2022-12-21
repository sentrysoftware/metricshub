package com.sentrysoftware.hardware.agent.service.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.ThreadContext;

import com.sentrysoftware.hardware.agent.dto.HostConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.UserConfiguration;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.MetricsMapping;
import com.sentrysoftware.hardware.agent.service.opentelemetry.signal.OtelAlertHelper;
import com.sentrysoftware.hardware.agent.service.opentelemetry.signal.OtelHelper;
import com.sentrysoftware.hardware.agent.service.opentelemetry.signal.OtelMetadataToMetricObserver;
import com.sentrysoftware.hardware.agent.service.opentelemetry.signal.OtelParameterToMetricObserver;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.model.alert.AlertInfo;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.resources.Resource;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class StrategyTask implements Runnable {

	@NonNull
	private StrategyTaskInfo strategyTaskInfo;
	@NonNull
	private UserConfiguration userConfiguration;
	@NonNull
	private Map<String, String> otelSdkConfiguration;

	private int numberOfCollects;

	private AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk;

	private Set<String> otelInitializedMonitors = new HashSet<>();

	private Logger otelLogger;

	@Override
	public void run() {

		// Configure Logger
		final IHostMonitoring hostMonitoring = strategyTaskInfo.getHostMonitoring();
		final int discoveryCycle = strategyTaskInfo.getDiscoveryCycle();

		final String hostId = hostMonitoring.getEngineConfiguration().getHost().getId();

		configureLoggerContext(hostId);

		// Are we supposed to run the host discovery?
		if (numberOfCollects == 0) {
			log.info("Calling the engine to discover host: {}.", hostId);

			// Run detection and discovery strategies first, the collect strategy will be run when all the OpenTelemetry
			// observers are registered
			hostMonitoring.run(new DetectionOperation(), new DiscoveryOperation());

			// Initialize the OpenTelemetry observers and LogEmitter after the discovery
			// as at this time we should have what we want to observe
			initOtelSdk(hostMonitoring);

		}

		log.info("Calling the engine to collect host: {}.", hostId);

		// Make sure the engine configuration is updated correctly with the trigger
		hostMonitoring.getEngineConfiguration().setAlertTrigger(this::triggerAlertAsOtelLog);

		// One more, run only the collect strategy
		hostMonitoring.run(new CollectOperation());

		// Call the flush of all the metricInfo readers associated with this meter provider
		autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().getSdkMeterProvider().forceFlush();

		// Request the active log processor to process all logs that have not yet been processed
		autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().getSdkLoggerProvider().forceFlush();

		// Increment the number of collects
		numberOfCollects++;

		// Reset the number of collects
		if (numberOfCollects >= discoveryCycle) {
			numberOfCollects = 0;
		}
	}

	/**
	 * Trigger the metric's alert as OpenTelemetry log
	 * 
	 * @param alertInfo
	 */
	void triggerAlertAsOtelLog(@NonNull final AlertInfo alertInfo) {

		// Is alerts disabled?
		if (Boolean.TRUE.equals(userConfiguration.getHostConfigurationDto().getDisableAlerts())) {
			return;
		}

		final String message = OtelAlertHelper.buildHardwareProblem(alertInfo,
				userConfiguration.getHostConfigurationDto().getHardwareProblemTemplate());

		final Severity severity = OtelAlertHelper.convertToOtelSeverity(alertInfo);

		// Emit the log to OpenTelemetry Collector
		otelLogger
			.logRecordBuilder()
			.setBody(message)
			.setSeverity(severity)
			.setSeverityText(severity.name())
			.setAllAttributes(autoConfiguredOpenTelemetrySdk.getResource().getAttributes())
			.setContext(Context.current())
			.setEpoch(OtelAlertHelper.getAlertTime(alertInfo), TimeUnit.MILLISECONDS)
			.emit();

	}

	/**
	 * Initialize OpenTelemetry observers and {@link LogEmitter}
	 * 
	 * @param hostMonitoring The instance where all the monitors are managed
	 */
	void initOtelSdk(final IHostMonitoring hostMonitoring) {
		// Create a resource if it hasn't been created by the previous cycle
		if (autoConfiguredOpenTelemetrySdk == null) {

			// Create the resource
			final Monitor hostMonitor = hostMonitoring.getHostMonitor();
			final HostConfigurationDto hostConfigurationDto = userConfiguration.getHostConfigurationDto();

			final Resource resource = OtelHelper.createHostResource(
					hostMonitor.getId(),
					hostConfigurationDto.getHost().getHostname(),
					hostConfigurationDto.getHost().getType(),
					hostMonitor.getFqdn(),
					userConfiguration.getMultiHostsConfigurationDto().isResolveHostnameToFqdn(),
					hostConfigurationDto.getExtraLabels(),
					userConfiguration.getMultiHostsConfigurationDto().getExtraLabels()
			);

			autoConfiguredOpenTelemetrySdk = OtelHelper.initOpenTelemetrySdk(resource, otelSdkConfiguration);

			// Instantiate the LogEmitter
			otelLogger = autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().getSdkLoggerProvider()
					.get(hostConfigurationDto.getHost().getId());
		}

		hostMonitoring
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.filter(monitor -> !otelInitializedMonitors.contains(monitor.getId())) // Skip initialized monitors
			.forEach(monitor -> {
				initParameterObersvers(monitor);
				initMetadataToMetricObservers(monitor);
				otelInitializedMonitors.add(monitor.getId());
			});

	}

	/**
	 * Initialize metadata to metricInfo transformation observers
	 * 
	 * @param monitor the monitor we wish to observe its metadata as metrics through
	 *                OpenTelemetry
	 */
	void initMetadataToMetricObservers(final Monitor monitor) {
		MetricsMapping
			.getMatrixMetadataToMetricMap()
			.entrySet()
			.stream()
			.filter(moTypeEntry -> monitor.getMonitorType().equals(moTypeEntry.getKey()))
			.forEach(moTypeEntry -> moTypeEntry
					.getValue()
					.entrySet()
					.forEach(metricEntry ->
						OtelMetadataToMetricObserver
							.builder()
							.monitor(monitor)
							.matrixMetadata(metricEntry.getKey())
							.metricInfoList(metricEntry.getValue())
							.sdkMeterProvider(autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().getSdkMeterProvider())
							.multiHostsConfigurationDto(userConfiguration.getMultiHostsConfigurationDto())
							.hostMonitoring(strategyTaskInfo.getHostMonitoring())
							.build()
							.init()
					)
			);

	}

	/**
	 * Initialize parameter observers
	 * 
	 * @param monitor the monitor we wish to observe its metrics through OpenTelemetry
	 */
	void initParameterObersvers(final Monitor monitor) {
		MetricsMapping
			.getMatrixParamToMetricMap()
			.entrySet()
			.stream()
			.filter(entry -> monitor.getMonitorType().equals(entry.getKey()))
			.forEach(entry -> 
				entry.getValue().forEach((parameterName, metricInfoList) -> OtelParameterToMetricObserver
					.builder()
					.monitor(monitor)
					.metricInfoList(metricInfoList)
					.matrixParameterName(parameterName)
					.multiHostsConfigurationDto(userConfiguration.getMultiHostsConfigurationDto())
					.sdkMeterProvider(autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().getSdkMeterProvider())
					.build()
					.init() // Initialize using the current monitor/parameter context
				)
		);
	}

	/**
	 * Configure the logger context with the hostId, port, debugMode and outputDirectory.
	 *
	 * @param hostId	The unique identifier of the host
	 */
	void configureLoggerContext(final String hostId) {

		ThreadContext.put("logId", String.format("hws-agent-%s", hostId));
		ThreadContext.put("loggerLevel", strategyTaskInfo.getLoggerLevel());

		String outputDirectory = strategyTaskInfo.getOutputDirectory();
		if (outputDirectory  != null) {
			ThreadContext.put("outputDirectory", outputDirectory);
		}
	}

}