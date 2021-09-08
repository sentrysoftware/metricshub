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

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CORRECTED_ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CURRENT_SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CURRENT_SPEED_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PREDICTED_FAILURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.CORRECTED_ERROR_COUNT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

public class Cpu implements IMetaMonitor {

	public static final MetaParameter CORRECTED_ERROR_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(CORRECTED_ERROR_COUNT_PARAMETER)
			.unit(ERROR_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter CURRENT_SPEED = MetaParameter.builder()
			.basicCollect(true)
			.name(CURRENT_SPEED_PARAMETER)
			.unit(CURRENT_SPEED_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, VENDOR, MODEL, MAXIMUM_SPEED, ADDITIONAL_INFORMATION1,
			ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(Cpu::checkMissingCondition,
			PRESENT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(Cpu::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(Cpu::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule CORRECTED_ERROR_COUNT_ALERT_RULE = new AlertRule(Cpu::checkCorrectedFiewErrorCountCondition,
			CORRECTED_ERROR_COUNT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule PREDICTED_FAILURE_ALERT_RULE = new AlertRule(Cpu::checkPredictedFailureCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(CORRECTED_ERROR_COUNT_PARAMETER, CORRECTED_ERROR_COUNT);
		map.put(CURRENT_SPEED_PARAMETER, CURRENT_SPEED);
		map.put(PREDICTED_FAILURE_PARAMETER, PREDICTED_FAILURE);
		map.put(PRESENT_PARAMETER, PRESENT);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(CORRECTED_ERROR_COUNT_PARAMETER, Collections.singletonList(CORRECTED_ERROR_COUNT_ALERT_RULE));
		alertRulesMap.put(PREDICTED_FAILURE_PARAMETER, Collections.singletonList(PREDICTED_FAILURE_ALERT_RULE));

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
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This processor is degraded or is about to fail." + IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("If degraded, this processor may not be fully operational. This could adversely impact the overall performance of the system. If it is about to fail, this processor is likely to crash very soon.")
					.recommendedAction("Replace this processor as soon as possible to prevent a system crash.")
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
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This processor has failed." +  IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("This processor is not operational. This will impact the overall performance of the system. If it is about to fail this processor may crash very soon.")
					.recommendedAction("Replace this processor as soon as possible to prevent a system overload.")
					.build();

		}

		return null;
	}

	/**
	 * Check missing processor condition.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<PresentParam> assertedPresent = monitor.assertPresentParameter(conditions);
		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This processor is not detected anymore.")
					.consequence("This could mean that the processor has been disabled by BIOS, or that it has been deallocated for this system (in case of a dynamically reconfigurable machine)." +
							" The computing capabilities have been reduced and this may impact the overall performance of this computer.")
					.recommendedAction("Check whether the processor has been intentionally removed by authorized personnel or whether it has failed. In the latter case, replace the faulty processor.")
					.build();

		}

		return null;
	}

	/**
	 * Check condition when the CPU predicted failure is in WARN state.
	 * 
	 * @param monitor    The CPU we wish to check its predicted failure
	 * @param conditions The conditions used to determine if the alarm is reached 
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkPredictedFailureCondition(Monitor monitor, Set<AlertCondition> conditions) {
		AssertedParameter<StatusParam> assertedPredictedFailure = monitor.assertStatusParameter(PREDICTED_FAILURE_PARAMETER, conditions);
		if (assertedPredictedFailure.isAbnormal()) {

			return AlertDetails.builder()
					.problem("An imminent failure is predicted for this processor.")
					.consequence("A system crash is very likely to occur soon.")
					.recommendedAction("Replace this processor or disable it as soon as possible, if disabling it will not affect the performance of the system.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the CPU encountered and fixed a few internal errors.
	 * 
	 * @param monitor    The CPU we wish to check its correctedErrorCount
	 * @param conditions The conditions used to determine if the alarm is reached 
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkCorrectedFiewErrorCountCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedCorrectedErrorCount = monitor.assertNumberParameter(CORRECTED_ERROR_COUNT_PARAMETER, conditions);
		if (assertedCorrectedErrorCount.isAbnormal()) {

			return AlertDetails.builder()
					.problem( String.format("The processor encountered and fixed a few internal errors (%f).",
							assertedCorrectedErrorCount.getParameter().getValue()))
					.consequence("The stability of the system may be affected. A system crash is likely to occur soon.")
					.recommendedAction("Check as soon as possible whether the processor environment is normal (voltage levels and temperature). If so, the processor may be defective and needs to be replaced quickly.")
					.build();

		}

		return null;
	}

	/**
	 * Check condition when the CPU encountered and fixed a large number of internal errors.
	 * 
	 * @param monitor    The CPU we wish to check its correctedErrorCount
	 * @param conditions The conditions used to determine if the alarm is reached 
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkCorrectedLargeErrorCountCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedCorrectedErrorCount = monitor.assertNumberParameter(CORRECTED_ERROR_COUNT_PARAMETER, conditions);
		if (assertedCorrectedErrorCount.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The processor encountered and fixed a high number of internal errors (%f).",
							assertedCorrectedErrorCount.getParameter().getValue()))
					.consequence("The stability of the system may be critically affected. A system crash is very likely to occur soon.")
					.recommendedAction("Check as soon as possible if the processor environment is normal (voltage levels and temperature). If so, the processor may be defective and needs to be replaced quickly.")
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