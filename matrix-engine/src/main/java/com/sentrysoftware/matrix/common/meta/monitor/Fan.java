package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FAN_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PERCENT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.NumberHelper.formatNumber;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.getValue;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.SPEED_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.SPEED_PERCENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.SPEED_PERCENT_WARN_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.SPEED_WARN_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

public class Fan implements IMetaMonitor {

	private static final String RECOMMENDED_ACTION_FOR_BAD_FAN = "Check if the fan is no longer cooling the system. If so, replace the fan.";

	public static final MetaParameter SPEED = MetaParameter.builder()
			.basicCollect(true)
			.name(SPEED_PARAMETER)
			.unit(SPEED_PARAMETER_UNIT)
			.type(SimpleParamType.NUMBER)
			.build();

	public static final MetaParameter SPEED_PERCENT = MetaParameter.builder()
			.basicCollect(true)
			.name(SPEED_PERCENT_PARAMETER)
			.unit(SPEED_PERCENT_PARAMETER_UNIT)
			.type(SimpleParamType.NUMBER)
			.build();

	public static final MetaParameter POWER_CONSUMPTION = MetaParameter.builder()
			.basicCollect(false)
			.name(POWER_CONSUMPTION_PARAMETER)
			.unit(POWER_CONSUMPTION_PARAMETER_UNIT)
			.type(SimpleParamType.NUMBER)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, FAN_TYPE, IDENTIFYING_INFORMATION);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(Fan::checkMissingCondition,
			PRESENT_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(Fan::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			Severity.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(Fan::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule SPEED_ALARM_ALERT_RULE = new AlertRule((monitor, conditions) -> 
			checkZeroSpeedCondition(monitor, SPEED_PARAMETER, conditions),
			SPEED_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule SPEED_WARN_ALERT_RULE = new AlertRule((monitor, conditions) -> 
			checkOutOfRangeSpeedCondition(monitor, SPEED_PARAMETER, conditions),
			SPEED_WARN_CONDITION,
			Severity.WARN);
	public static final AlertRule SPEED_PERCENT_ALARM_ALERT_RULE = new AlertRule((monitor, conditions) -> 
			checkZeroSpeedCondition(monitor, SPEED_PERCENT_PARAMETER, conditions),
			SPEED_PERCENT_ALARM_CONDITION,
			Severity.ALARM);
	public static final AlertRule SPEED_PERCENT_WARN_ALERT_RULE = new AlertRule((monitor, conditions) -> 
			checkOutOfRangeSpeedCondition(monitor, SPEED_PERCENT_PARAMETER, conditions),
			SPEED_PERCENT_WARN_CONDITION,
			Severity.WARN);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(PRESENT_PARAMETER, PRESENT);
		map.put(SPEED_PARAMETER, SPEED);
		map.put(SPEED_PERCENT_PARAMETER, SPEED_PERCENT);
		map.put(ENERGY_PARAMETER, ENERGY);
		map.put(ENERGY_USAGE_PARAMETER, ENERGY_USAGE);
		map.put(POWER_CONSUMPTION_PARAMETER, POWER_CONSUMPTION);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(SPEED_PARAMETER, List.of(SPEED_ALARM_ALERT_RULE, SPEED_WARN_ALERT_RULE));
		alertRulesMap.put(SPEED_PERCENT_PARAMETER, List.of(SPEED_PERCENT_ALARM_ALERT_RULE, SPEED_PERCENT_WARN_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);

	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to check the parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The fan is degraded or about to fail." + IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("This may lead to a temperature increase of the device cooled by this fan and therefore, to a system crash or damaged hardware.")
					.recommendedAction(RECOMMENDED_ACTION_FOR_BAD_FAN)
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to check the parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The fan has failed and no longer cools down the system." +  IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("This may lead to a temperature increase of the device cooled by this fan and therefore, to a system crash or damaged hardware.")
					.recommendedAction(RECOMMENDED_ACTION_FOR_BAD_FAN)
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor speed is in an abnormal state.
	 * 
	 * @param monitor        The monitor we wish to check its speed
	 * @param parameterName  The name of the parameter to check
	 * @param conditions     The conditions used to check the parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkZeroSpeedCondition(Monitor monitor, String parameterName, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedSpeed = monitor.assertNumberParameter(parameterName, conditions);
		if (assertedSpeed.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This fan is reported as not spinning anymore.")
					.consequence("The temperature of the chip, component or device that was cooled by this fan, may rise rapidly. This could lead to severe hardware damage and system crashes.")
					.recommendedAction(RECOMMENDED_ACTION_FOR_BAD_FAN)
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor speed is in an abnormal state. (out of range)
	 * 
	 * @param monitor        The monitor we wish to check its speed
	 * @param parameterName  The name of the parameter to check
	 * @param conditions     The conditions used to check the parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkOutOfRangeSpeedCondition(Monitor monitor, String parameterName, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedSpeed = monitor.assertNumberParameter(parameterName, conditions);
		if (assertedSpeed.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The speed of this fan is out of normal range (%s %s).",
							getValue(() -> formatNumber(assertedSpeed.getParameter().getValue()), EMPTY),
							META_PARAMETERS.get(parameterName).getUnit()))
					.consequence("The fan is not behaving as expected and probably not properly cooling down the system. This could lead to severe hardware damage and system crashes.")
					.recommendedAction("Check if the fan is not working as expected. If so, replace the fan.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor speed is in an abnormal state (low).
	 * 
	 * @param monitor        The monitor we wish to check its speed
	 * @param parameterName  The name of the parameter to check
	 * @param conditions     The conditions used to check the parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkLowSpeedCondition(Monitor monitor, String parameterName, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedSpeed = monitor.assertNumberParameter(parameterName, conditions);
		if (assertedSpeed.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The speed of this fan is critically low (%s %s).",
							getValue(() -> formatNumber(assertedSpeed.getParameter().getValue()), EMPTY),
							META_PARAMETERS.get(parameterName).getUnit()))
					.consequence("The temperature of the chip, component or device that was cooled down by this fan, may rise rapidly. This could lead to severe hardware damage and system crashes.")
					.recommendedAction(RECOMMENDED_ACTION_FOR_BAD_FAN)
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor speed is in an abnormal state (insufficient).
	 * 
	 * @param monitor        The monitor we wish to check its speed
	 * @param parameterName  The name of the parameter to check
	 * @param conditions     The conditions used to check the parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkInsufficientSpeedCondition(Monitor monitor, String parameterName, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedSpeed = monitor.assertNumberParameter(parameterName, conditions);
		if (assertedSpeed.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The speed of this fan is insufficient (%s %s).",
							getValue(() -> formatNumber(assertedSpeed.getParameter().getValue()), EMPTY),
							META_PARAMETERS.get(parameterName).getUnit()))
					.consequence("The temperature of the chip, component or device that was cooled down by this fan, may increase slightly. This could lead to system crashes.")
					.recommendedAction("Check why the fan is running slow. This may be caused by dust or wear and tear. Replace the fan if needed.")
					.build();

		}
		return null;
	}

	/**
	 * Check missing Fan condition.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedPresent = monitor.assertPresentParameter(conditions);
		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This fan is not detected anymore.")
					.consequence("This could either mean that the fan is no longer powered or that the sensor has failed. If the fan is turned off, it may lead to a temperature increase on the device cooled by this fan, could hence result in a system crash or damaged hardware.")
					.recommendedAction(RECOMMENDED_ACTION_FOR_BAD_FAN)
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
		return MonitorType.FAN;
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