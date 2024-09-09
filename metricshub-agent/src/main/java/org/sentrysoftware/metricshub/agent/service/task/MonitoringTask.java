package org.sentrysoftware.metricshub.agent.service.task;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import static org.sentrysoftware.metricshub.agent.helper.ConfigHelper.getLoggerLevel;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.sentrysoftware.metricshub.agent.config.ResourceConfig;
import org.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import org.sentrysoftware.metricshub.agent.helper.OtelHelper;
import org.sentrysoftware.metricshub.agent.service.signal.MetricTypeVisitor;
import org.sentrysoftware.metricshub.agent.service.signal.SimpleUpDownCounterMetricObserver;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.strategy.collect.CollectStrategy;
import org.sentrysoftware.metricshub.engine.strategy.collect.PrepareCollectStrategy;
import org.sentrysoftware.metricshub.engine.strategy.collect.ProtocolHealthCheckStrategy;
import org.sentrysoftware.metricshub.engine.strategy.detection.DetectionStrategy;
import org.sentrysoftware.metricshub.engine.strategy.discovery.DiscoveryStrategy;
import org.sentrysoftware.metricshub.engine.strategy.simple.SimpleStrategy;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import org.sentrysoftware.metricshub.hardware.strategy.HardwarePostCollectStrategy;
import org.sentrysoftware.metricshub.hardware.strategy.HardwarePostDiscoveryStrategy;
import org.sentrysoftware.metricshub.hardware.strategy.HardwareStrategy;

/**
 * Task responsible for running the monitoring process, including detection, discovery, and collection strategies.
 */
@RequiredArgsConstructor
@Data
@Slf4j
public class MonitoringTask implements Runnable {

	private static final String HOST_CONFIGURED_METRIC_NAME = "metricshub.host.configured";

	private static final String GENERIC_METRIC_DESCRIPTION_FORMAT = "Reports Metric %s";

	@NonNull
	private final MonitoringTaskInfo monitoringTaskInfo;

	private int numberOfCollects;
	private AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk;

	private Map<String, Set<String>> initializedMetricsPerMonitorId = new HashMap<>();
	private Map<String, String> mainResourceAttributes;

	@Override
	public void run() {
		final TelemetryManager telemetryManager = monitoringTaskInfo.getTelemetryManager();
		final ResourceConfig resourceConfig = monitoringTaskInfo.getResourceConfig();
		final int discoveryCycle = resourceConfig.getDiscoveryCycle();
		final ExtensionManager extensionManager = monitoringTaskInfo.getExtensionManager();

		final String hostId = telemetryManager.getHostConfiguration().getHostId();

		configureLoggerContext(hostId);

		final ClientsExecutor clientsExecutor = new ClientsExecutor(telemetryManager);

		// Are we supposed to run the discovery?
		final long discoveryTime = System.currentTimeMillis();
		if (numberOfCollects == 0) {
			log.info("Calling the engine to discover resource: {}.", hostId);

			// Run detection and discovery strategies first, the collect strategy will be run when all the OpenTelemetry
			// observers are registered

			telemetryManager.run(
				new DetectionStrategy(telemetryManager, discoveryTime, clientsExecutor, extensionManager),
				new DiscoveryStrategy(telemetryManager, discoveryTime, clientsExecutor, extensionManager),
				new SimpleStrategy(telemetryManager, discoveryTime, clientsExecutor, extensionManager),
				new HardwarePostDiscoveryStrategy(telemetryManager, discoveryTime, clientsExecutor, extensionManager)
			);

			// Initialize the OpenTelemetry observers and LogEmitter after the discovery
			// as at this time we should have what we want to observe
			initOtelSdk(telemetryManager, resourceConfig);
		}

		log.info("Calling the engine to collect resource: {}.", hostId);

		final long collectTime = System.currentTimeMillis();

		// One more, run only prepare, collect simple and post strategies
		telemetryManager.run(
			new PrepareCollectStrategy(telemetryManager, collectTime, clientsExecutor, extensionManager),
			new ProtocolHealthCheckStrategy(telemetryManager, collectTime, clientsExecutor, extensionManager),
			new CollectStrategy(telemetryManager, collectTime, clientsExecutor, extensionManager),
			new SimpleStrategy(telemetryManager, collectTime, clientsExecutor, extensionManager),
			new HardwarePostCollectStrategy(telemetryManager, collectTime, clientsExecutor, extensionManager)
		);

		// Run the hardware strategy
		telemetryManager.run(new HardwareStrategy(telemetryManager, collectTime));

		// Initialize metric observers
		initAllObservers(telemetryManager);

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
	 * Initialize metric observers for all metrics associated with each monitor within the {@link TelemetryManager} instance.
	 *
	 * @param telemetryManager Wraps monitors and metrics
	 */
	void initAllObservers(final TelemetryManager telemetryManager) {
		// Retrieve the connector store that has been prepared within the global context
		final ConnectorStore connectorStore = telemetryManager.getConnectorStore();

		telemetryManager
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.forEach(monitor -> {
				if (monitor.isEndpointHost()) {
					// The host's metric definitions cannot be null because they are available as resources in metricshub-host-metrics.yaml
					final Map<String, MetricDefinition> hostMetricDefinitions = monitoringTaskInfo
						.getHostMetricDefinitions()
						.metrics();

					// Initialize endpoint host metric observers
					initMonitorMetricObservers(monitor, telemetryManager, hostMetricDefinitions);

					// Initialize the metricshub.host.configured metric observer
					initializeHostConfiguredMetricObserver(monitor, hostMetricDefinitions);
				} else {
					initMonitorMetricObservers(
						monitor,
						telemetryManager,
						ConfigHelper.fetchMetricDefinitions(
							connectorStore,
							monitor.getAttribute(MetricsHubConstants.MONITOR_ATTRIBUTE_CONNECTOR_ID)
						)
					);
				}
			});
	}

	/**
	 * Initialize a periodic observer for the metricshub.host.configured metric
	 *
	 * @param host                  Host monitor instance
	 * @param hostMetricDefinitions Map of the Host's metric definitions
	 */
	void initializeHostConfiguredMetricObserver(
		final Monitor host,
		final Map<String, MetricDefinition> hostMetricDefinitions
	) {
		if (!isMetricObserverNotInitialized(host.getId(), HOST_CONFIGURED_METRIC_NAME)) {
			return;
		}

		// Get the metric definition from the metric definition map
		final MetricDefinition metricDefinition = lookupMetricDefinition(
			HOST_CONFIGURED_METRIC_NAME,
			hostMetricDefinitions
		);

		// Merge main resource attributes and monitor attributes
		final Map<String, String> attributesMap = new HashMap<>();
		ConfigHelper.mergeAttributes(mainResourceAttributes, attributesMap);
		ConfigHelper.mergeAttributes(host.getAttributes(), attributesMap);

		// A registry for creating named Meters
		final SdkMeterProvider sdkMeterProvider = autoConfiguredOpenTelemetrySdk
			.getOpenTelemetrySdk()
			.getSdkMeterProvider();

		SimpleUpDownCounterMetricObserver
			.builder()
			.withMetricName(HOST_CONFIGURED_METRIC_NAME)
			.withMetricValue(1D)
			.withMeter(
				sdkMeterProvider.get(
					String.format(
						"%s.%s.%s.%s",
						monitoringTaskInfo.getResourceGroupKey(),
						monitoringTaskInfo.getResourceKey(),
						host.getId(),
						HOST_CONFIGURED_METRIC_NAME
					)
				)
			)
			.withAttributes(OtelHelper.buildOtelAttributesFromMap(attributesMap))
			.withUnit(metricDefinition.getUnit())
			.withDescription(metricDefinition.getDescription())
			.build()
			.init();

		// Set the metric's observer as initialized
		initializedMetricsPerMonitorId
			.computeIfAbsent(host.getId(), id -> new HashSet<>())
			.add(HOST_CONFIGURED_METRIC_NAME);
	}

	/**
	 * Initialize an observer for each metric in the given monitor
	 *
	 * @param monitor             {@link Monitor} instance
	 * @param telemetryManager    Wraps monitors and metrics
	 * @param metricDefinitionMap Map of Metric definitions
	 */
	void initMonitorMetricObservers(
		final Monitor monitor,
		final TelemetryManager telemetryManager,
		final Map<String, MetricDefinition> metricDefinitionMap
	) {
		monitor
			.getMetrics()
			.entrySet()
			.stream()
			.filter(entry -> Objects.nonNull(entry.getValue()))
			.filter(entry -> OtelHelper.isAcceptedKey(entry.getKey()))
			.filter(metricEntry -> isMetricObserverNotInitialized(monitor.getId(), metricEntry.getKey()))
			.forEach(metricEntry -> initMetricObserver(monitor, metricDefinitionMap, metricEntry));
	}

	/**
	 * Initialize an observer for the given metric entry
	 *
	 * @param monitor             {@link Monitor} instance
	 * @param metricDefinitionMap Map of Metric definitions (E.g. metric definitions from Hardware.yaml or Storage.yaml)
	 * @param metricEntry         Key-value where the key is the unique metric key and the value
	 *                            is the {@link AbstractMetric}
	 */
	void initMetricObserver(
		final Monitor monitor,
		final Map<String, MetricDefinition> metricDefinitionMap,
		final Entry<String, AbstractMetric> metricEntry
	) {
		// Retrieve the metric unique key
		final String metricKey = metricEntry.getKey();

		// Extract the metric name from the metric key. E.g. extract hw.power from hw.power{hw.type="fan"} // NOSONAR
		final String metricName = MetricFactory.extractName(metricKey);

		// Get the metric definition from the metric definition map
		final MetricDefinition metricDefinition = lookupMetricDefinition(metricName, metricDefinitionMap);

		// Merge main resource attributes and monitor attributes
		final Map<String, String> attributesMap = new HashMap<>();
		ConfigHelper.mergeAttributes(mainResourceAttributes, attributesMap);
		ConfigHelper.mergeAttributes(monitor.getAttributes(), attributesMap);

		final AbstractMetric metric = metricEntry.getValue();

		// A registry for creating named Meters
		final SdkMeterProvider sdkMeterProvider = autoConfiguredOpenTelemetrySdk
			.getOpenTelemetrySdk()
			.getSdkMeterProvider();

		// Build the metric attributes
		final Attributes attributes = OtelHelper.mergeOtelAttributes(
			OtelHelper.buildOtelAttributesFromMap(attributesMap),
			OtelHelper.buildOtelAttributesFromMap(metric.getAttributes())
		);

		// Initialize the metric observer using the MetricTypeVisitor
		// that handles each metric type
		metricDefinition
			.getType()
			.get()
			.getMetricKeyType()
			.accept(
				MetricTypeVisitor
					.builder()
					.withMetric(metric)
					.withMetricDefinition(metricDefinition)
					.withSdkMeterProvider(sdkMeterProvider)
					.withAttributes(attributes)
					.withMetricName(metricName)
					.withMonitorId(monitor.getId())
					.withResourceGroupKey(monitoringTaskInfo.getResourceGroupKey())
					.withResourceKey(monitoringTaskInfo.getResourceKey())
					.withStateSetCompression(monitoringTaskInfo.getResourceConfig().getStateSetCompression())
					.build()
			);

		// Set the metric's observer as initialized
		initializedMetricsPerMonitorId.computeIfAbsent(monitor.getId(), id -> new HashSet<>()).add(metricKey);
	}

	/**
	 * Search the {@link MetricDefinition} instance defined for the given metric.
	 *
	 * @param metricName          The name of the metric e.g. hw.status.
	 * @param metricDefinitionMap All the existing metric definitions.
	 * @return {@link MetricDefinition} instance, never <code>null</code>.
	 */
	static MetricDefinition lookupMetricDefinition(
		final String metricName,
		final Map<String, MetricDefinition> metricDefinitionMap
	) {
		return Optional
			.ofNullable(metricDefinitionMap.get(metricName))
			.orElseGet(() ->
				MetricDefinition.builder().description(String.format(GENERIC_METRIC_DESCRIPTION_FORMAT, metricName)).build()
			);
	}

	/**
	 * Check if the metric observer is not initialized for the given monitor id and metric key
	 *
	 * @param monitorId Unique id of the monitor
	 * @param metricKey Metric key. E.g. hw.energy{hw.type="fan"}
	 *
	 * @return boolean value.
	 */
	boolean isMetricObserverNotInitialized(final String monitorId, final String metricKey) {
		final Set<String> metricKeys = initializedMetricsPerMonitorId.get(monitorId);
		return metricKeys == null || !metricKeys.contains(metricKey);
	}

	/**
	 * Initialize the OpenTelemetry SDK if it is not initialized
	 *
	 * @param telemetryManager Wraps monitors and metrics
	 * @param resourceConfig   The user's resource configuration
	 */
	void initOtelSdk(final TelemetryManager telemetryManager, final ResourceConfig resourceConfig) {
		// Create a resource if it hasn't been created during the previous cycle
		if (autoConfiguredOpenTelemetrySdk == null) {
			// Create the resource
			final Monitor hostMonitor = telemetryManager.getEndpointHostMonitor();
			final Map<String, String> userAttributes = resourceConfig.getAttributes();

			final Map<String, String> hostMonitorResourceAttributes;
			final org.sentrysoftware.metricshub.engine.telemetry.Resource monitorResource = hostMonitor.getResource();
			if (monitorResource != null) {
				hostMonitorResourceAttributes = monitorResource.getAttributes();
			} else {
				hostMonitorResourceAttributes = Map.of();
			}

			final Resource resource = OtelHelper.createHostResource(
				hostMonitorResourceAttributes,
				userAttributes,
				resourceConfig.getResolveHostnameToFqdn()
			);

			// Store the host monitor attributes for future use
			mainResourceAttributes =
				resource
					.getAttributes()
					.asMap()
					.entrySet()
					.stream()
					.collect(
						Collectors.toMap(
							entry -> entry.getKey().getKey(),
							entry -> entry.getValue().toString(),
							(oldValue, newValue) -> oldValue,
							HashMap::new
						)
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
