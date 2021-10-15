package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLOR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LED_INDICATOR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LED_INDICATOR_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.parameter.DiscreteParamType;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.state.LedColorStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.LedIndicator;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertDetails;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.alert.Severity;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitor.Monitor.AssertedParameter;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;

public class Led implements IMetaMonitor {

	public static final MetaParameter COLOR = MetaParameter.builder()
			.basicCollect(false)
			.name(COLOR_PARAMETER)
			.unit(STATUS_PARAMETER_UNIT)
			.type(new DiscreteParamType(LedColorStatus::interpret))
			.build();

	public static final MetaParameter STATUS = MetaParameter.builder()
		.basicCollect(false)
		.name(STATUS_PARAMETER)
		.unit(STATUS_PARAMETER_UNIT)
		.type(new DiscreteParamType(Status::interpret))
		.build();

	public static final MetaParameter LED_INDICATOR = MetaParameter.builder()
			.basicCollect(false)
			.name(LED_INDICATOR_PARAMETER)
			.unit(LED_INDICATOR_PARAMETER_UNIT)
			.type(new DiscreteParamType(LedIndicator::interpret))
			.build();

	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(
			(monitor, conditions) -> checkStatusWarnCondition(monitor, STATUS_PARAMETER, conditions),
			STATUS_WARN_CONDITION,
			Severity.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(
			(monitor, conditions) -> checkStatusAlarmCondition(monitor, STATUS_PARAMETER, conditions),
			STATUS_ALARM_CONDITION,
			Severity.ALARM);

	public static final AlertRule COLOR_WARN_ALERT_RULE = new AlertRule(
			(monitor, conditions) -> checkStatusWarnCondition(monitor, COLOR_PARAMETER, conditions),
			STATUS_WARN_CONDITION,
			Severity.WARN);
	public static final AlertRule COLOR_ALARM_ALERT_RULE = new AlertRule(
			(monitor, conditions) -> checkStatusAlarmCondition(monitor, COLOR_PARAMETER, conditions),
			STATUS_ALARM_CONDITION,
			Severity.ALARM);

	private static final List<String> METADATA = List.of(DEVICE_ID, NAME, IDENTIFYING_INFORMATION);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(COLOR_PARAMETER, COLOR);
		map.put(LED_INDICATOR_PARAMETER, LED_INDICATOR);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(COLOR_PARAMETER, List.of(COLOR_WARN_ALERT_RULE, COLOR_ALARM_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param parameter  The name of the parameter we wish to check
	 * @param conditions The conditions used to check the parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, String parameter, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(parameter, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This LED reports a degraded state." + IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("This may lead to a system crash or damaged hardware.")
					.recommendedAction("Check the hardware component for any visible problem and replace it if necessary.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param parameter  The name of the parameter we wish to check
	 * @param conditions The conditions used to check the parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, String parameter, Set<AlertCondition> conditions) {
		final AssertedParameter<DiscreteParam> assertedStatus = monitor.assertStatusParameter(parameter, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This LED reports a failed state." + IMetaMonitor.getStatusInformationMessage(monitor))
					.consequence("This may lead to a system crash or damaged hardware.")
					.recommendedAction("Replace or repair the faulty hardware component.")
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
		return MonitorType.LED;
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