package com.sentrysoftware.hardware.agent.service.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.ThreadContext;

import com.sentrysoftware.hardware.agent.dto.HostConfigurationDTO;
import com.sentrysoftware.hardware.agent.dto.UserConfiguration;
import com.sentrysoftware.hardware.agent.service.opentelemetry.MetricsMapping;
import com.sentrysoftware.hardware.agent.service.opentelemetry.OtelHelper;
import com.sentrysoftware.hardware.agent.service.opentelemetry.OtelMetadataToMetricObserver;
import com.sentrysoftware.hardware.agent.service.opentelemetry.OtelParameterToMetricObserver;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
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
	private MetricReaderFactory periodicReaderFactory;

	private int numberOfCollects;

	private SdkMeterProvider sdkMeterProvider;

	private Set<String> otelInitializedMonitors = new HashSet<>();

	@Override
	public void run() {

		// Configure Logger
		final IHostMonitoring hostMonitoring = strategyTaskInfo.getHostMonitoring();
		final int discoveryCycle = strategyTaskInfo.getDiscoveryCycle();

		final String targetId = hostMonitoring.getEngineConfiguration().getTarget().getId();

		configureLoggerContext(targetId);

		// Are we supposed to run the target discovery?
		if (numberOfCollects == 0) {
			log.info("Calling the engine to discover target: {}.", targetId);

			// Run detection and discovery strategies first, the collect strategy will be run when all the OpenTelemetry
			// observers are registered
			hostMonitoring.run(new DetectionOperation(), new DiscoveryOperation());

			// Initialize the OpenTelemetry observers after the discovery
			// at this time we should have what we want to observe
			initOtelObservers(hostMonitoring);

		}

		log.info("Calling the engine to collect target: {}.", targetId);

		// One more, run only the collect strategy
		hostMonitoring.run(new CollectOperation());

		// Call the flush of all the metricInfo readers associated with this meter provider
		sdkMeterProvider.forceFlush();

		// Increment the number of collects
		numberOfCollects++;

		// Reset the number of collects
		if (numberOfCollects >= discoveryCycle) {
			numberOfCollects = 0;
		}
	}

	/**
	 * Initialize OpenTelemetry observers
	 * 
	 * @param hostMonitoring The instance where all the monitors are managed
	 */
	void initOtelObservers(final IHostMonitoring hostMonitoring) {
		// Create a resource if it hasn't been created by the previous cycle
		if (sdkMeterProvider == null) {

			// Create the resource
			final Monitor targetMonitor = hostMonitoring.getTargetMonitor();
			final HostConfigurationDTO hostConfigurationDTO = userConfiguration.getHostConfigurationDTO();

			final Resource resource = OtelHelper.createHostResource(
					targetMonitor.getId(),
					hostConfigurationDTO.getTarget().getHostname(),
					hostConfigurationDTO.getTarget().getType(),
					targetMonitor.getFqdn(),
					userConfiguration.getMultiHostsConfigurationDTO().isResolveHostnameToFqdn(),
					hostConfigurationDTO.getExtraLabels(),
					userConfiguration.getMultiHostsConfigurationDTO().getExtraLabels()
			);

			sdkMeterProvider = OtelHelper.initOpenTelemetryMetrics(resource, periodicReaderFactory);
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
							.metricInfo(metricEntry.getValue())
							.sdkMeterProvider(sdkMeterProvider)
							.multiHostsConfigurationDTO(userConfiguration.getMultiHostsConfigurationDTO())
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
				entry.getValue().forEach((parameterName, metricInfo) -> OtelParameterToMetricObserver
					.builder()
					.monitor(monitor)
					.metricInfo(metricInfo)
					.matrixParameterName(parameterName)
					.multiHostsConfigurationDTO(userConfiguration.getMultiHostsConfigurationDTO())
					.sdkMeterProvider(sdkMeterProvider)
					.build()
					.init() // Initialize using the current monitor/parameter context
				)
		);
	}

	/**
	 * Configure the logger context with the targetId, port, debugMode and outputDirectory.
	 *
	 * @param targetId	The unique identifier of the target
	 */
	void configureLoggerContext(final String targetId) {

		ThreadContext.put("targetId", targetId);
		ThreadContext.put("loggerLevel", strategyTaskInfo.getLoggerLevel());
		ThreadContext.put("port", String.valueOf(strategyTaskInfo.getServerPort()));

		String outputDirectory = strategyTaskInfo.getOutputDirectory();
		if (outputDirectory  != null) {
			ThreadContext.put("outputDirectory", outputDirectory);
		}
	}

}