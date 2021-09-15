package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertDetails;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitor.Monitor.AssertedParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CHARGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CHEMISTRY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PERCENT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_LEFT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.CHARGE_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.CHARGE_WARN_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

public class Battery implements IMetaMonitor {

	public static final MetaParameter CHARGE = MetaParameter.builder()
		.basicCollect(false)
		.name(CHARGE_PARAMETER)
		.unit(PERCENT_PARAMETER_UNIT)
		.type(ParameterType.NUMBER)
		.build();

	public static final MetaParameter TIME_LEFT = MetaParameter.builder()
			.basicCollect(false)
			.name(TIME_LEFT_PARAMETER)
			.unit(TIME_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, VENDOR, MODEL, TYPE, CHEMISTRY, IDENTIFYING_INFORMATION);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(Battery::checkMissingCondition,
			PRESENT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(Battery::checkStatusWarnCondition, 
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(Battery::checkStatusAlarmConditionChecker,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule CHARGE_WARN_ALERT_RULE = new AlertRule(Battery::checkChargeWarnCondition,
			CHARGE_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule CHARGE_ALARM_ALERT_RULE = new AlertRule(Battery::checkChargeAlarmCondition, 
			CHARGE_ALARM_CONDITION,
			ParameterState.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(PRESENT_PARAMETER, PRESENT);
		map.put(CHARGE_PARAMETER, CHARGE);
		map.put(TIME_LEFT_PARAMETER, TIME_LEFT);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(CHARGE_PARAMETER, List.of(CHARGE_WARN_ALERT_RULE, CHARGE_ALARM_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);

	}

	/**
	 * Check missing battery condition.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<PresentParam> assertedPresent = monitor.assertPresentParameter(conditions);
		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder().problem("This battery is not detected anymore.")
					.consequence("This could mean that the battery has been removed and is no longer available.")
					.recommendedAction("Check whether the battery was intentionally removed from the system or if it not responding.").build();

		}

		return null;
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This battery is degraded." + IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("Check that number of allowed recharges has not been exceeded and that the current full charge capacity (FCC)"
							+ " is close to the full charge capacity of initial battery.")
					.recommendedAction("The battery needs to be replaced or reconditioned.").build();

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
	public static AlertDetails checkStatusAlarmConditionChecker(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This battery has failed." + IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("This battery is nonoperational and a power outage will certainly lead to data loss and/or corruption.")
					.recommendedAction("Replace the battery.").build();

		}
		return null;
	}

	/**
	 * Check condition when the battery charge is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check the charge
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkChargeWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedCharge = monitor.assertNumberParameter(CHARGE_PARAMETER, conditions);
		if (assertedCharge.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("Although not yet critical, the battery charge is abnormally low (%f %s).",
							assertedCharge.getParameter().getValue(), PERCENT_PARAMETER_UNIT))
					.consequence("A low charge battery may lead to data loss in case of a power outage.")
					.recommendedAction(
							"Check why the battery is not fully charged (it may be due to a power outage or an unplugged power cable) and fully recharge the battery when possible.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the battery charge is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check the charge
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkChargeAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedCharge = monitor.assertNumberParameter(CHARGE_PARAMETER, conditions);
		if (assertedCharge.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The battery charge is very low and will soon run out of charge (%f %s).",
							assertedCharge.getParameter().getValue(), PERCENT_PARAMETER_UNIT))
					.consequence("A low charge battery may lead to data loss in case of a power outage.")
					.recommendedAction(
							"Check why the battery is not fully charged (it may be due to a power outage or an unplugged power cable) and fully recharge the battery when possible.")
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
		return MonitorType.BATTERY;
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