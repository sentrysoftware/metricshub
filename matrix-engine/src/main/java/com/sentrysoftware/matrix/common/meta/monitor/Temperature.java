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
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_TYPE;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

public class Temperature implements IMetaMonitor {

	private static final String HIGH_TEMPERATURE_RECOMMENDED_ACTION = "Check why the temperature is out of the normal range (it may be due to a fan failure, a severe system overload or a failure in the data center cooling system).";

	private static final String HIGH_TEMPERATURE_CONSEQUENCE = "An out-of-range temperature may lead to a system crash or even damaged hardware.";

	public static final MetaParameter _TEMPERATURE = MetaParameter.builder()
			.basicCollect(false)
			.name(TEMPERATURE_PARAMETER)
			.unit(TEMPERATURE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, TEMPERATURE_TYPE, IDENTIFYING_INFORMATION);

	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(Temperature::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(Temperature::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(TEMPERATURE_PARAMETER, _TEMPERATURE);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("Although not yet critical, the temperature is out of the normal range."
							+ IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence(HIGH_TEMPERATURE_CONSEQUENCE)
					.recommendedAction(HIGH_TEMPERATURE_RECOMMENDED_ACTION)
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The temperature is critically out of the normal range."
							+  IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence(HIGH_TEMPERATURE_CONSEQUENCE)
					.recommendedAction(HIGH_TEMPERATURE_RECOMMENDED_ACTION)
					.build();

		}
		return null;
	}


	/**
	 * Check condition when the monitor temperature is abnormally high.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to check the temperature
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkTemperatureAbnormallyHighCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedTemperature = monitor.assertNumberParameter(TEMPERATURE_PARAMETER, conditions);
		if (assertedTemperature.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("Although not yet critical, the temperature is abnormally high (%f °C).", assertedTemperature.getParameter().getValue()))
					.consequence(HIGH_TEMPERATURE_CONSEQUENCE)
					.recommendedAction(HIGH_TEMPERATURE_RECOMMENDED_ACTION)
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor temperature is critically high.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to check the temperature
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkTemperatureCriticallyHighCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedTemperature = monitor.assertNumberParameter(TEMPERATURE_PARAMETER, conditions);
		if (assertedTemperature.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The temperature is critically high (%f °C).", assertedTemperature.getParameter().getValue()))
					.consequence(HIGH_TEMPERATURE_CONSEQUENCE)
					.recommendedAction(HIGH_TEMPERATURE_RECOMMENDED_ACTION)
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
		return MonitorType.TEMPERATURE;
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