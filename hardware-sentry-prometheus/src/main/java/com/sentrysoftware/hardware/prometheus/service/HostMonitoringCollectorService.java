package com.sentrysoftware.hardware.prometheus.service;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.base.CaseFormat;
import com.sentrysoftware.hardware.prometheus.dto.PrometheusParameter;
import com.sentrysoftware.hardware.prometheus.dto.TargetContext;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import io.prometheus.client.Collector;
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
	protected static final List<String> LABELS = Arrays.asList(ID, PARENT, LABEL, FQDN);

	@Autowired
	private Map<String, IHostMonitoring> hostMonitoringMap;

	private static final Map<MonitorType, String> MONITOR_TYPE_NAMES;

	static {

		final Map<MonitorType, String> monitorTypeNames = new EnumMap<>(MonitorType.class);
		for (MonitorType monitorType : MonitorType.values()) {
			switch (monitorType) {
			case CPU:
				monitorTypeNames.put(monitorType, "cpu");
				break;
			case LED:
				monitorTypeNames.put(monitorType, "led");
				break;
			default:
				monitorTypeNames.put(monitorType, monitorType.getName());
			}
		}
		MONITOR_TYPE_NAMES = Collections.unmodifiableMap(monitorTypeNames);
	}

	/**
	 * Add a metric sample in the given Gauge metric
	 * 
	 * @param gauge         Prometheus {@link GaugeMetricFamily}
	 * @param monitor       Collected {@link Monitor} instance
	 * @param parameterName The parameter name we wish to add
	 */
	static void addMetric(final GaugeMetricFamily gauge, final Monitor monitor, final String parameterName) {

		gauge.addMetric(
			// Id, parentId (can be null), label, fqdn
			createLabels(monitor),
			convertParameterValue(monitor, parameterName));
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
	 * Convert the parameter number value according to the factor indicated for
	 * Prometheus
	 * 
	 * @param monitor       The monitor we wish to extract the parameter value
	 * @param parameterName The parameter name we want to extract from the given
	 *                      monitor instance
	 * @return {@link Number} value
	 */
	static Double convertParameterValue(final Monitor monitor, final String parameterName) {

		PrometheusParameter prometheusParamFactor = PrometheusSpecificities.getPrometheusParameter(monitor.getMonitorType().getName(),
				parameterName);
		Number paramValue = getParameterValue(monitor, parameterName);
		if (paramValue != null) {
			if (prometheusParamFactor != null) {
				return paramValue.doubleValue() * prometheusParamFactor.getPrometheusParameterFactor();
			} else {
				return paramValue.doubleValue();
			}
		}
		return null;
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

	}

	/**
	 * Process info metrics (*_info) for the given map of monitors
	 * 
	 * @param monitorType The {@link MonitorType} of the given <code>monitors</code>
	 * @param monitors    The monitors we wish to extract the discovered metadata
	 * @param mfs         {@link List} of {@link MetricFamilySamples} provided by the Prometheus Client library
	 */
	static void processMonitorMetricInfo(final MonitorType monitorType, final Map<String, Monitor> monitors, final List<MetricFamilySamples> mfs) {

		final List<String> labels = PrometheusSpecificities.getLabels(monitorType);

		Assert.state(labels != null && !labels.isEmpty(), () -> "No Labels defined for the monitor type: " + monitorType.getName());

		String monitorTypeName = MONITOR_TYPE_NAMES.get(monitorType);
		String metricName = buildMetricName(monitorTypeName, "info");
		final String help = String.format("Metric: %s info", buildMetricName(monitorTypeName));

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
				labelValues.add(getValueOrElse(monitor.getParentId(), HardwareConstants.EMPTY));
			} else if (LABEL.equals(label)) {
				labelValues.add(monitor.getName());
			} else if (FQDN.equals(label)) {
				labelValues.add(monitor.getFqdn());
			} else {
				labelValues.add(getValueOrElse(monitor.getMetadata(label), HardwareConstants.EMPTY));
			}
		}

		gauge.addMetric(labelValues, 1);
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
		if (!isParameterFamilyAvailableOnMonitors(metaParameter, monitors)) {
			return;
		}

		String paramName = metaParameter.getName();
		PrometheusParameter prometheusParam = PrometheusSpecificities.getPrometheusParameter(monitorType.getName(), paramName);
		String prometheusParamName = prometheusParam == null ? paramName : prometheusParam.getPrometheusParameterName();
		final String metricName = buildMetricName(MONITOR_TYPE_NAMES.get(monitorType), prometheusParamName);

		final String help = buildHelp(monitorType.getName(), metaParameter);

		final GaugeMetricFamily labeledGauge = new GaugeMetricFamily(
				metricName,
				help,
				LABELS);

		monitors
			.values()
			.stream()
			.filter(monitor -> isParameterAvailable(monitor, metaParameter.getName()))
			.forEach(monitor -> addMetric(labeledGauge, monitor, metaParameter.getName()));

		mfs.add(labeledGauge);
	}

	/**
	 * Build help for metric using format: <em>Metric: $monitorName $parameterName - Unit: $parameterUnit</em>
	 * 
	 * @param monitorType   The type of the monitor as string
	 * @param metaParameter {@link MetaParameter} information
	 * @return {@link String} value
	 */
	static String buildHelp(final String monitorType, final MetaParameter metaParameter) {
		String paramName = metaParameter.getName();
		PrometheusParameter prometheusParam = PrometheusSpecificities.getPrometheusParameter(monitorType, paramName);
		String prometheusParamName = prometheusParam == null ? buildMetricName(paramName) : prometheusParam.getPrometheusParameterName();
		String paramUnit = metaParameter.getUnit();
		String prometheusParamUnit = prometheusParam == null ? paramUnit : prometheusParam.getPrometheusParameterUnit();
		return String.format("Metric: %s %s - Unit: %s", monitorType, prometheusParamName , prometheusParamUnit);
	}

	/**
	 * Build a Prometheus metric name. E.g. <em>fan_status</em>, <em>enclosure_energy_status</em>, <em>voltage_voltage</em>
	 * 
	 * @param names {@link String} values to concatenate
	 * @return {@link String} value
	 */
	static String buildMetricName(final String... names) {
		return Arrays.stream(names)
				.map(name -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name.trim()))
				.map(String::toLowerCase)
				.collect(Collectors.joining(HardwareConstants.ID_SEPARATOR));
	}

	/**
	 * Check if the parameter defined in the passed {@link MetaParameter} is collected on the given monitors lookup
	 * 
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
	 * Label order: <em>$monitorId</em>, <em>$monitorParentId</em> then <em>$monitorName</em>
	 * 
	 * @param monitor The monitor we wish to extract its id, parentId and name
	 * @return {@link List} of {@link String} values
	 */
	static List<String> createLabels(final Monitor monitor) {

		return Arrays.asList(monitor.getId(),
				getValueOrElse(monitor.getParentId(), HardwareConstants.EMPTY),
				monitor.getName(),
				monitor.getFqdn());
	}
}
