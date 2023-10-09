package com.sentrysoftware.matrix.agent.service.task;

import static com.sentrysoftware.matrix.agent.helper.ConfigHelper.getLoggerLevel;

import com.sentrysoftware.matrix.agent.config.ResourceConfig;
import com.sentrysoftware.matrix.agent.helper.OtelHelper;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.collect.CollectStrategy;
import com.sentrysoftware.matrix.strategy.detection.DetectionStrategy;
import com.sentrysoftware.matrix.strategy.discovery.DiscoveryStrategy;
import com.sentrysoftware.matrix.strategy.simple.SimpleStrategy;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;

@RequiredArgsConstructor
@Data
@Slf4j
public class MonitoringTask implements Runnable {

	public static final Set<String> INTERNAL_RESOURCE_ATTRIBUTES = Set.of(
		"host.id",
		"host.name",
		"host.type",
		"os.type",
		"agent.host.name"
	);

	@NonNull
	private final MonitoringTaskInfo monitoringTaskInfo;

	private int numberOfCollects;
	private AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk;

	@Override
	public void run() {
		final TelemetryManager telemetryManager = monitoringTaskInfo.getTelemetryManager();
		final ResourceConfig resourceConfig = monitoringTaskInfo.getResourceConfig();
		final int discoveryCycle = resourceConfig.getDiscoveryCycle();

		final String hostId = telemetryManager.getHostConfiguration().getHostId();

		configureLoggerContext(hostId);

		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);

		// Are we supposed to run the discovery?
		if (numberOfCollects == 0) {
			log.info("Calling the engine to discover resource: {}.", hostId);

			// Run detection and discovery strategies first, the collect strategy will be run when all the OpenTelemetry
			// observers are registered
			telemetryManager.run(
				new DetectionStrategy(telemetryManager, System.currentTimeMillis(), matsyaClientsExecutor),
				new DiscoveryStrategy(telemetryManager, System.currentTimeMillis(), matsyaClientsExecutor),
				new SimpleStrategy(telemetryManager, System.currentTimeMillis(), matsyaClientsExecutor)
			);

			// Initialize the OpenTelemetry observers and LogEmitter after the discovery
			// as at this time we should have what we want to observe
			initOtelSdk(telemetryManager, resourceConfig);
		}

		log.info("Calling the engine to collect resource: {}.", hostId);

		// One more, run only collect and simple strategies
		telemetryManager.run(
			new CollectStrategy(telemetryManager, System.currentTimeMillis(), matsyaClientsExecutor),
			new SimpleStrategy(telemetryManager, System.currentTimeMillis(), matsyaClientsExecutor)
		);

		// Call the flush of all the metric readers associated with this meter provider
		autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().getSdkMeterProvider().forceFlush();

		// Increment the number of collects
		numberOfCollects++;

		// Reset the number of collects
		if (numberOfCollects >= discoveryCycle) {
			numberOfCollects = 0;
		}
	}

	/**
	 * Initialize the OpenTelemetry SDK if it is not initialized
	 *
	 * @param telemetryManager Wraps all the monitors and metrics
	 * @param resourceConfig   The user's resource configuration
	 */
	private void initOtelSdk(final TelemetryManager telemetryManager, final ResourceConfig resourceConfig) {
		// Create a resource if it hasn't been created during the previous cycle
		if (autoConfiguredOpenTelemetrySdk == null) {
			// Create the resource
			final Monitor hostMonitor = telemetryManager.getHostMonitor();
			final Map<String, String> userAttributes = resourceConfig.getAttributes();

			final Map<String, String> hostAttributes;
			final com.sentrysoftware.matrix.telemetry.Resource monitorResource = hostMonitor.getResource();
			if (monitorResource != null) {
				hostAttributes = monitorResource.getAttributes();
			} else {
				hostAttributes = Map.of();
			}

			final Resource resource = OtelHelper.createHostResource(
				hostAttributes,
				userAttributes,
				resourceConfig.getResolveHostnameToFqdn()
			);

			autoConfiguredOpenTelemetrySdk =
				OtelHelper.initOpenTelemetrySdk(resource, monitoringTaskInfo.getOtelSdkConfiguration());
		}
	}

	/**
	 * Configure the logger context with the hostId, loggerLevel and outputDirectory.
	 *
	 * @param logId	Unique identifier of used by the logId context attribute.
	 */
	void configureLoggerContext(final String logId) {
		ThreadContext.put("logId", String.format("metricshub-agent-%s", logId));

		final ResourceConfig resourceConfig = monitoringTaskInfo.getResourceConfig();

		ThreadContext.put("loggerLevel", getLoggerLevel(resourceConfig.getLoggerLevel()).name());

		final String outputDirectory = resourceConfig.getOutputDirectory();
		if (outputDirectory != null) {
			ThreadContext.put("outputDirectory", outputDirectory);
		}
	}
}
