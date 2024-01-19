package com.sentrysoftware.metricshub.agent.service.task;

import static com.sentrysoftware.metricshub.agent.helper.ConfigHelper.getLoggerLevel;

import com.sentrysoftware.metricshub.agent.config.ResourceConfig;
import com.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import com.sentrysoftware.metricshub.agent.helper.OtelHelper;
import com.sentrysoftware.metricshub.agent.service.signal.MetricTypeVisitor;
import com.sentrysoftware.metricshub.agent.service.signal.SimpleUpDownCounterMetricObserver;
import com.sentrysoftware.metricshub.engine.ClientsExecutor;
import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import com.sentrysoftware.metricshub.engine.strategy.collect.CollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.collect.PrepareCollectStrategy;
import com.sentrysoftware.metricshub.engine.strategy.detection.DetectionStrategy;
import com.sentrysoftware.metricshub.engine.strategy.discovery.DiscoveryStrategy;
import com.sentrysoftware.metricshub.engine.strategy.simple.SimpleStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import com.sentrysoftware.metricshub.hardware.strategy.HardwarePostCollectStrategy;
import com.sentrysoftware.metricshub.hardware.strategy.HardwarePostDiscoveryStrategy;
import com.sentrysoftware.metricshub.hardware.strategy.HardwareStrategy;
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
				new DetectionStrategy(telemetryManager, discoveryTime, clientsExecutor),
				new DiscoveryStrategy(telemetryManager, discoveryTime, clientsExecutor),
				new SimpleStrategy(telemetryManager, discoveryTime, clientsExecutor),
				new HardwarePostDiscoveryStrategy(telemetryManager, discoveryTime, clientsExecutor)
			);

			// Initialize the OpenTelemetry observers and LogEmitter after the discovery
			// as at this time we should have what we want to observe
			initOtelSdk(telemetryManager, resourceConfig);
		}

		log.info("Calling the engine to collect resource: {}.", hostId);

		final long collectTime = System.currentTimeMillis();

		// One more, run only prepare, collect simple and post strategies
		telemetryManager.run(
			new PrepareCollectStrategy(telemetryManager, collectTime, clientsExecutor),
			new CollectStrategy(telemetryManager, collectTime, clientsExecutor),
			new SimpleStrategy(telemetryManager, collectTime, clientsExecutor),
			new HardwarePostCollectStrategy(telemetryManager, collectTime, clientsExecutor)
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
					final Optional<Map<String, MetricDefinition>> maybeHostMetricDefinitions = Optional.ofNullable(
						monitoringTaskInfo.getHostMetricDefinitions().metrics()
					);

					// Initialize endpoint host metric observers
					initMonitorMetricObservers(monitor, telemetryManager, maybeHostMetricDefinitions);

					// Initialize the metricshub.host.configured metric observer
					initializeHostConfiguredMetricObserver(monitor, maybeHostMetricDefinitions);
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
	 * @param host                       Host monitor instance
	 * @param maybeHostMetricDefinitions Optional Metric definitions
	 */
	void initializeHostConfiguredMetricObserver(
		final Monitor host,
		final Optional<Map<String, MetricDefinition>> maybeHostMetricDefinitions
	) {
		if (!isMetricObserverNotInitialized(host.getId(), HOST_CONFIGURED_METRIC_NAME)) {
			return;
		}

		// Get the metric definition from the metric definition map
		final MetricDefinition metricDefinition = lookupMetricDefinition(
			HOST_CONFIGURED_METRIC_NAME,
			maybeHostMetricDefinitions
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
	 * @param monitor                  {@link Monitor} instance
	 * @param telemetryManager         Wraps monitors and metrics
	 * @param maybeMetricDefinitionMap Optional Metric definitions
	 */
	void initMonitorMetricObservers(
		final Monitor monitor,
		final TelemetryManager telemetryManager,
		final Optional<Map<String, MetricDefinition>> maybeMetricDefinitionMap
	) {
		monitor
			.getMetrics()
			.entrySet()
			.stream()
			.filter(entry -> Objects.nonNull(entry.getValue()))
			.filter(entry -> OtelHelper.isAcceptedKey(entry.getKey()))
			.filter(metricEntry -> isMetricObserverNotInitialized(monitor.getId(), metricEntry.getKey()))
			.forEach(metricEntry -> initMetricObserver(monitor, maybeMetricDefinitionMap, metricEntry));
	}

	/**
	 * Initialize an observer for the given metric entry
	 *
	 * @param monitor                  {@link Monitor} instance
	 * @param maybeMetricDefinitionMap Optional Metric definitions (E.g. metric definitions from Hardware.yaml or Storage.yaml)
	 * @param metricEntry              Key-value where the key is the unique metric key and the value
	 *                                 is the {@link AbstractMetric}
	 */
	void initMetricObserver(
		final Monitor monitor,
		final Optional<Map<String, MetricDefinition>> maybeMetricDefinitionMap,
		final Entry<String, AbstractMetric> metricEntry
	) {
		// Retrieve the metric unique key
		final String metricKey = metricEntry.getKey();

		// Extract the metric name from the metric key. E.g. extract hw.power from hw.power{hw.type="fan"} // NOSONAR
		final String metricName = MetricFactory.extractName(metricKey);

		// Get the metric definition from the metric definition map
		final MetricDefinition metricDefinition = lookupMetricDefinition(metricName, maybeMetricDefinitionMap);

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
					.build()
			);

		// Set the metric's observer as initialized
		initializedMetricsPerMonitorId.computeIfAbsent(monitor.getId(), id -> new HashSet<>()).add(metricKey);
	}

	/**
	 * Search the {@link MetricDefinition} instance defined for the given metric
	 *
	 * @param metricName               The name of the metric e.g. hw.status
	 * @param maybeMetricDefinitionMap Optional Map of all the existing definitions
	 * @return {@link MetricDefinition} instance, never <code>null</code>.
	 */
	static MetricDefinition lookupMetricDefinition(
		final String metricName,
		final Optional<Map<String, MetricDefinition>> maybeMetricDefinitionMap
	) {
		return maybeMetricDefinitionMap
			.map(map -> map.get(metricName))
			.filter(Objects::nonNull)
			.orElseGet(() ->
				MetricDefinition.builder().description(String.format(GENERIC_METRIC_DESCRIPTION_FORMAT, metricName)).build()
			);
	}

	/**
	 * Check if the metric observer is not initialized for the given monitor id and metric key
	 *
	 * @param monitorId  unique id of the monitor
	 * @param metricKey  metric key. E.g. hw.energy{hw.type="fan"}
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
			final com.sentrysoftware.metricshub.engine.telemetry.Resource monitorResource = hostMonitor.getResource();
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
