package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_TYPE;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.USED_CAPACITY_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.USED_CAPACITY_WARN_CONDITION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
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

public class PowerSupply implements IMetaMonitor {

	public static final MetaParameter USED_CAPACITY = MetaParameter.builder()
			.basicCollect(false)
			.name(HardwareConstants.USED_CAPACITY_PARAMETER)
			.unit(HardwareConstants.PERCENT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, POWER_SUPPLY_TYPE, ADDITIONAL_INFORMATION1,
			ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(PowerSupply::checkMissingCondition,
			PRESENT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(PowerSupply::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(PowerSupply::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule USED_CAPACITY_WARN_ALERT_RULE = new AlertRule(PowerSupply::checkAbnormalHighUsedCapacityWarnCondition,
			USED_CAPACITY_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule USED_CAPACITY_ALARM_ALERT_RULE = new AlertRule(PowerSupply::checkUsedCapacityCondition,
			USED_CAPACITY_ALARM_CONDITION,
			ParameterState.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.PRESENT_PARAMETER, PRESENT);
		map.put(HardwareConstants.USED_CAPACITY_PARAMETER, USED_CAPACITY);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(HardwareConstants.PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.USED_CAPACITY_PARAMETER, List.of(USED_CAPACITY_WARN_ALERT_RULE, USED_CAPACITY_ALARM_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);
	}

	/**
	 * Check missing PowerSupply condition.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<PresentParam> assertedPresent = monitor.assertPresentParameter(conditions);
		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This power supply is no longer detected.")
					.consequence("This could mean that this power supply unit is dead or has been removed. Power redundancy may not be available anymore.")
					.recommendedAction("Verify that the power supply is actually out of order or has been removed by an administrator. In either case, please add a new power supply unit in the system to ensure power redundancy.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(HardwareConstants.STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This power supply is in degraded state, or is about to fail." + IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("If this is the only power supply in the enclosure and it is about to fail, the server is about to be turned off. A degraded power supply may also lead to an unstable power conversion and thus to severe system crashes or hardware damages.")
					.recommendedAction("Quickly replace the faulty power supply.")
					.build();

		}
		return null;
	}

	/**
	 * Condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(HardwareConstants.STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The power supply is in a critical state." +  IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("A failing power supply will turn off the server soon.")
					.recommendedAction("Quickly replace the faulty power supply.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor used capacity parameter is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its used capacity
	 * @param conditions The conditions used to check the used capacity parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkAbnormalHighUsedCapacityWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedUsedCapacity = monitor.assertNumberParameter(HardwareConstants.USED_CAPACITY_PARAMETER, conditions);
		if (assertedUsedCapacity.isAbnormal()) {

			String information = getUsedCapacityInfo(monitor, assertedUsedCapacity.getParameter());

			return AlertDetails.builder()
					.problem(String.format("The power used by the system is abnormally high for the capacity of the power supply %s", information))
					.consequence("Overloading a power supply may lead to voltage unstability and system crashes. It can also cause severe damage to the power supply.")
					.recommendedAction("Find out why the power used by the system has increased this much (typically: new hard-drives). Upgrade to a power supply that can support such a power demand.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor used capacity parameter is critical.
	 * 
	 * @param monitor    The monitor we wish to check its used capacity
	 * @param conditions The condition used to check the used capacity parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkUsedCapacityCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedUsedCapacity = monitor.assertNumberParameter(HardwareConstants.USED_CAPACITY_PARAMETER, conditions);
		if (assertedUsedCapacity.isAbnormal()) {

			String information = getUsedCapacityInfo(monitor, assertedUsedCapacity.getParameter());

			return AlertDetails.builder()
					.problem(String.format("The power consumed by the system is critically high for the capacity of the power supply %s.", information))
					.consequence("Overloading a power supply may lead to voltage unstability and system crashes. It can also cause severe damage to the power supply.")
					.recommendedAction("Find out why the power used by the system has increased this much (typically: new hard-drives). Upgrade to a power supply that can support such a power demand.")
					.build();
		}

		return null;
	}

	/**
	 * Build the used capacity information
	 * 
	 * @param monitor      The monitor (PowerSupply) from which we want to extract the Power metadata
	 * @param usedCapacity The used capacity parameter
	 * @return {@link String} value
	 */
	private static String getUsedCapacityInfo(Monitor monitor, NumberParam usedCapacity) {
		String power = monitor.getMetadata(HardwareConstants.POWER);
		return power != null ? String.format("%f of %s W", usedCapacity.getValue(), power)
					: usedCapacity.getValue().toString();
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
		return MonitorType.POWER_SUPPLY;
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