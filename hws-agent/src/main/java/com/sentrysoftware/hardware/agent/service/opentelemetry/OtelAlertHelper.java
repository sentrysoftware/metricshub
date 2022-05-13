package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.OtelParameterToMetricObserver.getParameterValue;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.OtelParameterToMetricObserver.isParameterAvailable;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BIOS_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DRIVER_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOGICAL_ADDRESS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PHYSICAL_ADDRESS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_INFORMATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.sentrysoftware.hardware.agent.dto.MetricInfo;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.common.meta.parameter.state.IState;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.alert.AlertInfo;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access =  AccessLevel.PRIVATE)
public class OtelAlertHelper {

	private static final String TWO_NEW_LINES = "\n\n";

	private static final String METRIC_INFO_MUST_BE_PRESENT = "metricInfo must be present.";

	private static final Map<String, String> METADATA_TO_DISPLAY_PREFIX;

	static {
		final Map<String, String> map = new LinkedHashMap<>();
		map.put(VENDOR, "Manufacturer      : ");
		map.put(MODEL, "Model             : ");
		map.put(SERIAL_NUMBER, "Serial Number     : ");
		map.put(BIOS_VERSION, "Bios Version      : ");
		map.put(DRIVER_VERSION, "Driver Version    : ");
		map.put(PHYSICAL_ADDRESS, "Physical Address  : ");
		map.put(LOGICAL_ADDRESS, "Logical Address   : ");
		map.put(SIZE, "Size              : ");
		map.put(MAXIMUM_SPEED, "Maximum Speed     : ");

		METADATA_TO_DISPLAY_PREFIX = Collections.unmodifiableMap(map);
	}

	/**
	 * Build the hardware problem message for the actual {@link AlertInfo}
	 * 
	 * @param alertInfo          Alert information wrapping required data for building the hardware problem message
	 * @param hwsProblemTemplate The Hardware problem template, could be overridden by the user
	 * 
	 * @return String value
	 */
	public static String buildHardwareProblem(@NonNull final AlertInfo alertInfo, final String hwsProblemTemplate) {

		validState(alertInfo);

		// The metric must be mapped, same as on the hardware problem template 
		if (MetricsMapping.getMetricInfo(alertInfo.getMonitor().getMonitorType(), alertInfo.getParameterName())
				.isEmpty() || hwsProblemTemplate == null || hwsProblemTemplate.isBlank()) {
			return EMPTY;
		}

		final Monitor monitor = alertInfo.getMonitor();
		final String parameterName = alertInfo.getParameterName();
		final AlertRule alertRule = alertInfo.getAlertRule();

		String result = StringHelper.replace("${FQDN}", monitor::getFqdn, hwsProblemTemplate);

		result = StringHelper.replace("${MONITOR_NAME}", monitor::getName, result);

		result = StringHelper.replace("${MONITOR_ID}", monitor::getId, result);

		result = StringHelper.replace("${MONITOR_TYPE}", () -> monitor.getMonitorType().getDisplayName(), result);

		result = StringHelper.replace("${PARENT_ID}", StringHelper.getValue(monitor::getParentId, EMPTY), result);

		result = StringHelper.replace("${METRIC_NAME}",
				() -> getMetricName(monitor.getMonitorType(), parameterName), result);

		result = StringHelper.replace("${METRIC_VALUE}", () -> buildMetricValue(monitor, parameterName), result);

		result = StringHelper.replace("${SEVERITY}", () -> alertRule.getSeverity().name(), result);

		result = StringHelper.replace("${ALERT_RULE}",
				() -> buildAlertRule(alertRule, monitor.getMonitorType(), parameterName), result);

		result = StringHelper.replace("${ALERT_DATE}", () -> buildAlertDate(alertInfo), result);

		result = StringHelper.replace("${CONSEQUENCE}", () -> alertRule.getDetails().getConsequence(), result);

		result = StringHelper.replace("${RECOMMENDED_ACTION}",  () -> alertRule.getDetails().getRecommendedAction(), result);

		result = StringHelper.replace("${PROBLEM}",  () -> alertRule.getDetails().getProblem(), result);

		result = StringHelper.replace("${ALERT_DETAILS}",
				() -> buildAlertDetails(alertRule, monitor.getMonitorType(), parameterName), result);

		result = StringHelper.replace("${FULLREPORT}", () -> buildFullReport(alertInfo), result);

		return StringHelper.replace("${NEWLINE}", NEW_LINE, result);

	}

	/**
	 * Build the full report including information about the monitor, its parent and
	 * all the attached metrics with theirs values and alert rules
	 * 
	 * @param alertInfo Alert information
	 * @return String value
	 */
	static String buildFullReport(@NonNull final AlertInfo alertInfo) {

		final Monitor monitor = alertInfo.getMonitor();

		return new StringBuilder("Hardware Health Report (")
				.append(buildAlertDate(alertInfo))
				.append(")\n================================================\n")
				.append("\nMonitor           : ").append(monitor.getName())
				.append("\nType              : ").append(monitor.getMonitorType().getDisplayName())
				.append("\nOn Host           : ").append(monitor.getFqdn())
				.append("\nMonitor ID        : ").append(monitor.getId())
				.append("\nConnector Used    : ").append(StringHelper.getValue(() -> monitor.getMetadata(HardwareConstants.CONNECTOR), EMPTY))
				.append("\nParent ID         : ").append(StringHelper.getValue(monitor::getParentId, EMPTY))
				.append(NEW_LINE)
				.append(buildMonitorInformation(monitor))
				.append(TWO_NEW_LINES)
				.append(buildParentInformation(monitor.getName(), monitor.getParentId(), alertInfo.getHostMonitoring()))
				.append(NEW_LINE)
				.append(buildMetricsInformation(monitor))
				.toString();

	}

	/**
	 * Build metrics information
	 * 
	 * @param monitor The monitor from which we try to extract all the mapped metrics.
	 * 
	 * @return String value
	 */
	static String buildMetricsInformation(@NonNull final Monitor monitor) {

		// Stream all the metrics and build the details for each metrics. The entries are joined then with \n
		return MetricsMapping
				.getMatrixParamToMetricMap()
				.get(monitor.getMonitorType())
				.entrySet()
				.stream()
				.map(entry -> buildMetricDetails(entry.getKey(), entry.getValue(), monitor))
				.filter(Objects::nonNull)
				.collect(Collectors.joining(NEW_LINE));

	}

	/**
	 * Build metric details including the current value, state and unit and alert
	 * rules
	 * 
	 * @param parameterName The name of the parameter
	 * @param metricInfo    The metric information
	 * @param monitor       The monitor from which we extract the alert rules of the
	 *                      parameter
	 * @return String value
	 */
	static String buildMetricDetails(@NonNull final String parameterName,
			@NonNull final MetricInfo metricInfo,
			@NonNull final Monitor monitor) {
		// At this time the parameter must be available
		if (!isParameterAvailable(monitor, parameterName)) {
			return null;
		}

		final StringBuilder builder = new StringBuilder("\n=================================================================")
				.append("\nMetric: ").append(metricInfo.getName())
				.append("\n-----------------------------------------------------------------")
				.append("\nCurrent Value     : ").append(NumberHelper
						.formatNumber(getParameterValue(monitor, parameterName).doubleValue() * metricInfo.getFactor()));
		final IParameter parameter = monitor.getParameters().get(parameterName);

		// Let's get the state of the parameter
		final Optional<IState> maybeState = getParameterState(parameter);

		Optional<String> maybeStatusInfo = Optional.empty();
		// Append the state if present
		if (maybeState.isPresent()) {
			IState state = maybeState.get();
			builder.append(" (").append(state.getDisplayName()).append(")");

			// Get the status information
			if (state instanceof Status) {
				maybeStatusInfo = getStatusInformation(monitor);
			}
		}

		// Is there any unit?
		if (!metricInfo.getUnit().isBlank()) {
			builder
				.append("\nUnit              : ").append(metricInfo.getUnit());
		}

		// Maybe a status information?
		if (maybeStatusInfo.isPresent()) {
			builder
				.append("\nStatus Information: \n")
				.append(maybeStatusInfo.get())
				.append(NEW_LINE);
		}

		final List<AlertRule> alertRules = monitor.getAlertRules().get(parameterName);

		// Do we have alert rules on this parameter? If yes let's append them
		if (alertRules != null && !alertRules.isEmpty()) {
			final String thresholds = alertRules
					.stream()
					.map(alertRule -> String.format(" - %s: %s", alertRule.getSeverity(),
							buildAlertRule(alertRule, monitor.getMonitorType(), parameterName)))
					.collect(Collectors.joining(NEW_LINE));

			builder
				.append("\nAlert Rules       : \n")
				.append(thresholds);
		}

		return builder.toString();
	}

	/**
	 * Get the parameter state
	 * 
	 * @param parameter The matrix parameter {@link IParameter}
	 * @return Optional {@link IState} value
	 */
	static Optional<IState> getParameterState(final IParameter parameter) {
		if (parameter instanceof DiscreteParam) {
			final IState state = ((DiscreteParam) parameter).getState();
			return Optional.ofNullable(state);
		}

		return Optional.empty();
	}

	/**
	 * Build monitor information (Serial Number, Model, Manufacturer, ...)
	 * 
	 * @param monitor The monitor from which we extract the required metadata
	 * @return String value
	 */
	static String buildMonitorInformation(@NonNull final Monitor monitor) {
		// Stream all the known metadata and build an entry for each metadata. The entries are joined then with \n
		return METADATA_TO_DISPLAY_PREFIX.keySet()
				.stream()
				.map(metadata -> buildMetadataEntry(monitor, metadata, METADATA_TO_DISPLAY_PREFIX.get(metadata)))
				.filter(Objects::nonNull)
				.collect(Collectors.joining(NEW_LINE));
	}

	/**
	 * Build metadata report entry.
	 * 
	 * @param monitor  The monitor from which we extract metadata
	 * @param metadata The monitor's metadata key
	 * @param prefix   The metadata prefix in the report message
	 * @return String value
	 */
	static String buildMetadataEntry(@NonNull final Monitor monitor,
			@NonNull final String metadata, @NonNull final String prefix) {

		String value = monitor.getMetadata(metadata);

		// No value?
		if (value == null) {
			return null;
		}

		// A metadata from the matrix engine can be converted to an OpenTelemetry metric
		// that's why we try to get the metricInfo of the metadata. Example: Size is a
		// metadata in matrix but a metric in the Hardware Sentry Agent
		final Optional<MetricInfo> metricInfoOpt = MetricsMapping.getMetadataAsMetricInfo(monitor.getMonitorType(),
				metadata);
		if (!metricInfoOpt.isPresent()) {
			return prefix + value;
		}

		// Get the metric information
		final MetricInfo metricInfo = metricInfoOpt.get();

		// Get the unit
		final String unit = metricInfo.getUnit();

		// Parse the metadata to double because matrix stores the value in string 
		final Double parsed = NumberHelper.parseDouble(value, null);
		if (parsed != null) {
			// Apply the factor
			value = NumberHelper.formatNumber(parsed * metricInfo.getFactor());
		}

		return String.format("%s%s %s", prefix, value, unit);
	}

	/**
	 * Get the information related to the parent monitor instance
	 * 
	 * @param childName      The parent's child monitor
	 * @param parentId       The parent identifier
	 * @param hostMonitoring The wrapper of all the monitors used to extract parent
	 *                       information
	 * @return String value
	 */
	static String buildParentInformation(final String childName, String parentId, @NonNull final IHostMonitoring hostMonitoring) {
		// This is the target or any other root element
		if (parentId == null) {
			return String.format("%s is the root monitor", childName);
		}

		// Fetch the parent monitor
		final Monitor parent = hostMonitoring.findById(parentId);
		if (parent == null) {
			return "No information available on the parent";
		}

		// Build the final string
		return new StringBuilder("This object is attached to: ")
				.append(parent.getName())
				.append("\nType              : ").append(parent.getMonitorType().getDisplayName())
				.append(NEW_LINE)
				.append(buildMonitorInformation(parent))
				.toString();

	}

	/**
	 * Build the alert date using default time zone of the Java virtual machine
	 * 
	 * @param alertInfo Alert information wrapping the first trigger time
	 * @return An ISO local date time in string format.
	 */
	static String buildAlertDate(@NonNull AlertInfo alertInfo) {
		return LocalDateTime
				.ofInstant(Instant.ofEpochMilli(getAlertTime(alertInfo)), TimeZone.getDefault().toZoneId())
				.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	/**
	 * Build the alert rule in string format. E.g. hw.battery.charge <= 0.3 && hw.battery.charge >= 0.2
	 * 
	 * @param alertRule       The alert rule defining all the sub conditions
	 * @param monitorType     The type of the monitor used to get the {@link MetricInfo} instance
	 * @param matrixParamName The name of the parameter used to get the {@link MetricInfo} instance
	 * @return String value
	 */
	static String buildAlertRule(@NonNull final AlertRule alertRule,
			@NonNull final MonitorType monitorType,
			@NonNull final String matrixParamName) {

		// Get the metric information
		final Optional<MetricInfo> metricInfoOpt = MetricsMapping.getMetricInfo(monitorType, matrixParamName);

		// The metric information must be present
		Assert.isTrue(metricInfoOpt.isPresent(), METRIC_INFO_MUST_BE_PRESENT);

		final MetricInfo metricInfo = metricInfoOpt.get();

		// The metric unit provided by the connector couldn't be the same as the one defined by the Hardware Sentry Agent
		// The factor is less than equals 1 and greater than 0. E.G. 0.001 for hw.voltage.voltage_volts
		final double factor = metricInfo.getFactor();

		final String metricName = metricInfo.getName();

		// Build the alert rule and concatenate all the sub rules using `&&`
		return alertRule
				.getConditions()
				.stream()
				.map(condition -> String.format("%s %s %s",
						metricName, condition.getOperator().getExpression(), NumberHelper.formatNumber(condition.getThreshold() * factor)))
				.collect(Collectors.joining(" && "));
	}

	/**
	 * Get the name of the metric from the actual mapping defined in {@link MetricsMapping}
	 * 
	 * @param type            The type of the monitor
	 * @param matrixParamName The name of the matrix parameter
	 * @return The name of the OpenTelemetry metric (Gauge or Counter)
	 */
	static String getMetricName(@NonNull final MonitorType type, @NonNull final String matrixParamName) {

		// Get the metric information from the Hardware Sentry Agent mapping
		final Optional<MetricInfo> metricInfoOpt = MetricsMapping.getMetricInfo(type, matrixParamName);

		// The metric information must be present
		Assert.isTrue(metricInfoOpt.isPresent(), METRIC_INFO_MUST_BE_PRESENT);

		// Get the name of the counter or gauge metric
		return metricInfoOpt.get().getName();

	}


	/**
	 * Build metric value including its unit if present
	 * 
	 * @param monitor         The monitor from which we extract the parameter value
	 * @param matrixParamName The name of the matrix parameter
	 * @return String value
	 */
	static String buildMetricValue(@NonNull final Monitor monitor, @NonNull final String matrixParamName) {

		// The parameter must be available
		if (isParameterAvailable(monitor, matrixParamName)) {

			// Get the metricInfo. This represents the Hardware Sentry Agent's predefined mapping.
			final Optional<MetricInfo> metricInfoOpt = MetricsMapping.getMetricInfo(monitor.getMonitorType(), matrixParamName);

			// No mercy
			Assert.isTrue(metricInfoOpt.isPresent(), METRIC_INFO_MUST_BE_PRESENT);

			final MetricInfo metricInfo = metricInfoOpt.get();

			// Get the parameter value.
			double value = getParameterValue(monitor, matrixParamName).doubleValue() * metricInfo.getFactor();

			// Convert the value to string
			String metricValue = NumberHelper.formatNumber(value);

			final Optional<IState> maybeState = getParameterState(monitor.getParameters().get(matrixParamName));

			if (maybeState.isPresent()) {
				return String.format("%s (%s)", metricValue, maybeState.get().getDisplayName());
			}

			return metricValue;
		}

		return EMPTY;
	}

	/**
	 * Build alert details including the severity and the alert rule
	 * 
	 * @param alertRule       The alert rule defining the severity and the alert conditions
	 * @param monitorType     The type of the monitor which is defined by the matrix engine
	 * @param matrixParamName The name of the matrix parameter
	 * 
	 * @return {@link String} value
	 */
	static String buildAlertDetails(@NonNull final AlertRule alertRule, @NonNull final MonitorType monitorType,
			@NonNull final String matrixParamName) {

		return new StringBuilder("Alert Severity    : ")
				.append(alertRule.getSeverity())
				.append("\nAlert Rule        : ")
				.append(buildAlertRule(alertRule, monitorType, matrixParamName))
				.append("\n\nAlert Details")
				.append("\n=============\n")
				.append(alertRule.getDetails().toString())
				.toString();
	}

	/**
	 * Validate the alertInfo state
	 * 
	 * @param alertInfo Defines the information used to trigger the alert
	 * 
	 */
	static void validState(@NonNull final AlertInfo alertInfo) {
		Assert.notNull(alertInfo.getAlertRule(), "Alert rule cannot be null");
		Assert.notNull(alertInfo.getMonitor(), "Monitor cannot be null");
		Assert.notNull(alertInfo.getParameterName(), "Parameter name cannot be null");
		Assert.notNull(alertInfo.getHardwareTarget(), "Hardware Traget cannot be null");
		Assert.notNull(alertInfo.getHostMonitoring(), "HostMonitoring cannot be null");
	}

	/**
	 * Convert the Matrix Severity to OpenTelemetry Severity
	 * 
	 * @param alertInfo Matrix alert information
	 * @return OpenTelemetry Severity
	 */
	public static io.opentelemetry.sdk.logs.data.Severity convertToOtelSeverity(@NonNull final AlertInfo alertInfo) {

		Assert.notNull(alertInfo.getAlertRule(), "The alert information must include the alert rule to be triggered.");
		Assert.notNull(alertInfo.getAlertRule().getSeverity(), "The alert information must include the alert rule and its severity.");

		switch (alertInfo.getAlertRule().getSeverity()) {
		case INFO:
			return io.opentelemetry.sdk.logs.data.Severity.INFO;
		case WARN:
			return io.opentelemetry.sdk.logs.data.Severity.WARN;
		case ALARM:
		default:
			// There is no ALARM code on the OpenTeleemtry log severity. Let's map our ALARM to their ERROR Severity
			return io.opentelemetry.sdk.logs.data.Severity.ERROR;
		}
	}

	/**
	 * Get the alert time from the given alert info.
	 * 
	 * @param alertInfo The alert information wrapping the alert rule
	 * @return long value
	 */
	public static long getAlertTime(@NonNull final AlertInfo alertInfo) {

		Assert.notNull(alertInfo.getAlertRule(), "The alert information must include an alert rule defining the first trigger time.");

		return alertInfo.getAlertRule().getFirstTriggerTimestamp();

	}

	/**
	 * Get the status information parameter value from the given monitor
	 * 
	 * @param monitor {@link Monitor} instance from which we want to extract the
	 *                status information
	 * @return Optional of string value. The result is not present if status
	 *         information is not collected.
	 */
	static Optional<String> getStatusInformation(@NonNull final Monitor monitor) {
		final TextParam statusInformation = monitor.getParameter(STATUS_INFORMATION_PARAMETER, TextParam.class);

		return statusInformation != null ? Optional.ofNullable(statusInformation.getValue()) : Optional.empty();
	}

}
