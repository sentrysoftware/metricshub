package com.sentrysoftware.hardware.prometheus.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.CaseFormat;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.TriConsumer;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

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

	@Autowired
	private IHostMonitoring hostMonitoring;

	private static final Map<ParameterType, BiFunction<Monitor, String, Boolean>> PARAMETER_TYPE_PREDICATES;
	private static final Map<ParameterType, TriConsumer<GaugeMetricFamily, Monitor, String>> PARAMETER_TYPE_CONSUMERS;
	static {
		final Map<ParameterType, BiFunction<Monitor, String, Boolean>> predicates = new EnumMap<>(ParameterType.class);

		predicates.put(ParameterType.STATUS, HostMonitoringCollectorService::checkStatusParameter);
		predicates.put(ParameterType.NUMBER, HostMonitoringCollectorService::checkNumberParameter);
		PARAMETER_TYPE_PREDICATES = Collections.unmodifiableMap(predicates);

		final Map<ParameterType, TriConsumer<GaugeMetricFamily, Monitor, String>> consumers = new EnumMap<>(ParameterType.class);

		consumers.put(ParameterType.STATUS, HostMonitoringCollectorService::addStatusMetric);
		consumers.put(ParameterType.NUMBER, HostMonitoringCollectorService::addNumberMetric);
		PARAMETER_TYPE_CONSUMERS = Collections.unmodifiableMap(consumers);
	}

	@Override
	public List<MetricFamilySamples> collect() {

		final List<MetricFamilySamples> mfs = new ArrayList<>();

		// Loop over all the monitor and create metrics (Prometheus samples)
		hostMonitoring.getMonitors().forEach((monitorType, monitors) -> processSameTypeMonitors(monitorType, monitors, mfs));

		return mfs;
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

		final GaugeMetricFamily labeledGauge = new GaugeMetricFamily(
				buildMetricName(monitorType.getName(), metaParameter.getName()),
				buildHelp(monitorType.getName(), metaParameter),
				Arrays.asList(ID, PARENT, LABEL));

		monitors.values()
		.stream()
		.filter(monitor -> isParameterAvailable(metaParameter, monitor))
		.forEach(monitor -> PARAMETER_TYPE_CONSUMERS.get(metaParameter.getType())
				.accept(labeledGauge, monitor, metaParameter.getName()));

		mfs.add(labeledGauge);
	}

	/**
	 * Build help for metric using format: <em>Metric: $monitorName $parameterName - Unit: $parameterUnit</em>
	 * 
	 * @param monitorName   The name of the monitor
	 * @param metaParameter {@link MetaParameter} information
	 * @return {@link String} value
	 */
	static String buildHelp(final String monitorName, final MetaParameter metaParameter) {
		return String.format("Metric: %s %s - Unit: %s", monitorName, metaParameter.getName(), metaParameter.getUnit());
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
				&& monitors.values().stream().anyMatch(monitor -> isParameterAvailable(metaParameter, monitor));
	}

	/**
	 * Check if the parameter defined in the passed {@link MetaParameter} is collected on the given monitor instance
	 * 
	 * @param metaParameter The {@link MetaParameter} defined by the matrix engine
	 * @param monitor       The monitor we wish to check the parameter
	 * @return <code>true</code> if the metric is collected otherwise <code>false</code>
	 */
	static Boolean isParameterAvailable(final MetaParameter metaParameter, final Monitor monitor) {
		return PARAMETER_TYPE_PREDICATES.get(metaParameter.getType()).apply(monitor, metaParameter.getName());
	}

	/**
	 * Check if a number parameter is collected
	 * 
	 * @param monitor       The monitor we wish to check its number parameter
	 * @param parameterName The name of the parameter e.g. energyUsage, voltage, temperature.
	 * @return
	 */
	static boolean checkNumberParameter(final Monitor monitor, final String parameterName) {
		return checkParameter(monitor, parameterName)
				&& getNumberParameterValue(monitor, parameterName) != null;
	}

	/**
	 * Check if a status parameter is collected
	 * 
	 * @param monitor       The monitor we wish to check its status parameter
	 * @param parameterName The name of the parameter e.g. status, intrusionStatus, errorStatus.
	 * @return <code>true</code> if the metric is collected otherwise <code>false</code>
	 */
	static boolean checkStatusParameter(final Monitor monitor, final String parameterName) {
		return checkParameter(monitor, parameterName)
				&& getStatusParameterValue(monitor, parameterName) != null;
	}

	/**
	 * Get the status parameter value (0,1 or 2)
	 * 
	 * @param monitor       The monitor we wish to get its status parameter value
	 * @param parameterName The name of the parameter e.g. status, intrusionStatus, errorStatus.
	 * @return {@link Integer} value
	 */
	static Integer getStatusParameterValue(final Monitor monitor, final String parameterName) {
		return ((StatusParam) monitor.getParameters().get(parameterName)).getStatus();
	}

	/**
	 * Get the number parameter value
	 * 
	 * @param monitor       The monitor we wish to get its number parameter value
	 * @param parameterName The name of the parameter e.g. energyUsage, voltage, temperature.
	 * @return {@link Double} value
	 */
	static Double getNumberParameterValue(final Monitor monitor, final String parameterName) {
		return ((NumberParam) monitor.getParameters().get(parameterName)).getValue();
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
	 * Add a status metric in the given Gauge metric
	 * 
	 * @param gauge         Prometheus {@link GaugeMetricFamily}
	 * @param monitor       Collected {@link Monitor} instance
	 * @param parameterName The parameter name we wish to add
	 */
	static void addStatusMetric(final GaugeMetricFamily gauge, final Monitor monitor, final String parameterName) {
		gauge.addMetric(
				createLabels(monitor),
				getStatusParameterValue(monitor, parameterName));
	}

	/**
	 * Add a number metric in the given Gauge metric
	 * 
	 * @param gauge         Prometheus {@link GaugeMetricFamily}
	 * @param monitor       Collected {@link Monitor} instance
	 * @param parameterName The parameter name we wish to add
	 */
	static void addNumberMetric(final GaugeMetricFamily gauge, final Monitor monitor, final String parameterName) {
		gauge.addMetric(
				// Id, parentId (can be null), label
				createLabels(monitor),
				getNumberParameterValue(monitor, parameterName));
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
				monitor.getName());
	}
}
