package com.sentrysoftware.hardware.prometheus.service;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.sentrysoftware.hardware.prometheus.dto.PrometheusParameter;
import com.sentrysoftware.hardware.prometheus.dto.PrometheusParameter.PrometheusMetricType;
import com.sentrysoftware.hardware.prometheus.dto.TargetContext;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import lombok.extern.slf4j.Slf4j;

/**
 * Since we don't directly instrument the code and we are a PROXY metrics which fetches data from other systems,
 * we need to create a custom collector which needs to be registered as a normal metric registry
 */
@Slf4j
@Service
public class HostMonitoringCollectorService extends Collector {

	public static final String LABEL = "label";
	public static final String PARENT = "parent";
	public static final String ID = "id";
	protected static final List<String> LABELS = Arrays.asList(FQDN, ID, LABEL, PARENT);
	private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("(_)([a-z])");

	@Autowired
	private Map<String, IHostMonitoring> hostMonitoringMap;

	/**
	 * Add a metric sample in the given Gauge metric
	 *
	 * @param labeledMetric Prometheus {@link MetricFamilySamples}
	 * @param monitor       Collected {@link Monitor} instance
	 * @param parameterName The parameter name we wish to add
	 */
	static void addMetric(final MetricFamilySamples labeledMetric, final Monitor monitor, final String parameterName, final double factor) {

		// The add metric is not defined in the MetricFamilySamples (super class of CounterMetricFamily and GaugeMetricFamily)
		// that's why the following code looks a bit ugly...
		if (labeledMetric instanceof CounterMetricFamily) {
			((CounterMetricFamily) labeledMetric).addMetric(
					// fqdn, Id, label, parentId (can be null)
					createLabels(monitor),
					convertParameterValue(monitor, parameterName, factor));
		} else {
			((GaugeMetricFamily) labeledMetric).addMetric(
					// fqdn, Id, label, parentId (can be null)
					createLabels(monitor),
					convertParameterValue(monitor, parameterName, factor));
		}
	}

	/**
	 * Add a metadata as metric sample
	 *
	 * @param labeledMetric Prometheus {@link MetricFamilySamples}
	 * @param monitor       Collected {@link Monitor} instance
	 * @param parameterName The parameter name we wish to add
	 */
	static void addMetadataAsMetric(final MetricFamilySamples labeledMetric, final Monitor monitor, final String parameterName, final double factor) {

		if (labeledMetric instanceof CounterMetricFamily) {
			((CounterMetricFamily) labeledMetric).addMetric(
					// fqdn, Id, label, parentId (can be null)
					createLabels(monitor),
					convertMetadataValue(monitor, parameterName, factor)
				);
		} else {
			((GaugeMetricFamily) labeledMetric).addMetric(
					// fqdn, Id, label, parentId (can be null)
					createLabels(monitor),
					convertMetadataValue(monitor, parameterName, factor)
				);
		}
	}

	/**
	 * Get the parameter number value from the monitor instance
	 *
	 * @param monitor       The monitor we wish to extract the parameter value
	 * @param parameterName The parameter name we want to extract from the given monitor instance
	 * @return {@link Number} value
	 */
	static Number getParameterValue(final Monitor monitor, final String parameterName) {

		return monitor.getParameters().get(parameterName).numberValue();
	}

	/**
	 * Convert the parameter number value according to the factor indicated for Prometheus
	 *
	 * @param monitor       The monitor we wish to extract the parameter value
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
	 * @param monitor
	 * @param metadata
	 * @param factor
	 * @return
	 */
	static Double convertMetadataValue(final Monitor monitor, final String metadata, double factor) {

		// monitor.getMetadata(metadata) can never return null when the current method is called
		// Because the precedent method has been filtred
		return NumberHelper.parseDouble(monitor.getMetadata(metadata), null) * factor;
	}

	@Override
	public List<MetricFamilySamples> collect() {

		final List<MetricFamilySamples> metricFamilySamplesList = new ArrayList<>();

		// Loop over all the monitors and create metrics (Prometheus samples)
		hostMonitoringMap.entrySet()
			.stream()
			.filter(entry -> TargetContext.getTargetId() == null || entry.getKey().equals(TargetContext.getTargetId()))
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
	 * @param monitors    The monitors we wish to extract the collected parameters
	 * @param mfs         {@link List} of {@link MetricFamilySamples} provided by the Prometheus Client library
	 */
	static void processSameTypeMonitors(final MonitorType monitorType, final Map<String, Monitor> monitors, final List<MetricFamilySamples> mfs) {

		if (monitors == null || monitors.isEmpty()) {
			log.info("No monitor found for type {}", monitorType);
			return;
		}

		monitorType.getMetaMonitor()
		.getMetaParameters()
		.values()
		.stream()
		.sorted(Comparator.comparing(MetaParameter::getName))
		.forEach(metaParameter -> processMonitorsMetric(metaParameter, monitorType, monitors, mfs));

		processMonitorMetricInfo(monitorType, monitors, mfs);
		processMonitorsMetadataMetrics(monitorType, monitors, mfs);
	}


	/**
	 * Process info metrics (*_info) for the given map of monitors
	 *
	 * @param monitorType The {@link MonitorType} of the given <code>monitors</code>
	 * @param monitors    The monitors we wish to extract the discovered metadata
	 * @param mfs         {@link List} of {@link MetricFamilySamples} provided by the Prometheus Client library
	 */
	static void processMonitorMetricInfo(final MonitorType monitorType, final Map<String, Monitor> monitors, final List<MetricFamilySamples> mfs) {

		final String metricName = PrometheusSpecificities.getInfoMetricName(monitorType);
		if (metricName == null || metricName.isBlank()) {
			log.warn("The metric name is not defined for monitor type {}. Received: {}", monitorType, metricName);
			return;
		}

		final List<String> labels = PrometheusSpecificities.getLabels(monitorType);
		Assert.state(labels != null && !labels.isEmpty(), () -> "The labels are not defined for the monitor type: " + monitorType.getName());

		final String help = String.format("Metric: %s", metricName);

		final GaugeMetricFamily labeledGauge = new GaugeMetricFamily(metricName, help, labels);

		monitors.values()
			.stream()
			.filter(Objects::nonNull)
			.forEach(monitor -> addInfoMetric(labeledGauge, monitor, labels));

		mfs.add(labeledGauge);
	}

	/**
	 * Add the info metric for the given monitor
	 *
	 * @param gauge   Prometheus {@link GaugeMetricFamily}
	 * @param monitor Collected {@link Monitor} instance
	 * @param labels  List of the specific labels to be
	 */
	static void addInfoMetric(final GaugeMetricFamily gauge, final Monitor monitor, final List<String> labels) {
		final List<String> labelValues = new ArrayList<>();
		for (String label : labels) {
			if (ID.equals(label)) {
				labelValues.add(monitor.getId());
			} else if (PARENT.equals(label)) {
				labelValues.add(getValueOrElse(monitor.getParentId(), ""));
			} else if (LABEL.equals(label)) {
				labelValues.add(monitor.getName());
			} else if (FQDN.equals(label)) {
				labelValues.add(monitor.getFqdn());
			} else {
				labelValues.add(convertMetadataInfoValue(monitor, label));
			}
		}

		gauge.addMetric(labelValues, 1);
	}

	/**
	 * Check if the metadata value stored in metric_info needs to be converted
	 * @param monitor
	 * @param label
	 * @return
	 */
	static String convertMetadataInfoValue(final Monitor monitor, String label) {
		if (label == null || label.isEmpty() || monitor == null || monitor.getMetadata() == null) {
			return "";
		}
		// get the actual label (from Matrix-Model)
		label = snakeCaseToCamelCase(label);
		// check if its value needs to be converted
		String metricValue = getValueOrElse(monitor.getMetadata(label), "");
		// check if there is a prometheus metadata specificity in order to get the factor
		final Optional<PrometheusParameter> maybePrometheusParameter = PrometheusSpecificities
				.getPrometheusMetadataToParameters(monitor.getMonitorType(), label);

		if (maybePrometheusParameter.isPresent() && !metricValue.isEmpty()) {
			// Ok, now we can get the prometheus parameter related to the given metadata
			final PrometheusParameter prometheusParameter = maybePrometheusParameter.get();
			metricValue = convertMetadataValue(monitor, label, prometheusParameter.getFactor()).toString() ;
		}
		return metricValue;
	}

	/**
	 * Check if at least the metric of the passed {@link MetaParameter} is collected then extract and append the metric value to the
	 * list of {@link MetricFamilySamples}
	 *
	 * @param metaParameter The {@link MetaParameter} defined by the matrix engine
	 * @param monitorType   The {@link MonitorType} of the given <code>monitors</code>
	 * @param monitors      The monitors we wish to extract the collected parameters
	 * @param mfs           {@link List} of {@link MetricFamilySamples}. The implementation is provided by the Prometheus Client library
	 */
	static void processMonitorsMetric(final MetaParameter metaParameter, final MonitorType monitorType,
			final Map<String, Monitor> monitors, final List<MetricFamilySamples> mfs) {
		// Get the prometheus parameter, some parameters are not reported in the hardware sentry exporter for prometheus
		final Optional<PrometheusParameter> maybePrometheusParameter = PrometheusSpecificities
				.getPrometheusParameter(monitorType, metaParameter.getName());

		// Check if the parameter is reported and if it is available at least in one monitor
		if (!maybePrometheusParameter.isPresent() || !isParameterFamilyAvailableOnMonitors(metaParameter, monitors)) {
			return;
		}

		// Ok, now we can get the prometheus parameter
		final PrometheusParameter prometheusParameter = maybePrometheusParameter.get();

		// Create the help section
		final String help = buildHelp(prometheusParameter);

		// Create the MetricFamily, Gauge or Counter
		final MetricFamilySamples labeledMetric = createMetricFamilySamples(prometheusParameter, help);

		// For each monitor, check if the parameter is available then add the metric value
		monitors
			.values()
			.stream()
			.filter(monitor -> isParameterAvailable(monitor, metaParameter.getName()))
			.forEach(monitor -> addMetric(labeledMetric, monitor, metaParameter.getName(), prometheusParameter.getFactor()));

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
	static void processMonitorsMetadataMetrics(MonitorType monitorType, Map<String, Monitor> monitors,
			List<MetricFamilySamples> mfs) {

		if (!PrometheusSpecificities.getPrometheusMetadataToParameters().containsKey(monitorType) || monitors == null) {
			return;
		}

		// for each monitor check if some metadata needs to be also created as metrics
		// get metadata and check if they need to be converted to metrci
		List<String> monitorMetadata = monitorType.getMetaMonitor().getMetadata();

		for (String metadata : monitorMetadata) {
			// Get the prometheus metadata
			final Optional<PrometheusParameter> maybePrometheusParameter = PrometheusSpecificities
					.getPrometheusMetadataToParameters(monitorType, metadata);

			// Check if the metadata is reported and if it is available at least in one
			// monitor
			if (!maybePrometheusParameter.isPresent() || !isMetadataFamilyAvailableOnMonitors(metadata, monitors)) {
				continue;
			}

			// Ok, now we can get the prometheus parameter related to the given metadata
			final PrometheusParameter prometheusParameter = maybePrometheusParameter.get();

			// Create the help section
			final String help = buildHelp(prometheusParameter);

			// Create the MetricFamily, Gauge or Counter
			final MetricFamilySamples labeledMetric = createMetricFamilySamples(prometheusParameter, help);

			// For each monitor, check if the metadata is available then add its value
			monitors.values()
					.stream()
					.filter(monitor -> checkMetadata(monitor, metadata))
					.forEach(monitor -> addMetadataAsMetric(labeledMetric, monitor, metadata, prometheusParameter.getFactor()));

			mfs.add(labeledMetric);
		}
	}

	/**
	 * Create prometheus metric family based on the given {@link PrometheusParameter} instance defining the format of the prometheus metric
	 *
	 * @param prometheusParameter {@link PrometheusParameter} object defining the type and the name of the metric
	 * @param help                metric help
	 * @return new instance of {@link MetricFamilySamples}. GaugeMetricFamily if the type is GAUGE otherwise CounterMetricFamily.
	 */
	private static MetricFamilySamples createMetricFamilySamples(final PrometheusParameter prometheusParameter, final String help) {

		if (PrometheusMetricType.GAUGE.equals(prometheusParameter.getType())) {
			return new GaugeMetricFamily(prometheusParameter.getName(), help, LABELS);
		}

		return new CounterMetricFamily(prometheusParameter.getName(), help, LABELS);

	}

	/**
	 * Build help for metric using format: <em>Metric: $metricName - Unit: $metricUnit</em> or
	 * <em>Metric: $metricName</em> if the unit is not available
	 *
	 * @param prometheusParameter {@link PrometheusParameter} prometheus parameter information
	 * @return {@link String} value
	 */
	static String buildHelp(final PrometheusParameter prometheusParameter) {

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
	 * Check if the metadata exists in the given monitor
	 *
	 * @param monitor       The monitor we wish to check its metadata
	 * @param metadata The name of the metadata
	 * @return <code>true</code> if the metric is collected otherwise <code>false</code>
	 */
	static boolean checkMetadata(final Monitor monitor, final String metadata) {
		return monitor != null
				&& monitor.getMetadata() != null
				&& monitor.getMetadata(metadata) != null
				&& NumberHelper.parseDouble(monitor.getMetadata(metadata), null) != null;
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
	 * @param monitor The monitor we wish to extract its id, parentId and name
	 * @return {@link List} of {@link String} values
	 */
	static List<String> createLabels(final Monitor monitor) {

		return Arrays.asList(
					monitor.getFqdn(),
					monitor.getId(),
					monitor.getName(),
					getValueOrElse(monitor.getParentId(), "")
				);
	}


	/*
	 * Converts a {@link String} written in snake_case to its camelCase version.<br>
	 * <b>Example: "parent_id" -> "parentId"</b>
	 *
	 * @param snakeCase	The {@link String} that should be converted.
	 *
	 * @return			The camelCase version of the given {@link String}.
	 */
	private static String snakeCaseToCamelCase(String snakeCase) {

		return SNAKE_CASE_PATTERN
			.matcher(snakeCase)
			.replaceAll(matchResult -> matchResult.group(2).toUpperCase());
	}
}
