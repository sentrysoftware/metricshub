package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VOLTAGE_TYPE;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import com.sentrysoftware.matrix.model.parameter.StatusParam;

public class Voltage implements IMetaMonitor {

	private static final List<String> METADATA = List.of(DEVICE_ID, VOLTAGE_TYPE, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3);

	private static final String OUT_OF_RANGE_VOLTAGE_RECOMMENDED_ACTION = "Check why the voltage is out of the normal range (a feeble power supply unit, an unstable power converter, or a severe power overload owing to too many devices within the system).";

	private static final String OUT_OF_RANGE_VOLTAGE_CONSEQUENCE = "An out-of-range voltage may lead to a system crash.";

	public static final MetaParameter _VOLTAGE = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.VOLTAGE_PARAMETER)
			.unit(HardwareConstants.VOLTAGE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(Voltage::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(Voltage::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.VOLTAGE_PARAMETER, _VOLTAGE);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap .put(HardwareConstants.STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, List<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(HardwareConstants.STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("Although still not critical, the voltage level is out of the normal range." + IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence(OUT_OF_RANGE_VOLTAGE_CONSEQUENCE)
					.recommendedAction(OUT_OF_RANGE_VOLTAGE_RECOMMENDED_ACTION)
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
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, List<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(HardwareConstants.STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			String problem = "The voltage is critically out of the normal range." +  IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter());
			return AlertDetails.builder()
					.problem(problem)
					.consequence(OUT_OF_RANGE_VOLTAGE_CONSEQUENCE)
					.recommendedAction(OUT_OF_RANGE_VOLTAGE_RECOMMENDED_ACTION)
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor voltage is high.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to check the voltage
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkVoltageHighCondition(Monitor monitor, List<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedVoltage = monitor.assertNumberParameter(HardwareConstants.VOLTAGE_PARAMETER, conditions);
		if (assertedVoltage.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The voltage has reached an abnormal level (%f).", assertedVoltage.getParameter().getValue()))
					.consequence(OUT_OF_RANGE_VOLTAGE_CONSEQUENCE)
					.recommendedAction(OUT_OF_RANGE_VOLTAGE_RECOMMENDED_ACTION)
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
		return MonitorType.VOLTAGE;
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