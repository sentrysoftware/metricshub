package com.sentrysoftware.matrix.common.meta.monitor;

import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.SimpleParamType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertDetails;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.alert.Severity;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitor.Monitor.AssertedParameter;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.NumberParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BYTES_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BYTES_RATE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CORRECTED_ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DECODER_USED_TIME_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DECODER_USED_TIME_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DRIVER_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENCODER_USED_TIME_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENCODER_USED_TIME_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FIRMWARE_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MEMORY_UTILIZATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PERCENT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PREDICTED_FAILURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_TIME_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_TIME_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.common.helpers.NumberHelper.formatNumber;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.getValue;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.ERROR_COUNT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.MEMORY_UTILIZATION_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.MEMORY_UTILIZATION_WARN_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.USED_TIME_PERCENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.USED_TIME_PERCENT_WARN_CONDITION;

public class Gpu implements IMetaMonitor {

	public static final MetaParameter USED_TIME = MetaParameter.builder()
		.basicCollect(false)
		.name(USED_TIME_PARAMETER)
		.unit(TIME_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter USED_TIME_PERCENT = MetaParameter.builder()
		.basicCollect(false)
		.name(USED_TIME_PERCENT_PARAMETER)
		.unit(PERCENT_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter MEMORY_UTILIZATION = MetaParameter.builder()
		.basicCollect(true)
		.name(MEMORY_UTILIZATION_PARAMETER)
		.unit(PERCENT_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter ENCODER_USED_TIME = MetaParameter.builder()
		.basicCollect(false)
		.name(ENCODER_USED_TIME_PARAMETER)
		.unit(TIME_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter ENCODER_USED_TIME_PERCENT = MetaParameter.builder()
		.basicCollect(false)
		.name(ENCODER_USED_TIME_PERCENT_PARAMETER)
		.unit(PERCENT_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter DECODER_USED_TIME = MetaParameter.builder()
		.basicCollect(false)
		.name(DECODER_USED_TIME_PARAMETER)
		.unit(TIME_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter DECODER_USED_TIME_PERCENT = MetaParameter.builder()
		.basicCollect(false)
		.name(DECODER_USED_TIME_PERCENT_PARAMETER)
		.unit(PERCENT_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter TRANSMITTED_BYTES = MetaParameter.builder()
		.basicCollect(false)
		.name(TRANSMITTED_BYTES_PARAMETER)
		.unit(BYTES_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter TRANSMITTED_BYTES_RATE = MetaParameter.builder()
		.basicCollect(false)
		.name(TRANSMITTED_BYTES_RATE_PARAMETER)
		.unit(BYTES_RATE_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter RECEIVED_BYTES = MetaParameter.builder()
		.basicCollect(false)
		.name(RECEIVED_BYTES_PARAMETER)
		.unit(BYTES_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter RECEIVED_BYTES_RATE = MetaParameter.builder()
		.basicCollect(false)
		.name(RECEIVED_BYTES_RATE_PARAMETER)
		.unit(BYTES_RATE_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter CORRECTED_ERROR_COUNT = MetaParameter.builder()
		.basicCollect(false)
		.name(CORRECTED_ERROR_COUNT_PARAMETER)
		.unit(ERROR_COUNT_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	public static final MetaParameter ERROR_COUNT = MetaParameter.builder()
		.basicCollect(false)
		.name(ERROR_COUNT_PARAMETER)
		.unit(ERROR_COUNT_PARAMETER_UNIT)
		.type(SimpleParamType.NUMBER)
		.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, VENDOR, MODEL, DRIVER_VERSION, FIRMWARE_VERSION,
		SERIAL_NUMBER, SIZE, IDENTIFYING_INFORMATION);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(Gpu::checkMissingCondition,
		PRESENT_ALARM_CONDITION,
		Severity.ALARM);
	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(Gpu::checkStatusWarnCondition,
		STATUS_WARN_CONDITION,
		Severity.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(Gpu::checkStatusAlarmCondition,
		STATUS_ALARM_CONDITION,
		Severity.ALARM);
	public static final AlertRule PREDICTED_FAILURE_ALERT_RULE = new AlertRule(Gpu::checkPredictedFailureCondition,
		STATUS_WARN_CONDITION,
		Severity.WARN);
	public static final AlertRule USED_TIME_PERCENT_WARN_ALERT_RULE = new AlertRule(
		Gpu::checkUsedTimePercentWarnCondition,
		USED_TIME_PERCENT_WARN_CONDITION,
		Severity.WARN);
	public static final AlertRule USED_TIME_PERCENT_ALARM_ALERT_RULE = new AlertRule(
		Gpu::checkUsedTimePercentAlarmCondition,
		USED_TIME_PERCENT_ALARM_CONDITION,
		Severity.ALARM);
	public static final AlertRule MEMORY_UTILIZATION_WARN_ALERT_RULE = new AlertRule(
		Gpu::checkMemoryUtilizationWarnCondition,
		MEMORY_UTILIZATION_WARN_CONDITION,
		Severity.WARN);
	public static final AlertRule MEMORY_UTILIZATION_ALARM_ALERT_RULE = new AlertRule(
		Gpu::checkMemoryUtilizationAlarmCondition,
		MEMORY_UTILIZATION_ALARM_CONDITION,
		Severity.ALARM);
	public static final AlertRule ERROR_COUNT_ALERT_RULE = new AlertRule(
		Gpu::checkErrorCountCondition,
		ERROR_COUNT_ALARM_CONDITION,
		Severity.ALARM
	);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(USED_TIME_PARAMETER, USED_TIME);
		map.put(USED_TIME_PERCENT_PARAMETER, USED_TIME_PERCENT);
		map.put(MEMORY_UTILIZATION_PARAMETER, MEMORY_UTILIZATION);
		map.put(ENCODER_USED_TIME_PARAMETER, ENCODER_USED_TIME);
		map.put(ENCODER_USED_TIME_PERCENT_PARAMETER, ENCODER_USED_TIME_PERCENT);
		map.put(DECODER_USED_TIME_PARAMETER, DECODER_USED_TIME);
		map.put(DECODER_USED_TIME_PERCENT_PARAMETER, DECODER_USED_TIME_PERCENT);
		map.put(TRANSMITTED_BYTES_PARAMETER, TRANSMITTED_BYTES);
		map.put(TRANSMITTED_BYTES_RATE_PARAMETER, TRANSMITTED_BYTES_RATE);
		map.put(RECEIVED_BYTES_PARAMETER, RECEIVED_BYTES);
		map.put(RECEIVED_BYTES_RATE_PARAMETER, RECEIVED_BYTES_RATE);
		map.put(STATUS_PARAMETER, STATUS);
		map.put(PRESENT_PARAMETER, PRESENT);
		map.put(ERROR_COUNT_PARAMETER, ERROR_COUNT);
		map.put(CORRECTED_ERROR_COUNT_PARAMETER, CORRECTED_ERROR_COUNT);
		map.put(PREDICTED_FAILURE_PARAMETER, PREDICTED_FAILURE);
		map.put(ENERGY_PARAMETER, ENERGY);
		map.put(ENERGY_USAGE_PARAMETER, ENERGY_USAGE);
		map.put(POWER_CONSUMPTION_PARAMETER, POWER_CONSUMPTION);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(PREDICTED_FAILURE_PARAMETER, Collections.singletonList(PREDICTED_FAILURE_ALERT_RULE));
		alertRulesMap.put(USED_TIME_PERCENT_PARAMETER,
			List.of(USED_TIME_PERCENT_WARN_ALERT_RULE, USED_TIME_PERCENT_ALARM_ALERT_RULE));
		alertRulesMap.put(MEMORY_UTILIZATION_PARAMETER,
			List.of(MEMORY_UTILIZATION_WARN_ALERT_RULE, MEMORY_UTILIZATION_ALARM_ALERT_RULE));
		alertRulesMap.put(ERROR_COUNT_PARAMETER, Collections.singletonList(ERROR_COUNT_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 *
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {

		final AssertedParameter<DiscreteParam> assertedStatus = monitor
			.assertStatusParameter(STATUS_PARAMETER, conditions);

		if (assertedStatus.isAbnormal()) {

			return AlertDetails
				.builder()
				.problem("This GPU is degraded or is about to fail."
					+ IMetaMonitor.getStatusInformationMessage(monitor))
				.consequence("This GPU may not be fully operational. If the GPU is part of a cluster, this could adversely impact the cluster's performance." +
					" If it is used as passthrough, it may crash the virtual machines that uses this GPU.")
				.recommendedAction("Replace this GPU as soon as possible.")
				.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 *
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {

		final AssertedParameter<DiscreteParam> assertedStatus = monitor
			.assertStatusParameter(STATUS_PARAMETER, conditions);

		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
				.problem("This GPU has failed." + IMetaMonitor.getStatusInformationMessage(monitor))
				.consequence("This GPU is not operational. This will impact the overall performance of the system or the GPU cluster it is part of.")
				.recommendedAction("Replace this GPU. If used as passthrough, make sure the virtual machines that were using this GPU are still up and running.")
				.build();
		}

		return null;
	}

	/**
	 * Check missing GPU condition.
	 *
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {

		final AssertedParameter<DiscreteParam> assertedPresent = monitor.assertPresentParameter(conditions);

		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder()
				.problem("This GPU is not detected anymore.")
				.consequence("The GPU is probably no longer operating.")
				.recommendedAction("Check whether the GPU has been intentionally removed by authorized personnel or whether it has failed." +
					" In the latter case, replace the faulty GPU.")
				.build();
		}

		return null;
	}

	/**
	 * Check condition when the GPU predicted failure is in WARN state.
	 *
	 * @param monitor    The GPU we wish to check its predicted failure
	 * @param conditions The conditions used to determine if the alarm is reached
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkPredictedFailureCondition(Monitor monitor, Set<AlertCondition> conditions) {

		AssertedParameter<DiscreteParam> assertedPredictedFailure = monitor
			.assertStatusParameter(PREDICTED_FAILURE_PARAMETER, conditions);

		if (assertedPredictedFailure.isAbnormal()) {

			return AlertDetails.builder()
				.problem("An imminent failure is predicted for this GPU.")
				.consequence("If part of a cluster of GPUs, this will impact the cluster's overall performance." +
					" If used in a system or as passthrough, a system crash is very likely to occur soon.")
				.recommendedAction("Replace this GPU as soon as possible.")
				.build();
		}

		return null;
	}

	/**
	 * Check condition when the GPU used time percentage is outside of the expected range.
	 *
	 * @param monitor    The monitor we wish to check its used time percentage
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkUsedTimePercentWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {

		final AssertedParameter<NumberParam> assertedUsedTimePercent = monitor
			.assertNumberParameter(USED_TIME_PERCENT_PARAMETER, conditions);

		if (assertedUsedTimePercent.isAbnormal()) {

			return AlertDetails.builder()
				.problem(String.format("The GPU time usage is outside of the expected range (%s %s).",
						getValue(() -> formatNumber(assertedUsedTimePercent.getParameter().getValue()), EMPTY),
						PERCENT_PARAMETER_UNIT))
				.consequence("The processing load may not be optimal and therefore may lead to lower system performance.")
				.recommendedAction("Check why this GPU is used this way (it may be normal). " +
					" If part of a cluster, check if any other GPUs have failed, consequently overloading this GPU."+ 
					" Look for a driver update that may fix usage issues.")
				.build();
		}

		return null;
	}

	/**
	 * Check condition when the GPU used time percentage is outside of the tolerated range.
	 *
	 * @param monitor    The monitor we wish to check its used time percentage
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkUsedTimePercentAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {

		final AssertedParameter<NumberParam> assertedUsedTimePercent = monitor
			.assertNumberParameter(USED_TIME_PERCENT_PARAMETER, conditions);

		if (assertedUsedTimePercent.isAbnormal()) {

			return AlertDetails.builder()
				.problem(String.format("The GPU time usage is outside of the tolerated range (%s %s).",
						getValue(() -> formatNumber(assertedUsedTimePercent.getParameter().getValue()), EMPTY),
						PERCENT_PARAMETER_UNIT))
				.consequence("The processing load may not be optimal and therefore may lead to lower system performance.")
				.recommendedAction("Check why this GPU is used this way (it may be normal)." +
					" If part of a cluster, check if any other GPUs have failed, consequently overloading this GPU." +
					" Look for a driver update that may fix usage issues.")
				.build();
		}

		return null;
	}

	/**
	 * Check condition when the GPU memory utilization is outside of the expected range.
	 *
	 * @param monitor    The monitor whose memory utilization we wish to check.
	 * @param conditions The conditions used to determine the abnormality.
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null.
	 */
	public static AlertDetails checkMemoryUtilizationWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {

		final AssertedParameter<NumberParam> assertedMemoryUtilization = monitor
			.assertNumberParameter(MEMORY_UTILIZATION_PARAMETER, conditions);

		if (assertedMemoryUtilization.isAbnormal()) {

			return AlertDetails.builder()
				.problem(String.format("The GPU memory utilization is outside of the expected range (%s %s).",
						getValue(() -> formatNumber(assertedMemoryUtilization.getParameter().getValue()), EMPTY),
						PERCENT_PARAMETER_UNIT))
				.consequence("A high memory utilization may lead to lower system performance.")
				.recommendedAction("Check which processes use most of the GPU's memory and verify that it is an expected behavior.")
				.build();
		}

		return null;
	}

	/**
	 * Check condition when the GPU memory utilization is outside of the expected range.
	 *
	 * @param monitor    The monitor whose memory utilization we wish to check.
	 * @param conditions The conditions used to determine the abnormality.
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null.
	 */
	public static AlertDetails checkMemoryUtilizationAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {

		final AssertedParameter<NumberParam> assertedMemoryUtilization = monitor
			.assertNumberParameter(MEMORY_UTILIZATION_PARAMETER, conditions);

		if (assertedMemoryUtilization.isAbnormal()) {

			return AlertDetails.builder()
				.problem(String.format("The GPU memory utilization is outside of the tolerated range (%s %s).",
						getValue(() -> formatNumber(assertedMemoryUtilization.getParameter().getValue()), EMPTY),
						PERCENT_PARAMETER_UNIT))
				.consequence("A high memory utilization may lead to lower system performance.")
				.recommendedAction("Check which processes use most of the GPU's memory and verify that it is an expected behavior.")
				.build();
		}

		return null;
	}


	/**
	 * Check condition when the monitor error count parameter is abnormal.
	 * 
	 * @param monitor    The monitor we wish to check its error count
	 * @param conditions The condition used to check the error count parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkErrorCountCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedErrorCount = monitor.assertNumberParameter(ERROR_COUNT_PARAMETER, conditions);
		if (assertedErrorCount.isAbnormal()) {

			String serialNumber = monitor.getMetadata(SERIAL_NUMBER);
			return AlertDetails.builder()
					.problem(String.format("The GPU encountered a high number of errors (%s).",
							getValue(() -> formatNumber(assertedErrorCount.getParameter().getValue()), EMPTY)))
					.consequence("The stability of the system used by this GPU may be critically affected. A system crash is very likely to occur soon.")
					.recommendedAction("Check as soon as possible if the GPU environment is normal (voltage levels and temperature)." +
						" If so, the GPU may be defective and needs to be replaced quickly."
						+ ((serialNumber != null) ? String.format(" Please note this GPU's serial number: %s.", serialNumber) : ""))
					.build();
		}

		return null;
	}

	@Override
	public void accept(IMonitorVisitor monitorVisitor) {
		monitorVisitor.visit(this);
	}

	@Override
	public Map<String, MetaParameter> getMetaParameters() {
		return META_PARAMETERS;
	}

	@Override
	public MonitorType getMonitorType() {
		return MonitorType.CPU;
	}

	@Override
	public List<String> getMetadata() {
		return METADATA;
	}

	@Override
	public Map<String, List<AlertRule>> getStaticAlertRules() {
		return ALERT_RULES;
	}
}