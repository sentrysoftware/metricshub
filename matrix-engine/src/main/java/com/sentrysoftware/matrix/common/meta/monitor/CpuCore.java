package com.sentrysoftware.matrix.common.meta.monitor;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

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

public class CpuCore implements IMetaMonitor {

	public static final MetaParameter CURRENT_SPEED = MetaParameter.builder()
			.basicCollect(true)
			.name(HardwareConstants.CURRENT_SPEED_PARAMETER)
			.unit(HardwareConstants.CURRENT_SPEED_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter USED_TIME_PERCENT = MetaParameter.builder()
			.basicCollect(false)
			.name(HardwareConstants.USED_TIME_PERCENT_PARAMETER)
			.unit(HardwareConstants.PERCENT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(CpuCore::checkMissingCondition,
			PRESENT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(CpuCore::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(CpuCore::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(HardwareConstants.STATUS_PARAMETER, STATUS);
		map.put(HardwareConstants.PRESENT_PARAMETER, PRESENT);
		map.put(HardwareConstants.CURRENT_SPEED_PARAMETER, CURRENT_SPEED);
		map.put(HardwareConstants.USED_TIME_PERCENT_PARAMETER, USED_TIME_PERCENT);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(HardwareConstants.PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(HardwareConstants.STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap);

	}

	/**
	 * Check missing processor core condition.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<PresentParam> assertedPresent = monitor.assertPresentParameter(conditions);
		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This processor core is not detected anymore.")
					.consequence("This could mean that the processor core has been disabled by BIOS, or that it has been deallocated for this system (in case of a dynamically reconfigurable machine). The computing capabilities have been reduced and this may impact the overall performance of this computer.")
					.recommendedAction("Check whether the processor core has been intentionally disabled by authorized personnel or whether it has failed. In the latter case, disable the core and consider replacing the entire physical processor.")
					.build();

		}

		return null;
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(HardwareConstants.STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This processor core is degraded or is about to fail." + IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("If degraded, this processor core may not be fully operational. This could adversely impact the overall performance of the system. If it is about to fail, this processor core is likely to crash very soon.")
					.recommendedAction("If possible, disable this processor core as soon as possible to prevent a system crash. Consider replacing the entire physical processor.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(HardwareConstants.STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This processor core has failed." +  IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("This processor core is not operational. This will impact the overall performance of the system. If it is about to fail this processor may crash very soon.")
					.recommendedAction("Disable this processor core if possible and consider replacing the corresponding physical processor as soon as possible to prevent a system overload.")
					.build();

		}
		return null;
	}

	/**
	 * Check condition when the CPU Core used time percentage is outside of the expected range.
	 * 
	 * @param monitor    The monitor we wish to check its used time percentage
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkUsedTimePercentOutsideExpectedCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedusedTimePercent = monitor.assertNumberParameter(HardwareConstants.USED_TIME_PERCENT_PARAMETER, conditions);
		if (assertedusedTimePercent.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The processor core time usage is outside of expected range (%f %s).",
							assertedusedTimePercent.getParameter().getValue(), HardwareConstants.PERCENT_PARAMETER_UNIT))
					.consequence("The processing load may not be optimal and therefore lead to lower system performance.")
					.recommendedAction("Check why this processor is used this way and whether it is caused by other failed or offlined cores or by the inability of the system to share the load across the various cores.")
					.build();

		}

		return null;
	}

	/**
	 * Check condition when the CPU Core used time percentage is outside of the tolerated range.
	 * 
	 * @param monitor    The monitor we wish to check its used time percentage
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkUsedTimePercentOutsideToleratedCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedusedTimePercent = monitor.assertNumberParameter(HardwareConstants.USED_TIME_PERCENT_PARAMETER, conditions);
		if (assertedusedTimePercent.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The processor core time usage is outside of tolerated range (%f %s).",
							assertedusedTimePercent.getParameter().getValue(), HardwareConstants.PERCENT_PARAMETER_UNIT))
					.consequence("The processing load may not be optimal and therefore lead to lower system performance.")
					.recommendedAction("Check why this processor is used this way and whether it is caused by other failed or offlined cores or by the inability of the system to share the load across the various cores.")
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
		return MonitorType.CPU_CORE;
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