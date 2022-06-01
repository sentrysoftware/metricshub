package com.sentrysoftware.hardware.agent.service.prometheus;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.hardware.agent.dto.HostContext;
import com.sentrysoftware.hardware.agent.service.ServiceHelper;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.IParameter;

import io.prometheus.client.Collector;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Since we don't directly instrument the code and we are a PROXY metrics which
 * fetches data from other systems, we need to create a custom collector which
 * needs to be registered as a normal metric registry
 * 
 * @deprecated A collector building a set of metrics is no more required as the
 *             agent shouldn't export metrics through /metrics
 */
@Slf4j
@Service
@Deprecated(since = "1.1")
public class HostMonitoringCollectorService extends Collector {

	public static final String LABEL = "label";
	public static final String PARENT = "parent";
	public static final String ID = "id";
	protected static final List<String> LABELS = Arrays.asList(FQDN, ID, LABEL, PARENT);
	private static final Map<String, Function<Monitor, String>> LABEL_FUNCTIONS = Map.of(
			FQDN, Monitor::getFqdn,
			ID, Monitor::getId,
			LABEL, Monitor::getName,
			PARENT, monitor -> getValueOrElse(monitor.getParentId(), "")
	);

	@Autowired
	private Map<String, IHostMonitoring> hostMonitoringMap;

	@Autowired
	private AgentInfoService exporterInfoService;

	@Autowired
	private MultiHostsConfigurationDto multiHostsConfigurationDto;

	@Autowired 
	private ExtraMetricsService extraMetricsService;

	/**
	 * Add a metric sample in the given Gauge metric
	 *
	 * @param labeledMetric Prometheus {@link AbstractHardwareMetricFamily}
	 * @param monitor       Collected {@link Monitor} instance
	 * @param parameterName The parameter name we wish to add
	 */
	void addMetric(final AbstractHardwareMetricFamily labeledMetric, final Monitor monitor,
			final String parameterName, final double factor) {

		labeledMetric.addMetric(
				// fqdn, Id, label, parentId (can be null)
				createLabels(monitor, labeledMetric.getLabelNames()),
				convertParameterValue(monitor, parameterName, factor),
				getCollectTime(monitor, parameterName)
		);

	}

	/**
	 * Add a metadata as metric sample
	 *
	 * @param labeledMetric      Prometheus {@link AbstractHardwareMetricFamily}
	 * @param monitor            Collected {@link Monitor} instance
	 * @param matrixMetadataName The name of the metadata
	 */
	void addMetadataAsMetric(final AbstractHardwareMetricFamily labeledMetric, final Monitor monitor,
			final String matrixMetadataName, final double factor) {

		labeledMetric.addMetric(
				// fqdn, Id, label, parentId (can be null)
				createLabels(monitor, labeledMetric.getLabelNames()),
				convertMetadataValue(monitor.getMetadata(matrixMetadataName), factor),
				// collect time is not provided for metadata, let's use the discovery time
				getDiscoveryTime(monitor)
		);
	}

	/**
	 * Get the parameter number value from the monitor instance
	 *
	 * @param monitor       The monitor from which we extract the parameter value
	 * @param parameterName The parameter name we want to extract from the given monitor instance
	 * @return {@link Number} value
	 */
	static Number getParameterValue(final Monitor monitor, final String parameterName) {

		return monitor.getParameters().get(parameterName).numberValue();
	}

	/**
	 * Convert the parameter number value according to the factor indicated for Prometheus
	 *
	 * @param monitor       The monitor from which we extract the parameter value
	 * @param parameterName The parameter name we want to extract from the given monitor instance
	 * @param factor        The factor used to convert the metric value
	 * @return {@link Number} value
	 */
	static Double convertParameterValue(final Monitor monitor, final String parameterName, double factor) {

		// getParameterValue can never return null when the current method convertParameterValue is called
		// Because a first check is done in isParameterAvailable
		return getParameterValue(monitor, parameterName).doubleValue() * factor;
	}

	/**
	 * Convert metadata value according to the given factor
	 *
	 * @param metadataValue cannot be null and must be a number
	 * @return {@link Double} value
	 */
	static Double convertMetadataValue(@NonNull final String metadataValue, double factor) {

		// metadataValue can never be null and not a number as it is already checked and validated before calling this method
		return NumberHelper.parseDouble(metadataValue, null) * factor;
	}

	@Override
	public List<MetricFamilySamples> collect() {

		final List<MetricFamilySamples> metricFamilySamplesList = new ArrayList<>();

		metricFamilySamplesList.add(exporterInfoService.buildAgentInfoMetric());
		metricFamilySamplesList.addAll(extraMetricsService.buildExtraMetrics());

		// Loop over all the monitors and create metrics (Prometheus samples)
		hostMonitoringMap.entrySet()
			.stream()
			.filter(entry -> HostContext.getHostId() == null || entry.getKey().equals(HostContext.getHostId()))
			.map(Entry::getValue)
			.forEach(hostMonitoring -> hostMonitoring
				.getMonitors()
				.forEach((monitorType, monitors) -> processSameTypeMonitors(monitorType, monitors, metricFamilySamplesList)));


		return metricFamilySamplesList;
	}

	/**
	 * Process same type monitors metrics
	 *
	 * @param monitorType The {@link MonitorType} of the given <code>monitors</code>
	 * @param monitors    The monitors from which we extract the collected parameters
	 * @param mfs         {@link List} of {@link MetricFamilySamples} provided by the Prometheus Client library
	 */
	void processSameTypeMonitors(final MonitorType monitorType, final Map<String, Monitor> monitors, final List<MetricFamilySamples> mfs) {

		if (monitors == null || monitors.isEmpty()) {
			log.info("No monitor found for type {}", monitorType);
			return;
		}

		// Process parameters as metrics
		monitorType.getMetaMonitor()
		.getMetaParameters()
		.values()
		.stream()
		.sorted(Comparator.comparing(MetaParameter::getName))
		.forEach(metaParameter -> processMonitorsMetric(metaParameter, monitorType, monitors, mfs));

		// Process metadata as *_info metrics
		processMonitorMetricInfo(monitorType, monitors, mfs);

		// Process number metadata as metrics
		processMonitorsMetadataMetrics(monitorType, monitors, mfs);
	}


	/**
	 * Process info metrics (*_info) for the given map of monitors
	 *
	 * @param monitorType The {@link MonitorType} of the given <code>monitors</code>
	 * @param monitors    The monitors from which we extract the discovered metadata
	 * @param mfs         {@link List} of {@link MetricFamilySamples} provided by the Prometheus Client library
	 */
	void processMonitorMetricInfo(final MonitorType monitorType, final Map<String, Monitor> monitors,
			final List<MetricFamilySamples> mfs) {

		final String metricName = PrometheusSpecificities.getInfoMetricName(monitorType);
		if (metricName == null || metricName.isBlank()) {
			log.warn("The metric name is not defined for monitor type {}. Received: {}", monitorType, metricName);
			return;
		}

		final List<String> staticLabels = PrometheusSpecificities.getLabels(monitorType);
		Assert.state(staticLabels != null && !staticLabels.isEmpty(),
				() -> "The labels are not defined for the monitor type: " + monitorType.getDisplayName());

		// Concatenate extra labels. Sonar doesn't know that the previous 
		// Assert.state throws the IllegalArgumentException if staticLabels is null, so never null here.
		final List<String> labels = Stream
				.concat(staticLabels.stream(), multiHostsConfigurationDto.getExtraLabels().keySet().stream()) // NOSONAR
				.sorted()
				.collect(Collectors.toList());

		final String help = String.format("Metric: %s", metricName);

		final HardwareGaugeMetric labeledGauge = new HardwareGaugeMetric(metricName, help, labels);

		monitors.values()
			.stream()
			.filter(Objects::nonNull)
			.forEach(monitor -> addInfoMetric(labeledGauge, monitor));

		mfs.add(labeledGauge);
	}

	/**
	 * Add the info metric for the given monitor
	 *
	 * @param gauge   Prometheus {@link HardwareGaugeMetric}
	 * @param monitor Collected {@link Monitor} instance
	 */
	void addInfoMetric(final HardwareGaugeMetric gauge, final Monitor monitor) {

		final List<String> labelValues = gauge.getLabelNames()
			.stream()
			.map(labelKey -> LABEL_FUNCTIONS
					.getOrDefault(labelKey, mo -> multiHostsConfigurationDto
							.getExtraLabels()
							.getOrDefault(labelKey, convertMetadataInfoValue(mo, labelKey)))
					.apply(monitor))
			.collect(Collectors.toList());

		gauge.addMetric(
				labelValues,
				1D,
				getDiscoveryTime(monitor)
		);
	}

	/**
	 * Convert the metadata value if needed otherwise get the value as it is
	 * 
	 * @param monitor The monitor from which we extract the metadata value
	 * @param label   The metric label
	 * @return String value
	 */
	static String convertMetadataInfoValue(final Monitor monitor, final String label) {
		if (label == null || label.isEmpty() || monitor == null || monitor.getMetadata() == null) {
			return EMPTY;
		}

		// Get the actual label (from Matrix-Model)
		final String matrixMetadataName = ServiceHelper.snakeCaseToCamelCase(label);

		// Check if its value needs to be converted
		String metricValue = getValueOrElse(monitor.getMetadata(matrixMetadataName), EMPTY);

		// Check if there is a Prometheus metadata specificity in order to get the factor
		final Optional<MetricInfo> maybePrometheusParameter = PrometheusSpecificities
				.getPrometheusMetadataToParameters(monitor.getMonitorType(), matrixMetadataName);

		if (!maybePrometheusParameter.isPresent()) {
			return metricValue;
		}

		// Check the metadata is a number value
		if (canParseDoubleValue(metricValue)) {
			// Ok, now we can get the Prometheus parameter related to the given metadata
			final MetricInfo prometheusParameter = maybePrometheusParameter.get();

			return convertMetadataValue(metricValue, prometheusParameter.getFactor()).toString() ;
		}

		// This is an unexpected metadata value (expected as number value)
		// Let's use empty instead of a bad non-number value in number label.
		return EMPTY;


	}

	/**
	 * Check if at least the metric of the passed {@link MetaParameter} is collected then extract and append the metric value to the
	 * list of {@link MetricFamilySamples}
	 *
	 * @param metaParameter The {@link MetaParameter} defined by the matrix engine
	 * @param monitorType   The {@link MonitorType} of the given <code>monitors</code>
	 * @param monitors      The monitors from which we extract the collected parameters
	 * @param mfs           {@link List} of {@link MetricFamilySamples}. The implementation is provided by the Prometheus Client library
	 */
	void processMonitorsMetric(final MetaParameter metaParameter, final MonitorType monitorType,
			final Map<String, Monitor> monitors, final List<MetricFamilySamples> mfs) {
		// Get the prometheus parameter, some parameters are not reported in the hardware sentry exporter for prometheus
		final Optional<MetricInfo> maybePrometheusParameter = PrometheusSpecificities
				.getPrometheusParameter(monitorType, metaParameter.getName());

		// Check if the parameter is reported and if it is available at least in one monitor
		if (!maybePrometheusParameter.isPresent() || !isParameterFamilyAvailableOnMonitors(metaParameter, monitors)) {
			return;
		}

		// Ok, now we can get the prometheus parameter
		final MetricInfo prometheusParameter = maybePrometheusParameter.get();

		// Create the help section
		final String help = buildHelp(prometheusParameter);

		// Create the MetricFamily, Gauge or Counter
		final AbstractHardwareMetricFamily labeledMetric = createMetricFamilySamples(prometheusParameter, help);

		// For each monitor, check if the parameter is available then add the metric value
		monitors
			.values()
			.stream()
			.filter(monitor -> isParameterAvailable(monitor, metaParameter.getName()))
			.forEach(monitor -> addMetric(
									labeledMetric,
									monitor,
									metaParameter.getName(),
									prometheusParameter.getFactor()
								)
			);

		mfs.add(labeledMetric);
	}

	/**
	 * Check if at least the metadata of the passed {@link MetaParameter} is
	 * collected then extract and append the according metric value to the list of
	 * {@link MetricFamilySamples}
	 *
	 * @param monitorType
	 * @param monitors
	 * @param mfs
	 */
	void processMonitorsMetadataMetrics(MonitorType monitorType, Map<String, Monitor> monitors,
			List<MetricFamilySamples> mfs) {

		if (!PrometheusSpecificities.getPrometheusMetadataToParameters().containsKey(monitorType) || monitors == null) {
			return;
		}

		// Get metadata that need to be converted to metrics
		final Map<String, MetricInfo> monitorMetadataMap = PrometheusSpecificities
						.getPrometheusMetadataToParameters()
						.getOrDefault(monitorType, Collections.emptyMap());

		for (final Entry<String, MetricInfo> entry : monitorMetadataMap.entrySet()) {

			final String matrixMetadataName = entry.getKey();
			final MetricInfo prometheusParameter = entry.getValue();

			// Check if the metadata is available at least in one monitor
			if (!isMetadataFamilyAvailableOnMonitors(matrixMetadataName, monitors)) {
				continue;
			}

			// Create the help section
			final String help = buildHelp(prometheusParameter);

			// Create the MetricFamily, Gauge or Counter
			final AbstractHardwareMetricFamily labeledMetric = createMetricFamilySamples(prometheusParameter, help);

			// For each monitor, check if the metadata is available then add its value
			monitors.values()
					.stream()
					.filter(monitor -> checkMetadata(monitor, matrixMetadataName))
					.forEach(monitor -> addMetadataAsMetric(
											labeledMetric,
											monitor,
											matrixMetadataName,
											prometheusParameter.getFactor()
										)
							);

			mfs.add(labeledMetric);
		}
	}

	/**
	 * Create Prometheus metric family based on the given
	 * {@link MetricInfo} instance defining the format of the Prometheus
	 * metric
	 *
	 * @param prometheusParameter {@link MetricInfo} object defining the
	 *                            type and the name of the metric
	 * @param help                metric help
	 * @return new instance of {@link MetricFamilySamples}.
	 *         {@link HardwareGaugeMetric} if the type is GAUGE otherwise
	 *         {@link HardwareCounterMetric}.
	 */
	private AbstractHardwareMetricFamily createMetricFamilySamples(final MetricInfo prometheusParameter, final String help) {

		final List<String> labels = Stream
				.concat(LABELS.stream(), multiHostsConfigurationDto.getExtraLabels().keySet().stream())
				.sorted()
				.collect(Collectors.toList());

		if (MetricType.GAUGE.equals(prometheusParameter.getType())) {
			return new HardwareGaugeMetric(prometheusParameter.getName(), help, labels);
		}

		return new HardwareCounterMetric(prometheusParameter.getName(), help, labels);

	}

	/**
	 * Build help for metric using format: <em>Metric: $metricName - Unit: $metricUnit</em> or
	 * <em>Metric: $metricName</em> if the unit is not available
	 *
	 * @param prometheusParameter {@link MetricInfo} prometheus parameter information
	 * @return {@link String} value
	 */
	static String buildHelp(final MetricInfo prometheusParameter) {

		if (!prometheusParameter.getUnit().isEmpty()) {
			return String.format("Metric: %s - Unit: %s", prometheusParameter.getName(), prometheusParameter.getUnit());
		}

		return String.format("Metric: %s", prometheusParameter.getName());
	}

	/**
	 * Check if the parameter defined in the passed {@link MetaParameter} is collected on the given monitors lookup
	 * @param metaParameter The {@link MetaParameter} defined by the matrix engine
	 * @param monitors      The monitors we wish to check the parameter
	 * @return <code>true</code> if the metric is collected otherwise <code>false</code>
	 */
	static boolean isParameterFamilyAvailableOnMonitors(final MetaParameter metaParameter, final Map<String, Monitor> monitors) {

		return monitors != null
				&& !monitors.isEmpty()
				&& monitors.values().stream().anyMatch(monitor -> isParameterAvailable(monitor, metaParameter.getName()));
	}

	/**
	 * Check if the parameter defined in the passed {@link MetaParameter} is collected on the given monitor instance
	 *
	 * @param monitor       The monitor we wish to check the parameter
	 * @param parameterName The name of the parameter to check
	 * @return <code>true</code> if the metric is collected otherwise <code>false</code>
	 */
	static Boolean isParameterAvailable(final Monitor monitor, final String parameterName) {
		return checkParameter(monitor, parameterName)
				&& getParameterValue(monitor, parameterName) != null;
	}

	/**
	 * Check if the parameter exists in the given monitor
	 *
	 * @param monitor       The monitor we wish to check its parameter
	 * @param parameterName The name of the parameter e.g. energyUsage, voltage, temperature.
	 * @return <code>true</code> if the metric is collected otherwise <code>false</code>
	 */
	static boolean checkParameter(final Monitor monitor, final String parameterName) {
		return monitor != null
				&& monitor.getParameters() != null
				&& monitor.getParameters().get(parameterName) != null;
	}

	/**
	 * Check if the metadata defined in the passed {@link String} is collected on the given monitors lookup
	 * @param metadata The {@link String} defined by the matrix engine
	 * @param monitors      The monitors we wish to check the parameter
	 * @return <code>true</code> if the metric is collected otherwise <code>false</code>
	 */
	static boolean isMetadataFamilyAvailableOnMonitors(final String metadata, final Map<String, Monitor> monitors) {

		return monitors != null
				&& !monitors.isEmpty()
				&& monitors.values().stream().anyMatch(monitor -> checkMetadata(monitor, metadata));
	}


	/**
	 * Check if the metadata is collected and available as a number in the given monitor
	 *
	 * @param monitor       The monitor we wish to check its metadata
	 * @param metadata      The name of the metadata
	 * @return <code>true</code> if the metric is collected otherwise <code>false</code>
	 */
	static boolean checkMetadata(final Monitor monitor, final String metadata) {
		return monitor != null
				&& monitor.getMetadata() != null
				&& canParseDoubleValue(monitor.getMetadata(metadata));
	}

	/**
	 * Check if the given value can be parsed as double
	 *
	 * @param value The value we wish to check
	 * @return <code>true</code> if the value is not <code>null</code> and the parse succeeds otherwise <code>false</code>
	 */
	static boolean canParseDoubleValue(final String value) {
		return value != null && NumberHelper.parseDouble(value, null) != null;
	}

	/**
	 * Get actual value or other if actual is null
	 *
	 * @param <T>
	 * @param actual
	 * @param other
	 * @return
	 */
	static <T> T getValueOrElse(T actual, T other) {
		return actual != null ? actual : other;
	}

	/**
	 * Create Prometheus labels. The values between { } after the metric name <br>
	 * Labels order: <em>$fqdn</em>, <em>$monitorId</em>, <em>$monitorName</em>, <em>$monitorParentId</em>
	 *
	 * @param monitor   The monitor we wish to extract its id, parentId and name
	 * @param labelKeys The label names as keys
	 * @return {@link List} of {@link String} values
	 */
	List<String> createLabels(final Monitor monitor, List<String> labelKeys) {

		return labelKeys
				.stream()
				.map(labelKey -> LABEL_FUNCTIONS
						.getOrDefault(
								labelKey,
								mo -> multiHostsConfigurationDto.getExtraLabels().getOrDefault(labelKey, "")
						)
						.apply(monitor))
				.collect(Collectors.toList());
	}

	/**
	 * Get the discovery time of the given monitor if we are in the honorTimestamps
	 * mode otherwise <code>null</code> is returned
	 *
	 * @param monitor the monitor we wish to extract its discovery time
	 * @return Long value
	 */
	Long getDiscoveryTime(final Monitor monitor) {
		return multiHostsConfigurationDto.isExportTimestamps() ? monitor.getDiscoveryTime() : null;
	}

	/**
	 * Get the parameter collect time if we are in the honorTimestamps
	 * mode otherwise <code>null</code> is returned
	 *
	 * @param monitor       The monitor from which we extract the parameter collect time
	 * @param parameterName The parameter name we want to extract from the given monitor instance
	 * @return Long value
	 */
	Long getCollectTime(final Monitor monitor, final String parameterName) {
		if (!multiHostsConfigurationDto.isExportTimestamps()) {
			return null;
		}

		final IParameter parameter = monitor.getParameters().get(parameterName);
		if (parameter != null) {
			return parameter.getCollectTime();
		}

		return null;
	}
}
